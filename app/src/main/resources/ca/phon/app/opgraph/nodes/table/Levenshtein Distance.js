/*
params = { label, "", "" }
;
*/

function getRowValue(table, row) {
	ipaTarget = table.getValueAt(row, "IPA Target");
	ipaActual = table.getValueAt(row, "IPA Actual");
	
	return ipaTarget.levenshteinDistance.distance(ipaActual);
}
