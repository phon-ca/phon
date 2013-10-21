/**
 * Filter for checking aligned group values.
 */
 
var PatternFilter = require("lib/PatternFilter").PatternFilter;
 
exports.TierFilter = function(id) {

	this.patternFilter = new PatternFilter(id+".patternFilter");
	
	var tierParamInfo = {
		"id": id+".tier",
		"title": "Tier name:",
		"prompt": "Enter a single tier name",
		"def": ""
	};
	var tierParam;
	this.tier = "";

	/**
	 * Param setup
	 */
	this.param_setup = function(params) {
		tierParam = new StringScriptParam(
			tierParamInfo.id,
			tierParamInfo.title,
			tierParamInfo.def);
//		tierParam.getEditorComponent().setPrompt(tierParamInfo.prompt);
		
		params.add(tierParam);
		
		this.patternFilter.param_setup(params);
	};
	
	this.isUseFilter =  function() {
		return this.tier.length > 0 && this.patternFilter.isUseFilter();
	};
	
	this.setEnabled = function(enabled) {
	    tierParam.getEditorComponent().setEnabled(enabled);
	    this.patternFilter.setEnabled(enabled);
	};
	
	this.check_group = function(record, groupIdx) {
	    var group = record.getGroup(groupIdx);
	    var tier = group.getTier(this.tier);
	    return this.patternFilter.check_filter(tier);
	};
	
	this.filter_groups = function(record, groups) {
	    var retVal = new Array();
	    
	    for(var i = 0; i < groups.length; i++) {
	        var group = groups[i];
	        var tier = group.getTier(this.tier);
	        
	        if(this.patternFilter.check_filter(tier)) {
	            retVal.push(group);
	        }
	    }
	    
	    return retVal;
	};
	
	this.check_word = function(word) {
	    var retVal = false;
	    
	    var tierVal = word.getTier(this.tier);
	    if(tierVal) {
	        retVal = this.patternFilter.check_filter(tierVal);
	    }
	    
	    return retVal;
	};
}