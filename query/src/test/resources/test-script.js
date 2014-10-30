/*
 * Report script for phon test: TestBasicAnalysis 
 */

// inputs
//  - queryAnalysisResult : ca.phon.query.analysis.QueryAnalysisResult

var keySet = queryAnalysisResult.resultSetKeys;

println("\"Session\",\"Number of Results\"");

for(sessionPath in keySet) {
    var resultSet = queryAnalysisResult.getResultSet(sessionPath);
    
    println("\"" + sessionPath + "\",\"" + resultSet.numberOfResults(true) + "\"");
}
