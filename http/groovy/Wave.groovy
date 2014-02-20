public class Wave extends WebPage {
    public Wave() {
        pageTitle= "Wave Data"
        cssSrcs << 'css/heatmap.css'
        jsSrcs << 'js/heatmap.js'
    }

    protected void fillBody(builder) {
        def data= reader.executeQuery("server_wave_data", queries.difficulty, queries.length)
        def perks= new HashSet(), waves= new HashSet(), categories= new HashSet()
        data.each {
            perks << it.perk
            waves << it.wave
            categories << it.category
        }
        
        def wavePerks= reader.executeQuery("server_wave_perks", queries.difficulty, queries.length)
        def index= -1
        builder.h1(class:'text-center', "Wave Data")
        waves.each {wave ->
            builder.h2(class:'text-center', "Wave $wave")
            
            builder.table(cellpadding:"0", cellspacing:"0", border:"0", class:"heat-map") {
                thead() {
                    tr() {
                        th(colspan: 2) {
                            h3("Perk Tallies")
                        }
                    }
                    tr() {
                        th(class: 'first', 'Perk')
                        th('Count')
                    }
                }
                tbody() {
                    wavePerks.findAll{ it.wave == wave}.each {perk ->
                        tr(class:'stats-row') {
                            td(class: 'stats-title', perk.name)
                            td(perk.count)
                        }
                    }
                }
            }
            categories.each {category ->
                builder.table(cellpadding:"0", cellspacing:"0", border:"0", 
                        class:"heat-map", id:"heat-map-${(++index)}") {
                    thead() {
                        tr() {
                            th(colspan: perks.size() + 1) {
                                h3(category)
                            }
                        }
                        tr() {
                            th(class: 'first', 'Name')
                            perks.each{
                                th(it)
                            }
                        }
                    }
                    tbody() {
                        data.findAll{it.category == category && it.wave == wave}.collect(new HashSet()){ it.statistic }.each{ stat ->
                            tr(class:'stats-row') {
                                td(class:'stats-title', stat)
                                perks.each{ perk ->
                                    def result= data.find{it.statistic == stat & it.perk == perk && it.category == category && it.wave == wave}
                                    td(result == null ? 0 : result.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
