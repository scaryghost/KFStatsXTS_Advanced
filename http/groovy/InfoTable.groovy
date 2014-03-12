public abstract class InfoTable extends WebPage {
    protected def queryName

    public InfoTable() {
        cssSrcs << 'css/heatmap.css'
        jsSrcs << 'js/heatmap.js'
    }
    protected void fillBody(builder) {
        builder.table(cellpadding:"0", cellspacing:"0", border:"0", 
                    class:"heat-map") {
            def rows= reader.executeQuery(queryName)
            thead() {
                tr() {
                    rows[0].keySet().each {
                        th(it)
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
