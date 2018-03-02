
/*
    
Oxygen WebHelp Plugin
Copyright (c) 1998-2017 Syncro Soft SRL, Romania.  All rights reserved.

*/

var txt_browser_not_supported = "Your browser is not supported. Use of Mozilla Firefox is recommended.";

/**
 * Constant with maximum search items presented for a single page.
 * @type {number}
 */
var MAX_ITEMS_PER_PAGE = 10;

/**
 * Variable with total page number.
 *
 * @type {number}
 */
var totalPageNumber = -1;

function debug(msg, obj) {
  logLocal(msg);
}

if(typeof String.prototype.trim !== 'function') {
    String.prototype.trim = function() {
        return $.trim(this);
    }
}

$(document).ready(function () {
    $('.wh_indexterms_link').find('a').text('');

    $('.gcse-searchresults-only').attr('data-queryParameterName', 'searchQuery');

    // Select page from parameter in the pages widget
    window.onpopstate = function(event) {
        if (lastSearchResultItems != null && lastSearchResult != null) {
            // Get the value for the 'page' parameter
            var pageToShow = getParameter("page");

            // Set to 1 if it is undefined
            if (pageToShow == undefined || pageToShow == "undefined" || pageToShow == "") {
                pageToShow = 1;
            }

            displayPageResults(pageToShow);

            // Update the active page
            $('.pagination li[class~="active"]').removeClass("active");
            $('.pagination li[data-lp="' + pageToShow + '"]:not([class~="prev"]):not([class~="next"])').addClass("active");

        }
    };
});


/**
 * @description Search using Google Search if it is available, otherwise use our search engine to execute the query
 * @return {boolean} Always return false
 */
function executeQuery() {
    var input = document.getElementById('textToSearch');
    try {
        var element = google.search.cse.element.getElement('searchresults-only0');
    } catch (e) {
        debug(e);
    }
    if (element != undefined) {
        if (input.value == '') {
            element.clearAllResults();
        } else {
            element.execute(input.value);
        }
    } else {
        executeSearchQuery($("#textToSearch").val());
    }

    return false;
}

function clearHighlights() {

}

/**
 * Execute search query with internal search engine.
 *
 * @description This function find all matches using the search term
 * @param {HTMLObjectElement} ditaSearch_Form The search form from WebHelp page as HTML Object
 */
function executeSearchQuery(query) {
    debug('SearchToc(..)');

    // Check browser compatibility
    if (navigator.userAgent.indexOf("Konquerer") > -1) {
        alert(getLocalization(txt_browser_not_supported));
        return;
    }

    searchAndDisplayResults(query);
}

function searchAndDisplayResults(query) {
    var searchResult = performSearch(query);
    if (searchResult.searchExpression.trim().length > 0 || searchResult.excluded.length > 0) {
        displayResults(searchResult);
    }
}

/**
 * @description Display results in HTML format
 * @param {SearchResult} searchResult The search result.
 */
function displayResults(searchResult) {

    preprocessSearchResult(searchResult, 'wh-responsive');

    // Add search query to history
    addSearchQueryToHistory(searchResult.originalSearchExpression);

    // Compute the total page number
    totalPageNumber =
        Math.ceil(lastSearchResultItems.length / MAX_ITEMS_PER_PAGE);

    // Get the value for the 'page' parameter
    var pageToShow = getParameter("page");

    // Set to 1 if it is undefined
    if (pageToShow == undefined || pageToShow == "undefined" || pageToShow == "") {
        pageToShow = 1;
    }

    // Display a page
    displayPageResults(pageToShow);

    if (totalPageNumber > 1) {
        // Add pagination widget
        $('#wh-search-pagination').bootpag({
            total: totalPageNumber,          // total pages
            page: pageToShow,            // default page
            maxVisible: 10,     // visible pagination
            leaps: true         // next/prev leaps through maxVisible
        }).on("page", function(event, num){
            console.log("Display page with number: ", num);

            // Replace or add the page query
            var oldPage = getParameter("page");
            var oldQuery = window.location.search;
            var oldHref = window.location.href;
            var oldLocation = oldHref.substr(0, oldHref.indexOf(oldQuery));

            var newQuery = "";
            if (oldPage == undefined || oldPage == "undefined" || oldPage == "") {
                newQuery = oldQuery + "&page=" + num;
            } else {
                var re = new RegExp("(\\?|&)page\=" + oldPage);
                newQuery = oldQuery.replace(re, "$1page="+num);
            }

            window.history.pushState("searchPage" + num, document.title, oldLocation + newQuery);

            displayPageResults(num);
            /*$("#content").html("Page " + num); // or some ajax content loading...
             // ... after content load -> change total to 10
             $(this).bootpag({total: 10, maxVisible: 10});*/
        });
    }


    $("#search").trigger('click');
}

/**
 * Display search results for a specific page.
 *
 * @param pageIdx The page index.
 */
function displayPageResults(pageIdx) {
    var s = pageIdx * MAX_ITEMS_PER_PAGE;
    var e = s + MAX_ITEMS_PER_PAGE;

    var searchResultHTML =
        computeHTMLResult('wh-responsive', pageIdx, totalPageNumber, MAX_ITEMS_PER_PAGE);

    document.getElementById('searchResults').innerHTML = searchResultHTML;
    window.scrollTo(0, 0);
}