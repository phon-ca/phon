/*
params =
		{enum, searchTier, "IPA Target"|"IPA Actual", 0, "<html><b>Search Tier</b></html>"}
	;
*/

var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var SyllableFilter = require("lib/SyllableFilter").SyllableFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;
var Pcc = require("lib/Pcc").Pcc;
var PccOptions = require("lib/Pcc").PccOptions;
var StressPatternOptions = require("lib/StressPattern").StressPatternOptions;
var CvPatternOptions = require("lib/CvPattern").CvPatternOptions;
var ResultType = require("lib/PhonScriptConstants").ResultType;
	
/********************************
 * Setup params
 *******************************/

var filters = {
    "primary": new PatternFilter("filters.primary"),
    "targetResultFilter": new PatternFilter("filters.targetResultFilter"),
    "actualResultFilter": new PatternFilter("filters.actualResultFilter"),
    "group": new GroupFilter("filters.group"),
    "groupPattern": new PatternFilter("filters.groupPattern"),
    "alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
    "word": new WordFilter("filters.word"),
    "wordPattern": new PatternFilter("filters.wordPattern"),
    "alignedWord": new AlignedWordFilter("filters.alignedWord"),
    "syllable": new SyllableFilter("filters.syllable"),
    "speaker": new ParticipantFilter("filters.speaker")
};

var metadataOptions = {
    "pcc_aligned": new PccOptions("metadataOptions.pcc_aligned", true),
    "pcc_standard": new PccOptions("metadataOptions.pcc_standard", false),
    "stressPattern": new StressPatternOptions("metadataOptions.stressPattern"),
    "cvPattern": new CvPatternOptions("metadataOptions.cvPattern")
};

var includeAlignedParamInfo = {
    "id": "includeAligned",
    "title": "",
    "desc": "Include aligned phones",
    "def": true
};
var includeAlignedParam;
var includeAligned = includeAlignedParamInfo.def;

function setup_params(params) {

	filters.primary.setSelectedPatternType(PatternType.PHONEX);
	filters.primary.param_setup(params);
	filters.primary.set_required(true);
	
	// setup result filter section
	var resultFilterSection = new SeparatorScriptParam("Aligned Phones", true);
	var targetLbl = new LabelScriptParam("", "<html><b>IPA Target Matcher</b></html>");
	var actualLbl = new LabelScriptParam("", "<html><b>IPA Actual Matcher</b></html>");
	
	includeAlignedParam = new BooleanScriptParam(
	    includeAlignedParamInfo.id,
	    includeAlignedParamInfo.desc,
	    includeAlignedParamInfo.title,
	    includeAlignedParamInfo.def);
    
	params.add(resultFilterSection);
	params.add(includeAlignedParam);
	params.add(targetLbl);
	filters.targetResultFilter.setSelectedPatternType(PatternType.PHONEX);
	filters.targetResultFilter.param_setup(params);
	params.add(actualLbl);
	filters.actualResultFilter.setSelectedPatternType(PatternType.PHONEX);
	filters.actualResultFilter.param_setup(params);
	
	var includeAlignedListener = new java.beans.PropertyChangeListener() {
	    propertyChange: function(e) {
	        var enabled = e.source.getValue(e.source.paramId);
	        filters.targetResultFilter.setEnabled(enabled);
	        filters.actualResultFilter.setEnabled(enabled);
	    }  
	};
	includeAlignedParam.addPropertyChangeListener(includeAlignedParamInfo.id, includeAlignedListener);
	var alignedEnabled = includeAlignedParamInfo.def;
    filters.targetResultFilter.setEnabled(alignedEnabled);
	filters.actualResultFilter.setEnabled(alignedEnabled);
	
	filters.group.param_setup(params);
	filters.groupPattern.param_setup(params);
	var sep = new LabelScriptParam("", "<html><b>Aligned Group</b></html>");
	params.add(sep);
	filters.alignedGroup.param_setup(params);
	
	filters.word.param_setup(params);
	filters.wordPattern.param_setup(params);
    filters.wordPattern.setEnabled(false);
	var wordsep = new LabelScriptParam("", "<html><b>Aligned Word</b></html>");
    params.add(wordsep);
    filters.alignedWord.param_setup(params);
    var searchByWordListener = new java.beans.PropertyChangeListener {
        propertyChange: function(e) {
            var enabled = e.source.getValue(e.source.paramId);
            filters.wordPattern.setEnabled(enabled);
            filters.alignedWord.setEnabled(enabled);
        }    
    };
    filters.word.searchByWordOpt.addPropertyChangeListener(filters.word.searchByWordOpt.paramId, searchByWordListener);
    var enabled = filters.word.searchByWordOpt.getValue(filters.word.searchByWordOpt.paramId);
    filters.wordPattern.setEnabled(enabled);
    filters.alignedWord.setEnabled(enabled);
    
	filters.syllable.param_setup(params);
	filters.speaker.param_setup(params);
	
	// add metadata options
	var metadataSep = new SeparatorScriptParam("Metadata Options", true);
	params.add(metadataSep);
	
	var spLbl = new LabelScriptParam("", "<html><b>Stress Pattern</b></html>");
	params.add(spLbl);
	metadataOptions.stressPattern.param_setup(params);
	
	var cvLbl = new LabelScriptParam("", "<html><b>CGV Pattern</b></html>");
	params.add(cvLbl);
	metadataOptions.cvPattern.param_setup(params);

    var pccStandardLbl = new LabelScriptParam("", "<html><b>PCC/PVC (standard)</b></html>");
    params.add(pccStandardLbl);
    metadataOptions.pcc_standard.param_setup(params);

	var pccAlignedLbl = new LabelScriptParam("", "<html><b>PCC/PVC (aligned)</b></html>");
	params.add(pccAlignedLbl);
	metadataOptions.pcc_aligned.param_setup(params);
}

