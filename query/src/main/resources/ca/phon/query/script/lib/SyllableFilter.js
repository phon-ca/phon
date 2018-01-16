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
 * Code for filtering based on syllable position and stress.
 */

var PatternFilter = require("lib/PatternFilter").PatternFilter;

exports.SyllableFilter = function (id) {

	var sectionTitle = "Syllable Options";

	var searchBySyllableParamInfo = {
		"id": id + ".searchBySyllable",
		"def": false,
		"title": "",
		"desc": "Search by syllable",
		"enbled": true
	};
	this.searchBySyllableParam;
	this.searchBySyllable = searchBySyllableParamInfo.def;

	var ignoreTruncatedParamInfo = {
		"id": id + ".ignoreTruncated",
		"def": false,
		"title": "Truncated syllables:",
		"desc": "Ignore results from truncated syllables"
	};
	this.ignoreTruncated = ignoreTruncatedParamInfo.def;

	var singletonParamInfo = {
		"id": id + ".sSingleton",
		"def": true,
		"title": "Singleton syllables:",
		"desc": "(words with only one syllable)"
	};
	this.sSingleton = singletonParamInfo.def;

	var posParamInfo = {
		"id":[id + ".sInitial", id + ".sMedial", id + ".sFinal"],
		"def":[ true, true, true],
		"title": "Multiple syllables:",
		"desc":[ "Initial", "Medial", "Final"],
		"numCols": 3
	};
	this.sInitial = posParamInfo.def[0];
	this.sMedial = posParamInfo.def[1];
	this.sFinal = posParamInfo.def[2];

	var stressParamInfo = {
		"id":[id + ".sPrimary", id + ".sSecondary", id + ".sNone"],
		"def":[ true, true, true],
		"title": "Syllable stress:",
		"desc":[ "Primary", "Secondary", "Unstressed"],
		"numCols": 3
	};
	this.sPrimary = stressParamInfo.def[0];
	this.sSecondary = stressParamInfo.def[1];
	this.sNone = stressParamInfo.def[2];

	var syllableTypeExprs =[
	".*", // any
	"\\s?.:sctype(\"O|OEHS|LA\")*.:N+", // open
	"\\s?.:sctype(\"O|OEHS|LA\")*.:N+.:C+.*", // closed
	"\\s?.:N+.*", // onset-less
	"\\s?.:N+", // open and onset-less
	"\\s?.:N+.:C+.*", // closed and onset-less
	];

	var syllableTypeParamInfo = {
		"id": id + ".syllableType",
		"title": "Syllable type:",
		"desc":[ "Any syllable", "Open", "Closed", "Onsetless", "Open and onsetless", "Closed and onsetless", "Other (specify below)"],
		"def": 0
	};
	var syllableTypeParam;
	this.syllableType = {
		"index": 0, "toString": "Any syllable"
	};

	// filter for other syllables
	this.otherSyllTypePattern = new PatternFilter(id + ".otherSyllTypePattern");
	var patternFilter = this.otherSyllTypePattern;

	this.searchBySyllableEnabled = true;
	this.searchBySyllOpt;

	var singletonGroupOpt;
	var posGroupOpt;
	var stressGroupOpt;

	/**
	 * Setup parameters
	 *
	 * @param params
	 */
	this.param_setup = function (params) {
		// create a new section (collapsed by default)
		var sep = new SeparatorScriptParam(sectionTitle, true);
		params.add(sep);

		ignoreTruncatedOpt = new BooleanScriptParam(
		ignoreTruncatedParamInfo.id,
		ignoreTruncatedParamInfo.desc,
		ignoreTruncatedParamInfo.title,
		ignoreTruncatedParamInfo.def);

		// search singleton groups
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

		stressGroupOpt = new MultiboolScriptParam(
		stressParamInfo.id,
		stressParamInfo.def,
		stressParamInfo.desc,
		stressParamInfo.title,
		stressParamInfo.numCols);

		syllableTypeParam = new EnumScriptParam(
		syllableTypeParamInfo.id,
		syllableTypeParamInfo.title,
		syllableTypeParamInfo.def,
		syllableTypeParamInfo.desc);

		if (this.searchBySyllableEnabled == true) {
			var searchBySyllOpt = new BooleanScriptParam(
			searchBySyllableParamInfo.id,
			searchBySyllableParamInfo.desc,
			searchBySyllableParamInfo.title,
			searchBySyllableParamInfo.def);
            this.searchBySyllableParam = searchBySyllOpt;
            params.add(searchBySyllOpt);
            
			var searchBySyllListener = new java.beans.PropertyChangeListener {
				propertyChange: function (e) {
					var enabled = e.source.getValue(e.source.paramId) == true;
					ignoreTruncatedOpt.setEnabled(enabled);
					singletonGroupOpt.setEnabled(enabled);
					posGroupOpt.setEnabled(enabled);
					stressGroupOpt.setEnabled(enabled);
					syllableTypeParam.setEnabled(enabled);

					patternFilter.setEnabled(enabled && syllableTypeParam.getValue(syllableTypeParamInfo.id).index == 6);
				}
			};
			var enabled = searchBySyllOpt.getValue(searchBySyllOpt.paramId) == true;
			ignoreTruncatedOpt.setEnabled(enabled);
			singletonGroupOpt.setEnabled(enabled);
			posGroupOpt.setEnabled(enabled);
			stressGroupOpt.setEnabled(enabled);
			searchBySyllOpt.addPropertyChangeListener(searchBySyllOpt.paramId, searchBySyllListener);
			syllableTypeParam.setEnabled(enabled);
			this.searchBySyllOpt = searchBySyllOpt;
		}

		params.add(ignoreTruncatedOpt);
		params.add(singletonGroupOpt);
		params.add(posGroupOpt);
		params.add(stressGroupOpt);

		var syllTypeListener = new java.beans.PropertyChangeListener {
			propertyChange: function (e) {
				var selected = e.source.getValue(e.source.paramId).index;

				patternFilter.setEnabled(selected == 6);
			}
		};
		syllableTypeParam.addPropertyChangeListener(syllTypeListener);

		params.add(syllableTypeParam);
		this.otherSyllTypePattern.param_setup(params);
		this.otherSyllTypePattern.setEnabled(false);
	};

	this.checkStress = function (syll) {
		var stressOk =
		(this.sNone == true && syll.syllableStress == "NoStress") ||
		(this.sPrimary == true && syll.syllableStress == "PrimaryStress") ||
		(this.sSecondary == true && syll.syllableStress == "SecondaryStress");
		return stressOk;
	};

	this.checkType = function (syll) {
		var selectedType = this.syllableType.index;
		var retVal = true;
		switch (selectedType) {
			case 0: // any
			break;

			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			retVal = syll.matches(syllableTypeExprs[selectedType]);
			break;

			case 6:
			retVal = (this.otherSyllTypePattern.isUseFilter() && this.otherSyllTypePattern.check_filter(syll));
			break;
		}

		return retVal;
	};

	/**
	 * Return a list of syllables with the requested position
	 * and stress.  This method works on a single IPATranscript
	 * object.
	 *
	 * @param obj
	 * @return list of syllables as IPATranscript objects
	 */
	this.getRequestedSyllables = function (ipaObj, aligned) {
		var retVal = new java.util.ArrayList();
		var retIdx = 0;

		if (ipaObj.syllables === undefined) return retIdx;

		var syllables = ipaObj.syllables();
		for (var sIndex = 0; sIndex < syllables.size();
		sIndex++) {
			var syll = syllables.get(sIndex);
			var stressOk = this.checkStress(syll);

			var posOk = false;
			if (sIndex == 0 && this.sInitial == true) posOk = true;
			if (sIndex > 0 && sIndex < syllables.size() -1 && this.sMedial == true) posOk = true;
			if (sIndex == syllables.size() -1 && this.sFinal == true) posOk = true;

			// take care of singleton cases
			if (sIndex == 0 && syllables.size() == 1) posOk = this.sSingleton;

			var truncatedOk = true;
			if (aligned != null && this.ignoreTruncated == true) {
				truncatedOk = (aligned != null && aligned.getAligned(syll.audiblePhones()).size() > 0);
			}

			var typeOk = this.checkType(syll);

			if (posOk == true && stressOk == true && truncatedOk == true && typeOk) {
				retVal.add(syll);
			}
		}

		return retVal.toArray();
	};
	
	/**
	 * Get requested aligned syllables from the given object.
	 * The object should be either a Group or AlignedWord.  The return
	 * value will be a list of AlignedSyllables.
	 * 
	 * @param obj - either a record 'ca.phon.session.Group' or 'ca.phon.session.Word'
	 * @param searchTier - 'IPA Target' (default) or 'IPA Actual'
	 * @return java.util.List of aligned syllables as defined by the filter parameters
	 */
	this.getRequestedAlignedSyllables = function (obj, searchTier) {
		var retVal = new java.util.ArrayList();
		
		if(obj == null) return retVal;
		
		var syllAlign = obj.syllableAlignment;
		var targetSylls = syllAlign.topAlignmentElements;
		var actualSylls = syllAlign.bottomAlignmentElements;
		
		var itrSylls = (searchTier == "IPA Target" ? targetSylls : actualSylls);
		var numSylls = itrSylls.size();
		
		for(var alignIdx = 0; alignIdx < obj.alignedSyllableCount; alignIdx++) {
			var alignedSyll = obj.getAlignedSyllable(alignIdx);
			
			var targetSyll = targetSylls.get(alignIdx);
			var actualSyll = actualSylls.get(alignIdx);
			
			var syll = (searchTier == "IPA Target" ? targetSyll : actualSyll);
			var otherSyll = (searchTier == "IPA Target" ? actualSyll : targetSyll);
			if(syll == null) continue;
			
			var stressOk = this.checkStress(syll);
			var typeOk = this.checkType(syll);
			
			var sIndex = itrSylls.indexOf(syll);
			var posOk = false;
			if (sIndex == 0 && this.sInitial == true) posOk = true;
			if (sIndex > 0 && sIndex < numSylls -1 && this.sMedial == true) posOk = true;
			if (sIndex == numSylls -1 && this.sFinal == true) posOk = true;

			// take care of singleton cases
			if (sIndex == 0 && numSylls == 1) posOk = this.sSingleton;
			
			var truncatedOk = true;
			if(this.ignoreTruncated == true) {
				var otherSyll = (searchTier == "IPA Target" ? actualSyll : targetSyll);
				if(otherSyll == null) truncatedOk = false;
			}
			
			if (posOk == true && stressOk == true && truncatedOk == true && typeOk) {
				retVal.add(alignedSyll);
			}		
		}
	
		return retVal;
	};

	this.filter = function (record, ipaObjs) {
		var retVal = new Array();
		for (var i = 0; i < ipaObjs.length; i++) {
			var ipaObj = ipaObjs[i];
			var sylls = this.getRequestedSyllables(record, ipaObj);

			for (var j = 0; j < sylls.length; j++)
			retVal = retVal.concat(sylls[j]);
		}
		return retVal;
	};

	this.isUseFilter = function () {
		if (this.searchBySyllableEnabled == true) {
			return this.searchBySyllable == true;
		} else {
			return true;
		}
	};
};