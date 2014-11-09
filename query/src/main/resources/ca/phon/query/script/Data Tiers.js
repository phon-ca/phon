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

function setup_params(params) {
    filters.primary.param_setup(params);
    filters.primary.set_required(true);
    
    filters.group.param_setup(params);
    var sep = new LabelScriptParam("", "<html><b>Aligned Group</b></html>");
    params.add(sep);
    filters.alignedGroup.param_setup(params);
    
    filters.word.param_setup(params);
    var wordsep = new LabelScriptParam("", "<html><b>Aligned Word</b></html>");
    params.add(wordsep);
    filters.alignedWord.param_setup(params);
    
    var alignedWordListener = new java.beans.PropertyChangeListener {
        propertyChange: function(e) {
            var enabled = e.source.getValue(e.source.paramId);
            filters.alignedWord.setEnabled(enabled);
        }  
    };
    filters.word.searchByWordOpt.addPropertyChangeListener(alignedWordListener);
    filters.alignedWord.setEnabled(false);

    filters.speaker.param_setup(params);
}

function query_record(recordIndex, record) {
	if(!filters.speaker.check_speaker(record.speaker)) return;
    
    var searchTier = filters.primary.tier;
	var groups = filters.group.getRequestedGroups(record);
	
	// check aligned group for each group returned
	if(filters.alignedGroup.isUseFilter()) {
	    groups = filters.alignedGroup.filter_groups(record, groups);
	}
	
	for(var gIdx = 0; gIdx < groups.length; gIdx++) {
		var group = groups[gIdx];
		
		if(filters.word.isUseFilter()) {
			var words = filters.word.getRequestedWords(group);
			
			for(var wIdx = 0; wIdx < words.length; wIdx++) {
				var word = words[wIdx];
				var checkWord = true;
				if(filters.alignedWord.isUseFilter()) {
					var aligned = word.getTier(filters.alignedWord.tier);
					checkWord = filters.alignedWord.patternFilter.check_filter(aligned);
				}
				if(checkWord == true) {
					var vals = filters.primary.patternFilter.find_pattern(word.getTier(searchTier));
					
					for(var i = 0; i < vals.length; i++) {
						var v = vals[i];
						
						var result = factory.createResult();
				        result.recordIndex = recordIndex;
				        result.schema = "LINEAR";
				        
				        var rv = factory.createResultValue();
				        rv.tierName = searchTier;
				        rv.groupIndex = group.groupIndex;
				        rv.range = new Range(v.start, v.end, false);
				        rv.data = v.value;
				        result.addResultValue(rv);
				        
				        results.addResult(result);
					}
				}
			}
		} else {
			var vals = filters.primary.patternFilter.find_pattern(group.getTier(searchTier));
			
			for(var i = 0; i < vals.length; i++) {
				var v = vals[i];
				
				var result = factory.createResult();
		        result.recordIndex = recordIndex;
		        result.schema = "LINEAR";
		        
		        var rv = factory.createResultValue();
		        rv.tierName = searchTier;
		        rv.groupIndex = group.groupIndex;
		        rv.range = new Range(v.start, v.end, false);
		        rv.data = v.value;
		        result.addResultValue(rv);
		        
		        results.addResult(result);
			}
		}
	}
}

