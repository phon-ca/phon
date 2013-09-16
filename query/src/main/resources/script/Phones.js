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
    "alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
    "word": new WordFilter("filters.word"),
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

function param_setup(params) {

	filters.primary.setSelectedPatternType(PatternType.PHONEX);
	filters.primary.param_setup(params);
	
	// setup result filter section
	var resultFilterSection = new SeparatorScriptParam("Aligned Phones", true);
	var targetLbl = new LabelScriptParam("", "<html><b>IPA Target Matcher</b></html>");
	var actualLbl = new LabelScriptParam("", "<html><b>IPA Actual Matcher</b></html>");
	
	includeAlignedParam = new BooleanScriptParam(
	    includeAlignedParamInfo.id,
	    includeAlignedParamInfo.desc,
	    includeAlignedParamInfo.title,
	    includeAlignedParamInfo.def);
    var includeAlignedListener = new java.awt.event.ItemListener() {
        itemStateChanged: function(e) {
	        var enabled = e.getSource().isSelected();
	        filters.targetResultFilter.setEnabled(enabled);
	        filters.actualResultFilter.setEnabled(enabled);
	    }
	};
	includeAlignedParam.getEditorComponent().addItemListener(includeAlignedListener);
	
	params.add(resultFilterSection);
	params.add(includeAlignedParam);
	params.add(targetLbl);
	filters.targetResultFilter.setSelectedPatternType(PatternType.PHONEX);
	filters.targetResultFilter.param_setup(params);
	params.add(actualLbl);
	filters.actualResultFilter.setSelectedPatternType(PatternType.PHONEX);
	filters.actualResultFilter.param_setup(params);
	
	filters.group.param_setup(params);
	var sep = new LabelScriptParam("", "<html><b>Aligned Group</b></html>");
	params.add(sep);
	filters.alignedGroup.param_setup(params);
	
	filters.word.param_setup(params);
	var wordsep = new LabelScriptParam("", "<html><b>Aligned Word</b></html>");
    params.add(wordsep);
    filters.alignedWord.param_setup(params);
    var alignedWordListener = new java.awt.event.ItemListener {
          itemStateChanged: function(e) {
              var enabled = e.getSource().isSelected();
              filters.alignedWord.setEnabled(enabled);
          }
    };
    filters.alignedWord.setEnabled(false);
    filters.word.searchByWordOpt.getEditorComponent().addItemListener(alignedWordListener);
    
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


/********************************
 * query_record
 * params:
 * 	record - the current record
 *******************************/
function query_record(record) {
    if(!filters.speaker.check_speaker(record.speaker)) return;
    
	var searchObjects = filters.group.getRequestedGroups(record, searchTier);
	
	// check aligned group for each group returned
	if(filters.alignedGroup.isUseFilter()) {
	    searchObjects = filters.alignedGroup.filter_groups(record, searchObjects);
	}
	
	if(filters.word.isUseFilter()) {
	    searchObjects = filters.word.filter(searchObjects);
	    
	    if(filters.alignedWord.isUseFilter()) {
    	    var tempArray = new Array();
    	    for(var i = 0; i < searchObjects.length; i++) {
    	        var word = searchObjects[i];
    	        var aWord = WordAligner.findAlignedWord(word, filters.alignedWord.tier);
    	        
    	        if(filters.alignedWord.patternFilter.check_filter(aWord)) {
    	            tempArray.push(word);
    	        }
    	    }
            searchObjects = tempArray;	    
	    }
	}
	
	if(filters.syllable.isUseFilter()) {
	    searchObjects = filters.syllable.filter(record, searchObjects);
	}

	// perform searches
	for(var i = 0; i < searchObjects.length; i++)
	{
		var searchObj = searchObjects[i];
		
		if(includeAligned) {
		    searchAlignedPhones(record, searchObj);
		} else {
		    searchPhones(record, searchObj);
		}
	}
}

/********************************
 * Functions
 *******************************/

/* Generate metadata based on parmeters */
function getMetadata(record, ipaTVal, ipaAVal) {
    var retVal = new Metadata();
    
    if(metadataOptions.stressPattern.include) {
        var tsp = (ipaTVal == null ? null : ipaTVal.stressPattern);
        var asp = (ipaAVal == null ? null : ipaAVal.stressPattern)
        
        if(tsp != null && asp != null && !metadataOptions.stressPattern.separate) {
            var sp = tsp + " \u2194 " + asp;
            retVal.put("SP", sp);
        } else {
            if(tsp != null) {
                var name = (metadataOptions.stressPattern.separate ? "SP-T" : "SP");
                retVal.put(name, tsp);
            }
            if(asp != null) {
                var name = (metadataOptions.stressPattern.separate ? "SP-A" : "SP");
                retVal.put(name, asp);
            }
        }
    }
    
    if(metadataOptions.cvPattern.include) {
        var tcv = (ipaTVal == null ? null : ipaTVal.cvPattern);
        var acv = (ipaAVal == null ? null : ipaAVal.cvPattern);
        
        if(tcv != null && acv != null && !metadataOptions.cvPattern.separate) {
            var cv = tcv + " \u2194 " + acv;
            retVal.put("CGV", cv);
        } else {
            if(tcv != null) {
                var name = (metadataOptions.cvPattern.separate ? "CGV-T" : "SP");
                retVal.put(name, tcv);
            }
            if(acv != null) {
                var name = (metadataOptions.cvPattern.separate ? "CGV-A" : "SP");
                retVal.put(name, acv);
            }
        }
    }
    
    if(ipaTVal != null && ipaAVal != null) {
        metadataOptions.pcc_standard.setup_pcc_standard_metadata(ipaTVal, ipaAVal, retVal);
        metadataOptions.pcc_aligned.setup_pcc_aligned_metadata(record, ipaTVal, ipaAVal, retVal);
    }
    
    return retVal;
}

function searchPhones(record, ipaObj) {
    var pFilter = filters.primary;
    var res = pFilter.find_pattern(ipaObj);
    for(i = 0; i < res.length; i++) {
        var obj = res[i];
        var result = [ obj ];
        var meta = getMetadata(record, obj, null);
        results.add(result, meta, ResultType.LINEAR);
    }
}

function searchAlignedPhones(record, ipaObj) {
	// get values from primary search filter
	var pFilter = filters.primary;
	var ipaTFilter = filters.targetResultFilter;
	var ipaAFilter = filters.actualResultFilter;
	
	// check for instances of the given pattern
	var res = pFilter.find_pattern(ipaObj);
	for(i = 0; i < res.length; i++) {
	    var obj = res[i];
	    var aligned = record.getAlignedPhones(obj);
	    
	    var ipaTVal = (searchTier.index == 0 ? obj : aligned);
	    var ipaAVal = (searchTier.index == 0 ? aligned : obj);
	    
	    var addResult = true;
	    if(ipaTFilter.isUseFilter()) {
	        addResult &= ipaTFilter.check_filter(ipaTVal);
	    }
	    if(ipaAFilter.isUseFilter()) {
	        addResult &= ipaAFilter.check_filter(ipaAVal);
	    }
	    
	    if(addResult) {
	        var result = [ ipaTVal, ipaAVal ];
	        var meta = getMetadata(record, ipaTVal, ipaAVal);
	        results.add(result, meta, ResultType.ALIGNED);
	    }
	}
}
