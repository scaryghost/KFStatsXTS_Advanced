import scaryghost.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class Player extends WebPage {
    protected def pageTitle= "Player"

    public Player() {
        cssSrcs << 'css/heatmap.css'
        jsSrcs << 'js/heatmap.js'
    }

    protected void fillBody(builder) {
        def data= reader.executeQuery("player_total_data", queries.steamid64)
        def totals= reader.executeQuery("player_totals", queries.steamid64)
        def info= reader.executeQuery("player_info", queries.steamid64)

        println totals
        builder.h1(class:'text-center', "Player Data")
        builder.table(cellpadding:"0", cellspacing:"0", border:"0", class:"heat-map") {
            thead() {
                tr() {
                    th(colspan: 3) {
                        h3(info.name)
                    }
                }
            }
            tbody() {
                def first= true
                totals.each {key, value ->
                    tr(class: 'stats-row') {
                        td(class: 'stats-title', key)
                        td(value)
                        if (first) {
                            td(rowspan: totals.keySet().size()) {
                                img(src:info.avatar)
                            }
                            first= false
                        }
                    }
                }
            }
        }
        data.collect(new HashSet()){ it.category }.each {category ->
            builder.table(cellpadding:"0", cellspacing:"0", border:"0", class:"heat-map") {
                thead() {
                    tr() {
                        th(colspan: 2) {
                            h3(category)
                        }
                    }
                    tr(class: 'stats-row') {
                        th(class:'first', 'Name')
                        th('value')
                    }
                }
                tbody() {
                    data.findAll{ it.category == category }.each{ row ->
                        tr(class:'stats-row') {
                            td(class:'stats-title', row.statistic)
                            td(row.value)
                        }
                    }
                }
            }
        }
    }
}
