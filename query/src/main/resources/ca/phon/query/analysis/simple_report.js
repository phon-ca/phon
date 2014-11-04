/**
 * Print all query results in a single table, with result values and metadata
 * in separate columns.
 *
 */

importPackage(Packages.ca.phon.app.log)
importPackage(Packages.ca.phon.ipa)
importPackage(Packages.ca.phon.query)
importPackage(Packages.ca.phon.query.analysis)
 
var keySet = queryAnalysisResult.resultSetKeys;
 
// use the first result as our model
var model = new Array();
 
var firstResult = queryAnalysisResult.getResultSet(keySet.iterator().next());

var resultColumns = firstResult.metadataKeys;

out.print("\"Session\",\"Speaker\",\"Record\",\"Group\",\"IPA Target\",\"IPA Actual\"");
for(i = 0; i < resultColumns.length; i++) {
	var col = resultColumns[i];
	out.print(",\"" + col + "\"");
}
out.println();

var sessionPathItr = keySet.iterator();

var project = queryAnalysisResult.input.project;

while(sessionPathItr.hasNext()) {
	var sessionPath = sessionPathItr.next();
	var session = project.openSession(sessionPath.corpus, sessionPath.session);
	
	var resultSet = queryAnalysisResult.getResultSet(sessionPath);
	for(j = 0; j < resultSet.numberOfResults(true); j++) {
		var result = resultSet.getResult(j);
		
		out.print("\"" + sessionPath + "\"");
		out.print(",\"" + session.getRecord(result.recordIndex).speaker + "\"");
		out.print(",\"" + (result.recordIndex+1) + "\"");
		out.print(",\"" + (result.getResultValue(0).groupIndex+1) + "\"");
		out.print(",\"" + result.getResultValue(0).data + "\"");
		out.print(",\"" + result.getResultValue(1).data + "\"");
		
		for(k = 0; k < resultColumns.length; k++) {
			var col = resultColumns[k];
			out.print(",\"" + result.metadata.get(col) + "\"");
		}
		out.println();
	}
	
}
