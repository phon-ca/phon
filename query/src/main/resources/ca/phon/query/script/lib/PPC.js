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
importClass(Packages.ca.phon.query.script.params.DiacriticOptionsScriptParam)

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
		substituted: numSubstituted,
		deleted: numDeleted,
		epen: numEpenthesized
		expanded_results: array of expanded results set based on filter
		{
			target: targetIdx,
			actual: actualIdx,
			correct: (1|0),
			substituted: (1|0),
			deleted: (1|0),
			epen: (1|0)
		}
	};
	 */
	calc_ppc_aligned: function (group, filter, diacriticOptions) {
		var numTarget = 0;
		var numDeleted = 0;
		var numActual = 0;
		var numSubstituted = 0;
		var numEpenthesized = 0;
		var numCorrect = 0;

		var targetGroup = (group.getIPATarget() == null ? new IPATranscript(): group.getIPATarget());
		var actualGroup = (group.getIPAActual() == null ? new IPATranscript(): group.getIPAActual());
		var alignment = group.getPhoneAlignment();

		var expanded_results = new Array();
		// check target side
		var targetResults = filter.find_pattern(targetGroup);
		for(var i = 0; i < targetResults.length; i++) {
			var targetResult = targetResults[i];
			var audiblePhones = targetResult.value.audiblePhones();
			numTarget += audiblePhones.length();

			for(var j = 0; j < audiblePhones.length(); j++) {
				var phone = audiblePhones.elementAt(j);
				var alignedData = alignment["getAligned(java.lang.Iterable)"]([phone]);
				
				var wasCorrect = false;
				var wasSub = false;
				var wasDeleted = false;
				
				if (alignedData.size() > 0) {
					var actualPhone = alignedData.get(0);
					if (actualPhone != null) {
						var targetPhoneString =
							(diacriticOptions.ignoreDiacritics == true 
								? this.strip_diacritics(new IPATranscript([phone]), diacriticOptions.selectionMode, diacriticOptions.selectedDiacritics).toString()
								: phone.toString());
						var actualPhoneString =
							(diacriticOptions.ignoreDiacritics == true 
								? this.strip_diacritics(new IPATranscript([actualPhone]), diacriticOptions.selectionMode, diacriticOptions.selectedDiacritics).toString()
								: actualPhone.toString());

						if (targetPhoneString == actualPhoneString) {
							wasCorrect = true;
						} else {
							wasSub = true;
						}
					} else {
						wasDeleted = true;
					}
					
					var expandedResult = {
						target: (phone != null ? targetGroup.indexOf(phone) : -1),
						actual: (actualPhone != null ? actualGroup.indexOf(actualPhone) : -1),
						correct: (wasCorrect == true ? 1 : 0),
						substituted: (wasSub == true ? 1 : 0),
						deleted: (wasDeleted == true ? 1 : 0),
						epen: 0
					};
					expanded_results.push(expandedResult);
				} else {
					wasDeleted = true;
					var expandedResult = {
						target: (phone != null ? targetGroup.indexOf(phone) : -1),
						actual: -1,
						correct: 0,
						substituted: 0,
						deleted: 1,
						epen: 0
					};
					expanded_results.push(expandedResult);
				}
				
				if(wasCorrect == true)
					numCorrect++;
				if(wasSub == true)
					numSubstituted++;
				if(wasDeleted == true)
					numDeleted++;
				
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
				if(alignedData.size() == 0) {
					numEpenthesized++;

					var expandedResult = {
						target: -1,
						actual: (phone != null ? actualGroup.indexOf(phone) : -1),
						correct: 0,
						substituted: 0,
						deleted: 0,
						epen: 1
					};
					expanded_results.push(expandedResult);
				}
			}
		}

		var retVal = {
			target: numTarget,
			actual: numActual,
			correct: numCorrect,
			substituted: numSubstituted,
			deleted: numDeleted,
			epen: numEpenthesized,
			expanded_results: expanded_results
		};
		return retVal;
	},
	
	/**
	 * Strip diacritics using given diacritic options.
     * @param 
     */
	strip_diacritics: function (ipa, selectionMode, selectedDiacritics) {
		if("except" == selectionMode.toLowerCase()) {
			return ipa.stripDiacriticsExcept(selectedDiacritics);
		} else if("only" == selectionMode.toLowerCase()) {
			return ipa.stripDiacritics(selectedDiacritics);
		} else {
			return ipa;
		}
	}
};

