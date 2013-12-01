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
    protected static class MatchState {
        public def uuid, maxWaveSeen, receivedResult
    }

    protected final def matchState, dateFormat, sql

    public TSAdvancedWriter(Connection conn) {
        this.sql= new Sql(conn)
        matchState= [:]
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
        checkServerState(packet.getServerAddressPort())
        def key= packet.getServerAddressPort()
        def state= matchState[key]

        if (packet.getWave() < state.maxWaveSeen) {
            matchState.remove(key)
            checkServerState(packet.getServerAddressPort())
            state= matchState[key]
        }
        sql.withTransaction {
            if (state.maxWaveSeen == 0) {
                insertSetting(packet.getDifficulty(), packet.getLength())
                insertLevel(packet.getLevel())
                insertMatch(state.uuid, packet.getDifficulty(), packet.getLength(), packet.getLevel())
            }
            if (packet.getCategory() == "result") {
                def packetAttrs= packet.getAttributes()
                def result= packetAttrs.result == Result.WIN ? 1 : -1
                state.receivedResult= true
                updateMatch(packet.getWave(), result, dateFormat.format(Calendar.getInstance().getTime()), packetAttrs.duration, state.uuid)
            } else {
                packet.getStats().each {name, value ->
                    insertStatistic(packet.getCategory(), name)
                }
                insertWaveStatistic(packet.getCategory(), packet.getStats(), packet.getWave(), state.uuid)
            }
        }
        state.maxWaveSeen= [state.maxWaveSeen, packet.getWave()].max()
    }
    public void writePlayerData(PlayerContent content) {
        checkServerState(content.getServerAddressPort())
        def key= content.getServerAddressPort()
        def state= matchState[key]
        def matchInfo= content.getMatchInfo()
        
        if (matchInfo.wave < state.maxWaveSeen) {
            matchState.remove(key)
            checkServerState(packet.getServerAddressPort())
            state= matchState[key]
        }
        sql.withTransaction {
            insertSetting(matchInfo.difficulty, matchInfo.length)
            insertLevel(matchInfo.level)
            insertMatch(state.uuid, matchInfo.difficulty, matchInfo.length, matchInfo.level)
            insertPlayerSession(content.getSteamID64(), matchInfo, state.uuid, dateFormat.format(Calendar.getInstance().getTime()))
            content.getPackets().each {packet ->
                packet.getStats().keySet().each {name ->
                    insertStatistic(packet.getCategory(), name)
                }
            }
            insertPlayerStatistic(content.getPackets(), content.getSteamID64(), state.uuid)
        }
        state.maxWaveSeen= [state.maxWaveSeen, matchInfo.wave].max()
    }

    private void checkServerState(String addressPort) {
        if (matchState[addressPort] == null) {
            matchState[addressPort]= new MatchState(uuid: UUID.randomUUID(), maxWaveSeen: 0, receivedResult: false)
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
