import java.sql.Connection

public class SQLiteWriter extends TSAdvancedWriter {
    public SQLiteWriter(Connection conn) {
        super(conn)
    }
    protected void upsertPlayer(info) {
        sql.execute("insert or ignore into player(id) values (${info.steamID64})")
        sql.execute("update player set name=?,avatar=? where id=?", 
                [info.steamID64, info.name, info.avatar])
    }
    protected void insertWaveSummaryPerk(uuid, wave, stat, count) {
        super.insertWaveSummaryPerk(uuid.toString(), wave, stat, count)
    }
    protected void insertWaveStatistics(uuid, wave, type, perk, stats) {
        super.insertWaveStatistics(uuid.toString(), wave, type, perk, stats)
    }
    protected void insertWaveSummary(uuid, wave) {
        super.insertWaveSummary(uuid.toString(), wave)
    }
    protected void upsertWaveSummary(uuid, wave, completed, duration, time) {
        sql.execute("insert or ignore into wave_summary(match_id, wave) values (?, ?)", 
                [uuid.toString(), wave])
        sql.execute("update wave_summary set survived=?,duration=?,time_end=? where id=? and wave=?", 
                [completed, duration, time, uuid.toString(), wave])
    }
    protected void insertMatch(uuid, difficulty, length, map, address, port) {
        sql.execute("insert or ignore into setting(difficulty, length) values (?, ?)", 
                [difficulty, length])
        sql.execute("insert or ignore into map(name) values (?)", [map])
        sql.execute("insert or ignore into server(address, port) values (?, ?)", 
                [address, port])
        sql.execute("""insert into match (id, setting_id, map_id, server_id) values (?, 
                (select id from setting where difficulty=? and length=?),
                (select id from map where name=?),
                (select id from server where address=? and port=?))""", 
                [uuid.toString(), difficulty, length, map, address, port])
    }
    protected void updateMatch(wave, result, time, duration, uuid) {
        super.updateMatch(wave, result, time, duration, uuid.toString())
    }
    protected void insertStatistic(category, name) {
        sql.execute("insert or ignore into category(name) values ($category)")
        sql.execute("""insert or ignore into statistic(category_id, name) values (
                (select id from category where name=$category),
                $name)""")
    }
    protected void insertPlayerSession(steamid64, info, uuid, time) {
        super.insertPlayerSession(steamid64, info, uuid.toString(), time)
    }
    protected void insertPlayerStatistic(packets, steamid64, uuid, time) {
        super.insertPlayerStatistic(packets, steamid64, uuid.toString(), time)
    }
}
