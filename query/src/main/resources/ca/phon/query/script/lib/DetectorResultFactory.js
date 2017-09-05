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
		var rv1 = factory.createResultValue();
		rv1.tierName = "IPA Target";
		rv1.groupIndex = groupIndex;
		ipaE = phoneMap.topAlignmentElements.get(p1);
		stringIdx = (ipaE == null ? -1: ipaT.stringIndexOfElement(ipaE));
		rv1.range = new Range(stringIdx, stringIdx+(ipaE.toString().length()), false);
		rv1.data = (ipaE == null ? "": ipaE.text);
		retVal.addResultValue(rv1);
		
		var rv2 = factory.createResultValue();
		rv2.tierName = "IPA Target";
		rv2.groupIndex = groupIndex;
		ipaE = phoneMap.topAlignmentElements.get(p2);
		stringIdx = (ipaE == null ? -1: ipaT.stringIndexOfElement(ipaE));
		rv2.range = new Range(stringIdx, stringIdx+(ipaE.toString().length()), false);
		rv2.data = (ipaE == null ? "": ipaE.text);
		retVal.addResultValue(rv2);
		
		var rv3 = factory.createResultValue();
		rv3.tierName = "IPA Actual";
		rv3.groupIndex = groupIndex;
		ipaE = phoneMap.bottomAlignmentElements.get(p1);
		stringIdx = (ipaE == null ? -1: ipaA.stringIndexOfElement(ipaE));
		rv3.range = new Range(stringIdx, stringIdx+(ipaE.toString().length()), false);
		rv3.data = (ipaE == null ? "": ipaE.text);
		retVal.addResultValue(rv3);
		
		var rv4 = factory.createResultValue();
		rv4.tierName = "IPA Actual";
		rv4.groupIndex = groupIndex;
		ipaE = phoneMap.bottomAlignmentElements.get(p2);
		stringIdx = (ipaE == null ? -1: ipaA.stringIndexOfElement(ipaE));
		rv4.range = new Range(stringIdx, stringIdx+(ipaE.toString().length()), false);
		rv4.data = (ipaE == null ? "": ipaE.text);
		retVal.addResultValue(rv4);
		
		return retVal;
	};
	
};
