
function toggleToC() {
    var toc = $("#toc");
    if(toc.is(":visible")) {
        toc.slideDown();
    } else {
        toc.slideUp();
    }
    
}

function tableToCSV(table) {
    var retVal = "";

    var tableRows =[].slice.call(table.querySelectorAll("tr"));
    tableRows.forEach(function (tableRow, index) {
        var rowData = "";

        var tableData =[].slice.call(tableRow.querySelectorAll("th, td"));
        for (var i = 0; i < tableData.length; i++) {
            rowData += (rowData.length > 0 ? ",": "") + "\"" + tableData[i].textContent.trim() + "\"";
        }

        retVal += rowData + "\n";
    });

    return retVal;
}

function onCopyTableData(button, tableId) {
    var table = document.getElementById(tableId);
    if (table != null) {
        var tableCSV = tableToCSV(table);

        if (window.buffer == undefined) {
            var textArea = document.createElement("textarea");
            textArea.style.position = 'fixed';
            textArea.style.top = 0;
            textArea.style.left = 0;

            // Ensure it has a small width and height. Setting to 1px / 1em
            // doesn't work as this gives a negative w/h on some browsers.
            textArea.style.width = '2em';
            textArea.style.height = '2em';

            // We don't need padding, reducing the size if it does flash render.
            textArea.style.padding = 0;

            // Clean up any borders.
            textArea.style.border = 'none';
            textArea.style.outline = 'none';
            textArea.style.boxShadow = 'none';

            // Avoid flash of white box if rendered for any reason.
            textArea.style.background = 'transparent';


            textArea.value = tableCSV;

            document.body.appendChild(textArea);

            textArea.select();

            var successful = document.execCommand('copy');

            document.body.removeChild(textArea);
        } else {
            window.buffer.copyTextToClipboard(tableCSV);
        }
    }
}

function addCopyTableButton(table, index) {
    var documentRef = document;
    
    var tableId = table.getAttribute("id");
    var buttonId = "copyTable" + (index + 1);

    var div = document.createElement("div");
    div.setAttribute("class", "tableButtonRow");
    var buttonRowId = "tableButtonRow" + (index + 1);
    div.setAttribute("id", buttonRowId);

    var tableCaption = table.querySelector("caption");
    var button = documentRef.createElement("button");
    button.setAttribute("class", "tableButton ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only");
    button.setAttribute("id", buttonId);
    button.setAttribute("title", "Copy table to clipboard as CSV");
    button.setAttribute("onclick", "onCopyTableData(this, '" + tableId + "')");

    var copyIcon = documentRef.createElement("span");
    copyIcon.setAttribute("class", "ui-icon ui-icon-clipboard");
    button.appendChild(copyIcon);

    div.appendChild(button);
    tableCaption.appendChild(div);
}

/*
 * Functions called by the application when using the embedded viewer
 */
function showTable(tableId) {
	app.showTable(tableId, tableMap.get(tableId));
}

function showTableMenu(tablePopupId) {
	document.getElementById(tablePopupId).classList.toggle("show");
}

function saveTableAsCSV(tableId) {
	app.saveTableAsCSV(tableId, tableMap.get(tableId));
}

function saveTableAsExcel(tableId) {
	app.saveTableAsWorkbook(tableId, tableMap.get(tableId));
}

function highlightResultValue(tableId, row, column) {
	app.onHighlightResultValue(tableMap.get(tableId), row, column);
}

function addMenuButtons(table, index) {
    var documentRef = document;

    var buttonRowId = "tableButtonRow" + (index + 1);
    
    var tableCaption = table.querySelector("caption");
    var div = tableCaption.lastChild;
    
    var tablePopupId = table.getAttribute("id") + "_popup";
    
    var button = documentRef.createElement("button");
    button.setAttribute("class", "tableButton ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only");
    button.setAttribute("onclick", "showTableMenu('" + tablePopupId + "')");
    button.setAttribute("title", "Show menu");
    
    var menuImg = documentRef.createElement("span");
    menuImg.setAttribute("class", "ui-icon ui-icon-triangle-1-s");
    button.appendChild(menuImg);
    
    var tableMenu = documentRef.createElement("div");
	tableMenu.setAttribute("class", "tablePopupMenu");
	tableMenu.setAttribute("id", tablePopupId);

    var menuList = documentRef.createElement("ul");
    menuList.setAttribute("id", "table_menu_" + (index+1));
    
	// add actions
	var saveAsCSVItem = createMenuLink("Save table as CSV...", "saveTableAsCSV('" + table.getAttribute("id") + "')");
	menuList.appendChild(saveAsCSVItem);
	
	var saveAsExcelItem = createMenuLink("Save table as Excel\u2122 Workbook...", "saveTableAsExcel('" + table.getAttribute("id") + "')");
	menuList.appendChild(saveAsExcelItem);
	
	var showBufferItem = createMenuLink("Show table", "showTable('" + table.getAttribute("id") + "')");
	menuList.appendChild(showBufferItem);
	
	tableMenu.appendChild(menuList);
    
    div.insertBefore(button, div.firstChild);
    tableCaption.appendChild(tableMenu);
}

function createMenuLink(title, onclick) {
    var menuItem = document.createElement("li");
    var menuItemWrapper = document.createElement("div");
    menuItemWrapper.setAttribute("onclick", onclick);
	var link = document.createElement("a");
	link.append(title);
	menuItemWrapper.appendChild(link);
	menuItem.appendChild(menuItemWrapper);

    return menuItem;
}

function page_init(documentRef) {
    $("#menu").menu();
    
    // add copy table buttons
    for(i = 0; i < tableIds.length; i++) {
        addCopyTableButton(document.getElementById(tableIds[i]), i);
        $("#table_menu_" + (i+1)).menu();
    }
    
    $(".tableButton").button();
    
    $(document).on("click", function(event){
        if(currentPopupMenu != null && !event.target.closest(".tableButton")) {
            currentPopupMenu.classList.toggle("show");
            currentPopupMenu = null;
        }
    });
}

