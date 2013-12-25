import java.sql.Connection

public class PostgreSQLWriter extends TSAdvancedWriter {
    public PostgreSQLWriter(Connection conn) {
        super(conn)
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
        sql.execute("insert into wave_summary values (?,?)", [uuid, wave])
    }
    protected void upsertWaveSummary(uuid, wave, completed, duration) {
        sql.call("{call upsert_wave_summary(?, ?::smallint, ?, ?)}", [uuid, wave, completed, duration])
    }
    protected void insertMatch(uuid, difficulty, length, map, address, port) {
        sql.call("{call insert_match(?, ?, ?, ?, ?, ?::smallint)}", [uuid, difficulty, length, map, address, port])
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
