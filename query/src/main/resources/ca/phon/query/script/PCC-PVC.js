/*
params = {separator, "Information", false},
			{label, "<html><p>This script calculates PCC/PVC (percent consonants/vowels correct)<br/> for each group in the selected sessions.</p></html>", ""},
			{label, "<html><br/><p>Results are reported as:<br/>&lt;# correct&gt;/&lt;# attempted&gt;;&lt;# deleted&gt;;&lt;# epenthesized&gt;</p></html>", ""}
        ;
*/

var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var PccOptions = require("lib/Pcc").PccOptions;
var Pcc = require("lib/Pcc").Pcc;

var ignoreTruncatedParamInfo = {
	"id": "ignoreTruncated",
	"desc": "Ignore truncated words",
	"title": "",
	"def": true
};
var ignoreTruncatedParam;
var ignoreTruncated = ignoreTruncatedParamInfo.def;

var pccOptions = {
    "standard": new PccOptions("pccOptions.standard"),
    "aligned": new PccOptions("pccOptions.aligned")
};

var filters = {
	"group": new GroupFilter("filters.group"),
	"alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
	"word": new WordFilter("filters.word"),
	"alignedWord": new AlignedWordFilter("filters.alignedWord"),
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
    var sep = new SeparatorScriptParam("PCC/PVC Options", false);
    params.add(sep);
    
    var pccStandardLbl = new LabelScriptParam("", "<html><b>PCC/PVC (standard)</b></html>");
    params.add(pccStandardLbl);
    pccOptions.standard.param_setup(params);

	var pccAlignedLbl = new LabelScriptParam("", "<html><b>PCC/PVC (aligned)</b></html>");
	params.add(pccAlignedLbl);
	pccOptions.aligned.param_setup(params);
	
	filters.group.param_setup(params);
	var sep = new LabelScriptParam("", "Aligned Group Filter");
	params.add(sep);
	filters.alignedGroup.param_setup(params);
	
	filters.word.searchByWordEnabled = false;
	filters.word.param_setup(params);
	var wordsep = new LabelScriptParam("", "<html><b>Aligned Word</b></html>");
	params.add(wordsep);
	filters.alignedWord.param_setup(params);

	filters.speaker.param_setup(params);
}

/********************************
 * query_record (required)
 *
 * Called for each record in a session.
 * Perform search operations here.
 *
 * params:
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
		    
		    var metadata = result.metadata;
		    
		    // exclude values which bias results. 
		    // (e.g., all-vowel words such as 'eau' in French when looking only at PCC)
		    if( (pccOptions.standard.includePcc == true || pccOptions.aligned.includePcc == true)
		    		&& (pccOptions.standard.includePvc == false && pccOptions.aligned.includePvc == false)
		    		&& ipaT.indexOf("\\c") < 0) {
		    	continue;
		    }
		    if( (pccOptions.standard.includePcc == false && pccOptions.aligned.includePcc == false)
		    		&& (pccOptions.standard.includePvc == true || pccOptions.aligned.includePvc == true)
		    		&& ipaT.indexOf("\\v") < 0) {
		    	continue;
		    }
		    
		    pccOptions.standard.setup_pcc_standard_metadata(word, metadata);
		    pccOptions.aligned.setup_pcc_aligned_metadata(word, metadata);
		    
		    results.addResult(result);
		}
	}
}
