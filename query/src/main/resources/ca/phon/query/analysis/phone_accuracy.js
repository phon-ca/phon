/**
 * inventory.js
 * 
 * Creates an inventory of result accuracy.
 * 
 */
 
importClass(Packages.java.util.ArrayList)
importClass(Packages.java.util.LinkedHashMap)
importPackage(Packages.ca.phon.ipa)
importPackage(Packages.ca.phon.ipa.tree)
importPackage(Packages.ca.phon.ipa.features)
importPackage(Packages.ca.phon.query)
importPackage(Packages.ca.phon.query.db)
importPackage(Packages.ca.phon.query.analysis)

var project = queryAnalysisResult.input.project;

var ipaTree = new IpaTernaryTree(FeatureComparator.createPlaceComparator());

var keySet = queryAnalysisResult.resultSetKeys;
var sessionPathItr = keySet.iterator();
while(sessionPathItr.hasNext()) {
	var sessionPath = sessionPathItr.next();
	var resultSet = queryAnalysisResult.getResultSet(sessionPath);
	
	for(j = 0; j < resultSet.numberOfResults(true); j++) {
		var result = resultSet.getResult(j);
		
		// make sure we have 2 result values
		if(result.numberOfResultValues != 2) continue;
		
		var targetRv = result.getResultValue(0);
		var actualRv = result.getResultValue(1);
		try {
			var ipa = IPATranscript.parseIPATranscript(targetRv.data);
			
			var tierData = ipaTree.get(ipa);
			if(tierData == null) {
				tierData = new Array();
				tierData["count"] = 0;
				tierData["accurate"] = 0;
				tierData["sub"] = 0;
				tierData["deleted"] = 0;
				ipaTree.put(ipa, tierData);
			}
			
			var count = 
				(tierData["count"] != null ? tierData["count"] + 1 : 1);
			tierData["count"] = count;
			
			if(actualRv.data != null && targetRv.data.equals(actualRv.data)) {
				var accurate =
					(tierData["accurate"] != null ? tierData["accurate"] + 1 : 1);
				tierData["accurate"] = accurate;
			} else {
				if(actualRv.data == null || actualRv.data.length() == 0) {
					var deleted = 
						(tierData["deleted"] != null ? tierData["deleted"] + 1 : 1);
					tierData["deleted"] = deleted;
				} else {
					var sub =
						(tierData["sub"] != null ? tierData["sub"] + 1: 1);
					tierData["sub"] = sub;
				}
			}
		} catch (e) {
			out.println(e.message);
		}
		
	}
}

// print table header
out.print("\"Value\",\"# Target\",\"# Accurate\",\"# Substituted\",\"# Deleted\"");
out.println();
out.flush();

// print data from tree, which is sorted by the comparator
var ipaItr = ipaTree.keySet().iterator();
while(ipaItr.hasNext()) {
	var ipa = ipaItr.next();
	var tierData = ipaTree.get(ipa);
	
	out.print("\"" + ipa + "\"");
	
	out.print(",\"" + tierData["count"] + "\"");
	out.print(",\"" + tierData["accurate"] + "\"");
	out.print(",\"" + tierData["sub"] + "\"");
	out.print(",\"" + tierData["deleted"] + "\"");
	out.println();
	out.flush();
}
