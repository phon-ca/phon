/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Helper method for detector (i.e., Harmony or Metathesis) results.
 */
exports.DetectorResultFactory = function () {
	
	/**
	 * Convert the given HarmonyDetectorResult to a query result
	 */
	this.createHarmonyResult = function (recordIndex, groupIndex, detectorResult) {
	    var retVal = this.createQueryResult(recordIndex, groupIndex, detectorResult);
	    
	    var metadata = retVal.metadata;
	    // add metadata
        var sharedProfile = detectorResult.sharedProfile;
        var neutralizedProfile = detectorResult.neutralizedProfile;
        
        var dimensions = sharedProfile.dimensions;
        var itr = dimensions.iterator();
        var dimTxt = "";
        while(itr.hasNext()) {
            var t = itr.next().toString().toLowerCase();
            t = Packages.org.apache.commons.lang3.StringUtils.capitalize(t);
            dimTxt += (dimTxt.length > 0 ? ", " : "") + t;
        }
        metadata.put("Dimensions", dimTxt);
        
        metadata.put("Shared", sharedProfile.toString());
        metadata.put("Neutralized", neutralizedProfile.toString());
        metadata.put("Direction", detectorResult.isLeftToRight() ? "Progressive" : "Regressive");
	    
	    return retVal;
	};
	
	/**
	 * Convert the given ca.phon.query.detector.DetectorResult object
	 * into a query Result
	 *
	 * @param factory
	 * @param detectorResults
	 *
	 * @return the new query result or null
	 */
	this.createQueryResult = function (recordIndex, groupIndex, detectorResult) {
		var SCHEMA = "DETECTOR";
		var FEATURES1 = "Features1";
		var FEATURES2 = "Features2";
		
		var retVal = factory.createResult();
		retVal.schema = SCHEMA;
		retVal.recordIndex = recordIndex;
		
		var phoneMap = detectorResult.getPhoneMap();
		var ipaT = phoneMap.targetRep;
		var ipaA = phoneMap.actualRep;
		
		var p1 = Math.min(detectorResult.firstPosition, detectorResult.secondPosition);
		var p2 = Math.max(detectorResult.firstPosition, detectorResult.secondPosition);
		
		// result values
		var rv1 = this.createResultValue(phoneMap, groupIndex, true, p1);
		var rv2 = this.createResultValue(phoneMap, groupIndex, true, p2);
		var rv3 = this.createResultValue(phoneMap, groupIndex, false, p1);
		var rv4 = this.createResultValue(phoneMap, groupIndex, false, p2);
		
		retVal.addResultValue(rv1);
		retVal.addResultValue(rv2);
		retVal.addResultValue(rv3);
		retVal.addResultValue(rv4);
		
		return retVal;
	};
	
	this.createResultValue = function(phoneMap, groupIndex, isTarget, index) {
	    var rv = factory.createResultValue();
		rv.tierName = (isTarget == true ? "IPA Target" : "IPA Actual");
		rv.groupIndex = groupIndex;
		
		var ipaE = null;
		if(index >= 0) {
		    ipaE = (isTarget == true ? phoneMap.topAlignmentElements.get(index)
		        : phoneMap.bottomAlignmentElements.get(index));
		}
		
		var ipa = (isTarget == true ? phoneMap.targetRep : phoneMap.actualRep);
		stringIdx = (ipaE == null ? -1: ipa.stringIndexOfElement(ipaE));
		rv.range = 
		    (stringIdx < 0 ? new Range(0, 0, true) : new Range(stringIdx, stringIdx+(ipaE.toString().length()), false));
		rv.data = (ipaE == null ? "\u2205": ipaE.text);
		
		return rv;
	};
	
};
