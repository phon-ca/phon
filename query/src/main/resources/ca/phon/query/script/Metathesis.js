
importPackage(Packages.ca.phon.query.detectors)

// param setup
var FeatureFilter = require("lib/FeatureFilter").FeatureFilter;
var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var SyllableFilter = require("lib/SyllableFilter").SyllableFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var DetectorResultFactory = require("lib/DetectorResultFactory").DetectorResultFactory;
	
/********************************
 * Setup params
 *******************************/

var filters = {
    "primary": new FeatureFilter("filters.primary"),
    "group": new GroupFilter("filters.group"),
    "alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
    "speaker": new ParticipantFilter("filters.speaker")
};

/*
 * Globals
 */
var session;

function begin_search(s) {
    session = s;
}

function setup_params(params) {
    filters.primary.param_setup(params);
    filters.group.param_setup(params);
    var sep = new LabelScriptParam("", "Aligned Group");
    params.add(sep);
    filters.alignedGroup.param_setup(params);
    filters.speaker.param_setup(params);
}

/********************************
 * query_record
 * params:
 * 	record - the current record
 *******************************/
function query_record(recordIndex, record) {
	var searchTier = "IPA Target";
	if(!filters.speaker.check_speaker(record.speaker)) return;
    
	var searchObjects = filters.group.getRequestedGroups(record);
	
	// check aligned group for each group returned
	if(filters.alignedGroup.isUseFilter()) {
	    searchObjects = filters.alignedGroup.filter_groups(record, searchObjects);
	}
	
	var featureSets = filters.primary.parse_features();
	
	var detector = new AdvancedMetathesisDetector();
	detector.features = featureSets.required;
	detector.absentFeatures = featureSets.absent;
	
	for(var i = 0; i < searchObjects.length; i++) {
	    var searchObj = searchObjects[i];
	    var phoneMap = searchObj.phoneAlignment;
	    var detectorResults = detector.detect(phoneMap);
	    addResults(recordIndex, searchObj.groupIndex, detectorResults);
	}
}

function addResults(recordIndex, groupIndex, detectorResults) 
{
    var detectorResultFactory = new DetectorResultFactory();
	for(var rIndex = 0; rIndex < detectorResults.size(); rIndex++)
	{
		var detectorResult = detectorResults.get(0);
		var result = detectorResultFactory.createQueryResult(recordIndex, groupIndex, detectorResult);
		results.addResult(result);
	}
	
}
