import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder
import java.util.UUID

public class Match extends WebPage {
    protected def pageTitle= "Match Recap"

    public Match() {
        cssSrcs << 'css/heatmap.css'
        jsSrcs << 'js/heatmap.js'
    }
    protected void fillBody(builder) {
        def matchUUID= UUID.fromString(queries.matchuuid)
        def wavePerks= reader.executeQuery("server_match_wave_perks", matchUUID)
        def waveSummaries= reader.executeQuery("server_wave_summaries", matchUUID)
        def data= reader.executeQuery("server_match_wave_data", matchUUID)
        def waves= wavePerks.collect(new TreeSet()){ it.wave }
        def index= -1

        builder.h1(class:'text-center', "Match Recap")
        def summary= reader.executeQuery("server_match", matchUUID)
        builder.table(class:'heat-map') {
            tbody() {
                summary.each{key, value ->
                    tr(class: 'stats-row') {
                        td(class: 'stats-title', key)
                        td(value)
                    }
                }
            }
        }
        waves.each {waveNum ->
            def perks= wavePerks.findAll{ it.wave == waveNum }
            builder.h2(class:'text-center', "Wave $waveNum")
            builder.table(class:'heat-map') {
                tbody() {
                    tr(class: 'stats-row') {
                        td(class:'stats-title', rowspan: 2, 'Perks')
                        perks.each{ perk ->
                            td(perk.name)
                        }
                    }
                    tr(class: 'stats-row') {
                        perks.each{ perk ->
                            td(perk.count)
                        }
                    }
                    waveSummaries.find{ it.wave == waveNum }.each {key, val ->
                        tr(class: 'stats-row') {
                            td(class:'stats-title', key)
                            td(colspan: perks.size() - 1 ,val)
                        }
                    }
                }
            }
            data.collect(new HashSet()){ it.category  }.each {category->
                builder.table(cellpadding:"0", cellspacing:"0", border:"0", class:"heat-map", id:"heat-map-${(++index)}") {
                    thead() {
                        tr() {
                            th(colspan: perks.size() + 1) {
                                h3(category)
                            }
                        }
                        tr(class: 'stats-row') {
                            th(class:'first', 'Name')
                            wavePerks.findAll{ it.wave == waveNum}.each {perk ->
                                th(perk.name)
                            }
                        }
                    }
                    tbody() {
                        data.findAll{it.category == category && it.wave == waveNum }.collect(new HashSet()){ it.stat }.each{ stat ->
                            tr(class:'stats-row') {
                                td(class:'stats-title', stat)
                                wavePerks.findAll{ it.wave == waveNum }.each {perk ->
                                    def result= data.find{it.stat == stat && it.perk == perk.name && it.category == category && it.wave == waveNum}
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
