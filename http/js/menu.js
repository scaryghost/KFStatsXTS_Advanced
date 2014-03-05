$(document).ready(function() {
    setTimeout(function() {
    // Slide
        $('#menu1 > li > p.expanded + ul').slideToggle('medium');
        $('#menu1 > li > p').click(function() {
            $(this).toggleClass('expanded').toggleClass('collapsed').parent().find('> ul').slideToggle('medium');
        });
        $('#example1 .expand_all').click(function() {
            $('#menu1 > li > p.collapsed').addClass('expanded').removeClass('collapsed').parent().find('> ul').slideDown('medium');
        });
        $('#example1 .collapse_all').click(function() {
            $('#menu1 > li > p.expanded').addClass('collapsed').removeClass('expanded').parent().find('> ul').slideUp('medium');
        });
    }, 250);
    updateFilter(null, null);
});

var filters= {};
function updateFilter(category, value) {
    var xmlhttp;
    if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
    } else {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }
    if (category != null && value != null) {
        if (!(category in filters)) {
            filters[category]= {};
        }
        if (value in filters[category]) {
            delete filters[category][value];
            if(Object.keys(filters[category]).length == 0) {
                delete filters[category];
            }
        } else {
            filters[category][value]= 1;
        }
    }
    var queries= [];
    for(var catIt in filters) {
        var values= [];
        for(var valIt in filters[catIt]) {
            values.push(valIt);
        }
        queries.push(catIt + "=" + values.join(","));
    }
    xmlhttp.onreadystatechange=function() {
        if (xmlhttp.readyState==4 && xmlhttp.status==200) {
            document.getElementById("content").innerHTML=xmlhttp.responseText;
        }
    }
    if (queries.length == 0) {
        xmlhttp.open("POST","serverstatsdata.html", true);
    } else {
        xmlhttp.open("POST","serverstatsdata.html?" + queries.join("&"), true);
    }
    xmlhttp.send();
}
