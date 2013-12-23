import java.sql.Connection

public class PostgreSQLWriter extends TSAdvancedWriter {
    public PostgreSQLWriter(Connection conn) {
        super(conn)
    }

    protected void upsertWaveSummary(uuid, wave, completed, duration) {
        sql.call("{call upsert_wave_summary(?, ?::int2, ?, ?)}", [uuid, wave, completed, duration])
    }
    protected void insertMatch(uuid, difficulty, length, map, address, port) {
        System.err.println([uuid, difficulty, length, map, address, port])
        sql.call("{call insert_match(?, ?, ?, ?, ?, ?::int2)}", [uuid, difficulty, length, map, address, port])
    }
    protected void updateMatch(wave, result, time, duration, uuid) {
        sql.execute("update match set wave=?, result=?, timestamp=?::timestamp, duration=? where id=?", 
                [wave, result, time, duration, uuid])
    }
    protected void insertStatistic(category, name) {
        sql.call("{call insert_statistic(?, ?)}", [category, name])
    }
    protected void insertPlayerSession(steamid64, info, uuid, time) {
        sql.execute("""insert into player_session (player_id, match_id, wave, timestamp, duration, disconnected, finale_played, finale_survived) 
            values (?, ?, ?, ?::timestamp, ?, ?, ?, ?)""", 
            [steamid64,uuid, info.wave, time, info.duration, info.disconnected, 
            info.finalWave == 1, info.finalWaveSurvived == 1])
    }
    protected void insertPlayerStatistic(packets, steamid64, uuid) {
        sql.withBatch("""insert into player_statistic (statistic_id, player_session_id, value) values (
                (select id from statistic where category_id=(select id from category where name=?) and name=?),
                (select id from player_session where player_id=? and match_id=?), ?)""") {ps ->
            packets.each {packet ->
                packet.getStats().each {name, value ->
                    ps.addBatch([packet.getCategory(), name, steamid64, uuid, value])
                }
            }
        }
    }
}
