
var openIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA3XAAAN1wFCKJt4AAAAB3RJTUUH4QQMDDsrN46U4wAAADtJREFUSMdjYKAxYBSJ6vlPSwuYaO2DUQtGARXyAQMDAwOt8sKbZSWMoxlt4C2geSSP5qPRCmfUAjoAAObLDgf9SOjwAAAAAElFTkSuQmCC";
var closeIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA3XAAAN1wFCKJt4AAAAB3RJTUUH4QQMDDcayuXb1QAAAIpJREFUSMfFVMkNgDAMqzMD0zEHYzBHp2MH+CGEaHNa5O0jTdxgWfezPeroG1qi3nqiATLinwZRkxFHvAQvFhpA24nGhRUYEb9fEDGxYhHpytMIqlI0GiUqUjTbUzgh1qQhk3FLjKWR678RUZdMjSn1o1FPBfXYUc91RtxiIllxDStZcY0jFeIz7gUXTIASLqhmngAAAABJRU5ErkJggg==";
var copyIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfhBgEVNQ8OU07+AAAAZUlEQVQoz52QyQ3AIAwEB+SK3EI6oCFcYQpJE+QZbC4p+0IwYsdOAGpUutzpOwuoUfpLbT2c1SjtYhmhQnp2gGsENV8inh99cnyOPv6HiU+o8D4AmUOOgMTNbTOiYcxfDn7NE5sXVLwX2PBwtxgAAAAldEVYdGRhdGU6Y3JlYXRlADIwMTctMDYtMDFUMjE6NTM6MTUrMDI6MDC+FxbWAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE3LTA2LTAxVDIxOjUzOjE1KzAyOjAwz0quagAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAAASUVORK5CYII=";
var showIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfhBgUTAScLI/4WAAAAS0lEQVQoz2NgoBQwGvP/FcQtzfye5d8GJgfcCv4dYFRh5+HEreDLd4rdyMBonPVfD4/0JSbK7SDoBkLeZOHdweiAWwHvAYJBTbkjAbcvEQj70zelAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDE3LTA2LTA1VDE5OjAxOjM5KzAyOjAwvGO9kQAAACV0RVh0ZGF0ZTptb2RpZnkAMjAxNy0wNi0wNVQxOTowMTozOSswMjowMM0+BS0AAAAZdEVYdFNvZnR3YXJlAHd3dy5pbmtzY2FwZS5vcmeb7jwaAAAAAElFTkSuQmCC";

var tocExpandedIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAUVBMVEUAAAAAYI8AW40AWowAWo0AXYsAWowAWYwAVaoAWowAW4wAWowAWooAWowAWosAWI0AWowAWY0AW5IAWosAW40AQIAAWowAWowAVZUAWowAAABgECiPAAAAGXRSTlMAEDjHfwvmZAPUSbww/aEd9oYO6msE19gMqZ/KiwAAAAFiS0dEAIgFHUgAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfhBxgQFC7YH7gNAAAAYElEQVQY01XIRwKAIBAEwUZQzJh1//9RAyowp+lCZckUWpJpTAqGvIi7yMHGYIGyCl2VF1AHqHnWfN34pu18d+0L9B76r3HD3YP7gfGGMTTTLDJPEbCILHGzbvtKKsd7TupkDGm+G0EiAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDE3LTA3LTI0VDE2OjIwOjQ2KzAyOjAwfYZCpQAAACV0RVh0ZGF0ZTptb2RpZnkAMjAxNy0wNy0yNFQxNjoyMDo0NiswMjowMAzb+hkAAAAZdEVYdFNvZnR3YXJlAHd3dy5pbmtzY2FwZS5vcmeb7jwaAAAAAElFTkSuQmCC";
var tocClosedIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAASFBMVEUAAAAAAP8AWowAWowAV4oAYIAAWowAWYwAYokAWowAW40AgIAAWo0AWYwAWowAXogAWowAW4wAZoAAWowAW4sAWowAWowAAADct4obAAAAFnRSTlMAAcyvIwj5iQ3oYgLNPKge9oEK41qztuL3PwAAAAFiS0dEAIgFHUgAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfhBxkMORydYo4TAAAAR0lEQVQY04XPNxKAQAwEwcNzeHPM/59KiiZBYVdJ2k1V3bTpOx30wxgB8jRHgGUVwLYLOM4rAtxFAM8PaMVH9VbBFD2Wc/0XAhULJxWg6xUAAAAldEVYdGRhdGU6Y3JlYXRlADIwMTctMDctMjVUMTI6NTc6MjgrMDI6MDBO4MWCAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE3LTA3LTI1VDEyOjU3OjI4KzAyOjAwP719PgAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAAASUVORK5CYII=";

var origDisplay = "flex";

var tocHeights = [];

function openNav() {
    document.getElementById("sidenav").style.width = "300px";
    document.getElementById("main").style.marginLeft = "300px";
    document.getElementById("toc").style.display = "block";
    document.getElementById("menuicon").setAttribute('src', closeIcn);

    document.getElementById("bannercontent").style.display = origDisplay;
}

