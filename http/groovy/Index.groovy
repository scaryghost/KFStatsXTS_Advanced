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
                li(class:'nav-list') {
                    a(href: 'serverlist.html', 'Servers')
                }
                li(class:'nav-list') {
                    a(href: 'maplist.html', 'Maps')
                }
                li(class:'nav-list') {
                    a(href: 'settinglist.html', 'Setting')
                }
                li(class:'nav-list') {
                    a(href: 'playerlist.html', 'Players')
                }
            }
        }
        ["server_totals"].each {query ->
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
