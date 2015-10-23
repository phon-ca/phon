/**
 * UBC-cvc_word_match.js
 *
 * @author Greg J. Hedlund <ghedlund@mun.ca>
 * Date: 31 Oct 2014
 */
 
/*
params = {label, "", "Whole-word comparison of IPA Actual vs. IPA Target forms."};
*/

importClass(Packages.ca.phon.syllable.SyllableConstituentType)

var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var ResultType = require("lib/PhonScriptConstants").ResultType;

var filters = {
	"group": new GroupFilter("filters.group"),
	"alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
	"word": new WordFilter("filters.word"),
	"alignedWord": new AlignedWordFilter("filters.alignedWord"),
	"speaker": new ParticipantFilter("filters.speaker")
};

var ignoreTruncatedParamInfo = {
	"id": "ignoreTruncated",
	"desc": "Ignore truncated words",
	"title": "",
	"def": true
};
var ignoreTruncatedParam;
var ignoreTruncated = ignoreTruncatedParamInfo.def;

var compTypesParamInfo = {
	"id":["includeExactMatch", "includeCVPatternMatch", "includeStressPatternMatch"],
	"title": "Comparators:",
	"desc":["Exact match", "CV pattern (see options below)", "Stress pattern"],
	"def":[true, true, true],
	"numCols":1
};
var compTypesParam;
var includeExactMatch = compTypesParamInfo.def[0];
var includeCVPatternMatch = compTypesParamInfo.def[1];
var includeStressPatternMatch = compTypesParamInfo.def[2];

var cvOptionsParamInfo = {
    "id":["includeSyllableBoundaries", "includeImplicitBoundaries", "includeStressMarkers"],
    "title": "CV Pattern Options",
    "desc":[ "Include syllable boundaries", "Include implicit boundaries", "Include stress markers"],
    "def":[ true, true, true],
    "numCols": 1
};
var cvOptionsParam;
var includeSyllableBoundaries = cvOptionsParamInfo.def[0];
var includeImplicitBoundaries = cvOptionsParamInfo.def[1];
var includeStressMarkers = cvOptionsParamInfo.def[2];

var searchTier = "IPA Target";
var alignedTier = "IPA Actual";

var mdataExactField = "Word Exact Match";
var mdataCVField = "Word CV Match";
var mdataStressField = "Word Stress Match";

/*
 * Globals
 */
var session;

function begin_search(s) {
    session = s;
}

function setup_params(params) {
	var cvOptionsSep = new SeparatorScriptParam("Comparison Options", false);
	params.add(cvOptionsSep);
	
	ignoreTruncatedParam = new BooleanScriptParam(
		ignoreTruncatedParamInfo.id,
		ignoreTruncatedParamInfo.desc,
		ignoreTruncatedParamInfo.title,
		ignoreTruncatedParamInfo.def);
	params.add(ignoreTruncatedParam);
	
	compTypesParam = new MultiboolScriptParam(
		compTypesParamInfo.id,
		compTypesParamInfo.def,
		compTypesParamInfo.desc,
		compTypesParamInfo.title,
		compTypesParamInfo.numCols);
	params.add(compTypesParam);

	cvOptionsParam = new MultiboolScriptParam(
		cvOptionsParamInfo.id,
		cvOptionsParamInfo.def,
		cvOptionsParamInfo.desc,
		cvOptionsParamInfo.title,
		cvOptionsParamInfo.numCols);
	params.add(cvOptionsParam);
	
	filters.group.param_setup(params);
	var sep = new LabelScriptParam("", "<html><b>Aligned Group</b></html>");
	params.add(sep);
	filters.alignedGroup.param_setup(params);
		
	filters.word.searchByWordEnabled = false;
	filters.word.param_setup(params);
	var wordsep = new LabelScriptParam("", "<html><b>Aligned Word</b></html>");
	params.add(wordsep);
	filters.alignedWord.param_setup(params);

	ignoreTruncatedParam = new BooleanScriptParam(ignoreTruncatedParamInfo.id,
		ignoreTruncatedParamInfo.title, ignoreTruncatedParamInfo.desc,
		ignoreTruncatedParamInfo.def);
	params.add(ignoreTruncatedParam);
	
	filters.speaker.param_setup(params);
}

/********************************
 * query_record (required)
 *
 * Called for each record in a session.
 * Perform search operations here.
 *
 * params:
 *  recordIndex - current record index
 *	record - current record
 * returns:
 *	void
 *******************************/
