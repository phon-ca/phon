
var openIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA3XAAAN1wFCKJt4AAAAB3RJTUUH4QQMDDsrN46U4wAAADtJREFUSMdjYKAxYBSJ6vlPSwuYaO2DUQtGARXyAQMDAwOt8sKbZSWMoxlt4C2geSSP5qPRCmfUAjoAAObLDgf9SOjwAAAAAElFTkSuQmCC";
var closeIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA3XAAAN1wFCKJt4AAAAB3RJTUUH4QQMDDcayuXb1QAAAIpJREFUSMfFVMkNgDAMqzMD0zEHYzBHp2MH+CGEaHNa5O0jTdxgWfezPeroG1qi3nqiATLinwZRkxFHvAQvFhpA24nGhRUYEb9fEDGxYhHpytMIqlI0GiUqUjTbUzgh1qQhk3FLjKWR678RUZdMjSn1o1FPBfXYUc91RtxiIllxDStZcY0jFeIz7gUXTIASLqhmngAAAABJRU5ErkJggg==";
var copyIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfhBgEVNQ8OU07+AAAAZUlEQVQoz52QyQ3AIAwEB+SK3EI6oCFcYQpJE+QZbC4p+0IwYsdOAGpUutzpOwuoUfpLbT2c1SjtYhmhQnp2gGsENV8inh99cnyOPv6HiU+o8D4AmUOOgMTNbTOiYcxfDn7NE5sXVLwX2PBwtxgAAAAldEVYdGRhdGU6Y3JlYXRlADIwMTctMDYtMDFUMjE6NTM6MTUrMDI6MDC+FxbWAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE3LTA2LTAxVDIxOjUzOjE1KzAyOjAwz0quagAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAAASUVORK5CYII=";

var origDisplay = "flex";

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

function htmlTableOfContents (documentRef) {
	var documentRef = documentRef || document;
	var toc = documentRef.getElementById('toc');
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
		toc.appendChild(div);
		heading.parentNode.insertBefore(anchor, heading);
	});
}

function tableToCSV(table) {
	var retVal = "";

	var tableRows =[].slice.call(table.querySelectorAll("tr"));
	tableRows.forEach(function (tableRow, index) {
		var rowData = "";

		var tableData =[].slice.call(tableRow.querySelectorAll("th, td"));
		for (var i = 0; i < tableData.length; i++) {
			rowData += (rowData.length > 0 ? ",": "") + "\"" + tableData[i].textContent + "\"";
		}

		retVal += rowData + "\n";
	});

	return retVal;
}

function onCopyTableData(button, tableId) {
	var table = document.getElementById(tableId);
	if (table != null) {
		var tableCSV = tableToCSV(table);

		if(window.app == undefined) {
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
			window.app.copyTextToClipboard(tableCSV);
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
		var buttonRowId = "tableButtonRow" + (index+1);
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
		button.append("Copy Table as CSV");

		div.appendChild(button);
		tableCaption.appendChild(div);
	});
}

function page_init(documentRef) {
	var documentRef = documentRef || document;
	htmlTableOfContents(documentRef);
	addCopyTableButtons(documentRef);

	if(window.app) {
		addShowBufferButtons(documentRef);
	}

	origDisplay = document.getElementById("bannercontent").style.display;

	var menuicon = document.getElementById('menuicon');
	menuicon.setAttribute('src', closeIcn);
}

/*
 * Functions called by the application when using the embedded viewer
 */
function addShowBufferButtons(documentRef) {
	var documentRef = documentRef || document;

	var tables =[].slice.call(documentRef.body.querySelectorAll('table'));
	tables.forEach(function (table, index) {
		var buttonRowId = "tableButtonRow" + (index+1);

		var tableCaption = table.querySelector("caption");
		var div = tableCaption.lastChild;

		var button = documentRef.createElement("div");
		button.setAttribute("class", "tableButton");
		button.setAttribute("onclick", "buffers.selectBuffer('" + table.getAttribute("id") + "')");
//		button.setAttribute("id", buttonId);

		var copyImg = documentRef.createElement("img");
		copyImg.setAttribute("src", copyIcn);
		copyImg.setAttribute("style", "padding-right: 2px;");
		button.appendChild(copyImg);
		button.append("Show Buffer");

		div.appendChild(button);
	});
}