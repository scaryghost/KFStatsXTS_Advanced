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
        throw new UnsupportedException("Not implemented")
    }
    public void writeSteamInfo(Collection<SteamInfo> steamInfo) {
        throw new UnsupportedException("Not implemented")
    };
    public void writeSteamInfo(SteamInfo steamInfo) {
        throw new UnsupportedException("Not implemented")
    };
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
                packet.getStats().each {name, value ->
                    sql.execute("""insert wave_statistic values (
                            (select id from statistic where category_id=(select id from category where name=?) and name=?), 
                            ?, ?, ?)""", [packet.getCategry(), name, state.uuid, packet.getWave(), value])
                }
            }
        }
        state.maxWaveSeen= [state.maxWaveSeen, packet.getWave()].max()
    };
    public void writePlayerData(PlayerContent content) {
        throw new UnsupportedException("Not implemented")
    };

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
