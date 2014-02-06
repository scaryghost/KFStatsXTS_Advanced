import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder
import java.util.UUID

public class Match extends Resource {
    public String generatePage() {
        def matchUUID= UUID.fromString(queries.matchuuid)
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)

        htmlBuilder.html() {
            head() {
                meta('http-equiv':"Content-Type", content:"text/html; charset=utf-8")
                title('Bubble Chart Example')
                script(type:'text/javascript', src:'//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js', '')
                script(type:'text/javascript') {
                    mkp.yieldUnescaped """
    function colorCells(tableId) {
        Array.max = function( array ){
        return Math.max.apply( Math, array );
    };

    var fullTableId= '#' + tableId + ' tbody td';
    // get all values
    var counts= \$(fullTableId).not('.stats-title').map(function() {
        return parseInt(\$(this).text());
    }).get();
	
	// return max value
	var max = Array.max(counts);
	
	xr = 255;
    xg = 255;
    xb = 255;
   
    yr = 243;
    yg = 32;
    yb = 117;

    n = 100;
	
	// add classes to cells based on nearest 10 value
	\$(fullTableId).not('.stats-title').each(function(){
		var val = parseInt(\$(this).text());
		var pos = parseInt((Math.round((val/max)*100)).toFixed(0));
		red = parseInt((xr + (( pos * (yr - xr)) / (n-1))).toFixed(0));
		green = parseInt((xg + (( pos * (yg - xg)) / (n-1))).toFixed(0));
		blue = parseInt((xb + (( pos * (yb - xb)) / (n-1))).toFixed(0));
		clr = 'rgb('+red+','+green+','+blue+')';
		\$(this).css({backgroundColor:clr});
	});
    }
    \$(document).ready(function(){
	// Function to get the Max value in Array
        Array.prototype.forEach.call(document.getElementsByClassName('heat-map'), function(entry) {
            colorCells(entry.getAttribute('id'));
        });
});"""
                }
                style(type:'text/css') {
                    mkp.yieldUnescaped """
    body,html,div,blockquote,img,label,p,h1,h2,h3,h4,h5,h6,pre,ul,ol,li,dl,dt,dd,form,a,fieldset,input,th,td{border:0;outline:none;margin:0;padding:0;}
body{height:100%;background:#fff;color:#1f1f1f;font-family:Arial,Verdana,sans-serif;font-size:13px;padding:7px 0;}
ul, ol{list-style:none;}
a {text-decoration: none;}
.text-center {text-align: center; padding: 10px 0;}
h1,h2 {margin-bottom: 30px;}
.wrap {width: 653px; margin: 0 auto;}
.clear {clear: both;}

/* Tutorial CSS */
.heat-map {border: 1px solid #ccc; margin: 0 auto 20px auto; width: 650px;}
.heat-map tr th {padding: 5px 7px; border-right: 1px solid #ccc; border-bottom: 1px solid #ccc; border-top: 1px solid #fff; border-left: 1px solid #fff; text-shadow: 0 1px 0 #fff; font: bold 11px Arial, sans-serif; text-transform: uppercase; color: #000;
background-color: #F1F1F1;
    background-image: -moz-linear-gradient(center top , #F9F9F9, #ECECEC);
}
.heat-map tr th.last {border-right: none;}
.heat-map tr th.first, .heat-map tr td.stats-title {border-left: none; text-align: left;}
.heat-map tr td {padding: 5px 7px; /*border-top: 1px solid #fff; border-right: 1px solid #fff; */ border-right: 1px solid #fff;border-bottom: 1px solid #fff;text-align right; color: #000; font-size: 11px;}
tr.stats-row {text-align: right;}
.heat-map tr.stats-row td.stats-title {text-align: left; color: #111; font-size: 13px; text-shadow: 0 1px 0 #fff; background: #efefef; border-top: 1px solid #fff; border-bottom: 1px solid #ccc;  border-right: 1px solid #ccc;}
.heat-map tr.stats-row td.stats-title.last {border-bottom: 1px solid #fff;}
"""
                }
            }
            body() {
                def wavePerks= reader.executeQuery("server_match_wave_perks", matchUUID)
                def waveSummaries= reader.executeQuery("server_wave_summaries", matchUUID)
                def data= reader.executeQuery("server_match_wave_data", matchUUID)
                def waves= wavePerks.collect(new TreeSet()){ it.wave }
                def index= -1

                h1(class:'text-center', "Match Information")
                def summary= reader.executeQuery("server_match", matchUUID)
                table(class:'heat-map') {
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
                    h2(class:'text-center', "Wave $waveNum")
                    table(class:'heat-map') {
                        tobdy() {
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
                        table(cellpadding:"0", cellspacing:"0", border:"0", class:"heat-map", id:"heat-map-${(++index)}") {
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

        return String.format("<!DOCTYPE HTML>%s%n", writer)
    }
}
