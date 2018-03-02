// Search implementation used by WebHelp Classic and Mobile distributuions

var txt_browser_not_supported = "Your browser is not supported. Use of Mozilla Firefox is recommended.";

if (!("console" in window) || !("firebug" in console)) {
    var names = ["log", "debug", "info", "warn", "error", "assert", "dir", "dirxml", "group", "groupEnd", "time", "timeEnd", "count", "trace", "profile", "profileEnd"];
    window.console = {};
    for (var i = 0, len = names.length; i < len; ++i) {
        window.console[names[i]] = function () {
        };
    }
}

if(typeof String.prototype.trim !== 'function') {
    String.prototype.trim = function() {
        return $.trim(this);
    }
}

function logLocal(msg){
    console.log(msg);
}

if (typeof debug !== 'function') {
    function debug(msg, obj) {
        if ( withFrames ){
            if (typeof parent.debug !== 'function') {
                logLocal(msg);
            }else{
                if (typeof msg!=="undefined"){
                    if (typeof msg==="object"){
                        parent.debug('['+src+']',msg);
                    }else{
                        parent.debug('['+src+']'+msg,obj);
                    }
                }
            }
        }else{
            logLocal(msg);
        }
    }
}

/**
 * @description This function make a search request. If all necessary resources are loaded search occurs
 *              otherwise search will be delayed until all resources are loaded.
 * @param whDistribution The WebHelp distribution.
 */
function searchRequest(whDistribution) {
    $('#search').trigger('click');
    var ditaSearch_Form = document.getElementById('searchForm');
    var ready = setInterval(function () {
        if (searchLoaded) {
            $('#loadingError').remove();
            SearchToc(ditaSearch_Form, whDistribution);
            clearInterval(ready);
        } else {
            if ($('#loadingError').length < 1) {
                $('#searchResults').prepend('<span id="loadingError">' + getLocalization('Loading, please wait ...') + '</span>');
            }
        }
    }, 100);
}

/**
 * @description This function find all matches using the search term
 * @param {HTMLObjectElement} ditaSearch_Form The search form from WebHelp page as HTML Object
 * @param whDistribution The WebHelp distribution.
 */
function SearchToc(ditaSearch_Form, whDistribution) {
    debug('SearchToc(..)');

    // Check browser compatibility
    if (navigator.userAgent.indexOf("Konquerer") > -1) {
        alert(getLocalization(txt_browser_not_supported));
        return;
    }

    var query = ditaSearch_Form.textToSearch.value;

    searchAndDisplayResults(query, whDistribution);

    clearHighlights();
    ditaSearch_Form.textToSearch.focus();
}

/**
 * Seach and display results.
 *
 * @param query The search query.
 * @param whDistribution The WebHelp distribution.
 */
function searchAndDisplayResults(query, whDistribution) {
    //START - EXM-30790
    var $searchResults = $("#searchResults");
    var footer = $searchResults.find(".footer");
    //END - EXM-30790

    if (query.trim().length > 0 || excluded.length > 0) {
        var results = performSearch(query);
        displayResults(results, whDistribution);

        //START - EXM-30790
        $searchResults.append(footer);
        $searchResults.scrollTop(0);
        //END - EXM-30790
    }
}

/**
 * @description Display results in HTML format
 *
 * @param {SearchResult} searchResult The search result.
 * @param whDistribution The WebHelp distribution.
 */
function displayResults(searchResult, whDistribution) {

    var warningMsg = '<div style="padding: 5px;margin-right:5px;;background-color:#FFFF00;">';
    warningMsg += '<b>Please note that due to security settings, Google Chrome does not highlight';
    warningMsg += ' the search results.</b><br>';
    warningMsg += 'This happens only when the WebHelp files are loaded from the local file system.<br>';
    warningMsg += 'Workarounds:';
    warningMsg += '<ul>';
    warningMsg += '<li>Try using another web browser.</li>';
    warningMsg += '<li>Deploy the WebHelp files on a web server.</li>';
    warningMsg += '</div>';

    preprocessSearchResult(searchResult, whDistribution);
    var results = computeHTMLResult(whDistribution);

    // Verify if the browser is Google Chrome and the WebHelp is used on a local machine
    // If browser is Google Chrome and WebHelp is used on a local machine a warning message will appear
    // Highlighting will not work in this conditions. There is 2 workarounds
    if (notLocalChrome) {
        document.getElementById('searchResults').innerHTML = results;
    } else {
        document.getElementById('searchResults').innerHTML = warningMsg + results;
    }

    $("#search").trigger('click');
}