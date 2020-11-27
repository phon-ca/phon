define(["options", 'util', 'jquery', 'jquery.highlight'], function(options, util, $) {
    // Add some Bootstrap classes when document is ready
    var highlighted = false;

    $(document).ready(function () {
        var scrollPosition = $(window).scrollTop();
        handleSideTocPosition(scrollPosition);
        handlePageTocPosition(scrollPosition);

        $(window).scroll(function() {
            scrollPosition = handleSideTocPosition(scrollPosition);
            scrollPosition = handlePageTocPosition(scrollPosition);
        });
        $(window).resize(function(){
            $("#wh_publication_toc").removeAttr('style');
            scrollPosition = handleSideTocPosition(scrollPosition);
            scrollPosition = handlePageTocPosition(scrollPosition);
        });

        // Show/hide the button which expands/collapse the subtopics
        // if there are at least two subtopics in a topic
        var countSubtopics = $('.topic.nested1').length;
        var countSections = $('section.section .title').length;
        if(countSubtopics > 1 || countSections >1){
            $('.webhelp_expand_collapse_sections').show();
        }

        // WH-231
        // Expanding the side-toc
        $('.dots-before').click(function(){
            $(this).siblings('.hide-before').show();
            $(this).hide();
        });

        $('.dots-after').click(function(){
            $(this).siblings('.hide-after').show();
            $(this).hide();
        });

		// WH-2209
        var showZoomedImage = options.getBoolean("webhelp.show.full.size.image");
        if (showZoomedImage) {
            // Get the image and insert it inside the modal - use its "alt" text as a caption
            $.each( $('img.image:not([usemap])'), function (e) {
                 var parentElement = $(this).parent().get(0).tagName;
                 if(this.naturalWidth > this.width && parentElement.toLowerCase() != 'a'){
                     $(this).addClass('zoom');
                 }
             });
            $('.zoom').click(function(){
                $('#modal_img_large').css("display","block");
                $("#modal-img").attr('src',$(this).attr('src') );
                $("#caption").html( $(this).attr('alt') );
            });
        }

        // When the user clicks on (x), close the modal
        $(".modal .close").click(function(){
            $(".modal").css("display","none");
        });
        $(document).keyup(function(e) {
            if (e.keyCode == 27 && $('#modal_img_large').is(":visible")) { // escape key maps to keycode `27`
               $(".modal").css("display","none");
           }
       });

        // Navigational links and print
        $('#topic_navigation_links .navprev>a').addClass("oxy-icon oxy-icon-arrow-left");
        $('#topic_navigation_links .navnext>a').addClass("oxy-icon oxy-icon-arrow-right");
        $('.wh_print_link button').addClass('oxy-icon oxy-icon-print');

        // Hide sideTOC when it is empty
        var sideToc = $('#wh_publication_toc');
        var pageToc = $('#wh_topic_toc');
        if (sideToc !== undefined) {
            var sideTocChildren = sideToc.find('*');
            if (sideTocChildren.length == 0) {
                sideToc.css('display', 'none');

                // The topic content should span on all 12 columns
                sideToc.removeClass('col-lg-4 col-md-4 col-sm-4 col-xs-12');
                var topicContentParent = $('.wh_topic_content').parent();
                if (topicContentParent !== undefined && pageToc == undefined) {
                    topicContentParent.removeClass(' col-lg-8 col-md-8 col-sm-8 col-xs-12 ');
                    topicContentParent.addClass(' col-lg-12 col-md-12 col-sm-12 col-xs-12 ');
                }
            } else {
                /* WH-1518: Check if the tooltip has content. */
                var emptyShortDesc = sideToc.find('.topicref .wh-tooltip .shortdesc:empty');
                if (emptyShortDesc.length > 0) {
                    var tooltip = emptyShortDesc.closest('.wh-tooltip');
                    tooltip.remove();
                }
            }
        }

        // WH-1518: Hide the Breadcrumb tooltip if it is empty.
        var breadcrumb = $('.wh_breadcrumb');
        var breadcrumbShortDesc = breadcrumb.find('.topicref .wh-tooltip .shortdesc:empty');
        if (breadcrumbShortDesc.length > 0) {
            var tooltip = breadcrumbShortDesc.closest('.wh-tooltip');
            tooltip.remove();
        }

        var $allAccordionHeaders = $(".wh_main_page_toc .wh_main_page_toc_accordion_header");
        var $allAccordionButtons = $(".wh_main_page_toc .wh_main_page_toc_accordion_header .header-button");

        $allAccordionHeaders.click(function(event) {
            $headerButton = $(this).find('.header-button');
            if ($(this).hasClass('expanded')) {
                $(this).removeClass("expanded");
                $headerButton.attr('aria-expanded', 'false');
            } else {
                $allAccordionHeaders.removeClass("expanded");
                $(this).addClass("expanded");
                $allAccordionButtons.attr('aria-expanded', 'false');
                $headerButton.attr('aria-expanded', 'true');
            }
            event.stopImmediatePropagation();
            return false;

        });
        /* Toggle expand/collapse on enter and space */
        $allAccordionButtons.keypress(function( event ) {
            // Enter & Spacebar events
            if ( event.which === 13 || event.which === 32) {
                event.preventDefault();
                var $parentHeader = $(this).closest('.wh_main_page_toc_accordion_header');
                if ($parentHeader.hasClass('expanded')) {
                    $parentHeader.removeClass("expanded");
                    $(this).attr('aria-expanded', 'false');
                } else {
                    $allAccordionHeaders.removeClass("expanded");
                    $parentHeader.addClass("expanded");
                    $allAccordionButtons.attr('aria-expanded', 'false');
                    $(this).attr('aria-expanded', 'true');
                }
            }
            return false;
        });

        $(".wh_main_page_toc a").click(function(event) {
            event.stopImmediatePropagation();
        });

        highlightSearchTerm();

        

        /**
         * Check to see if the window is top if not then display button
         */
        $(window).scroll(function(){
            if ($(this).scrollTop() > 5) {
                $('#go2top').fadeIn('fast');
            } else {
                $('#go2top').fadeOut('fast');
            }
        });

        /**
         * Click event to scroll to top
         */
        $('#go2top').click(function(){
            $('html, body').animate({scrollTop : 0},800);

            return false;
        });
    });

/**
 * @description Handle the vertical position of the side toc
 */
function handleSideTocPosition(scrollPosition) {
    var scrollPosition = scrollPosition !== undefined ? scrollPosition : 0;
    var $sideToc = $(".wh_publication_toc");
    var $sideTocID = $("#wh_publication_toc");
    var $navSection = $(".wh_tools");
    var bottomNavOffset = 0;
    var $slideSection = $('#wh_topic_body');
    var topOffset = 20;
    var visibleAreaHeight = parseInt($(window).height()) - parseInt($(".wh_footer").outerHeight());

    if ($sideToc.length > 0 && $slideSection.length > 0) {
        var minVisibleOffset = $(window).scrollTop();
        var tocHeight = parseInt($sideToc.height()) + parseInt($sideToc.css("padding-top")) + parseInt($sideToc.css("padding-bottom")) + parseInt($sideToc.css("margin-top")) + parseInt($sideToc.css("margin-bottom"));
        var tocWidth = parseInt($sideTocID.outerWidth()) - parseInt($sideTocID.css("padding-left")) - parseInt($sideTocID.css("padding-right"));
        var tocXNav = parseInt($slideSection.offset().left) - tocWidth;
    
        if (scrollPosition > $(window).scrollTop()) {
            if ($sideToc.offset().top < $sideToc.parent().offset().top) {
                $sideToc.css('position', 'inherit');
            }
        } else {
            if (tocHeight > $(window).height()) {
                $sideToc.css('position', 'inherit');
            }
        }

        if ($navSection.length > 0) {
            bottomNavOffset = parseInt($navSection.offset().top) + parseInt($navSection.height()) + parseInt($navSection.css("padding-top")) + parseInt($navSection.css("padding-bottom")) + parseInt($navSection.css("margin-top")) + parseInt($navSection.css("margin-bottom"));
        }
        if (bottomNavOffset > minVisibleOffset) {
            minVisibleOffset = bottomNavOffset;
        }
        if (tocHeight  <=   visibleAreaHeight) {
            var cHeight = parseInt($('.wh_content_area').height());
            if (parseInt(minVisibleOffset - topOffset) <=  $(window).scrollTop() && parseInt($(window).width()) > 767) {
                $('.wh_content_area').css('min-height', cHeight+'px');
                $sideToc.css("top", topOffset + "px").css("width", tocWidth + "px").css("position", "fixed").css("z-index", "999");
            } else {
                $sideToc.removeAttr('style');
            }
        } else {
            $sideToc.removeAttr('style');
        }
        scrollPosition = $(window).scrollTop();
    }

	return $(window).scrollTop();
}

/**
 * @description Highlight the current node in the page toc section on page scroll or clicking on Topic TOC items 
 */
function pageTocHighlightNode(scrollPosition) {
    var scrollPosition = scrollPosition !== undefined ? Math.round(scrollPosition) : 0;
    var topOffset = 150;
    var hash = location.hash != undefined ? location.hash : "";
    var hashOffTop = $(hash).offset() != undefined ? $(hash).offset().top : 0;
    var elemHashTop =  hash != "" ? Math.round(hashOffTop) : 0;
    
    if( hash.substr(1) != '' && elemHashTop >= scrollPosition && (elemHashTop <= (scrollPosition + topOffset)) ){
        $('#wh_topic_toc a').removeClass('current_node');
        $('#wh_topic_toc a[data-tocid = "'+ hash.substr(1) + '"]').addClass('current_node');
    } else {
        $.each( $('.wh_topic_content .title'), function (e) {
            var currentId = $(this).parent().attr('id');
            var elemTop = Math.round($(this).offset().top);
    
            if( elemTop >= scrollPosition && (elemTop <= (scrollPosition + topOffset)) ){
                $('#wh_topic_toc a').removeClass('current_node');
                $('#wh_topic_toc a[data-tocid = "'+ currentId + '"]').addClass('current_node');
            }  
        });
    }
    return $(window).scrollTop();
}



/**
 * @description Handle the vertical position of the page toc
 */
function handlePageTocPosition(scrollPosition) {
    scrollPosition = scrollPosition !== undefined ? scrollPosition : 0;
    var $pageTOCID = $("#wh_topic_toc");
    var $pageTOC = $(".wh_topic_toc");
    var $navSection = $(".wh_tools");
    var bottomNavOffset = 0;
    var topOffset = 33;
    var $contentBody = $(".wh_topic_content");

    if ($pageTOC.length > 0) {
        pageTocHighlightNode(scrollPosition);
        
        var visibleAreaHeight = parseInt($(window).height()) - parseInt($(".wh_footer").outerHeight());

        var tocHeight = parseInt($pageTOC.height()) + parseInt($pageTOC.css("padding-top")) + parseInt($pageTOC.css("padding-bottom")) + parseInt($pageTOC.css("margin-top")) + parseInt($pageTOC.css("margin-bottom"));
        var tocWidth =  parseInt($pageTOCID.outerWidth()) - parseInt($pageTOCID.css("padding-left")) - parseInt($pageTOCID.css("padding-right"));

        var minVisibleOffset = $(window).scrollTop();
        if ($navSection.length > 0) {
            bottomNavOffset = parseInt($navSection.offset().top) + parseInt($navSection.height()) + parseInt($navSection.css("padding-top")) + parseInt($navSection.css("padding-bottom")) + parseInt($navSection.css("margin-top")) + parseInt($navSection.css("margin-bottom"));
        }
        if (bottomNavOffset > minVisibleOffset) {
            minVisibleOffset = bottomNavOffset;
        }

        if ((tocHeight+topOffset) < visibleAreaHeight && (bottomNavOffset-topOffset) < $(window).scrollTop() && (tocHeight+topOffset) < $contentBody.height()) {
            if (parseInt(minVisibleOffset - topOffset) <=  $(window).scrollTop() && parseInt($(window).width()) > 767) {
                $pageTOC.css("top", "20px").css("position", "fixed").css("width", tocWidth + "px").css("height", tocHeight + "px");
            } else {
                $pageTOC.removeAttr('style');
            }
        } else {
            $pageTOC.removeAttr('style');
        }
    }
}

    /**
     * @description Highlight searched words
     */
    function highlightSearchTerm() {
        util.debug("highlightSearchTerm()");
        if (highlighted) {
            return;
        }
        try {
            var $body = $('.wh_topic_content');
            var $relatedLinks = $('.wh_related_links');
            var $childLinks = $('.wh_child_links');

            // Test if highlighter library is available
            if (typeof $body.removeHighlight != 'undefined') {
                $body.removeHighlight();
                $relatedLinks.removeHighlight();

                var hlParameter = util.getParameter('hl');
                if (hlParameter != undefined) {
                    var jsonString = decodeURIComponent(String(hlParameter));
                    util.debug("jsonString: ", jsonString);
                    if (jsonString !== undefined && jsonString != "") {
                        var words = jsonString.split(',');
                        util.debug("words: ", words);
                        for (var i = 0; i < words.length; i++) {
                            util.debug('highlight(' + words[i] + ');');
                            $body.highlight(words[i]);
                            $relatedLinks.highlight(words[i]);
                            $childLinks.highlight(words[i]);
                        }
                    }
                }
            } else {
                // JQuery highlights library is not loaded
            }
        }
        catch (e) {
            util.debug (e);
        }
        highlighted = true;
    }

    /*
     * Hide the highlight of the search results
     */
    $('.wh_hide_highlight').click(function(){
        $('.highlight').addClass('wh-h');
        $('.wh-h').toggleClass('highlight');
        $(this).toggleClass('hl-close');
    });

    /*
     * Show the highlight button only if 'hl' parameter is found
     */
    if( util.getParameter('hl')!= undefined ){
        $('.wh_hide_highlight').show();
    }

});
