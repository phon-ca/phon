String.prototype.replaceAll = function (search, replacement) {
	var target = this;
	return target.split(search).join(replacement);
};

function getOS() {
	var OSName = "";
	if (navigator.appVersion.indexOf("Win64" != -1)) {
		OSName = "windows-x64";
	} else if (navigator.appVersion.indexOf("Win") != -1) {
		OSName = "windows";
	} else if (navigator.appVersion.indexOf("Mac") != -1) {
		OSName = "macos";
	}
	return OSName;
}

function getExtension(os) {
	if (os == "windows-x64" || os == "windows")
	return ".exe"; else if (os == "macos")
	return ".dmg"; else
	return "";
}

function updateLink(linkElement) {
	var href = linkElement.getAttribute("href");
	
	var os = getOS();
	if (os.trim().length == 0) return;
	
	var phonVersion = href.substring(href.lastIndexOf("/") + 1, href.length);
	var fileVersion = phonVersion.replaceAll(".", "_");
	
	href = "https://github.com/phon-ca/phon/releases/download/" + phonVersion + "/Phon_" + os + "_" + fileVersion + getExtension(os);
	linkElement.setAttribute("href", href);
}

$(document).ready(function () {
	// replace download links with links to download for client os (if available)
	var stableLinkElement = document.getElementById("wh_download_banner");
	updateLink(stableLinkElement);
	
	var betaLinkElement = document.getElementById("wh_download_beta_banner");
	updateLink(betaLinkElement);
});
