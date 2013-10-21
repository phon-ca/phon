/*
 * Code for specifying the groups by position.
 */

var PatternFilter = require("lib/PatternFilter").PatternFilter;

exports.GroupFilter = function(id)  {
	
	var sectionTitle = "Group Filter";
	
	var singletonParamInfo = {
		"id" : id + ".gSingleton",
		"def": true,
		"title": "Singleton groups:",
		"desc": "(records with only one word group)" };
		
	var posParamInfo = {
		"id" : [ id + ".gInitial", id+".gMedial", id+".gFinal" ],
		"def": [ true, true, true ],
		"title": "Multiple groups:",
		"desc": [ "Initial", "Medial", "Final" ],
		"numCols": 3 };
	
	this.gSingleton = true;
	this.gInitial = true;
	this.gMedial = true;
	this.gFinal = true;
		
	/**
	 * Add params for the group, called automatically when needed.
	 *
	 * @param params a list of ScriptParam objects
	 */
	this.param_setup = function(params) {
		// create a new section (collapsed by default)
		var sep = new SeparatorScriptParam(sectionTitle, true); 
		
		// search singleton groups
		var singletonGroupOpt = new BooleanScriptParam(
			singletonParamInfo.id,
			singletonParamInfo.desc,
			singletonParamInfo.title,
			singletonParamInfo.def);
		
		var posGroupOpt = new MultiboolScriptParam(
			posParamInfo.id,
			posParamInfo.def,
			posParamInfo.desc,
			posParamInfo.title,
			posParamInfo.numCols);
		
		params.add(sep);
		params.add(singletonGroupOpt);
		params.add(posGroupOpt);
	}

	/**
	 * Returns a list of groups for the given
	 * tier which match the criteria given in the form.
	 *
	 * @param record 
	 *
	 * @return array of group objects.  Each value of the array
	 *  will be of type ca.phon.session.Group
	 */
	this.getRequestedGroups = function(record) {
		var retVal = new java.util.ArrayList();
		//if(!(record instanceof Record)) return retVal;
		
		var retIdx = 0;
	
		for(var gIndex = 0; gIndex < record.numberOfGroups(); gIndex++)
		{
			var group = record.getGroup(gIndex);

			var posOk = false;
			if(gIndex == 0 && this.gInitial) posOk = true;
			if(gIndex > 0 && gIndex < record.numberOfGroups()-1 && this.gMedial) posOk = true;
			if(gIndex == record.numberOfGroups()-1 && this.gFinal) posOk = true;
			
			if(gIndex == 0 && record.numberOfGroups() == 1) posOk = this.gSingleton;

			if(posOk)
			{
			    retVal.add(group);
			}
		}
	
		return retVal.toArray();
	}
	
}

