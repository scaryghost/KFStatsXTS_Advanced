import java.sql.Connection
import groovy.sql.Sql

public class StoredProcedureWriter extends AdvancedWriter {
    public StoredProcedureWriter(Connection conn) {
        super(conn)
    }
    protected void upsertPlayer(info) {
        sql.call("{call upsert_player(?, ?, ?)}", [info.steamID64, info.name, info.avatar])
    }
    protected void upsertWaveSummary(uuid, wave, completed, duration, time) {
        sql.call("{call upsert_wave_summary(?, ?, ?, ?, ?)}", [uuid, Sql.SMALLINT(wave), completed, duration, Sql.TIMESTAMP(time)])
    }
    protected void insertMatch(uuid, difficulty, length, map, address, port) {
        sql.call("{call insert_match(?, ?, ?, ?, ?, ?)}", [uuid, difficulty, length, map, address, Sql.SMALLINT(port)])
    }
    protected void insertStatistic(category, name) {
        sql.call("{call insert_statistic(?, ?)}", [category, name])
    }
}
