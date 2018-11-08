var showIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfhBgUTAScLI/4WAAAAS0lEQVQoz2NgoBQwGvP/FcQtzfye5d8GJgfcCv4dYFRh5+HEreDLd4rdyMBonPVfD4/0JSbK7SDoBkLeZOHdweiAWwHvAYJBTbkjAbcvEQj70zelAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDE3LTA2LTA1VDE5OjAxOjM5KzAyOjAwvGO9kQAAACV0RVh0ZGF0ZTptb2RpZnkAMjAxNy0wNi0wNVQxOTowMTozOSswMjowMM0+BS0AAAAZdEVYdFNvZnR3YXJlAHd3dy5pbmtzY2FwZS5vcmeb7jwaAAAAAElFTkSuQmCC";
var copyIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfhBgEVNQ8OU07+AAAAZUlEQVQoz52QyQ3AIAwEB+SK3EI6oCFcYQpJE+QZbC4p+0IwYsdOAGpUutzpOwuoUfpLbT2c1SjtYhmhQnp2gGsENV8inh99cnyOPv6HiU+o8D4AmUOOgMTNbTOiYcxfDn7NE5sXVLwX2PBwtxgAAAAldEVYdGRhdGU6Y3JlYXRlADIwMTctMDYtMDFUMjE6NTM6MTUrMDI6MDC+FxbWAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE3LTA2LTAxVDIxOjUzOjE1KzAyOjAwz0quagAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAAASUVORK5CYII=";

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
    var button = documentRef.createElement("div");
    button.setAttribute("class", "tableButton");
    button.setAttribute("id", buttonId);
    button.setAttribute("onclick", "onCopyTableData(this, '" + tableId + "')");

    var copyImg = documentRef.createElement("img");
    copyImg.setAttribute("src", copyIcn);
    copyImg.setAttribute("style", "padding-right: 2px;");
    button.appendChild(copyImg);
    button.append("Copy to clipboard");

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
    
    var button = documentRef.createElement("div");
    button.setAttribute("class", "tableButton");
    button.setAttribute("onclick", "showTableMenu('" + tablePopupId + "')");
    
    var menuImg = documentRef.createElement("img");
    menuImg.setAttribute("src", showIcn);
    menuImg.setAttribute("style", "padding-right: 2px;");
    menuImg.setAttribute("class", "tableMenuButton");
    button.appendChild(menuImg);
    
    var tableMenu = documentRef.createElement("div");
    tableMenu.setAttribute("class", "tablePopupMenu");
    tableMenu.setAttribute("id", tablePopupId);
    
    // add actions
    var saveAsCSVItem = document.createElement("div");
    saveAsCSVItem.setAttribute("onclick", "saveTableAsCSV('" + table.getAttribute("id") + "')");
    saveAsCSVItem.setAttribute("class", "tableMenuItem");
    saveAsCSVItem.append("Save table as CSV...");
    tableMenu.appendChild(saveAsCSVItem);
    
    var saveAsExcelItem = document.createElement("div");
    saveAsExcelItem.setAttribute("onclick", "saveTableAsExcel('" + table.getAttribute("id") + "')");
    saveAsExcelItem.setAttribute("class", "tableMenuItem");
    saveAsExcelItem.append("Save table as Excel\u2122 Workbook...");
    tableMenu.appendChild(saveAsExcelItem);
    
    var showBufferItem = document.createElement("div");
    showBufferItem.setAttribute("onclick", "showTable('" + table.getAttribute("id") + "')");
    showBufferItem.setAttribute("class", "tableMenuItem");
    showBufferItem.append("Show table");
    tableMenu.appendChild(showBufferItem);
    
    button.appendChild(tableMenu);
    
    div.insertBefore(button, div.firstChild);
}

function page_init(documentRef) {
    $("#menu").menu();
    
    // add copy table buttons
    for(i = 0; i < tableIds.length; i++) {
        addCopyTableButton(document.getElementById(tableIds[i]), i);
    }
}

window.onclick = function(event) {
  if (!event.target.matches('.tableMenuButton')) {

    var dropdowns = document.getElementsByClassName("tablePopupMenu");
    var i;
    for (i = 0; i < dropdowns.length; i++) {
      var openDropdown = dropdowns[i];
      if (openDropdown.classList.contains('show')) {
        openDropdown.classList.remove('show');
      }
    }
  }
}
