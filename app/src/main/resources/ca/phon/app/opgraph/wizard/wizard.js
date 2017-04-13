
var openIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA3XAAAN1wFCKJt4AAAAB3RJTUUH4QQMDDsrN46U4wAAADtJREFUSMdjYKAxYBSJ6vlPSwuYaO2DUQtGARXyAQMDAwOt8sKbZSWMoxlt4C2geSSP5qPRCmfUAjoAAObLDgf9SOjwAAAAAElFTkSuQmCC";
var closeIcn = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA3XAAAN1wFCKJt4AAAAB3RJTUUH4QQMDDcayuXb1QAAAIpJREFUSMfFVMkNgDAMqzMD0zEHYzBHp2MH+CGEaHNa5O0jTdxgWfezPeroG1qi3nqiATLinwZRkxFHvAQvFhpA24nGhRUYEb9fEDGxYhHpytMIqlI0GiUqUjTbUzgh1qQhk3FLjKWR678RUZdMjSn1o1FPBfXYUc91RtxiIllxDStZcY0jFeIz7gUXTIASLqhmngAAAABJRU5ErkJggg==";

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
	if(currentWidth.length == 0 || currentWidth == "300px") {
		closeNav();
	} else {
		openNav();
	}
}

function htmlTableOfContents (documentRef) {
    var documentRef = documentRef || document;
    var toc = documentRef.getElementById('toc');
    var headings = [].slice.call(documentRef.body.querySelectorAll('h1, h2, h3, h4, h5, h6'));
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

function page_init(documentRef) {
	var documentRef = documentRef || document;
	htmlTableOfContents(documentRef);

	origDisplay = document.getElementById("bannercontent").style.display;

	var menuicon = document.getElementById('menuicon');
	menuicon.setAttribute('src', closeIcn);
}