exports.PPCOptions = function (id, aligned) {

	this.pattern = new PatternFilter(id + ".pattern");

	var ppcTypeParamInfo = {
		"id": id + ".ppcType",
		"title": "Report type:",
		"choices": ["Percent Consonants Correct",
					"Percent Singleton Consonants Correct",
					"Percent Cluster Consonants Correct",
					"Percent Vowels Correct",
					"Percent Phones Correct",
					"Percent Correct (custom)"],
		"colnames": ["PPC", "PPC", "PPC", "PPC", "PPC", "PPC"],
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
	this.ppcType = { index:ppcTypeParamInfo.def, toString: function() { return ppcTypeParamInfo.choices[ppcTypeParamInfo.def]; } };

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
	this.singletonType = { index:singletonTypeParamInfo.def, toString: function() { return singletonTypeParamInfo.choices[singletonTypeParamInfo.def]; } };

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
	this.clusterType = { index:clusterTypeParamInfo.def, toString: function() { return clusterTypeParamInfo.choices[clusterTypeParamInfo.def]; } };

	var ignoreDistortedDiacriticParamInfo = {
		"id": id +(".ignoreDistortedDiacritic"),
		"title": "Ignore distorted diacritic (\u25cc\u033e)",
		"desc": "Productions marked as 'distorted' will be considered correct",
		"def": false
	};
	var ignoreDistoredDiacriticParam;
	this.ignoreDistortedDiacriticParameter;
	this.ignoreDistortedDiacritic = ignoreDistortedDiacriticParamInfo.def;

	var diacriticOptionsParamInfo = {
		"id": id +(".diacriticOptions"),
		"desc": "Diacritic Options",
		"def": false,
		"retainDia": new java.util.ArrayList()
	};
	var diacriticOptionsParam;
	this.diacriticOptions = {};
			
	var includePPCNoEpenParamInfo = {
		"id": id +(".includePPCNoEpen"),
		"title": "Include alternate PPC calculation",
		"desc": "Include PPC w/o epenthesis 'PPC (NoEpen)'",
		"def": false
	};
	var includePPCNoEpenParam;
	this.includePPCNoEpen = includePPCNoEpenParamInfo.def;

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
	
		diacriticOptionsParam = new DiacriticOptionsScriptParam(
			diacriticOptionsParamInfo.id,
			diacriticOptionsParamInfo.desc,
			diacriticOptionsParamInfo.def,
			diacriticOptionsParamInfo.retainDia
		);
		
		ignoreDistortedDiacriticParam = new BooleanScriptParam(
			ignoreDistortedDiacriticParamInfo.id,
			ignoreDistortedDiacriticParamInfo.desc,
			ignoreDistortedDiacriticParamInfo.title,
			ignoreDistortedDiacriticParamInfo.def);
		this.ignoreDistortedDiacriticParameter = ignoreDistortedDiacriticParam;
		ignoreDistortedDiacriticParam.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				if(e.getNewValue() == true) {
					diacriticOptionsParam.setIgnoreDiacritics(true);
					diacriticOptionsParam.setSelectionMode(Packages.ca.phon.query.script.params.DiacriticOptionsScriptParam.SelectionMode.ONLY);
					diacriticOptionsParam.setSelectedDiacritics(java.util.List.of((new IPAElementFactory()).createDiacritic(FeatureMatrix.getInstance().getCharactersWithFeature("distorted").get(0))));
					diacriticOptionsParam.setEnabled(false);
				} else {
					diacriticOptionsParam.setIgnoreDiacritics(false);
					diacriticOptionsParam.clearSelectedDiacritics();
					diacriticOptionsParam.setEnabled(true);
				}
			}
		});
		
		includePPCNoEpenParam = new BooleanScriptParam(
			includePPCNoEpenParamInfo.id,
			includePPCNoEpenParamInfo.desc,
			includePPCNoEpenParamInfo.title,
			includePPCNoEpenParamInfo.def);
	
		var patternParams = new java.util.ArrayList();
		this.pattern.setSelectedPatternType(PatternType.PHONEX);
		this.pattern.param_setup(patternParams);
		//this.pattern.setExactMatch(true);
		this.pattern.set_required(true);
		params.add(patternParams.get(1));

		// setup listeners
		var patternFilter = this.pattern;
		patternFilter.setVisible(ppcTypeParamInfo.def == 5);
		patternFilter.setPattern(ppcTypeParamInfo.phonex[ppcTypeParamInfo.def]);
		ppcTypeParam.addPropertyChangeListener(ppcTypeParamInfo.id, new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				var idx = e.source.getValue(e.source.paramId).index;

				if(idx < 5) {
    				patternFilter.setVisible(false);
				    if(idx == 1) {
				        patternFilter.setPattern(singletonTypeParamInfo.phonex[singletonTypeParam.getValue(singletonTypeParam.paramId).index]);
				    } else if(idx == 2) {
				        patternFilter.setPattern(clusterTypeParamInfo.phonex[clusterTypeParam.getValue(clusterTypeParam.paramId).index]);
				    } else {
    					patternFilter.setPattern(ppcTypeParamInfo.phonex[idx]);
					}
				} else {
					patternFilter.setVisible(true);
				}

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

		params.add(ignoreDistortedDiacriticParam);
		params.add(diacriticOptionsParam);
		params.add(includePPCNoEpenParam);
	};

};
