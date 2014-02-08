    function colorCells(tableId) {
        Array.max = function( array ){
        return Math.max.apply( Math, array );
    };

    var fullTableId= '#' + tableId + ' tbody td';
    // get all values
    var counts= $(fullTableId).not('.stats-title').map(function() {
        return parseInt($(this).text());
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
	$(fullTableId).not('.stats-title').each(function(){
		var val = parseInt($(this).text());
		var pos = parseInt((Math.round((val/max)*100)).toFixed(0));
		red = parseInt((xr + (( pos * (yr - xr)) / (n-1))).toFixed(0));
		green = parseInt((xg + (( pos * (yg - xg)) / (n-1))).toFixed(0));
		blue = parseInt((xb + (( pos * (yb - xb)) / (n-1))).toFixed(0));
		clr = 'rgb('+red+','+green+','+blue+')';
		$(this).css({backgroundColor:clr});
	});
    }
    $(document).ready(function(){
	// Function to get the Max value in Array
        Array.prototype.forEach.call(document.getElementsByClassName('heat-map'), function(entry) {
            colorCells(entry.getAttribute('id'));
        });
});
