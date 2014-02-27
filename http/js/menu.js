$(document).ready(function() {
    setTimeout(function() {
    // Slide
        $('#menu1 > li > a.expanded + ul').slideToggle('medium');
        $('#menu1 > li > a').click(function() {
            $(this).toggleClass('expanded').toggleClass('collapsed').parent().find('> ul').slideToggle('medium');
        });
        $('#example1 .expand_all').click(function() {
            $('#menu1 > li > a.collapsed').addClass('expanded').removeClass('collapsed').parent().find('> ul').slideDown('medium');
        });
        $('#example1 .collapse_all').click(function() {
            $('#menu1 > li > a.expanded').addClass('collapsed').removeClass('expanded').parent().find('> ul').slideUp('medium');
        });
    }, 250);
});
