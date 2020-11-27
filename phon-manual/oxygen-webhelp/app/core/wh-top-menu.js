define(['util', 'jquery', 'jquery.highlight'], function(util, $) {
    var isTouchEnabled = false;
    try {
        if (document.createEvent("TouchEvent")) {
            isTouchEnabled = true;
        }
    } catch (e) {
        util.debug(e);
    }
	
	/**
     * Open the link from top_menu when the current group is expanded.
     *
     * Apply the events also on the dynamically generated elements.
     */

    $(document).on('click', ".wh_top_menu li", function (event) {
        $(".wh_top_menu li").removeClass('active');
        $(this).addClass('active');
        $(this).parents('li').addClass('active');

        event.stopImmediatePropagation();
    });

    $(document).on('click', '.wh_top_menu a', function (event) {
        var pointerType;
        if (typeof event.pointerType !== "undefined") {
            pointerType = event.pointerType;
        }

        if ($(window).width() < 767 || isTouchEnabled || pointerType == "touch") {
            var areaExpanded = $(this).closest('li');
            var isActive = areaExpanded.hasClass('active');
            var hasChildren = areaExpanded.hasClass('has-children');
            if (isActive || !hasChildren) {
                window.location = $(this).attr("href");
                event.preventDefault();
                event.stopImmediatePropagation();
                return false;
            } else {
                event.preventDefault();
            }
        } else {
            return true;
        }
    });
});
