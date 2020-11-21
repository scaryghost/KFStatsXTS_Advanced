import scaryghost.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder
import java.util.UUID

public class PlayerSession extends WebPage {
    protected def pageTitle= "Player Session"

    public PlayerSession() {
        cssSrcs << 'css/heatmap.css'
        jsSrcs << 'js/heatmap.js'
    }

    protected void fillBody(builder) {
        def matchUUID= UUID.fromString(queries.matchuuid)
        def players= reader.executeQuery("server_match_players", matchUUID)
        def playerData= reader.executeQuery("server_match_player_data", matchUUID)

        builder.h1(class:'text-center', "Player Data")
        playerData.collect(new HashSet()){ it.category }.each {category ->
            builder.table(cellpadding:"0", cellspacing:"0", border:"0", class:"heat-map") {
                thead() {
                    tr() {
                        th(colspan: players.size() + 1) {
                            h3(category)
                        }
                    }
                    tr(class: 'stats-row') {
                        th(class:'first', 'Name')
                        players.each {player ->
                            th(player.name)
                        }
                    }
                }
                builder.tbody() {
                    playerData.findAll{ it.category == category }.collect(new HashSet()){ it.statistic }.each{ stat ->
                        tr(class:'stats-row') {
                            td(class:'stats-title', stat)
                            players.each {player ->
                                def result= playerData.find{it.statistic == stat && it.steamid64 == player.steamid64 && it.category == category}
                                td(result == null ? 0 : result.value)
                            }
                        }
                    }
                }
            }
        }
    }
}
