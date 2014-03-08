public class Index extends WebPage {
    public Index() {
        pageTitle= "KFStatsX"
        cssSrcs << 'css/heatmap.css'
        jsSrcs << 'js/heatmap.js'
    }
    protected void fillBody(builder) {
        builder.div(style: 'width: 650px;margin-left: auto;margin-right: auto;') {
            ul(class:'nav-list') {
                li(class:'nav-list') {
                    a(href: 'serverstats.html', 'Stats')
                }
                li(class:'nav-list', "servers")
                li(class:'nav-list', "players")
                li(class:'nav-list', "matches")
            }
        }
        ["server_totals", "server_setting_stats", "server_map_stats", "server_list_2"].each {query ->
            builder.table(cellpadding:"0", cellspacing:"0", border:"0", 
                    class:"heat-map") {
                def rows= reader.executeQuery(query)
                builder.thead() {
                    builder.tr() {
                        rows[0].keySet().each {
                            builder.th(it)
                        }
                    }
                }
                tbody() {
                    rows.each {result ->
                        tr(class: 'stats-row') {
                            result.each {stat, value ->
                                td(value)
                            }
                        }
                    }
                }
            }
        }
    }
}
