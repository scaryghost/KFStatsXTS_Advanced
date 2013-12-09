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
    protected final def matchUUIDs, dateFormat, sql

    public TSAdvancedWriter(Connection conn) {
        this.sql= new Sql(conn)
        matchUUIDs= [:]
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
        def uuid

        sql.withTransaction {
            if (packet.getCategory() == "info") {
                def attrs= packet.getAttributes()

                uuid= UUID.randomUUID()
                matchUUIDs[packet.getServerAddressPort()]= uuid
                insertSetting(attrs[MatchPacket.ATTR_DIFFICULTY], attrs[MatchPacket.ATTR_LENGTH])
                insertLevel(attrs[MatchPacket.ATTR_MAP])
                insertMatch(uuid, attrs[MatchPacket.ATTR_DIFFICULTY], attrs[MatchPacket.ATTR_LENGTH], attrs[MatchPacket.ATTR_MAP])
            } else {
                uuid= matchUUIDs[packet.getServerAddressPort()]
                if (packet.getCategory() == "result") {
                    def packetAttrs= packet.getAttributes()
                    def result= packetAttrs.result == Result.WIN ? 1 : -1
                    updateMatch(packet.getWave(), result, dateFormat.format(Calendar.getInstance().getTime()), packetAttrs.duration, uuid)
                } else {
                    packet.getStats().each {name, value ->
                        insertStatistic(packet.getCategory(), name)
                    }
                    insertWaveStatistic(packet.getCategory(), packet.getStats(), packet.getWave(), uuid)
                }
            }
        }
    }
    public void writePlayerData(PlayerContent content) {
        def uuid= matchUUIDs[content.getServerAddressPort()]
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

    protected abstract void insertSetting(difficulty, length)
    protected abstract void insertLevel(level)
    protected abstract void insertMatch(uuid, difficulty, length, level)
    protected abstract void updateMatch(wave, result, time, duration, uuid)
    protected abstract void insertStatistic(category, name)
    protected abstract void insertWaveStatistic(category, stats, wave, uuid)
    protected abstract void insertPlayerSession(steamid64, info, uuid, time)
    protected abstract void insertPlayerStatistic(packets, steamid64, uuid)
}
