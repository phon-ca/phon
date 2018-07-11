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
 * Code for specifying word by position.
 */

var PatternFilter = require("lib/PatternFilter").PatternFilter;

exports.WordFilter = function (id) {

	var sectionTitle = "Word Options";

	var singletonParamInfo = {
		"id": id + ".wSingleton",
		"def": true,
		"title": "Singleton words:",
		"desc": "(groups with only one word)"
	};
	this.wSingleton = singletonParamInfo.def;

	var posParamInfo = {
		"id":[id + ".wInitial", id + ".wMedial", id + ".wFinal"],
		"def":[ true, true, true],
		"title": "Multiple words:",
		"desc":[ "Initial", "Medial", "Final"],
		"numCols": 3
	};
	this.wInitial = posParamInfo.def[0];
	this.wMedial = posParamInfo.def[1];
	this.wFinal = posParamInfo.def[2];

	this.searchByWordEnabled = true;
	var searchByWordParamInfo = {
		"id": id + ".searchByWord",
		"def": true,
		"title": "",
		"desc": "Search by word"
	};
	this.searchByWord = searchByWordParamInfo.def;
	this.searchByWordParam;
	// @deprecated - kept for backwards compatibility
	this.searchByWordOpt;

	var singletonGroupOpt;
	var posGroupOpt;

	/**
	 * Add params for the group, called automatically when needed.
	 *
	 * @param params
	 */
	this.param_setup = function (params) {
		// create a new section (collapsed by default)
		var sep = new SeparatorScriptParam(id+".sectionHeader", sectionTitle, true);
		params.add(sep);

		// search singleton groups.
		singletonGroupOpt = new BooleanScriptParam(
        		singletonParamInfo.id,
        		singletonParamInfo.desc,
        		singletonParamInfo.title,
        		singletonParamInfo.def);

		posGroupOpt = new MultiboolScriptParam(
        		posParamInfo.id,
        		posParamInfo.def,
        		posParamInfo.desc,
        		posParamInfo.title,
        		posParamInfo.numCols);

		var searchByWordOpt = new BooleanScriptParam(
        		searchByWordParamInfo.id,
        		searchByWordParamInfo.desc,
        		searchByWordParamInfo.title,
        		this.searchByWord);
		var searchByWordListener = new java.beans.PropertyChangeListener {
			propertyChange: function (e) {
				var enabled = e.source.getValue(e.source.paramId) == true;
				singletonGroupOpt.setEnabled(enabled);
				posGroupOpt.setEnabled(enabled);
				
				sep.setCollapsed(enabled == false);
			}
		};
		searchByWordOpt.addPropertyChangeListener(searchByWordOpt.paramId, searchByWordListener);
		var enabled = searchByWordOpt.getValue(searchByWordOpt.paramId) == true;
		singletonGroupOpt.setEnabled(enabled);
		posGroupOpt.setEnabled(enabled);
		if(this.searchByWordEnabled == true)
			params.add(searchByWordOpt);

        this.searchByWordOpt = searchByWordOpt;
		this.searchByWordParam = searchByWordOpt;

		params.add(singletonGroupOpt);
		params.add(posGroupOpt);
	};

	/**
	 * Returns a list of words for the given
	 * group which match the criteria in the form.
	 *
	 * @param group
	 *
	 * @return array of object conforming to the following
	 *  protocol
	 *
	 * {
	 *   wordIndex: int,
	 *   start: int,
	 *   end: int,
	 *   word: obj
	 * }
	 */
	this.getRequestedWords = function (group, tierName) {
		var retVal = new java.util.ArrayList();
		var retIdx = 0;

		tierName = tierName || (searchTier || "Orthography");

		var words = new Array();
		var wordCount = group.getWordCount(tierName);
		for (var wIndex = 0; wIndex < wordCount; wIndex++) {
			var word = group.getAlignedWord(wIndex);

			var posOk = false;
			if (wIndex == 0 && this.wInitial == true) posOk = true;
			if (wIndex > 0 && wIndex < wordCount -1 && this.wMedial == true) posOk = true;
			if (wIndex == wordCount -1 && this.wFinal == true) posOk = true;

			if (wIndex == 0 && wordCount == 1) posOk = this.wSingleton;

			if (posOk == true) {
				retVal.add(word);
			}
		}

		return retVal.toArray();
	};

	this.isUseFilter = function () {
    		return this.searchByWord == true;
	};
};