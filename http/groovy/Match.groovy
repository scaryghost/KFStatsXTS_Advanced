import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

public class Index extends Resource {
    private def resultNames= [(-1): "Loss", (0): "Map Voted", (1): "Win"]

    public String generatePage() {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)

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
      });
'''
                }
            }
            body() {
                div(id:'header') {
                    h2('Match Information')
                }
                div(id:'content') {
                    div(style: 'float:left;width: 50%') {
                        def summary= reader.executeQuery("server_match",'3b4c0c99-5f51-4a61-b39e-f7e8d298e30c')
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
            }
        }

        return String.format("<!DOCTYPE HTML>%n%s", writer)
    }
}
