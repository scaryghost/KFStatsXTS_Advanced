import java.sql.Connection

public class SQLiteWriter extends TSAdvancedWriter {
    protected void insertWaveSummaryPerk(uuid, wave, stat, count) {
        super.insertWaveSummaryPerk(uuid.toString(), wave, stat, count)
    }
    protected void insertWaveStatistics(uuid, wave, type, perk, stats) {
        super.insertWaveStatistics(uuid.toString(), wave, type, perk, stats)
    }
    protected void insertWaveSummary(uuid, wave) {
        super.insertWaveSummary(uuid.toString(), wave)
    }
    protected void upsertWaveSummary(uuid, wave, completed, duration) {
        sql.execute("insert or ignore into wave_summary(?, ?)", [uuid.toString(), wave])
        sql.execute("update wave_summary set completed=?,duration=? where id=? and wave=?", 
                [uuid.toString(), wave, completed, duration])
    }
    protected void insertMatch(uuid, difficulty, length, map, address, port) {
        sql.execute("""insert into match(?, 
                (select id from setting where difficulty=? and length=?),
                (select id from map where name=?),
                (select id from server where address=? and port=?))""", 
                [uuid.toString(), difficulty, length, map, address, port])
    }
    protected void updateMatch(wave, result, time, duration, uuid) {
        sql.execute("update match set wave=?, result=?, time=?, duration=? where id=?",
                [wave, result, time, duration, uuid.toString()])
    }
    protected void insertStatistic(category, name) {
        sql.execute("insert or ignore into category(name) values ($name)")
        sql.execute("""insert or ignore into statistic(category_id, name) values (
                select id from category where name=$category,
                $name)""")
    }
    protected void insertPlayerSession(steamid64, info, uuid, time) {
        super.insertPlayerSession(steamid64, info, uuid.toString(), time)
    }
    protected void insertPlayerStatistic(packets, steamid64, uuid) {
        super.insertPlayerStatistic(packets, steamid64, uuid.toString())
    }
}
