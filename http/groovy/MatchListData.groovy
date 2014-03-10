import com.github.etsai.kfsxtrackingserver.DefaultReader.Order
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.json.JsonBuilder

public class MatchListData extends Resource {
    public String generatePage() {
        def order= queries.containsKey("sSortDir_0") ? 
                Order.valueOf(Order.class, queries.sSortDir_0.toUpperCase()) : Order.NONE
        def group= !queries.containsKey("iSortCol_0") ? null : 
                queries.sColumns.tokenize(",")[queries.iSortCol_0.toInteger()]

        def data= reader.executeQuery("server_match_list", group, order, 
                queries.iDisplayStart.toInteger(), queries.iDisplayLength.toInteger())
        def builder= new JsonBuilder()
        def playerCount= reader.executeQuery("server_match_count")

        builder {
            sEcho(queries.sEcho.toInteger())
            iTotalRecords(playerCount)
            iTotalDisplayRecords(playerCount)
            aaData data.collect {
                ["<a href=match.html?matchuuid=${it.id}>${it.address_port}</a>", it.difficulty,
                        it.length, it.map, it.wave, it.result, it.time_begin.toString(), it.time_end.toString()]
            }
        }
        return builder
    }
}
