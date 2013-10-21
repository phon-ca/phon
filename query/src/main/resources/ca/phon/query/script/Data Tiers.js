// param setup

var FeatureFilter = require("lib/FeatureFilter").FeatureFilter;
var GroupFilter = require("lib/GroupFilter").GroupFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var TierFilter = require("lib/TierFilter").TierFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var SyllableFilter = require("lib/SyllableFilter").SyllableFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
	
/********************************
 * Setup params
 *******************************/

var filters = {
    "primary": new TierFilter("filters.primary"),
    "group": new GroupFilter("filters.group"),
    "word": new WordFilter("filters.word"),
    "alignedWord": new AlignedWordFilter("filters.alignedWord"),
    "alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
    "speaker": new ParticipantFilter("filters.speaker")
};

function param_setup(params) {
    filters.primary.param_setup(params);
    
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
    
    filters.speaker.param_setup(params);
}

function query_record(record) {
	if(!filters.speaker.check_speaker(record.speaker)) return;
    
    var searchTier = filters.primary.tier;
	var searchObjects = filters.group.getRequestedGroups(record, searchTier);
	
	// check aligned group for each group returned
	if(filters.alignedGroup.isUseFilter()) {
	    searchObjects = filters.alignedGroup.filter_groups(record, searchObjects);
	}
	
	// word filter
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
	
	for(var i = 0; i < searchObjects.length; i++) {
	    var searchObj = searchObjects[i];
	    
	    var vals = filters.primary.patternFilter.find_pattern(searchObj);
	    
	    for(var j = 0; j < vals.length; j++) {
	        var result = [ vals[j] ];
	        results.add(result, new Metadata(), "LINEAR");
	    }
	}
}

