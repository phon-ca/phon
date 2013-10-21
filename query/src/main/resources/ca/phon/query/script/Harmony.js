
/*
params = 
		{multibool, bConsonant|bVowel,
					true|true,
					"Consonant"|"Vowel",
					"Harmony type", 2},
		{enum, eDirection, "Progressive"|"Regressive"|"Both",
			2, "Directionality"}
		;
*/

// param setup

var FeatureFilter = require("lib/FeatureFilter").FeatureFilter;
var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var SyllableFilter = require("lib/SyllableFilter").SyllableFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;

	
/********************************
 * Setup params
 *******************************/

var filters = {
    "shared": new FeatureFilter("filters.shared"),
    "neutralized": new FeatureFilter("filters.neutralized"),
    "group": new GroupFilter("filters.group"),
    "alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
    "speaker": new ParticipantFilter("filters.speaker")
};

function param_setup(params) {
    filters.shared.setFilterTitle("Shared features:");
    filters.shared.param_setup(params);
    filters.neutralized.setFilterTitle("Neutralized features:");
    filters.neutralized.param_setup(params);
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
function query_record(record) {
	var searchTier = "IPA Target";
    if(!filters.speaker.check_speaker(record.speaker)) return;
    
	var searchObjects = filters.group.getRequestedGroups(record, searchTier);
	
	// check aligned group for each group returned
	if(filters.alignedGroup.isUseFilter()) {
	    searchObjects = filters.alignedGroup.filter_groups(record, searchObjects);
	}
	
	var sharedFeatureSets = filters.shared.parse_features();
	var neutralizedFeatureSets = filters.neutralized.parse_features();
	
	// perform harmony searches on groups
	if(bConsonant) {
		var detector = DetectorFactory.newConsonantHarmonyDetector();
		
		detector.sharedFeatures = sharedFeatureSets.required;
    	detector.absentSharedFeatures = sharedFeatureSets.absent;
    	detector.neutralizedFeatures = neutralizedFeatureSets.required;
    	detector.absentNeutralizedFeatures = neutralizedFeatureSets.absent;
		detector.direction = eDirection.index;
		
		for(var i = 0; i < searchObjects.length; i++) {
		    var searchObj = searchObjects[i];
		    var detectorResults = detector.detect(record, searchObj.groupIndex);
		    addResults(detectorResults);
		}
	}
	
	if(bVowel) {
		var detector = DetectorFactory.newVowelHarmonyDetector();
		
		detector.sharedFeatures = sharedFeatureSets.required;
    	detector.absentSharedFeatures = sharedFeatureSets.absent;
    	detector.neutralizedFeatures = neutralizedFeatureSets.required;
    	detector.absentNeutralizedFeatures = neutralizedFeatureSets.absent;
		detector.direction = eDirection.index;
		
		for(var i = 0; i < searchObjects.length; i++) {
		    var searchObj = searchObjects[i];
		    var detectorResults = detector.detect(record, searchObj.groupIndex);
		    addResults(detectorResults);
		}
	}
}

function addResults(detectorResults) 
{
	for(var rIndex = 0; rIndex < detectorResults.length; rIndex++)
	{
		var result = detectorResults[rIndex];
		results.add(result.resultValues,
						  result.metadata,
						  result.type);
	}
}