function query_record(recordIndex, record)
{
	if(!filters.speaker.check_speaker(record.speaker)) return;
    
	var searchObjects = filters.group.getRequestedGroups(record);
	// check aligned group for each group returned
	if(filters.alignedGroup.isUseFilter()) {
	    searchObjects = filters.alignedGroup.filter_groups(record, searchObjects);
	}
	
	for(var gIdx = 0; gIdx < searchObjects.length; gIdx++) {
		var group = searchObjects[gIdx];
		var words = filters.word.getRequestedWords(group, "IPA Target");
		
		for(var wIdx = 0; wIdx < words.length; wIdx++) {
			var word = words[wIdx];
			if(filters.alignedWord.isUseFilter()) {
				var alignedWord = word.getTier(filters.alignedWord.tierName);
				if(!filters.alignedWord.patternFilter.check_filter(alignedWord)) continue;
			}
			
			checkWordMatch(recordIndex, record, word);
		}
	}
}

function checkWordMatch(recordIndex, record, obj) {
	var word = (obj.IPATarget != null ? obj.IPATarget : new IPATranscript());
	var aligned = (obj.IPAActual != null ? obj.IPAActual : new IPATranscript());
	
	if((aligned ==  null || aligned.length() == 0) && ignoreTruncated == true) {
		return;
	}

	var result = factory.createResult();
	result.recordIndex = recordIndex;
	result.schema = "ALIGNED";
	
	var orthoVal = factory.createResultValue();
	orthoVal.tierName = "Orthography";
	orthoVal.groupIndex = obj.group.groupIndex;
	var startIndex = obj.getOrthographyWordLocation();
	var length = obj.orthography.toString().length();
	orthoVal.range = new Range(startIndex, startIndex + length, false);
	orthoVal.data = obj.orthography;
	result.addResultValue(orthoVal);
	
	var rv = factory.createResultValue();
	rv.tierName = searchTier;
	rv.groupIndex = obj.group.groupIndex;
	var startIndex = obj.getIPATargetWordLocation();
	var length = word.toString().length();
	rv.range = new Range(startIndex, startIndex + length, false);
	rv.data = word;
	result.addResultValue(rv);
	
	rv = factory.createResultValue();
	rv.tierName = alignedTier;
	rv.groupIndex = obj.group.groupIndex;
	var startIndex = obj.getIPAActualWordLocation();
	var length = aligned.toString().length();
	rv.range = new Range(startIndex, startIndex + length, false);
	rv.data = aligned;
	result.addResultValue(rv);
	
	if(includeExactMatch == true) {
		result.metadata.put(mdataExactField, word.toString().equals(aligned.toString()) + "");
	}
	
	if(includeCVPatternMatch == true) {
		var targetCVPattern = cvPattern2(word, includeStressMarkers, includeSyllableBoundaries, includeImplicitBoundaries);
		var alignedCVPattern = cvPattern2(aligned, includeStressMarkers, includeSyllableBoundaries, includeImplicitBoundaries);
		
		var exactMatch = (targetCVPattern == alignedCVPattern);
		
		result.metadata.put(searchTier + " CV", targetCVPattern);
		result.metadata.put(alignedTier + " CV", alignedCVPattern)
		result.metadata.put(mdataCVField, exactMatch + "");
	}
	
	if(includeStressPatternMatch == true) {
		var targetStressPattern = word.stressPattern;
		var actualStressPattern = aligned.stressPattern;
		
		var exactMatch = word.matchesStressPattern(actualStressPattern);
		
		result.metadata.put(searchTier + " Stress", targetStressPattern);
		result.metadata.put(alignedTier + " Stress", actualStressPattern);
		result.metadata.put(mdataStressField, exactMatch + "");
	}
	
	results.addResult(result);
}

/**
 * Special CV pattern generator.
 */
function cvPattern2(obj, includeStressMarkers, includeSyllableBoundaries, includeImplicitBoundaries) {
	var retVal = "";

	if(includeSyllableBoundaries == true && includeImplicitBoundaries == true) {
		for(var sIdx = 0; sIdx < obj.syllables().size(); sIdx++) {
			var syll = obj.syllables().get(sIdx);
			retVal += (sIdx > 0 && !syll.matches(".:S.*") ? "." : "") + cvPattern(syll, includeStressMarkers, includeSyllableBoundaries);
		}
	} else {
		retVal = cvPattern(obj, includeStressMarkers, includeSyllableBoundaries);
	}
	
	return retVal;
}
 
function cvPattern(obj, includeStressMarkers, includeSyllableBoundaries) {
	var retVal = "";

	for(var pIdx = 0; pIdx < obj.length(); pIdx++) {
		var p = obj.elementAt(pIdx);
		
		if(p.getScType() == SyllableConstituentType.SYLLABLEBOUNDARYMARKER) {
			if(includeSyllableBoundaries == true && pIdx > 0)
				retVal += ".";
			continue;
		} else if(p.getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER) {
			if(includeStressMarkers == true)
				retVal += p.getText();
			continue;
		} else if(p.getScType() == SyllableConstituentType.WORDBOUNDARYMARKER) {
			retVal += " ";
			continue;
		}
		
		if(p.getFeatureSet().hasFeature("Consonant")) {
			retVal += "C";
		} else if(p.getFeatureSet().hasFeature("Vowel")) {
			retVal += "V";
		}
	}
	
	return retVal;
}
