import com.github.etsai.kfsxtrackingserver.PacketParser.Result
import java.sql.Connection

public class MySQLWriter extends PostgreSQLWriter {
    public MySQLWriter(Connection conn) {
        super(conn)
    }

    protected void insertMatch(uuid, difficulty, length, level) {
        sql.execute("""insert into `match`(id, setting_id, level_id) values (?, (select id from setting where difficulty=? and length=?)
            , (select id from level where name=?))""", [uuid.toString(), difficulty, length, level])
    }
    protected void updateMatch(wave, result, time, duration, uuid) {
        sql.execute("update `match` set wave=?, result=?, timestamp=?, duration=? where id=?", 
                [wave, result, time, duration, uuid.toString()])
    }
    protected void insertWaveStatistic(category, stats, wave, uuid) {
        sql.withBatch("""insert into wave_statistic (statistic_id, match_id, wave, value) values (
                (select id from statistic where category_id=(select id from category where name=?) and name=?), ?, ?, ?)""") {ps ->
            stats.each {name, value ->
                ps.addBatch([category, name, uuid.toString(), wave, value])
            }
        }
    }
    protected void insertPlayerSession(steamid64, info, uuid, time) {
        sql.execute("""insert into player_session (player_id, match_id, wave, timestamp, duration, disconnected, finale_played, finale_survived) 
            values (?, ?, ?, ?, ?, ?, ?, ?)""", 
            [steamid64,uuid.toString(), info.wave, time, info.duration, info.result == Result.DISCONNECT, 
            info.finalWave == 1, info.finalWaveSurvived == 1])
    }
    protected void insertPlayerStatistic(packets, steamid64, uuid) {
        sql.withBatch("""insert into player_statistic (statistic_id, player_session_id, value) values (
                (select id from statistic where category_id=(select id from category where name=?) and name=?),
                (select id from player_session where player_id=? and match_id=?), ?)""") {ps ->
            packets.each {packet ->
                packet.getStats().each {name, value ->
                    ps.addBatch([packet.getCategory(), name, steamid64, uuid.toString(), value])
                }
            }
        }
    }
}