/**
 * inventory.js
 * 
 * Creates an inventory of result values.
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
var tierNames = new ArrayList();

var keySet = queryAnalysisResult.resultSetKeys;
var sessionPathItr = keySet.iterator();
while(sessionPathItr.hasNext()) {
	var sessionPath = sessionPathItr.next();
	var resultSet = queryAnalysisResult.getResultSet(sessionPath);
	
	for(j = 0; j < resultSet.numberOfResults(true); j++) {
		var result = resultSet.getResult(j);
		
		for(k = 0; k < result.numberOfResultValues; k++) {
			var rv = result.getResultValue(k);
			
			try {
				var ipa = IPATranscript.parseIPATranscript(rv.data);
				
				var tierData = ipaTree.get(ipa);
				if(tierData == null) {
					tierData = new LinkedHashMap();
					ipaTree.put(ipa, tierData);
				}
				
				var count = 
					(tierData.get(rv.tierName) != null ? tierData.get(rv.tierName).intValue() + 1 : 1);
				tierData.put(rv.tierName, count);
				if(!tierNames.contains(rv.tierName)) {
					tierNames.add(rv.tierName);
				}
				
			} catch (e) {
				out.println(e.message);
			}
		}
	}
}

// print table header
out.print("\"Value\"");
for(i = 0; i < tierNames.size(); i++) {
	out.print(",\"" + tierNames.get(i) + "\"");
}
out.println();
out.flush();

// print data from tree, which is sorted by the comparator
var ipaItr = ipaTree.keySet().iterator();
while(ipaItr.hasNext()) {
	var ipa = ipaItr.next();
	var tierData = ipaTree.get(ipa);
	
	out.print("\"" + ipa + "\"");
	for(i = 0; i < tierNames.size(); i++) {
		var tierName = tierNames.get(i);
		var numInTier = 
			(tierData.get(tierName) == null ? 0 : tierData.get(tierName));
		out.print(",\"" + numInTier + "\"");
	}
	out.println();
	out.flush();
}
