import static com.github.etsai.kfsxtrackingserver.DataReader.*
import com.github.etsai.kfsxtrackingserver.DataReader
import groovy.sql.Sql
import java.sql.Connection

public class TSAdvancedReader implements DataReader {
    private final def sql

    public TSAdvancedReader(Connection conn) {
        this.sql= new Sql(conn)
    }

    private void resultAggregator(accum, row) {
        def rowResult= row.toRowResult()
        def result= rowResult.result
        def count= rowResult.result_count
        rowResult.remove("result")
        rowResult.remove("result_count")

        if (!accum.containsKey(rowResult)) {
            accum[rowResult]= rowResult
        }
        switch(result) {
            case -1:
            case 0:
                accum[rowResult].losses= count
                break
            case 1:
                accum[rowResult].wins= count
                break
            default:
                throw new Exception("Unrecognized result value: $result")
       }
    }
    public Collection<Difficulty> getDifficulties() {
        def diffStats= [:]

        sql.eachRow('''select s.difficulty as name,s.length,m.result,sum(m.duration) as time,count(m.result) as result_count 
                from match m inner join setting s on s.id=m.setting_id where m.result is not NULL 
                group by s.difficulty,s.length,m.result;''') {
            resultAggregator(diffStats, it)
        }
        diffStats.values().collect{ new Difficulty(it) }
    }
    public Collection<Level> getLevels() {
        def levelStats= [:]

        sql.eachRow('''select m2.name,m.result,sum(m.duration) as time,count(m.result) as result_count
                from match m inner join map m2 on m2.id=m.map_id where m.result is not NULL group by m2.name,m.result;''') {
            resultAggregator(levelStats, it)
        }
        levelStats.values().collect{ new Level(it) }
    }
    public PlayerRecord getRecord(String steamID64) {
        def prValues= [disconnects: sql.firstRow("select count(*) from player_session where player_id=? and disconnected=true", [steamID64])[0], 
                finales_played: 0, finales_survived: 0, time: 0, wins: 0, losses: 0, steamid64: steamID64]
        sql.eachRow('''select sum(finale_played::integer) as fp_sum,sum(finale_survived::integer) as fs_sum,sum(ps.duration) as d_sum,result,
                count(m.result) as result_count from player_session ps inner join match m on m.id=ps.match_id where player_id=? 
                and disconnected=false and m.result is not NULL group by m.result''', [steamID64]) {
            prValues.finales_played+= it.fp_sum
            prValues.finales_survived+= it.fs_sum
            prValues.time+= it.d_sum
            switch(it.result) {
                case -1:
                case 0:
                    prValues.losses= it.result_count
                    break
                case 1:
                    prValues.wins= it.result_count
                    break
                default:
                    throw new Exception("Unrecognized result value: $result")
            }
        }
        return new PlayerRecord(prValues)
    }
    public Collection<PlayerRecord> getRecords(String group, Order order, int start, int end) {
        def orderStr= (group != null && order != Order.NONE) ? "order by $group $order" : ""

        def prValues= [:]
        sql.eachRow('''select player_id,count(*) from player_session ps inner join match m 
                on m.id=ps.match_id where disconnected=true group by m.result,ps.player_id ''' + orderStr + 
                ' limit ? offset ?', [end - start, start]) {
            prValues[it.player_id]= [steamid64: it.player_id, disconnects: it[1], finales_played: 0, finales_survived: 0, 
                    time: 0, wins: 0, losses: 0]
        }
        sql.eachRow('''select player_id,result,count(m.result) as result_count, sum(finale_played::integer) as fp_sum,sum(finale_survived::integer) as fs_sum,sum(ps.duration) as d_sum
                from player_session ps inner join match m on m.id=ps.match_id where disconnected=false and m.result is not NULL 
                group by m.result,ps.player_id ''' + orderStr + ' limit ? offset ?', [end - start, start]) {
            if (prValues[it.player_id] == null) {
                prValues[it.player_id]= [steamid64: it.player_id, disconnects: 0, finales_played: 0, finales_survived: 0, 
                    time: 0, wins: 0, losses: 0]
            }
            prValues[it.player_id].finales_played+= it.fp_sum
            prValues[it.player_id].finales_survived+= it.fs_sum
            prValues[it.player_id].time+= it.d_sum
            switch(it.result) {
                case -1:
                case 0:
                    prValues[it.player_id].losses= it.result_count
                    break
                case 1:
                    prValues[it.player_id].wins= it.result_count
                    break
                default:
                    throw new Exception("Unrecognized result value: $result")
            }
        }
        prValues.values().collect{ new PlayerRecord(it) }
    }
    public Integer getNumRecords() {
        return sql.firstRow('select count(*) from player')[0]
    }
    public Collection<PlayerRecord> getRecords() {
        def prValues= [:]
        sql.eachRow('''select player_id,result,count(m.result) from player_session ps inner join match m 
                on m.id=ps.match_id where disconnected=false group by m.result,ps.player_id''') {
            prValues[it.player_id]= [steamid64: it.player_id, disconnects: it[1], finales_played: 0, finales_survived: 0, 
                    time: 0, wins: 0, losses: 0]
        }
        sql.eachRow('''select player_id,count(*) from player_session ps inner join match m 
                on m.id=ps.match_id where disconnected=true group by m.result,ps.player_id''') {
            if (prValues[it.player_id] == null) {
                prValues[it.player_id]= [steamid64: it.player_id, disconnects: 0, finales_played: 0, finales_survived: 0, 
                    time: 0, wins: 0, losses: 0]
            }
            prValues[it.player_id].finales_played+= it.fp_sum
            prValues[it.player_id].finales_survived+= it.fs_sum
            prValues[it.player_id].time+= it.d_sum
            switch(it.result) {
                case -1:
                case 0:
                    prValues[it.player_id].losses= it.result_count
                    break
                case 1:
                    prValues[it.player_id].wins= it.result_count
                    break
                default:
                    throw new Exception("Unrecognized result value: $result")
            }
        }
    }
    public Collection<Match> getMatchHistory(String steamID64, String group, Order order, int start, int end) {
        def orderStr= (group != null && order != Order.NONE) ? "order by $group $order" : ""

        def matches= []
        sql.eachRow('''select s.difficulty,s.length,m2.name as level,ps.duration,
                case when ps.disconnected=true then 'disconnect'
                    when m.result=1 then 'win'
                    when m.result=0 then 'loss'
                    when m.result=-1 then 'loss'
                end as result,
                ps.timestamp,ps.wave from player_session ps inner join match m on m.id=ps.match_id 
                inner join setting s on m.setting_id=s.id inner join map m2 on m2.id=m.map_id 
                where player_id=? and m.result is not NULL ''' + orderStr + ' LIMIT ? OFFSET ?', [steamID64, end - start, start]) {
            matches << new Match(it.toRowResult())
        }
        return matches

    }
    public Collection<Match> getMatchHistory(String steamID64) {
        def matches= []
        ///< TODO: match history result needs to be win,loss,or disconnect
        sql.eachRow("""select s.difficulty,s.length,m2.name as level,ps.duration,
                case when ps.disconnected=true then 'disconnect'
                    when m.result=1 then 'win'
                    when m.result=0 then 'loss'
                    when m.result=-1 then 'loss'
                end as result,
                ps.timestamp,ps.wave from player_session ps inner join match m on m.id=ps.match_id 
                inner join setting s on m.setting_id=s.id inner join map m2 on m2.id=m.map_id 
                where player_id=$steamID64 and m.result is not NULL""") {
            matches << new Match(it.toRowResult())
        }
        return matches
    }
    public Integer getNumMatches(String steamID64) {
        return sql.firstRow("""select count(*) from player_session ps inner join match m on m.id=ps.match_id 
                where player_id=$steamID64""")[0]
    }
    public Collection<String> getStatCategories() {
        def categories= []

        sql.eachRow('''select c.name from player_session ps1 inner join player_statistic ps2 on ps2.player_session_id=ps1.id 
                inner join statistic s on s.id=ps2.statistic_id inner join category c on c.id=s.category_id group by c.name''') {
            categories << it.name
        }
        return categories
    }
    public Collection<Stat> getAggregateData(String category) {
        def stats= []

        sql.eachRow("""select s.name as stat,sum(ps.value) as value from player_statistic ps inner join statistic s on s.id=ps.statistic_id 
                where s.category_id in (select id from category where name=$category) group by s.name""") {
            stats << new Stat(it.toRowResult())
        }
        return stats
    }
    public Collection<Stat> getAggregateData(String category, String steamID64) {
        def stats= []

        sql.eachRow("""select s.name as stat,sum(ps.value) as value from player_statistic ps inner join statistic s on s.id=ps.statistic_id 
                where s.category_id in (select id from category where name=$category) and ps.player_session_id  in 
                (select id from player_session ps2 where ps2.player_id=$steamID64) group by s.name""") {
            stats << new Stat(it.toRowResult())
        }
        return stats
    }
    public SteamIDInfo getSteamIDInfo(String steamID64) {
        def info= sql.firstRow("select name,avatar from player where id=$steamID64")
        return new SteamIDInfo(info)
    }
    public Collection<WaveStat> getWaveData(String difficulty, String length, String category) {
        def waveStats= []

        sql.eachRow("""select s.name as stat,sum(ws.value) as value,ws2.wave from wave_statistic ws inner join statistic s on ws.statistic_id=s.id 
                inner join wave_summary ws2 on ws2.id=ws.wave_summary_id inner join match m on m.id=ws2.match_id 
                where s.category_id in (select id from category c where c.name=$category) and 
                m.setting_id=(select id from setting where difficulty=$difficulty and length=$length) 
                group by s.name,ws2.wave""") {
            waveStats << new WaveStat(it.toRowResult())
        }
        return waveStats
    }
    public Collection<WaveStat> getWaveData(String level, String difficulty, String length, String category) {
        def waveStats= []

        sql.eachRow("""select s.name as stat,sum(ws.value) as value,ws2.wave from wave_statistic ws inner join statistic s on ws.statistic_id=s.id 
                inner join wave_summary ws2 on ws2.id=ws.wave_summary_id inner join match m on m.id=ws2.match_id 
                where s.category_id in (select id from category c where c.name=$category) and 
                m.setting_id=(select id from setting where difficulty=$difficulty and length=$length) and
                m.map_id=(select id from map m2 where m2.name=$level)
                group by s.name,ws2.wave""") {
            waveStats << new WaveStat(it.toRowResult())
        }
        return waveStats
    }

