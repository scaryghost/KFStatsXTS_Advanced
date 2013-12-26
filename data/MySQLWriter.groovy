import com.github.etsai.kfsxtrackingserver.PacketParser.Result
import java.sql.Connection

public class MySQLWriter extends PostgreSQLWriter {
    public MySQLWriter(Connection conn) {
        super(conn)
    }

    protected void insertWaveSummaryPerk(uuid, wave, stat, count) {
        super.insertWaveSummaryPerk(uuid.toString(), wave, stat, count)
    }
    protected void insertWaveStatistics(uuid, wave, type, perk, stats) {
        super.insertWaveStatistics(uuid.toString(), wave, type, perk, stats)
    }
    protected void upsertWaveSummary(uuid, wave, completed, duration) {
        sql.call("{call upsert_wave_summary(?, ?, ?, ?)}", [uuid.toString(), wave, completed, duration])
    }
    protected void insertWaveSummary(uuid, wave) {
        super.insertWaveSummary(uuid.toString(), wave)
    }
    protected void insertMatch(uuid, difficulty, length, map, address, port) {
        sql.call("{call insert_match(?, ?, ?, ?, ?, ?)}", [uuid.toString(), difficulty, length, map, address, port])
    }
    protected void updateMatch(wave, result, time, duration, uuid) {
        sql.execute("update `match` set wave=?, result=?, timestamp=?, duration=? where id=?", 
                [wave, result, time, duration, uuid.toString()])
    }
    protected void insertPlayerSession(steamid64, info, uuid, time) {
        super.insertPlayerSession(steamid64, info, uuid.toString(), time)
    }
    protected void insertPlayerStatistic(packets, steamid64, uuid) {
        super.insertPlayerStatistic(packets, steamid64, uuid.toString())
    }
}
