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

public abstract class TSAdvancedWriter implements DataWriter {
    protected final static def resultValues= [(Result.WIN): 1, (Result.LOSS): -1, (Result.INCOMPLETE): 0]
    protected final def matchStates, dateFormat, sql, perksCategory= "perks"

    protected static class State {
        public def uuid, difficulty, length, map, address, port, 
                createdMatchEntry, lastWave
    }

    public TSAdvancedWriter(Connection conn) {
        this.sql= new Sql(conn)
        matchStates= [:]
        dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
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
                upsertPlayer(steamInfo)
            }
        }
    }
    public void writeSteamInfo(SteamInfo steamInfo) {
        sql.withTransaction {
            upsertPlayer(steamInfo)
        }
    }
    public void writeMatchData(MatchPacket packet) {
        def state= matchStates[packet.getServerAddressPort()]

        sql.withTransaction {
            switch (packet.getCategory()) {
                case "info":
                    def values= packet.getAttributes() + [uuid: UUID.randomUUID(), address: packet.getServerAddress(), 
                            port: packet.getServerPort(), createdMatchEntry: false, lastWave: 0]
                    matchStates[packet.getServerAddressPort()]= new State(values) 
                    break
                case "result":
                    def packetAttrs= packet.getAttributes()
                    def result= resultValues[packetAttrs.result]

                    updateMatch(packet.getWave(), result, dateFormat.format(Calendar.getInstance().getTime()), 
                            packetAttrs.duration, state.uuid)
                    break
                case "wave":
                    def attrs= packet.getAttributes()
                    switch(attrs.type) {
                        case "summary":
                            if (!state.createdMatchEntry) {
                                insertMatch(state.uuid, state.difficulty, state.length, state.map, state.address, state.port)
                                state.createdMatchEntry= true
                            }
                            state.lastWave= packet.getWave()
                            if (attrs.duration < 0) {
                                upsertWaveSummary(state.uuid, packet.getWave(), null, null, null)
                            } else {
                                def time= dateFormat.format(Calendar.getInstance().getTime())
                                upsertWaveSummary(state.uuid, packet.getWave(), attrs.completed, attrs.duration, time)
                            }
                            packet.getStats().each {stat, count ->
                                insertStatistic(perksCategory, stat)
                                insertWaveSummaryPerk(state.uuid, packet.getWave(), stat, count)
                            }
                            break
                        default:
                            if (!state.createdMatchEntry) {
                                insertMatch(state.uuid, state.difficulty, state.length, state.map, state.address, state.port)
                                state.createdMatchEntry= true
                            }
                            if (state.lastWave != packet.getWave()) {
                                state.lastWave= packet.getWave()
                                insertWaveSummary(state.uuid, packet.getWave())
                            }
                            insertStatistic(perksCategory, attrs.perk)
                            packet.getStats().keySet().each {stat ->
                                insertStatistic(attrs.type, stat)
                            }
                            insertWaveStatistics(state.uuid, packet.getWave(), attrs.type, attrs.perk, packet.getStats())
                            break
                    }
                    break
            }
        }
    }
    public void writePlayerData(PlayerContent content) {
        def state= matchStates[content.getServerAddressPort()]
        def matchInfo= content.getMatchInfo()
        
        sql.withTransaction {
            if (!state.createdMatchEntry) {
                insertMatch(state.uuid, state.difficulty, state.length, state.map, state.address, state.port)
                state.createdMatchEntry= true
            }
            def time= dateFormat.format(Calendar.getInstance().getTime())
            insertPlayerSession(content.getSteamID64(), matchInfo, state.uuid, time)
            content.getPackets().each {packet ->
                packet.getStats().keySet().each {name ->
                    insertStatistic(packet.getCategory(), name)
                }
            }
            insertPlayerStatistic(content.getPackets(), content.getSteamID64(), state.uuid, time)
        }
    }

    protected void insertWaveSummaryPerk(uuid, wave, stat, count) {
        sql.execute("""insert into wave_summary_perk(wave_summary_id, perk_id, count) values (
                (select id from wave_summary where match_id=? and wave=?),
                (select id from statistic where category_id=(select id from category where name='$perksCategory') and name=?),
                ?)""", [uuid, wave, stat, count])
    }
    protected void insertWaveStatistics(uuid, wave, type, perk, stats) {
        sql.withBatch("""insert into wave_statistic (wave_summary_id, statistic_id, perk_id, value) values (
                (select id from wave_summary where match_id=? and wave=?),
                (select id from statistic where category_id=(select id from category where name=?) and name=?), 
                (select id from statistic where category_id=(select id from category where name='$perksCategory') and name=?),
                ?)""") {ps ->
            stats.each {name, value ->
                ps.addBatch([uuid, wave, type, name, perk, value])
            }
        }
    }
    protected void insertWaveSummary(uuid, wave) {
        sql.execute("insert into wave_summary(match_id, wave) values (?,?)", [uuid, wave])
    }
    protected abstract void upsertPlayer(info)
    protected abstract void upsertWaveSummary(uuid, wave, completed, duration, time)
    protected abstract void insertMatch(uuid, difficulty, length, map, address, port)
    protected void updateMatch(wave, result, time, duration, uuid) {
        sql.execute("update match set wave=?, result=?, time_end=?, duration=? where id=?", 
                [wave, result, Sql.TIMESTAMP(time), duration, uuid])
    }
    protected abstract void insertStatistic(category, name)
    protected void insertPlayerSession(steamid64, info, uuid, time) {
        sql.execute("""insert into player_session (player_id, match_id, wave, time_end, duration, disconnected, finale_played, finale_survived) 
            values (?, ?, ?, ?, ?, ?, ?, ?)""", 
            [steamid64, uuid, info.wave, Sql.TIMESTAMP(time), info.duration, info.disconnected, 
            info.finalWave == 1, info.finalWaveSurvived == 1])
    }
    protected void insertPlayerStatistic(packets, steamid64, uuid, time) {
        sql.withBatch("""insert into player_statistic (statistic_id, player_session_id, value) values (
                (select id from statistic where category_id=(select id from category where name=?) and name=?),
                (select id from player_session where player_id=? and match_id=? and time_end=?), ?)""") {ps ->
            packets.each {packet ->
                packet.getStats().each {name, value ->
                    ps.addBatch([packet.getCategory(), name, steamid64, uuid, Sql.TIMESTAMP(time), value])
                }
            }
        }
    }
}
