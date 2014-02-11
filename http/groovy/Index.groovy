import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class Index extends WebPage {
    public Index() {
        pageTitle= "KFStatsX Home"
        cssSrcs << 'css/heatmap.css'
        jsSrcs << 'js/heatmap.js'
    }
    protected void fillBody(builder) {
        def data= reader.executeQuery("server_total_data")

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
