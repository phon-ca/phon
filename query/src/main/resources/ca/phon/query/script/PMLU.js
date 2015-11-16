/**
 * PMLU.js
 *
 * @author Greg J. Hedlund <ghedlund@mun.ca>
 * Date: 05 Nov 2014
 */
 
/*
params = {label, "<html><p>Phonological Mean Length of Utterance (PMLU) [Ingram 2002] for aligned words</p></html>",  "<html><b>Phonological Mean Length of Utterance</b></html>"};
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

var includePMLUParamInfo = {
	"id": "includePMLU",
	"desc": "Include Phonological Mean Length of Utterance (Ingram 2002)",
	"title": "PMLU",
	"def": true
};
var includePMLUParam;
var includePMLU = includePMLUParamInfo.def;

var includeEPMLUParamInfo = {
	"id": "includeEPMLU",
	"desc": "Include Expanded Phonological Mean Length of Utterance (Arias & Lleó 2013)",
	"title": "ePMLU",
	"def": true
};
var includeEPMLUParam;
var includeEPMLU = includeEPMLUParamInfo.def;

var ePMLUClosedSyllBonusParamInfo = {
	"id": "closedSyllBonus",
	"desc": "Closed syllable bonus",
	"title": "",
	"prompt": "Enter a number",
	"def": "1.0"
};
var ePMLUClosedSyllBonusParam;
var closedSyllBonus;

var searchTier = "IPA Target";
var alignedTier = "IPA Actual";

function begin_search(session) {
	// fix closedSyllBonus so it is a number, not a string
	closedSyllBonus = parseFloat(closedSyllBonus);
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
	
	includePMLUParam = new BooleanScriptParam(
		includePMLUParamInfo.id,
		includePMLUParamInfo.desc,
		includePMLUParamInfo.title,
		includePMLUParamInfo.def);
	params.add(includePMLUParam);
	
	includeEPMLUParam = new BooleanScriptParam(
		includeEPMLUParamInfo.id,
		includeEPMLUParamInfo.desc,
		includeEPMLUParamInfo.title,
		includeEPMLUParamInfo.def);
	params.add(includeEPMLUParam);
	
	ePMLUClosedSyllBonusParam = new StringScriptParam(
		ePMLUClosedSyllBonusParamInfo.id,
		ePMLUClosedSyllBonusParamInfo.desc,
		ePMLUClosedSyllBonusParamInfo.def);
	ePMLUClosedSyllBonusParam.setPrompt(ePMLUClosedSyllBonusParamInfo.prompt);
	params.add(ePMLUClosedSyllBonusParam);
	
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

			if(ignoreTruncated && word.getIPAActual() == null || word.getIPAActual().length() == 0) {
				continue;
			}
			
		    var ipaT = (word.getIPATarget() != null ? word.getIPATarget() : new IPATranscript());
		    var ipaA = (word.getIPAActual() != null ? word.getIPAActual() : new IPATranscript());
		    
		    var result = factory.createResult();
		    result.schema = "ALIGNED";
		    result.recordIndex = recordIndex;
		    
		    var rvt = factory.createResultValue();
		    rvt.tierName = "IPA Target";
	    	rvt.groupIndex = gIdx;
	    	var startIndex = word.getIPATargetWordLocation();
	    	var endIndex = startIndex + ipaT.toString().length();
	    	rvt.range = new Range(startIndex, endIndex, false);
	    	rvt.data = ipaT;
	    	result.addResultValue(rvt);
	    	
	    	var rva = factory.createResultValue();
	    	rva.tierName = "IPA Actual";
	    	rva.groupIndex = gIdx;
	    	startIndex = word.getIPAActualWordLocation();
	    	endIndex = startIndex + ipaA.toString().length();
	    	rva.range = new Range(startIndex, endIndex, false);
	    	rva.data = ipaA;
	        result.addResultValue(rva);
	        
	        result.metadata.put("Word", wIdx + "");
	        
	        var nf = java.text.NumberFormat.getNumberInstance();
	    	nf.setMaximumFractionDigits(6);
	    	
	        var pm = (word.phoneAlignment != null ? word.phoneAlignment : new PhoneMap(ipaT, ipaA));
	        if(includePMLU == true && pm.PMLU|0 != 0) {
	        	var pmlu = pm.PMLU;
	        	result.metadata.put("target PMLU", nf.format(pmlu.targetPMLU()));
	        	result.metadata.put("actual PMLU", nf.format(pmlu.actualPMLU()));
	        	result.metadata.put("PWP", nf.format(pmlu.PWP()));
	        }
	        
	        if(includeEPMLU == true && pm.EPMLU|0 != 0) {
	        	var emplu = pm.EPMLU;
	        	result.metadata.put("target ePMLU-Features", nf.format(emplu.targetEPMLUFeatures()));
	        	result.metadata.put("actual ePMLU-Features", nf.format(emplu.actualEPMLUFeatures()));
	        	result.metadata.put("ePWP-Features", nf.format(emplu.ePWPFeatures()));
	        	
	        	result.metadata.put("target ePMLU-Syllables", nf.format(emplu.targetEPMLUSyllables(closedSyllBonus)));
	        	result.metadata.put("actual ePMLU-Syllables", nf.format(emplu.actualEPMLUSyllables(closedSyllBonus)));
	        	result.metadata.put("ePWP-Syllables", nf.format(emplu.ePWPSyllables(closedSyllBonus)));
	        	
	        	result.metadata.put("target ePMLU", nf.format(emplu.targetEPMLU(closedSyllBonus)));
	        	result.metadata.put("actual ePMLU", nf.format(emplu.actualEPMLU(closedSyllBonus)));
	        	result.metadata.put("ePWP", nf.format(emplu.ePWP(closedSyllBonus)));
	        }
	        
	        results.addResult(result);
		}
	}
}
