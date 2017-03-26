
var openIcn = "data:image/false;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAQAAABKfvVzAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfhAxkPIzt/g9k6AAAAL0lEQVQ4y2NgoDn4v+M/KWAH4///pFnARKqLmBh2kqSeNNWDFYzGw+AAo/FABAAAIBCYJraCa4wAAAAldEVYdGRhdGU6Y3JlYXRlADIwMTctMDMtMjVUMTU6MzU6NTkrMDE6MDCIvHkFAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE3LTAzLTI1VDE1OjM1OjU5KzAxOjAw+eHBuQAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAAASUVORK5CYII=";
var closeIcn = "data:image/false;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAQAAABKfvVzAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfhAxkPJQnhDi88AAAAfUlEQVQ4y6WUwQ3AIAhFSddyBTdwYE5uZPJ6M41a6E+5Ce9HUMAoOJ1qiVHpOMVwAAYtxBsDADc6ZJKJQzfqPLxIHvigro4W4i1yxpFzILx7D8apbkCKb5IcXyRf8E1ywC/7Z2JKYtHis4ofJ7aG2Hxie8sDpI+ovATENXMDbTD5YB852zQAAAAldEVYdGRhdGU6Y3JlYXRlADIwMTctMDMtMjVUMTU6Mzc6MDkrMDE6MDDEqadcAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE3LTAzLTI1VDE1OjM3OjA5KzAxOjAwtfQf4AAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAAASUVORK5CYII=";

function openNav() {
	document.getElementById("sidenav").style.width = "300px";
	document.getElementById("main").style.marginLeft = "300px";
	document.getElementById("toc").style.display = "block";
	document.getElementById("menuicon").setAttribute('src', closeIcn);
}

function closeNav() {
	document.getElementById("sidenav").style.width = "26px";
	document.getElementById("main").style.marginLeft = "26px";
	document.getElementById("toc").style.display = "none";
	document.getElementById("menuicon").setAttribute('src', openIcn);
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

	var menuicon = document.getElementById('menuicon');
	menuicon.setAttribute('src', closeIcn);
}
