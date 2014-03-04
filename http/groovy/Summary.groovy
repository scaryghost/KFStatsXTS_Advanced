public class Summary extends WebPage {
    public Summary() {
        pageTitle= "KFStatsX"
        cssSrcs << 'css/heatmap.css'
        jsSrcs << 'js/heatmap.js'
    }
    protected void fillBody(builder) {
        ["server_totals", "server_setting_stats", "server_map_stats"].each {query ->
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
