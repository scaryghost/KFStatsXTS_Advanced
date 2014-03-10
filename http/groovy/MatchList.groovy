public class MatchList extends WebPage {
    public MatchList() {
        pageTitle= "Matches"
        cssSrcs << 'css/jquery.dataTables_themeroller.css' << 
                'http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css'
        jsSrcs << 'js/jquery.dataTables.min.js'

        embeddedJs << """\$(document).ready(function() {
            \$('#records').dataTable({
                bPaginate: true,
                bProcessing: true,
                bLengthChange: true,
                bFilter: false,
                bSort: true,
                bInfo: true,
                bAutoWidth: false,
                bServerSide: true,
                bJQueryUI: true,
                iDisplayLength: 25,
                sAjaxSource: 'matchlistdata.json',
                sPaginationType: 'full_numbers',
                aLengthMenu: [[25, 50, 100, 250], [25, 50, 100, 250]],
                aaSorting: [],
                aoColumnDefs: [
                    { "sTitle": "Address", "sName": "address_port", "aTargets": [ 0 ] },
                    { "sTitle": "Difficulty", "sName": "dfficulty", "aTargets": [ 1 ] },
                    { "sTitle": "Length", "sName": "length", "aTargets": [ 2 ] },
                    { "sTitle": "Map", "sName": "map", "aTargets": [ 3 ] },
                    { "sTitle": "Wave", "sName": "wave", "aTargets": [ 4 ] },
                    { "sTitle": "Result", "sName": "result", "aTargets": [ 5 ] },
                    { "sTitle": "Time Begin", "sName": "time_begin", "aTargets": [ 6 ] },
                    { "sTitle": "Time End", "sName": "time_end", "aTargets": [ 7 ] }
                ],
                fnServerData: function (sSource, aoData, fnCallback) {
                    aoData.push({"name": "table", "value": "records"});

                    \$.getJSON(sSource, aoData, function(json) {
                        fnCallback(json)
                    })
                }
            });
        } );
"""
    }

    protected void fillBody(builder) {
        builder.table(id: "records", '')
    }
}
