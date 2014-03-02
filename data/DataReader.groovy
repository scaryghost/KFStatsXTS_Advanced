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
                where ws.statistic_id in (select id from statistic s where s.category_id=(select id from category c 
                where c.name='weapons')) and 
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
                where ps.statistic_id in (select id from statistic s where s.category_id=(select id from category c 
                where c.name=$category)) 
                order by m.timestamp ASC,ps.value DESC""")
    }
    @Query(name="server_match")
    public def queryMatch(matchUUID) {
        return sql.firstRow("""select (select count(*) from player_session ps where ps.match_id=m.id) as player_count,
                concat(s2.address,':',s2.port) as server,concat(s.difficulty,', ',s.length) as setting,
                m2.name as map,m.wave,m.result,m.time_end,m.duration from match m 
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
        sql.rows("""select ps2.player_id as steamid64, c.name as category,s.name as statistic,ps.value 
                from player_statistic ps 
                inner join player_session ps2 on ps2.id=ps.player_session_id 
                inner join statistic s on s.id=ps.statistic_id 
                inner join category c on c.id=s.category_id 
                where ps.player_session_id in (select id from player_session ps2 where ps2.match_id=$matchUUID)""")
    }
    @Query(name="player_total_data")
    public def queryPlayerData(steamid64) {
        sql.rows("""select c.name as category, s.name as statistic,sum(ps.value) as value 
                from player_statistic ps 
                inner join statistic s on s.id=ps.statistic_id 
                inner join category c on c.id=s.category_id 
                where player_session_id in (select id from player_session ps2 where ps2.player_id=$steamid64) 
                group by category, statistic;""")
    }
    @Query(name="player_info")
    public def queryPlayerInfo(steamid64) {
        sql.firstRow("""select * from player where id=$steamid64""")
    }

    @Query(name="player_totals")
    public def queryPlayerTotals(steamid64) {
        sql.firstRow("""select sum(ps.duration) as duration, 
                sum(case when ps.disconnected=true then 1 else 0 end) as disconnects, 
                sum(case when ps.finale_played=true then 1 else 0 end) as finales_played, 
                sum(case when ps.finale_survived=true then 1 else 0 end) as finales_survived, 
                sum(case when ps.disconnected=false and m.result=1 then 1 else 0 end) as wins, 
                sum(case when ps.disconnected=false and m.result=-1 then 1 else 0 end) as losses, 
                sum(case when ps.disconnected=false and m.result=0 then 1 else 0 end) as incomplete 
                from player_session ps 
                inner join match m on m.id=ps.match_id 
                where ps.player_id=$steamid64;""")
    }
    @Query(name="server_total_data")
    public def queryServerTotalData() {
        sql.rows("""select c.name as category,s.name as statistic, sum(ps.value) as value 
                from player_statistic ps 
                inner join statistic s on s.id=ps.statistic_id 
                inner join category c on c.id=s.category_id 
                group by c.name,s.name""")
    }
    @Query(name="server_wave_data")
    public def queryServerWaveData(difficulty, length) {
        sql.rows("""select c.name as category, s.name as statistic,ws.wave, 
                (select name from statistic s where s.id=ws2.perk_id) as perk,sum(ws2.value) as value from match m 
                inner join wave_summary ws on ws.match_id=m.id 
                inner join wave_statistic ws2 on ws2.wave_summary_id=ws.id 
                inner join statistic s on s.id=ws2.statistic_id 
                inner join category c on c.id=s.category_id 
                where m.setting_id in (select id from setting s where s.difficulty=$difficulty and s.length=$length) 
                group by category,statistic,ws.wave,perk""")
    }
    @Query(name="server_wave_perks")
    public def queryWavePErks(difficulty, length) {
        sql.rows("""select ws.wave,s.name,sum(wsp.count) as count from wave_summary_perk wsp 
                inner join wave_summary ws on wsp.wave_summary_id=ws.id 
                inner join statistic s on s.id=wsp.perk_id 
                inner join match m on m.id=ws.match_id 
                inner join setting s2 on s2.id=m.setting_id 
                where s2.length=$length and s2.difficulty=$difficulty 
                group by ws.wave,s.name""")
    }
    @Query(name="server_list")
    public def queryServerList() {
        sql.rows("select concat(s.address, ':', s.port) as address_port from server s")
    }
    @Query(name="server_difficulties")
    public def queryServerDifficulties() {
        sql.rows("select s.difficulty from setting s group by s.difficulty")
    }
    @Query(name="server_lengths")
    public def queryServerLengths() {
        sql.rows("select s.length from setting s group by s.length")
    }
    @Query(name="server_maps")
    public def queryServerMaps() {
        sql.rows("select m.name from map m")
    }
    @Query(name="server_filtered_total_data")
    public def queryServerTotalData(queries) {
        def stmt= """select c.name as category,s.name as statistic, sum(ps.value) as value 
                from player_statistic ps 
                inner join statistic s on s.id=ps.statistic_id 
                inner join category c on c.id=s.category_id 
                inner join player_session ps2 on ps2.id=ps.player_session_id 
                inner join match m on m.id=ps2.match_id """
        def whereConditions= []
        def joinSetting= false
        def psValues= []
        queries.each {key, values ->
            def criteria= []
            def ignore= false
            switch(key) {
                case "server":
                    values.tokenize(",").each {
                        def split= it.split(":")
                        criteria << "(s2.address=? and s2.port=?)"
                        psValues << split[0] << split[1].toInteger()
                    }
                    stmt+= "inner join server s2 on s2.id=m.server_id "
                    break
                case "length":
                case "difficulty":
                    values.tokenize(",").each {
                        criteria << "s3.$key=?"
                        psValues << it
                    }
                    joinSetting= true
                    break
                case "map":
                    values.tokenize(",").each {
                        criteria << "m2.name=?"
                        psValues << it
                    }
                    stmt+= "inner join map m2 on m2.id=m.map_id "
                    break
                default:
                    ignore= true
            }
            if (!ignore) {
                whereConditions << "(${criteria.join(' or ')})"
            }
        }
        if (joinSetting) {
            stmt+= "inner join setting s3 on s3.id=m.setting_id "
        }
        if (!whereConditions.isEmpty()) {
            stmt+= "where ${whereConditions.join(' and ')} "
        }
        stmt+= "group by c.name,s.name"
        sql.rows(stmt, psValues)
    }
}
