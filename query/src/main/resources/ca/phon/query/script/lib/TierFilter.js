/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	this.tier = tierParamInfo.def;

	/**
	 * Param setup
	 */
	this.param_setup = function(params) {
		tierParam = new StringScriptParam(
			tierParamInfo.id,
			tierParamInfo.title,
			tierParamInfo.def);
	    tierParam.setPrompt(tierParamInfo.prompt);
		
		params.add(tierParam);
		
		this.patternFilter.param_setup(params);
	};
	
	this.isUseFilter =  function() {
		return this.tier.length() > 0 && this.patternFilter.isUseFilter();
	};
	
	this.setEnabled = function(e) {
	    var enabled = (e == true);
	    tierParam.setEnabled(enabled);
	    this.patternFilter.setEnabled(enabled);
	};
	
	this.setVisible = function(v) {
	    var visible = (v == true);
	    tierParam.setVisible(visible);
	    this.patternFilter.setVisible(visible);
	};
	
	this.check_group = function(record, groupIdx) {
	    var group = record.getGroup(groupIdx);
	    var tier = group.getTier(this.tier);
	    return this.patternFilter.check_filter(tier);
	};
	
	this.set_required = function(required) {
		tierParam.setRequired(true);
		this.patternFilter.set_required(true);
	}
	
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