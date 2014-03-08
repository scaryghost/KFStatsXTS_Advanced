import com.github.etsai.kfsxtrackingserver.DefaultReader.Order
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.json.JsonBuilder

public class PlayerListData extends Resource {
    public String generatePage() {
        def order= queries.containsKey("sSortDir_0") ? 
                Order.valueOf(Order.class, queries.sSortDir_0.toUpperCase()) : Order.NONE
        def group= !queries.containsKey("iSortCol_0") ? null : 
                queries.sColumns.tokenize(",")[queries.iSortCol_0.toInteger()]

        def data= reader.executeQuery("server_player_list", group, order, 
                queries.iDisplayStart.toInteger(), queries.iDisplayLength.toInteger())
        def builder= new JsonBuilder()
        def playerCount= reader.executeQuery("server_player_count")

        builder {
            sEcho(queries.sEcho.toInteger())
            iTotalRecords(playerCount)
            iTotalDisplayRecords(playerCount)
            aaData data.collect {
                ["<a href=player.html?steamid64=${it.id}>${it.name}</a>", it.wins,
                        it.losses, it.disconnects, it.time_played]
            }
        }
        return builder
    }
}