/*
 * Globals
 */
var session;

function begin_search(s) {
    session = s;
}


/********************************
 * query_record
 * params:
 * 	record - the current record
 *******************************/
function query_record(recordIndex, record) {
    // check participant filter
    if(!filters.speaker.check_speaker(record.speaker)) return;
    
    // check group+groupPattern filters
	var groups = filters.group.getRequestedGroups(record);
    if(filters.groupPattern.isUseFilter()) {
        groups = filters.groupPattern.filter_groups(groups, searchTier);
    }
	
	// check aligned group for each group returned
	if(filters.alignedGroup.isUseFilter()) {
	    groups = filters.alignedGroup.filter_groups(record, groups);
	}

	// perform searches
	for(var i = 0; i < groups.length; i++)
	{
		var group = groups[i];
		var ipa = (searchTier == "IPA Target" ? group.IPATarget : group.IPAActual);
		
		var toSearch = new Array();
		toSearch.push(ipa);
		
		// search by word?
		if(filters.word.isUseFilter()) {
		   toSearch.length = 0;
		   var selectedWords = filters.word.getRequestedWords(group);
		   for(j = 0; j < selectedWords.length; j++) {
		       var word = selectedWords[j];
		       var addWord = true;

               var wordIpa = (searchTier == "IPA Target" ? word.IPATarget : word.IPAActual);
               // check word pattern if necessary
		       if(filters.wordPattern.isUseFilter()) {
		           addWord = filters.wordPattern.check_filter(wordIpa);
		       }
		      
		       // check aligned word pattern if necessary
		       if(filters.alignedWord.isUseFilter()) {
		           addWord = filters.alignedWord.check_word(word);
		       }
		       
		       if(addWord) {
		           toSearch.push(wordIpa);
		       }
		   }
		}
		
		// search by syllable?
		if(filters.syllable.isUseFilter()) {
		    var syllList = new Array();
		    for(j = 0; j < toSearch.length; j++) {
		        var obj = toSearch[j];
		        var sylls = filters.syllable.getRequestedSyllables(obj);
		        
		        for(k = 0; k < sylls.length; k++) {
		            syllList.push(sylls[k]);
		        }
		    }
		    toSearch = syllList;
		}
		
		for(j = 0; j < toSearch.length; j++) {
		    var obj = toSearch[j];
		    var matches = filters.primary.find_pattern(obj);
		    var primaryFilter = (searchTier == "IPA Target" ? filters.targetResultFilter : filters.actualResultFilter);
		    var alignedFilter = (searchTier == "IPA Target" ? filters.actualResultFilter : filters.targetResultFilter);
		    
		    for(k = 0; k < matches.length; k++) {
    	        var match = matches[k];
    	        
    	        if(primaryFilter.isUseFilter()) {
    	        	if(!primaryFilter.check_filter(new IPATranscript(match.value))) {
    	        		continue;
    	        	}
    	        }
    	        
    			var result = factory.createResult();
    			// calculate start/end positions of data in text
    			var startIndex = ipa.stringIndexOf(match.value);
    			var length = match.value.toString().length();
    			
    			result.recordIndex = recordIndex;
    			result.schema = "LINEAR";
    
    			var rv = factory.createResultValue();
    			rv.tierName = searchTier;
    			rv.groupIndex = group.groupIndex;
    			rv.range = new Range(startIndex, startIndex + length, false);
    			rv.data = (match.value != null ? new IPATranscript(match.value) : new IPATranscript());
    			result.addResultValue(rv);
    			
    			if(includeAligned) {
    			    var phoneMap = group.phoneAlignment;
    			    var alignedGroup = (searchTier == "IPA Target" ? group.getIPAActual() : group.getIPATarget());
    			    var aligned = phoneMap.getAligned(match.value);
			   		var alignedIpaElements = (aligned != null ? new IPATranscript(aligned) : new IPATranscript());
    			    
			   		// find location of aligned value in group
			   		var groupStartIdx = 
			   			(alignedIpaElements.length() > 0 ? alignedGroup.indexOf(alignedIpaElements.elementAt(0)) : 0);
			   		var groupEndIdx = 
			   			(alignedIpaElements.length() > 0 ? alignedGroup.indexOf(alignedIpaElements.elementAt(alignedIpaElements.length()-1)) : 0);
			   		var alignedIpa =
			   			(alignedIpaElements.length() > 0 ? alignedGroup.subsection(groupStartIdx, groupEndIdx+1) : new IPATranscript());
			   		
    			    if(alignedFilter.isUseFilter()) {
    			    	if(!alignedFilter.check_filter(alignedIpa)) {
    			    		continue;
    			    	}
    			    }
    			    
    			    var alignedRv = factory.createResultValue();
    			    alignedRv.tierName = (searchTier == "IPA Target" ? "IPA Actual" : "IPA Target");
    			    alignedRv.groupIndex = group.groupIndex;
    			   	if(aligned != null && aligned.length > 0) {
    			   		var alignedStart = alignedGroup.stringIndexOf(alignedIpa);
    			   		var alignedLength = alignedIpa.toString().length();
    			   		
    			   		alignedRv.range = new Range(alignedStart, alignedStart + alignedLength, false);
    			    	alignedRv.data = alignedIpa;
    			   	} else {
    			   		alignedRv.range = new Range(0, 0, true);
    			   		alignedRv.data = "";
    			   	}
    			    
    			    result.addResultValue(alignedRv);
    			    result.schema = "ALIGNED";
    			    calcMetadata(record, group, result.metadata, 
    			    		(match.value == null ? null : new IPATranscript(match.value)), 
    			    		(aligned == null ? null : new IPATranscript(aligned)) );
    			} else {
    				calcMetadata(record, group, result.metadata, 
    						(match.value == null ? null : new IPATranscript(match.value)), null);
    			}
    			
    			results.addResult(result);
    	    }
		}
	}
}

