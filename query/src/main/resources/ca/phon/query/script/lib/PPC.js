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
/*
 * PCC/PVC Library functions for query scripts
 */
importPackage(Packages.ca.phon.ipa.features)

var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;

exports.PPC = {
	/**
	 * Perform PPC (aligned) calculation for an aligned pair of
	 * IPA values
	 *
	 * @param word
	 * @param features
	 * @param ignoreDiacritics
	 *
	 * @return {
	target: numTarget,
	correct: numCorrect,
	deleted: numDeleted,
	epen: numEpenthesized
	};
	 */
	calc_ppc_aligned: function (group, filter, ignoreDiacritics) {
		var numTarget = 0;
		var numDeleted = 0;
		var numActual = 0;
		var numSubstituted = 0;
		var numEpenthesized = 0;
		var numCorrect = 0;

		var targetGroup = (group.getIPATarget() == null ? new IPATranscript(): group.getIPATarget());
		var actualGroup = (group.getIPAActual() == null ? new IPATranscript(): group.getIPAActual());
		var alignment = group.getPhoneAlignment();


		// check target side
		var targetResults = filter.find_pattern(targetGroup);
		for(var i = 0; i < targetResults.length; i++) {
			var targetResult = targetResults[i];
			var audiblePhones = targetResult.value.audiblePhones();
			numTarget += audiblePhones.length();

			for(var j = 0; j < audiblePhones.length(); j++) {
				var phone = audiblePhones.elementAt(j);
				var alignedData = alignment["getAligned(java.lang.Iterable)"]([phone]);
				if (alignedData.size() > 0) {
					var actualPhone = alignedData.get(0);
					if (actualPhone != null) {
						var targetPhoneString =
						(ignoreDiacritics ? (new IPATranscript([phone])).removePunctuation(true).stripDiacritics().toString(): phone.toString());
						var actualPhoneString =
						(ignoreDiacritics ? (new IPATranscript([actualPhone])).removePunctuation(true).stripDiacritics().toString(): actualPhone.toString());

						if (targetPhoneString == actualPhoneString) {
							numCorrect++;
						} else {
							numSubstituted++;
						}
					} else {
						numDeleted++;
					}
				} else {
					numDeleted++;
				}
			}
		}

		var actualResults = filter.find_pattern(actualGroup);
		for(var i = 0; i < actualResults.length; i++) {
			var actualResult = actualResults[i];
			var audiblePhones = actualResult.value.audiblePhones();
			numActual += audiblePhones.length();

			for(var j = 0; j < audiblePhones.length(); j++) {
				var phone = audiblePhones.elementAt(j);
				var alignedData = alignment["getAligned(java.lang.Iterable)"]([phone]);
				if(alignedData.size() == 0)
					numEpenthesized++;
			}
		}

		var retVal = {
			target: numTarget,
			actual: numActual,
			correct: numCorrect,
			substituted: numSubstituted,
			deleted: numDeleted,
			epen: numEpenthesized
		};
		return retVal;
	}

};

