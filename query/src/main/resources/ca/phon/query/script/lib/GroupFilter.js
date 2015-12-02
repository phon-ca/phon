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
 * Filter for specifying the groups by position.
 */
var PatternFilter = require("lib/PatternFilter").PatternFilter;

exports.GroupFilter = function(id)  {
	
	var sectionTitle = "Group Filter";
	
	var singletonParamInfo = {
		"id" : id + ".gSingleton",
		"def": true,
		"title": "Singleton groups:",
		"desc": "(records with only one word group)" };
	this.gSingleton = singletonParamInfo.def;
		
	var posParamInfo = {
		"id" : [ id + ".gInitial", id+".gMedial", id+".gFinal" ],
		"def": [ true, true, true ],
		"title": "Multiple groups:",
		"desc": [ "Initial", "Medial", "Final" ],
		"numCols": 3 };
	this.gInitial = posParamInfo.def[0];
	this.gMedial = posParamInfo.def[1];
	this.gFinal = posParamInfo.def[2];
		
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
			if(gIndex == 0 && this.gInitial == true) posOk = true;
			if(gIndex > 0 && gIndex < record.numberOfGroups()-1 && this.gMedial == true) posOk = true;
			if(gIndex == record.numberOfGroups()-1 && this.gFinal == true) posOk = true;
			
			if(gIndex == 0 && record.numberOfGroups() == 1) posOk = this.gSingleton;

			if(posOk == true)
			{
			    retVal.add(group);
			}
		}
	
		return retVal.toArray();
	}
	
}