    public Collection<String> getWaveDataCategories() {
        def categories= []

        sql.eachRow('''select c.name from wave_statistic ws inner join statistic s on ws.statistic_id=s.id 
                inner join category c on s.category_id=c.id group by c.name''') {
            categories << it.name
        }
        return categories
    }
    public Collection<LevelDifficulty> getLevelData(String level) {
        def levelData= [:]

        sql.eachRow("""select s.difficulty,s.length,sum(m.wave) as wave_sum,count(m.result) as result_count,sum(m.duration) as time,m.result from match m 
                inner join setting s on s.id=m.setting_id where m.map_id=(select id from map m2 where m2.name=$level) and 
                m.result is not NULL group by s.difficulty,s.length,m.result""") {
            resultAggregator(levelData, it)
        }
        levelData.values().collect{ new LevelDifficulty(it) }
    }
    public Collection<LevelDifficulty> getDifficultyData(String difficulty, String length) {
        def diffData= [:]
        sql.eachRow("""select m2.name as level,sum(m.wave) as wave_sum,count(m.result) as result_count,sum(m.duration) as time,m.result from match m 
                inner join map m2 on m2.id=m.map_id where m.setting_id=(select id from setting s where s.difficulty=$difficulty and s.length=$length) and 
                m.result is not NULL group by m2.name,m.result""") {
            resultAggregator(diffData, it)
        }
        diffData.values().collect{ new LevelDifficulty(it) }
    }
    public Object getData(String queryName, List<Object> parameters) {
        throw new UnsupportedOperationException("Not yet implemented")
    }

}
