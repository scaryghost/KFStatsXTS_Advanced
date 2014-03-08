public class PlayerList extends WebPage {
    public PlayerList() {
        pageTitle= "Players"
        cssSrcs << 'css/jquery.dataTables_themeroller.css' << 
                'http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css'
        jsSrcs << 'js/jquery.dataTables.min.js'

        embeddedJs << """var table
        \$(document).ready(function() {
            table= \$('#records').dataTable({
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
                sAjaxSource: 'playerlistdata.json',
                sPaginationType: 'full_numbers',
                aLengthMenu: [[25, 50, 100, 250], [25, 50, 100, 250]],
                aaSorting: [],
                aoColumnDefs: [
                    { "sTitle": "Name", "sName": "name", "aTargets": [ 0 ] },
                    { "sTitle": "Wins", "sName": "wins", "aTargets": [ 1 ] },
                    { "sTitle": "Losses", "sName": "losses", "aTargets": [ 2 ] },
                    { "sTitle": "Disconnects", "sName": "disconnects", "aTargets": [ 3 ] },
                    { "sTitle": "Time Played", "sName": "time_played", "aTargets": [ 4 ] }
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
