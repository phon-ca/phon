

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

function addCopyTableButtons(documentRef) {
    var doucmentRef = documentRef || document;
    var tables =[].slice.call(documentRef.body.querySelectorAll('table'));
    tables.forEach(function (table, index) {
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
    });
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

function addMenuButtons(documentRef) {
	var documentRef = documentRef || document;

	var tables = [].slice.call(documentRef.body.querySelectorAll('table'));
	tables.forEach(function (table, index) {
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
	});
}

function addShowBufferButtons(documentRef) {
    var documentRef = documentRef || document;

    var tables =[].slice.call(documentRef.body.querySelectorAll('table'));
    tables.forEach(function (table, index) {
        var buttonRowId = "tableButtonRow" + (index + 1);

        var tableCaption = table.querySelector("caption");
        var div = tableCaption.lastChild;

        var button = documentRef.createElement("div");
        button.setAttribute("class", "tableButton");
        button.setAttribute("onclick", "showBuffer('" + table.getAttribute("id") + "')");

        var copyImg = documentRef.createElement("img");
        copyImg.setAttribute("src", showIcn);
        copyImg.setAttribute("style", "padding-right: 2px;");
        button.appendChild(copyImg);
        button.append("Show data");

        div.appendChild(button);
    });
}

function page_init(documentRef) {
    $("#menu").menu();   
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
