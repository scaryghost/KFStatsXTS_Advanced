package com.github.etsai.kfstatsxtsadvanced

import com.github.etsai.kfsxtrackingserver.DataWriter
import com.github.etsai.kfsxtrackingserver.DataWriter.SteamInfo
import com.github.etsai.kfsxtrackingserver.PacketParser.StatPacket
import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket
import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket
import com.github.etsai.kfsxtrackingserver.PacketParser.Result
import com.github.etsai.kfsxtrackingserver.PlayerContent
import groovy.sql.Sql
import java.sql.Connection
import java.sql.BatchUpdateException
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.UUID;

public class TSAdvancedWriter implements DataWriter {
    private class MatchState {
        public def uuid, maxWaveSeen, receivedResult
    }

    private final def sql, matchState, dateFormat

    public TSAdvancedWriter(Connection conn) {
        this.sql= new Sql(conn)
        matchState= [:]
        dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
    }

    public List<String> getMissingSteamInfoIDs() {
        def missingInfo= []
        sql.eachRow("select id from player where name is NULL or avatar is NULL") {row ->
            missingInfo << row.id
        }
        return missingInfo
    }
    public void writeSteamInfo(Collection<SteamInfo> steamInfo) {
        sql.withTransaction {
            steamInfo.each {info ->
                sql.call("{call upsert_player(?, ?, ?)}", [info.steamID64, info.name, info.avatar])
            }
        }
    }
    public void writeSteamInfo(SteamInfo steamInfo) {
        sql.withTransaction {
            sql.call("{call upsert_player(?, ?, ?)}", [steamInfo.steamID64, steamInfo.name, steamInfo.avatar])
        }
    }
    public void writeMatchData(MatchPacket packet) {
        checkServerState(packet)
        def key= generateKey(packet)
        def state= matchState[key]

        if (packet.getWave() < state.maxWaveSeen) {
            matchState.remove(key)
            checkServerState(packet)
            state= matchState[key]
        }
        sql.withTransaction {
            if (state.maxWaveSeen == 0) {
                sql.call("{call insert_setting(?, ?)}", [packet.getDifficulty(), packet.getLength()])
                sql.call("{call insert_level(?)}", [packet.getLevel()])
                sql.execute("""insert into match values (?, (select id from setting where difficulty=? and length=?)
                        , (select id from level where name=?))""", [state.uuid, packet.getDifficulty(), packet.getLength(), packet.getLevel()])
            }
            if (packet.getCategory() == "result") {
                def packetAttrs= packet.getAttributes()
                def result= packetAttrs.result == Result.WIN ? 1 : -1
                state.receivedResult= true
                sql.execute("update match set wave=?, result=?, timestamp=?::timestamp, duration=? where id=?", [packet.getWave(), result, 
                        dateFormat.format(Calendar.getInstance().getTime()), packetAttrs.duration, state.uuid])
            } else {
                packet.getStats().each {name, value ->
                    sql.call("{call insert_statistic(?, ?)}", [packet.getCategory(), name])
                }
                sql.withBatch("""insert into wave_statistic (statistic_id, match_id, wave, value) values (
                        (select id from statistic where category_id=(select id from category where name=?) and name=?), 
                        ?, ?, ?)""") {ps ->
                    packet.getStats().each {name, value ->
                        ps.addBatch([packet.getCategory(), name, state.uuid, packet.getWave(), value])
                    }
                }
            }
        }
        state.maxWaveSeen= [state.maxWaveSeen, packet.getWave()].max()
    }
    public void writePlayerData(PlayerContent content) {
        checkServerState(content.getSenderAddress(), content.getSenderPort())
        def key= generateKey(content.getSenderAddress(), content.getSenderPort())
        def state= matchState[key]
        def info= content.getMatchInfo()
        
        sql.withTransaction {
            sql.execute("""insert into player_session (player_id, match_id, wave, timestamp, duration, disconnected, finale_played, finale_survived) 
                    values (?, ?, ?, ?::timestamp, ?, ?, ?, ?)""", 
                    [content.getSteamID64(), state.uuid, info.wave, dateFormat.format(Calendar.getInstance().getTime()), 
                    info.duration, info.result == Result.DISCONNECT, info.finalWave == 1, info.finalWaveSurvived == 1])

            content.getPackets().each {packet ->
                packet.getStats().keySet().each {name ->
                    sql.call("{call insert_statistic(?, ?)}", [packet.getCategory(), name])
                }
            }
                
            sql.withBatch("""insert into player_statistic (statistic_id, player_session_id, value) values (
                    (select id from statistic where category_id=(select id from category where name=?) and name=?),
                    (select id from player_session where player_id=? and match_id=?), ?)""") {ps ->
                content.getPackets().each {packet ->
                    packet.getStats().each {name, value ->
                        ps.addBatch([packet.getCategory(), name, content.getSteamID64(), state.uuid, value])
                    }
                }
            }
        }
    }

    private String generateKey(String address, int port) {
        return "$address:$port"
    }
    private String generateKey(StatPacket packet) {
        return generateKey(packet.getSenderAddress(), packet.getSenderPort())
    }

    private void checkServerState(StatPacket packet) {
        checkServerState(packet.getSenderAddress(), packet.getSenderPort())
    }
    private void checkServerState(String address, int port) {
        def addressPort= generateKey(address, port)
        if (matchState[addressPort] == null) {
            matchState[addressPort]= new MatchState(uuid: UUID.randomUUID(), maxWaveSeen: 0, receivedResult: false)
        }
    }

}
