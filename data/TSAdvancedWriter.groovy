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
    protected final def matchStates, dateFormat, sql, perksCategory= "perks"

    protected static class State {
        public def uuid, difficulty, length, map, address, port, createdMatchEntry
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
        def state= matchStates[packet.getServerAddressPort()]

        sql.withTransaction {
            switch (packet.getCategory()) {
                case "info":
                    def values= packet.getAttributes() + [uuid: UUID.randomUUID(), address: packet.getServerAddress(), 
                            port: packet.getServerPort(), createdMatchEntry: false]
                    matchStates[packet.getServerAddressPort()]= new State(values) 
                    break
                case "result":
                    def packetAttrs= packet.getAttributes()
                    def result= packetAttrs.result == Result.WIN ? 1 : -1

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
                            packet.getStats().each {stat, count ->
                                insertStatistic(perksCategory, stat)
                            }
                            upsertWaveSummary(state.uuid, packet.getWave(), attrs.completed, attrs.duration)
                            break
                        default:
                            if (!state.createdMatchEntry) {
                                insertMatch(state.uuid, state.difficulty, state.length, state.map, state.address, state.port)
                                state.createdMatchEntry= true
                            }
                            insertWaveSummary(state.uuid, packet.getWave())
                            insertStatistic(perksCategory, attrs.perk)
                            packet.getStats().keySet().each {stat ->
                                insertStatistic(packet.getCategory(), stat)
                            }
                            sql.withBatch("""insert into wave_statistic (wave_summary_id, statistic_id, perk_id, value) values (
                                    (select id from wave_summary where match_id=? and wave=?),
                                    (select id from statistic where category_id=(select id from category where name=?) and name=?), 
                                    (select id from statistic where category_id=(select id from category where name='$perksCategory') and name=?), ?)""") {ps ->
                                packet.getStats().each {name, value ->
                                    ps.addBatch([state.uuid, packet.getWave(), packet.getCategory(), name, attrs.perk, value])
                                }
                            }
                            break
                    }
                    break
            }
        }
    }
    public void writePlayerData(PlayerContent content) {
        def uuid= matchStates[content.getServerAddressPort()].uuid
        def matchInfo= content.getMatchInfo()
        
        sql.withTransaction {
            insertPlayerSession(content.getSteamID64(), matchInfo, uuid, dateFormat.format(Calendar.getInstance().getTime()))
            content.getPackets().each {packet ->
                packet.getStats().keySet().each {name ->
                    insertStatistic(packet.getCategory(), name)
                }
            }
            insertPlayerStatistic(content.getPackets(), content.getSteamID64(), uuid)
        }
    }

    protected abstract void insertWaveSummary(uuid, wave)
    protected abstract void upsertWaveSummary(uuid, wave, completed, duration)
    protected abstract void insertMatch(uuid, difficulty, length, map, address, port)
    protected abstract void updateMatch(wave, result, time, duration, uuid)
    protected abstract void insertStatistic(category, name)
    protected abstract void insertPlayerSession(steamid64, info, uuid, time)
    protected abstract void insertPlayerStatistic(packets, steamid64, uuid)
}
