/**
 * Helper method for detector results.
 * 
 * 
 */
 
exports.DetectorResultFactory = function() {

    /**
     * Convert the given ca.phon.query.detector.DetectorResult object
     * into a query Result
     * 
     * @param factory
     * @param detectorResults
     * 
     * @return the new query result or null
     */
    this.createQueryResult = function(recordIndex, groupIndex, detectorResult) {
        var SCHEMA = "DETECTOR";
        var FEATURES1 = "Features1";
        var FEATURES2 = "Features2";
    
        var retVal = factory.createResult();
        retVal.schema = SCHEMA;
        retVal.recordIndex = recordIndex;
        
        var phoneMap = detectorResult.getPhoneMap();
        var ipaT = phoneMap.targetRep;
        var ipaA = phoneMap.actualRep;

        // result values
        var rv1 = factory.createResultValue();
        rv1.tierName = ipaT.tierName;
        rv1.groupIndex = groupIndex;
        ipaE = phoneMap.topAlignmentElements.get(detectorResult.firstPosition);
        stringIdx = ipaT.stringIndexOfElement(ipaE);
        rv1.range = new Range(stringIdx, stringIdx, false);        
        rv1.data = ipaE.text;
        retVal.resultValues.add(rv1);
        
        var rv2 = factory.createResultValue();
        rv2.tierName = ipaT.tierName;
        rv2.groupIndex = groupIndex;
        ipaE = phoneMap.topAlignmentElements.get(detectorResult.secondPosition);
        stringIdx = ipaT.stringIndexOfElement(ipaE);
        rv2.range = new Range(stringIdx, stringIdx, false);        
        rv2.data = ipaE.text;
        retVal.resultValues.add(rv2);
        
        var rv3 = factory.createResultValue();
        rv3.tierName = ipaA.tierName;
        rv3.groupIndex = groupIndex;
        ipaE = phoneMap.bottomAlignmentElements.get(detectorResult.firstPosition);
        stringIdx = ipaA.stringIndexOfElement(ipaE);
        rv3.range = new Range(stringIdx, stringIdx, false);        
        rv3.data = ipaE.text;
        retVal.resultValues.add(rv3);
        
        var rv4 = factory.createResultValue();
        rv4.tierName = ipaA.tierName;
        rv4.groupIndex = groupIndex;
        ipaE = phoneMap.bottomAlignmentElements.get(detectorResult.secondPosition);
        stringIdx = ipaA.stringIndexOfElement(ipaE);
        rv4.range = new Range(stringIdx, stringIdx, false);        
        rv4.data = ipaE.text;
        retVal.resultValues.add(rv4);
        
        // metadata
        var metadata = retVal.metadata;
        metadata.put(FEATURES1, detectorResult.features1.toString());
        metadata.put(FEATURES2, detectorResult.features2.toString());
        
        return retVal;
    };

};