exports.PPCOptions = function (id, aligned) {

	this.pattern = new PatternFilter(id + ".pattern");

	var ppcTypeParamInfo = {
		"id": id + ".ppcType",
		"title": "Report type:",
		"choices": ["Percent Consonants Correct (PCC)",
					"Percent Singleton Consonants Correct (PCC)",
					"Percent Cluster Consonants Correct (PCC)",
					"Percent Vowels Correct (PVC)",
					"Percent Phones Correct (PPC)",
					"Percent Correct (custom)"],
		"colnames": ["PCC", "PCC", "PCC", "PVC", "PPC", "PC"],
		"phonex": [ "\\c",
					"see singletonTypeParamInfo",
					"see clusterTypeParamInfo",
					"\\v",
					"\\w" ],
		"def": 0,
		"cols": 1,
		"type": "radiobutton"
	};
	var ppcTypeParam;
	this.ppcTypeParameter;
	this.ppcType = { index:0, toString: function() { return ppcTypeParamInfo.choices[0]; } };

	var singletonTypeParamInfo = {
	    "id": id + ".singletonType",
		"title": "Singleton type:",
		"choices": ["All singleton consonants",
					"Singleton onsets",
					"Singleton codas"],
		"phonex": [ "(?<^\\s?)(\\c)$ || (?<^\\s?)(\\c)(?>\\v) || (?<\\v\\s?)(\\c)(?>\\s?\\v) || (?<\\v)(\\c)$",
		            "(?<^\\s?)(\\c:sctype(\"Onset|OEHS\"))$ || (?<\\S)(\\c:O)(?>\\v)",
		            "(?<^\\s?)(\\c:C)$ || (?<\\v)(\\c:C)(?>\\S)" ],
		"def": 0,
		"cols": 0,
		"type": "radiobutton"
	};
	var singletonTypeParam;
	this.singletonTypeParameter;
	this.singletonType = { index:0, toString: function() { return singletonTypeParamInfo.choices[0]; } };

	var clusterTypeParamInfo = {
	    "id": id + ".clusterType",
		"title": "Cluster type:",
		"choices": ["All clusters",
					"Tautosyllabic clusters",
					"Heterosyllabic clusters"],
		"phonex": [ "(\\c<2,>)(?>\\s?\\v) || (\\c<2,>)$ || (\\c+[\\s\\.]\\c+)",
		            "(\\c:sctype(\"LeftAppendix|Onset|OEHS\")<2,>) || (\\c:sctype(\"Coda|RightAppendix\")<2,>)",
		            "(\\c+\\S\\c+)" ],
		"def": 0,
		"cols": 0,
		"type": "radiobutton"
	};
	var clusterTypeParam;
	this.clusterTypeParameter;
	this.clusterType = { index:0, toString: function() { return clusterTypeParamInfo.choices[0]; } };

	var ignoreDiacriticsParamInfo = {
		"id": id +(".ignoreDiacritics"),
		"title": "",
		"desc": "Ignore diacritics",
		"def": true
	};
	var ignoreDiacriticsParam;
	this.ignoreDicacritics = ignoreDiacriticsParamInfo.def;

	this.getColumnName = function () {
		return ppcTypeParamInfo.colnames[ppcTypeParam.getValue(ppcTypeParamInfo.id).index];
	};

	this.param_setup = function (params) {
		ppcTypeParam = new EnumScriptParam(
			ppcTypeParamInfo.id,
			ppcTypeParamInfo.title,
			ppcTypeParamInfo.def,
			ppcTypeParamInfo.choices,
			ppcTypeParamInfo.type,
			ppcTypeParamInfo.cols);
		this.ppcTypeParameter = ppcTypeParam;
		params.add(ppcTypeParam);

		singletonTypeParam = new EnumScriptParam(
		    singletonTypeParamInfo.id,
		    singletonTypeParamInfo.title,
		    singletonTypeParamInfo.def,
		    singletonTypeParamInfo.choices,
		    singletonTypeParamInfo.type,
		    singletonTypeParamInfo.cols);
		singletonTypeParam.setVisible(false);
		singletonTypeParam.addPropertyChangeListener(singletonTypeParamInfo.id, new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				var idx = e.source.getValue(e.source.paramId).index;
                patternFilter.setPattern(singletonTypeParamInfo.phonex[idx]);
			}
		});
		this.singletonTypeParameters = singletonTypeParam;
		params.add(singletonTypeParam);

		clusterTypeParam = new EnumScriptParam(
		    clusterTypeParamInfo.id,
		    clusterTypeParamInfo.title,
		    clusterTypeParamInfo.def,
		    clusterTypeParamInfo.choices,
		    clusterTypeParamInfo.type,
		    clusterTypeParamInfo.cols);
		clusterTypeParam.setVisible(false);
		clusterTypeParam.addPropertyChangeListener(clusterTypeParamInfo.id, new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				var idx = e.source.getValue(e.source.paramId).index;
                patternFilter.setPattern(clusterTypeParamInfo.phonex[idx]);
			}
		});
		this.clusterTypeParameter = clusterTypeParam;
	    params.add(clusterTypeParam);

		ignoreDiacriticsParam = new BooleanScriptParam(
			ignoreDiacriticsParamInfo.id,
			ignoreDiacriticsParamInfo.desc,
			ignoreDiacriticsParamInfo.title,
			ignoreDiacriticsParamInfo.def);

		var patternParams = new java.util.ArrayList();
		this.pattern.setSelectedPatternType(PatternType.PHONEX);
		this.pattern.param_setup(patternParams);
		//this.pattern.setExactMatch(true);
		this.pattern.set_required(true);
		params.add(patternParams.get(1));

		// setup listeners
		var patternFilter = this.pattern;
		patternFilter.setEnabled(false);
		patternFilter.setPattern("\\c");
		ppcTypeParam.addPropertyChangeListener(ppcTypeParamInfo.id, new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				var idx = e.source.getValue(e.source.paramId).index;

				if(idx < 5) {
    				patternFilter.setEnabled(false);
				    if(idx == 1) {
				        patternFilter.setPattern(singletonTypeParamInfo.phonex[singletonTypeParam.getValue(singletonTypeParam.paramId).index]);
				    } else if(idx == 2) {
				        patternFilter.setPattern(clusterTypeParamInfo.phonex[clusterTypeParam.getValue(clusterTypeParam.paramId).index]);
				    } else {
    					patternFilter.setPattern(ppcTypeParamInfo.phonex[idx]);
					}
				} else
					patternFilter.setEnabled(true);

			    switch(idx) {
			    case 1:
			        singletonTypeParam.setVisible(true);
			        clusterTypeParam.setVisible(false);
			        break;

			    case 2:
			        singletonTypeParam.setVisible(false);
			        clusterTypeParam.setVisible(true);
			        break;

			    default:
			        singletonTypeParam.setVisible(false);
			        clusterTypeParam.setVisible(false);
			    };
			}
		});

		params.add(ignoreDiacriticsParam);
	};

};