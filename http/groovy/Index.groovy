import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class Index extends WebPage {
    public Index() {
        pageTitle= "KFStatsX Home"
        cssSrcs << 'css/heatmap.css' << 'css/menu.css'
        jsSrcs << 'js/heatmap.js' << 'js/menu.js'
    }
    protected void fillBody(builder) {
        def data= reader.executeQuery("server_total_data")

        builder.div(style: "float: left;", id:'example1') {
            h4() {
                mkp.yieldUnescaped 'Filters'
                a(class:'expand_all') {
                    img(src:'images/expand.gif', alt: '')
                }
                a(class:'collapse_all') {
                    img(src:'images/collapse.gif', alt:'')
                }
            }
            form() {
            def inputId= -1
            ul(id:'menu1', class:'example_menu') {
                [['Server', 'server_list', 'address_port'], ['Setting', 'server_settings', 'difficulty_length'], 
                        ['Map', 'server_maps', 'name']].each{ header, queryName, colName ->
                    li() {
                        p(class:'expanded', header)
                        ul() {
                            reader.executeQuery(queryName).each {row ->
                                li() {
                                    p() {
                                        input(type:"checkbox", name:header.toLowerCase(), value:row[colName], 
                                                onchange:"updateFilter(this.name, this.value)", 
                                                style: 'margin-right:5px;', id: "input_${++inputId}")
                                        label(for:"input_$inputId", row[colName])
                                    }
                                }
                            }
                        }
                    }
                    li(class: 'footer') {
                        span(' ')
                    }
                }
            }
            }
        }
        builder.div(id: 'content', '')
/*
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
*/
    }

}