/********************************
 * Functions
 *******************************/

/* Generate metadata based on parmeters */
function calcMetadata(record, group, metadata, ipaTVal, ipaAVal) {
    var retVal = metadata;
    
    if(metadataOptions.stressPattern.include == true) {
        var tsp = (ipaTVal == null ? null : ipaTVal.stressPattern);
        var asp = (ipaAVal == null ? null : ipaAVal.stressPattern)
        
        if(tsp != null && asp != null && metadataOptions.stressPattern.separate == false) {
            var sp = tsp + " \u2194 " + asp;
            retVal.put("SP", sp);
        } else {
            if(tsp != null) {
                var name = (metadataOptions.stressPattern.separate == true ? "SP-T" : "SP");
                retVal.put(name, tsp);
            }
            if(asp != null) {
                var name = (metadataOptions.stressPattern.separate == true ? "SP-A" : "SP");
                retVal.put(name, asp);
            }
        }
    }
    
    if(metadataOptions.cvPattern.include == true) {
        var tcv = (ipaTVal == null ? null : ipaTVal.cvPattern);
        var acv = (ipaAVal == null ? null : ipaAVal.cvPattern);
        
        if(tcv != null && acv != null && metadataOptions.cvPattern.separate == false) {
            var cv = tcv + " \u2194 " + acv;
            retVal.put("CGV", cv);
        } else {
            if(tcv != null) {
                var name = (metadataOptions.cvPattern.separate == true ? "CGV-T" : "SP");
                retVal.put(name, tcv);
            }
            if(acv != null) {
                var name = (metadataOptions.cvPattern.separate == true ? "CGV-A" : "SP");
                retVal.put(name, acv);
            }
        }
    }
    
    if(group != null) {
        metadataOptions.pcc_standard.setup_pcc_standard_metadata(group, retVal);
        metadataOptions.pcc_aligned.setup_pcc_aligned_metadata(group, retVal);
    }
}
