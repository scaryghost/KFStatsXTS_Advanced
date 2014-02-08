import com.github.etsai.kfsxtrackingserver.annotations.Query
import groovy.json.JsonBuilder
import groovy.sql.Sql
import java.sql.Connection

public class DataReader {
    private final def sql

    public DataReader(Connection conn) {
        this.sql= new Sql(conn)
    }

    @Query(name="server_weapons")
    public def queryWeapons(diff, length) {
        sql.eachRow("""select s.name,ws.value,ws2.wave,m.timestamp from wave_statistic ws 
                inner join statistic s on ws.statistic_id=s.id 
                inner join wave_summary ws2 on ws2.id=ws.wave_summary_id 
                inner join match m on m.id=ws2.match_id 
                where ws.statistic_id in (select id from statistic s where s.category_id=(select id from category c where c.name='weapons')) and 
                m.setting_id=(select id from setting s2 where s2.difficulty=$difficulty and s2.length=$length) 
                order by m.timestamp ASC,ws2.wave ASC,ws.value DESC""") {
        }
    }

    @Query(name="server_stat_totals")
    public def queryStatTotals(category) {
        sql.rows("""select s.name,ps.value,m.timestamp from player_statistic ps 
                inner join statistic s on ps.statistic_id=s.id 
                inner join player_session ps2 on ps2.id=ps.player_session_id 
                inner join match m on m.id=ps2.match_id 
                where ps.statistic_id in (select id from statistic s where s.category_id=(select id from category c where c.name=$category)) 
                order by m.timestamp ASC,ps.value DESC""")
    }
    @Query(name="server_match")
    public def queryMatch(matchUUID) {
        return sql.firstRow("""select (select count(*) from player_session ps where ps.match_id=m.id) as num_players,
                s2.address,s2.port,s.difficulty,s.length,m2.name,m.wave,m.result from match m 
                inner join setting s on s.id=m.setting_id 
                inner join map m2 on m2.id=m.map_id 
                inner join server s2 on s2.id=m.server_id
                where m.id=${matchUUID}""")
    }
    @Query(name="server_wave_summaries")
    public def queryMatchDurations(matchUUID) {
        sql.rows("select ws.wave,ws.duration,ws.survived from wave_summary ws where ws.match_id=${matchUUID}")
    }
    @Query(name="server_match_wave_data")
    public def queryMatchWaveData(matchUUID) {
        sql.rows("""select ws2.wave,(select name from statistic where ws.perk_id=id) as perk, 
                c.name as category,s.name as stat, ws.value from wave_statistic ws 
                inner join statistic s on ws.statistic_id=s.id 
                inner join category c on c.id=s.category_id 
                inner join wave_summary ws2 on ws2.id=ws.wave_summary_id 
                where ws.wave_summary_id in (select id from wave_summary ws2 where ws2.match_id=${matchUUID})""")
    }
    @Query(name="server_match_wave_perks")
    public def queryMatchWavePerks(matchUUID) {
        sql.rows("""select ws.wave,s.name,wsp.count from wave_summary ws 
                inner join wave_summary_perk wsp on ws.id=wsp.wave_summary_id 
                inner join statistic s on s.id=wsp.perk_id 
                where ws.match_id=${matchUUID}""")
    }
    @Query(name="server_match_players")
    public def queryMatchPlayers(matchUUID) {
        sql.rows("""select ps.player_id as steamid64,p.name from player_session ps 
                inner join player p on p.id=ps.player_id 
                where ps.match_id=$matchUUID""")
    }
    @Query(name="server_match_player_data")
    public def queryPlayerSession(matchUUID) {
        sql.rows("""select ps2.player_id as steamid64, c.name as category,s.name as statistic,ps.value from player_statistic ps 
                inner join player_session ps2 on ps2.id=ps.player_session_id 
                inner join statistic s on s.id=ps.statistic_id 
                inner join category c on c.id=s.category_id 
                where ps.player_session_id in (select id from player_session ps2 where ps2.match_id=$matchUUID)""")
    }
}