function closeNav() {
    document.getElementById("sidenav").style.width = "26px";
    document.getElementById("main").style.marginLeft = "26px";
    document.getElementById("toc").style.display = "none";
    document.getElementById("menuicon").setAttribute('src', openIcn);

    document.getElementById("bannercontent").style.display = "none";
}

function toggleNav() {
    var currentWidth = document.getElementById("sidenav").style.width;
    console.log(currentWidth)
    if (currentWidth.length == 0 || currentWidth == "300px") {
        closeNav();
    } else {
        openNav();
    }
}

function toggleTocSection(toggleBtn, sectionId) {
    var tocSection = document.getElementById(sectionId);
    var currentHeight = tocSection.clientHeight;
    var img = toggleBtn.getElementsByTagName("img")[0];

    if(currentHeight == 0) {
        tocSection.clientHeight = tocHeights[sectionId];
        tocSection.style.display = "block";
        img.setAttribute('src', tocExpandedIcn);
    } else {
        tocHeights[sectionId] = currentHeight;
        tocSection.clientHeight = 0;
        tocSection.style.display = "none";
        img.setAttribute('src', tocClosedIcn);
    }

    tocSection.parentNode.style.zIndex = 1;
}

function htmlTableOfContents (documentRef) {
    var documentRef = documentRef || document;

    var toc = documentRef.getElementById('toc');
    var containerStack =[];
    containerStack.push(toc);

    var tocSectionIdx = 1;
    var h1Section = documentRef.createElement('div');
    h1Section.setAttribute('class', 'toc_l1');
    h1Section.setAttribute('id', 'toc_section_' + (tocSectionIdx++));
    toc.appendChild(h1Section);
    containerStack.push(h1Section);

    var headerStack =[];

    var lastHeading = null;
    var lastLink = null;

    var headings =[].slice.call(documentRef.body.querySelectorAll('h1, h2, h3, h4, h5, h6'));
    headings.forEach(function (heading, index) {
        var anchor = documentRef.createElement('a');
        anchor.setAttribute('name', 'toc' + index);
        anchor.setAttribute('id', 'toc' + index);

        heading.setAttribute('id', '#toc' + index);

        var link = documentRef.createElement('a');
        link.setAttribute('href', '#toc' + index);
        link.setAttribute('onClick', 'document.getElementById(\'#toc' + index + '\').scrollIntoView(true)');
        link.textContent = heading.textContent;

        var div = documentRef.createElement('div');
        div.setAttribute('class', heading.tagName.toLowerCase());
        div.appendChild(link);

        if (lastHeading != null) {
            // if we have moved up in heading level,
            // add a new container to the stack
            var lastH = lastHeading.localName;
            var cmp = lastH.localeCompare(heading.localName);
            if (cmp < 0) {
                var tocDiv = document.createElement('div');
                tocDiv.setAttribute('class', 'toc_l' + containerStack.length);
                tocDiv.setAttribute('id', 'toc_section_' + (tocSectionIdx++));

                containerStack[containerStack.length -1].appendChild(tocDiv);
                containerStack.push(tocDiv);

                // add last heading to stack, this will
                // be the 'parent' of our toc section
                headerStack.push(lastLink);
            } else if (cmp > 0) {
                while(cmp > 0) {
                    var tocContainer = containerStack.pop();
                    var controlHeading = headerStack.pop();

                    var toggleBtn = document.createElement('div');
                    toggleBtn.setAttribute('class', 'toc_toggle_btn');
                    toggleBtn.setAttribute('onclick', "toggleTocSection(this, '" + tocContainer.getAttribute('id') + "')");

                    var toggleImg = document.createElement('img');
                    toggleImg.setAttribute('src', tocExpandedIcn);
                    toggleBtn.appendChild(toggleImg);

                    controlHeading.style.paddingLeft = "2px";
                    controlHeading.parentNode.insertBefore(toggleBtn, controlHeading);

                    lastH = controlHeading.parentNode.getAttribute('class');
                    cmp = lastH.localeCompare(heading.localName);
                }
            }
        }

        containerStack[containerStack.length -1].appendChild(div);

        heading.parentNode.insertBefore(anchor, heading);
        lastHeading = heading;

        lastLink = link;
    });

    while (headerStack.length > 0) {
        var tocContainer = containerStack.pop();
        var controlHeading = headerStack.pop();

        var toggleBtn = document.createElement('div');
        toggleBtn.setAttribute('class', 'toc_toggle_btn');

        var toggleImg = document.createElement('img');
        toggleImg.setAttribute('src', tocExpandedIcn);
        toggleBtn.setAttribute('onclick', "toggleTocSection(this, '" + tocContainer.getAttribute('id') + "')");
        toggleBtn.appendChild(toggleImg);

        controlHeading.style.paddingLeft = "4px";
        controlHeading.parentNode.insertBefore(toggleBtn, controlHeading);
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

        if (buffer == undefined) {
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
            buffer.copyTextToClipboard(tableCSV);
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

function page_init(documentRef) {
    var documentRef = documentRef || document;
    htmlTableOfContents(documentRef);
    addCopyTableButtons(documentRef);

    origDisplay = document.getElementById("bannercontent").style.display;

    var menuicon = document.getElementById('menuicon');
    menuicon.setAttribute('src', closeIcn);
}

/*
 * Functions called by the application when using the embedded viewer
 */
function showBuffer(bufferName) {
    buffers.selectBuffer(bufferName);
}

function showTableMenu(tablePopupId) {
	document.getElementById(tablePopupId).classList.toggle("show");
}

function saveTableAsCSV(tableId) {
	buffers.saveAsCSV(tableId);
}

function saveTableAsExcel(tableId) {
	buffers.saveAsWorkbook(tableId);
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
		showBufferItem.setAttribute("onclick", "showBuffer('" + table.getAttribute("id") + "')");
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

function getColumnIndex(table, columnName) {
    var retVal = -1;

    var headerCols =[].slice.call(table.querySelectorAll("th"));
    for (var i = 0; i < headerCols.length; i++) {
        var headerCol = headerCols[i];
        if (headerCol.textContent === columnName) {
            retVal = i;
            break;
        }
    }

    return retVal;
}

function highlightResult(sessionName, result) {
}

function makeSessionLink(documentRef, cell, onclick) {
    var documentRef = documentRef || document;

    var div = documentRef.createElement("div");
    div.setAttribute("class", "sessionLink");
    div.setAttribute("onclick", onclick);

    var cellText = cell.textContent;
    cell.textContent = "";
    div.textContent = cellText;

    cell.appendChild(div);
}

function addSessionLinks(documentRef) {
    var documentRef = documentRef || document;

    var tables =[].slice.call(documentRef.body.querySelectorAll("table"));
    tables.forEach(function (table, index) {
        var tableId = table.getAttribute("id");

        if (buffers) {
            var buffer = buffers.getBuffer(tableId);
            if (buffer != null) {
                var tableModel = buffer.getUserObject();
                if (tableModel != null) {
                    // check for session and Record # columns
                    var hasSession = (tableModel.getColumnIndex("Session") >= 0);
                    var hasRecord = (tableModel.getColumnIndex("Record #") >= 0);

                    // find column indicies in HTML table
                    var sessionColIdx = getColumnIndex(table, "Session");
                    var recordColIdx = getColumnIndex(table, "Record #");

                    if (hasSession && sessionColIdx >= 0) {
                        // add session links
                        var trs =[].slice.call(table.querySelectorAll("tr"));
                        for (var rowIdx = 1; rowIdx < trs.length; rowIdx++) {
                            var row = trs[rowIdx];
                            var cols =[].slice.call(row.querySelectorAll("td"));

                            if (sessionColIdx < cols.length) {
                                var col = cols[sessionColIdx];
                                var sessionName = col.textContent;
                                makeSessionLink(documentRef, col, "app.openSession('" + sessionName + "')");
                            }
                        }
                    }

                    if (hasSession && hasRecord && recordColIdx >= 0) {
                        // add session links
                        var trs =[].slice.call(table.querySelectorAll("tr"));
                        for (var rowIdx = 1; rowIdx < trs.length; rowIdx++) {
                            var row = trs[rowIdx];
                            var cols =[].slice.call(row.querySelectorAll("td"));

                            if (recordColIdx < cols.length) {
                                var col = cols[recordColIdx];
                                var rowData = tableModel.getRow(rowIdx -1);
                                var modelSessionIdx = tableModel.getColumnIndex("Session");
                                var sessionName = rowData[modelSessionIdx];
                                var recordNumber = col.textContent;
                                makeSessionLink(documentRef, col, "app.openSessionAtRecord('" + sessionName + "', (" + recordNumber + "-1))");
                            }
                        }
                    }

                    var resultCol = tableModel.getColumnIndex("Result");
                    if (resultCol >= 0) {
                        var trs =[].slice.call(table.querySelectorAll("tr"));
                        for (var rowIdx = 1; rowIdx < trs.length; rowIdx++) {
                            var rowData = tableModel.getRow(rowIdx -1);
                            var modelSessionIdx = tableModel.getColumnIndex("Session");
                            var sessionName = rowData[modelSessionIdx];
                            var result = rowData[resultCol];

                            for (var i = 0; i < result.getNumberOfResultValues();
                            i++) {
                                var rv = result.getResultValue(i);
                                var tableColIdx = getColumnIndex(table, rv.getName());

                                if (tableColIdx >= 0) {
                                    var row = trs[rowIdx];
                                    var cols =[].slice.call(row.querySelectorAll("td"));

                                    if (tableColIdx < cols.length) {
                                        var col = cols[tableColIdx];
                                        makeSessionLink(documentRef, col,
                                        "app.onHighlightResultValue('" + tableId + "',(" + rowIdx + "-1), '" + rv.getName() + "')");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    });
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