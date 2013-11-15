package com.github.etsai.kfstatsxtsadvanced

import com.github.etsai.kfsxtrackingserver.DataWriter
import com.github.etsai.kfsxtrackingserver.PlayerContent
import com.github.etsai.kfsxtrackingserver.PacketParser.*
import groovy.sql.Sql
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.UUID;

public class TSAdvancedWriter implements DataWriter {
    private class MatchState {
        public def uuid, maxWaveSeen, receivedResult
    }

    private final def sql, matchState, dateFormat

    public SQLiteWriter(Connection conn) {
        this.sql= new Sql(conn)
        matchState= [:]
        dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
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
            sql.addBatch("select upsert_player(?, ?, ?)") {ps ->
                steamInfo.each {info ->
                    ps.addBatch([info.steamID64, info.name, info.avatar])
                }
            }
        }
    }
    public void writeSteamInfo(SteamInfo steamInfo) {
        sql.withTransaction {
            sql.execute("select upsert_player(?, ?, ?)", [steamInfo.steamID64, steamInfo.name, steamInfo.avatar])
        }
    }
    public void writeMatchData(MatchPacket packet) {
        checkServerState(packet)
        def key= generateKey(packet)
        def state= matchState[key]

        if (packet.getWave() < maxWaveSeen) {
            matchState.remove(key)
            checkServerState(packet)
        }
        sql.withTransaction {
            if (state.maxWaveSeen == 0) {
                sql.execute("select insert_setting(?, ?)", [packet.getDifficulty(), packet.getLength()])
                sql.execute("select insert_level(?)", [packet.getLevel()])
                sql.execute("""insert into match values (?, (select id from setting where difficulty=? and length=?)
                        , (select id from level where name=?))""", [state.uuid, packet.getDifficulty(), packet.getLength()])
            }
            if (packet.getCategory() == "result") {
                def packetAttrs= packet.getAttrs()
                def result= packetAttrs.result == Result.WIN ? 1 : -1
                state.receivedResult= true
                sql.execute("update match set wave=?, result=?, timestamp=?, duration=? where id=?", [packet.getWave(), result, 
                        packetAttrs.duration, dateFormat.format(Calendar.getInstance().getTime()), packetAttrs.duration, state.uuid])
            } else {
                sql.execute("select insert_category(${packet.getCategory()})")
                sql.addBatch("select insert_statistic((select id from category where name=?), ?)") {ps ->
                    packet.getStats().each {name, value ->
                        ps.addBatch([packet.getCategory(), name])
                    }
                }
                sql.addBatch("""insert wave_statistic values (
                        (select id from statistic where category_id=(select id from category where name=?) and name=?), 
                        ?, ?, ?)""") {ps ->
                    packet.getStats().each {name, value ->
                        ps.addBatch([packet.getCategry(), name, state.uuid, packet.getWave(), value])
                    }
                }
            }
        }
        state.maxWaveSeen= [state.maxWaveSeen, packet.getWave()].max()
    }
    public void writePlayerData(PlayerContent content) {
        checkServerState(packet)
        def key= generateKey(packet)
        def state= matchState[key]
        def info= content.getMatchInfo()
        
        sql.withTransaction {
            sql.execute("""insert into player_session (player_id, match_id, wave, timestamp, duration, disconnected, finale_played, finale_survived) 
                    values (?, ?, ?, ?, ?, ?, ?, ?)""", [content.getSteamID64(), state.uuid, info.wave, dateFormat.format(Calendar.getInstance().getTime()), 
                    info.duration, info.result == Result.DISCONNECT, info.finalWave == 1, info.finalWaveSurvived == 1])
            sql.addBatch("select insert_category(?)") {ps ->
                content.getPackets().each {packet ->
                    ps.addBatch([packet.getCategory()])
                }
            }
            sql.addBatch("select insert_statistic((select id from category where name=?), ?)") {ps ->
                content.getPackets().each {packet ->
                    packet.getStats().each {name, value ->
                        ps.addBatch([packet.getCategory(), name])
                    }
                }
            }
            sql.addBatch("""insert into player_statistic values (
                    (select id from statistic where category_id=(select id from category where name=?) and name=?),
                    (select id from player_session where player_id=? and match_id=?), ?)""") {ps ->
                content.getPackets().each {packet ->
                    packet.getStats().each {name, value ->
                        ps->addBatch([packet.getCategory(), name, content.getSteamID64(), state.uuid, value])
                    }
                }
            }
        }
    }

    private def generateKey(StatPacket packet) {
        return "${packet.getSenderAddress()}:${packet.getSenderPort()}"
    }

    private void checkServerState(StatPacket packet) {
        def addressPort= generateKey(packet)
        if (!matchState.containsKey(addressPort)) {
            matchState[addressPort]= new MatchState(uuid: UUID.randomUUID(), maxWaveSeen: 0, receivedResult: false)
        }
    }

}
