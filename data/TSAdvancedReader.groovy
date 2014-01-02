import static com.github.etsai.kfsxtrackingserver.DataReader.*
import com.github.etsai.kfsxtrackingserver.DataReader
import groovy.sql.Sql
import java.sql.Connection

public class TSAdvancedReader implements DataReader {
    private final def sql

    public TSAdvancedReader(Connection conn) {
        this.sql= new Sql(conn)
    }
    public Collection<Difficulty> getDifficulties() {
        sql.eachRow('''select s.difficulty,s.length,m.result,sum(m.duration) as time,count(m.result) as result 
                from match m inner join setting s on s.id=m.setting_id where m.result is not NULL 
                group by s.difficulty,s.length,m.result;''') {
            
        }
    }
    public Collection<Level> getLevels() {
        sql.eachRow('''select m2.name,m.result,sum(m.duration) as time,count(m.result) as result
                from match m inner join map m2 on m2.id=m.map_id where m.result is not NULL group by m2.name,m.result;''') {
        }
    }
    public PlayerRecord getRecord(String steamID64) {
        def disconnects= sql.firstRow("select count(*) from player_session where player_id=? and disconnected=true")[0]
        sql.eachRow('''select result,count(m.result) from player_session ps inner join match m on m.id=ps.match_id 
                where player_id=? and disconnected=false and m.result is not NULL group by m.result''', steamID64) {
        }
    }
    public Collection<PlayerRecord> getRecords(String group, Order order, int start, int end) {
        def orderStr= (group != null && order != Order.NONE) ? "order by $group $order" : ""

        sql.eachRow('''select player_id,result,count(m.result) from player_session ps inner join match m 
                on m.id=ps.match_id where disconnected=false and m.result is not NULL group by m.result,ps.player_id ''' + orderStr + 
                'limit ?, ?', [start, end - start]) {
        }
        sql.eachRow('''select player_id,count(*) from player_session ps inner join match m 
                on m.id=ps.match_id where disconnected=true group by m.result,ps.player_id''' + orderStr + 
                'limit ?, ?', [start, end - start]) {
        }
    }
    public Integer getNumRecords() {
        return sql.firstRow('select count(*) from player')[0]
    }
    public Collection<PlayerRecord> getRecords() {
        sql.eachRow('''select player_id,result,count(m.result) from player_session ps inner join match m 
                on m.id=ps.match_id where disconnected=false group by m.result,ps.player_id''') {
        }
        sql.eachRow('''select player_id,count(*) from player_session ps inner join match m 
                on m.id=ps.match_id where disconnected=true group by m.result,ps.player_id''') {
        }
    }
    public Collection<Match> getMatchHistory(String steamID64, String group, Order order, int start, int end) {
        def orderStr= (group != null && order != Order.NONE) ? "order by $group $order" : ""

        sql.eachRow('''select s.difficulty,s.length,m2.name as map_name,m.result,ps.duration,
                ps.timestamp,ps.wave from player_session ps inner join match m on m.id=ps.match_id 
                inner join setting s on m.setting_id=s.id inner join map m2 on m2.id=m.map_id 
                where player_id=? and m.result is not NULL''' + orderStr + ' LIMIT ?, ?', [steamID64, start, end - start]) {
        }
        /*
            difficulty, length, level, result   - match
            duration, timestamp, wave           - player_session
        */

    }
    public Collection<Match> getMatchHistory(String steamID64) {
        sql.eachRow("""select s.difficulty,s.length,m2.name as map_name,m.result,ps.duration,
                ps.timestamp,ps.wave from player_session ps inner join match m on m.id=ps.match_id 
                inner join setting s on m.setting_id=s.id inner join map m2 on m2.id=m.map_id 
                where player_id=$steamID64 and m.result is not NULL""") {
        }
    }
    public Integer getNumMatches(String steamID64) {
        sql.eachRow("""select count(*) from player_session ps inner join match m on m.id=ps.match_id 
                where player_id=$steamID64""") {
        }
        
    }
    public Collection<String> getStatCategories() {
        sql.eachRow('select name from category')
    }
    public Collection<Stat> getAggregateData(String category) {
        sql.eachRow("""select s.name,sum(ps.value) from player_statistic ps inner join statistic s on s.id=ps.statistic_id 
                where s.category_id in (select id from category where name=$category) group by s.name""") {
        }
    }
    public Collection<Stat> getAggregateData(String category, String steamID64) {
        sql.eachRow("""select s.name,sum(ps.value) from player_statistic ps inner join statistic s on s.id=ps.statistic_id 
                where s.category_id in (select id from category where name=$category) and ps.player_id=$steamID64 group by s.name""") {
        }
    }
    public SteamIDInfo getSteamIDInfo(String steamID64) {
        sql.firstRow("select * from player where id=$steamID64")
    }
    public Collection<WaveStat> getWaveData(String difficulty, String length, String category) {
        sql.eachRow("""select s.name,sum(ws.value),ws2.wave from wave_statistic ws inner join statistic s on ws.statistic_id=s.id 
                inner join wave_summary ws2 on ws2.id=ws.wave_summary_id inner join match m on m.id=ws2.match_id 
                where s.category_id in (select id from category c where c.name=$category) and 
                m.setting_id=(select id from setting where difficulty=$difficulty and length=$length) 
                group by s.name,ws2.wave""") {
        }
    }
    public Collection<WaveStat> getWaveData(String level, String difficulty, String length, String category) {
        sql.eachRow("""select s.name,sum(ws.value),ws2.wave from wave_statistic ws inner join statistic s on ws.statistic_id=s.id 
                inner join wave_summary ws2 on ws2.id=ws.wave_summary_id inner join match m on m.id=ws2.match_id 
                where s.category_id in (select id from category c where c.name=$category) and 
                m.setting_id=(select id from setting where difficulty=$difficulty and length=$length) and
                m.map_id=(select id from map m2 where m2.name=$level)
                group by s.name,ws2.wave""") {
        }
    }

    public Collection<String> getWaveDataCategories() {
        sql.eachRow('''select c.name from wave_statistic ws inner join statistic s on ws.statistic_id=s.id 
                inner join category c on s.category_id=c.id group by c.name''') {
        }
        
    }
    public Collection<LevelDifficulty> getLevelData(String level) {
        sql.eachRow("""select s.difficulty,s.length,sum(m.wave),count(m.result),sum(m.duration) from match m 
                inner join setting s on s.id=m.setting_id where m.map_id=(select id from map m2 where m2.name=$level) and 
                m.result is not NULL group by s.difficulty,s.length,m.result""") {
        }
    }
    public Collection<LevelDifficulty> getDifficultyData(String difficulty, String length) {
        sql.eachRow("""select m2.name,sum(m.wave),count(m.result),sum(m.duration) from match m inner join map m2 on m2.id=m.map_id 
                where m.setting_id=(select id from setting s where s.difficulty=$diffiulty and s.length=$length) and 
                m.result is not NULL group by m2.name,m.result""") {
        }
    }
    public Object getData(String queryName, List<Object> parameters) {
        throw new UnsupportedOperationException("Not yet implemented")
    }

}
