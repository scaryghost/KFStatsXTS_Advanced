import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class WebPage extends Resource {
    public String generatePage() {
        def data= reader.executeQuery("server_filtered_total_data", queries.category, queries.value)
        def writer= new StringWriter()
        def builder= new MarkupBuilder(writer)

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

        return writer
    }
}
