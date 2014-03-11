public class MatchList extends WebPage {
    public MatchList() {
        pageTitle= "Matches"
        jsSrcs << 'https://www.google.com/jsapi?autoload={"modules":[{"name":"visualization","version":"1","packages":["controls"]}]}' << 
                '//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'
        embeddedJs << """
    function visualizationCallback() {
        var chart= new google.visualization.ChartWrapper({'chartType': 'AnnotationChart', 'containerId': "chart", 'options': {
            'chartArea': {height: '90%'},
            'allowHtml': true,
            'title': 'Match'
        }});
        var data= \$.ajax({url: "matchlistdata.json", dataType:"json", async: false}).responseText;
        var table= new google.visualization.DataTable(data);
        chart.setDataTable(table);
        chart.draw();
    }
    google.setOnLoadCallback(visualizationCallback);
"""
    }

    protected void fillBody(builder) {
        builder.div(id: "dashboard", '') {
            div(id: "filter", '')
            div(id: "chart", '')
        }
    }
}
