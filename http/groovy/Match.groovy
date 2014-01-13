import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class Index extends Resource {
    private def resultNames= [(-1): "Loss", (0): "Map Voted", (1): "Win"],
            matchUUID= '532c7656-073a-4a38-8f19-3ea56f352321'

    public String generatePage() {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)
        def waveData= reader.executeQuery("server_match_wave_data", matchUUID)
        def wavePerks= reader.executeQuery("server_match_wave_perks", matchUUID)
        def waves= wavePerks.collect(new TreeSet()){ it.wave }

        htmlBuilder.html() {
            head() {
                meta('http-equiv':"Content-Type", content:"text/html; charset=utf-8")
                title('Flot Examples: AJAX')
                style('td+td { text-align: right;}')
                link(href:'css/examples.css', rel:'stylesheet', type:'text/css')
                script(language:"javascript", type:"text/javascript", src:"js/jquery.js", '')
                script(language:"javascript", type:"text/javascript", src:"js/jquery.flot.js", '')
                script(language:"javascript") {
                    mkp.yieldUnescaped '''
    \$(function() {
        var options = {
            lines: {
                show: true
            },
            points: {
                show: true
            },
            grid: {
				hoverable: true,
				clickable: true
			},
            xaxis: {
                tickDecimals: 0,
                tickSize: 1
            }
        };
        function onDataReceived(series) {
                \$.plot("#placeholder", [series], options);
            }

            \$.ajax({
                    url: "data.json",
                    type: "GET",
                    dataType: "json",
                    success: onDataReceived
                });
    \$("<div id='tooltip'></div>").css({
			position: "absolute",
			display: "none",
			border: "1px solid #fdd",
			padding: "2px",
			"background-color": "#fee",
			opacity: 0.80
		}).appendTo("body");
        \$("#placeholder").bind("plothover", function (event, pos, item) {
				if (item) {
					var x = item.datapoint[0].toFixed(2),
						y = item.datapoint[1].toFixed(2);

					$("#tooltip").html(y + "s")
						.css({top: item.pageY+5, left: item.pageX+5})
						.fadeIn(200);
				} else {
					$("#tooltip").hide();
				}
		});
      });
'''
                }
            }
            body() {
                div(id:'header') {
                    h2('Match Information')
                }
                div(id:'content') {
                    div(style: 'float: left; width: 50%;') {
                        def summary= reader.executeQuery("server_match", matchUUID)
                        table() {
                            tbody() {
                                tr() {
                                    td('Number of Players')
                                    td(summary.num_players)
                                }
                                tr() {
                                    td('Server IP')
                                    td("${summary.address}:${summary.port}")
                                }
                                tr() {
                                    td('Difficulty')
                                    td(summary.difficulty)
                                }
                                tr() {
                                    td('Length')
                                    td(summary.length)
                                }
                                tr() {
                                    td('Map')
                                    td(summary.name)
                                }
                                tr() {
                                    td('Wave')
                                    td(summary.wave)
                                }
                                tr() {
                                    td('Result')
                                    td(resultNames[summary.result])
                                }
                            }
                        }
                    }
                    div(class:"demo-container") {
                        div(id:"placeholder",class:"demo-placeholder",'')
                    }
                }
                    def waveSummaries= reader.executeQuery("server_wave_summaries", matchUUID)
                    waves.each {wave ->
                        def perks= wavePerks.findAll{ it.wave == wave }
                        def summary= waveSummaries.find{ it.wave == wave }
                        def data= waveData.findAll{ it.wave == wave }

                        div(id: "wave_section_$wave", style:"clear: right; width: 1024px") {
                            table(style: 'width: 50%', border: 1) {
                                tr() {
                                    def head= perks.head()
                                    td(rowspan: perks.size(), 'Perks')
                                    td(head.name, style: "width: 50%")
                                    td(head.count)
                                }
                                perks.tail().each {p ->
                                    tr() {
                                        td(p.name, style: "width: 50%")
                                        td(p.count)
                                    }
                                }
                                tr() {
                                    td('Duration')
                                    td(summary.duration)
                                    td()
                                }
                                tr() {
                                    td('Survived')
                                    td(summary.survived)
                                    td()
                                }
                            }
                            data.collect(new HashSet()){ it.category }.each {category->
                                table() {
                                    thead() {
                                        tr() {
                                            th(category)
                                            perks.each {perk ->
                                                th(perk.name)
                                            }
                                        }
                                    }
                                    tbody() {
                                        data.findAll{it.category == category}.collect(new HashSet()){ it.stat }.each {stat ->
                                            tr() {
                                                td(stat)
                                                perks.each {perk ->

                                                    def result= data.find{it.category == category && it.stat == stat && it.perk == perk.name}
                                                    td(result == null ? 0 : result.value)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        hr()
                    }
            }
        }

        return String.format("<!DOCTYPE HTML>%n%s", writer)
    }
}
