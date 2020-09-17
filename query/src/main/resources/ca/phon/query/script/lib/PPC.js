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
	calc_ppc_aligned: function (group, filter, diacriticOptions, distortedIsCorrect) {
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
						if(distortedIsCorrect == true && 
							(phone.featureSet.hasFeature("distorted") || actualPhone.featureSet.hasFeature("distorted"))) {
							wasCorrect = true;
						} else {					
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
		"choices": ["Percent phones correct",
					"Percent consonants correct",
					"Percent vowels correct",
					"Percent singleton consonants correct",
					"Percent cluster consonants correct",
					"Percent onset consonants correct",
					"Percent coda consonants correct",
					"Percent nucleus phones correct",
					"Percent correct (custom)"],
		"phonex": [ "\\w",
					"\\c",
					"\\v",
					"(?<^\\s?)(\\c)$ || (?<^\\s?)(\\c)(?>\\v) || (?<\\v\\s?)(\\c)(?>\\s?\\v) || (?<\\v)(\\c)$",
					"see clusterTypeParamInfo",
					"see onsetTypeParamInfo",
					"see codaTypeParamInfo",
					"see nucleusTypeParamInfo"
				  ],
		"def": 0,
		"cols": 1,
		"type": "combobox"
	};
	var ppcTypeParam;
	this.ppcTypeParameter;
	this.ppcType = { index:ppcTypeParamInfo.def, toString: function() { return ppcTypeParamInfo.choices[ppcTypeParamInfo.def]; } };

	var onsetTypeParamInfo = {
	    "id": id + ".onsetType",
		"title": "Onset type:",
		"choices": ["All onsets",
					"Singleton onsets",
					"Cluster onsets"],
		"phonex": [ "\\c:O:L:E:A",
					"(?<\\S)(\\c:O:E:A)(?>.:N)",
					"(?<\\S)(\\c:L:O<2,>)(?>.:N)"
				  ],
		"def": 0,
		"cols": 0,
		"type": "radiobutton"
	};
	var onsetTypeParam;
	this.onsetTypeParameter;
	this.onsetType = { index:onsetTypeParamInfo.def, toString: function() { return onsetTypeParamInfo.choices[onsetTypeParamInfo.def]; } };
	
	var codaTypeParamInfo = {
	    "id": id + ".codaType",
		"title": "Coda type:",
		"choices": ["All codas",
					"Singleton codas",
					"Cluster codas"],
		"phonex": [ "\\c:C:R:A",
					"(?<.:N)(\\c:C:A)(?>\\S)",
					"(?<.:N)(\\c:C:R<2,>)(?>\\S)"
				  ],
		"def": 0,
		"cols": 0,
		"type": "radiobutton"
	};
	var codaTypeParam;
	this.codaTypeParameter;
	this.codaType = { index:codaTypeParamInfo.def, toString: function() { return codaTypeParamInfo.choices[codaTypeParamInfo.def]; } };
	
	var nucleusTypeParamInfo = {
	    "id": id + ".nucleusType",
		"title": "Nucleus type:",
		"choices": ["All nuclei",
					"Monophthongs",
					"Complex vocoids"],
		"phonex": [ "\\w:N",
					"(?<\\S\\c:L:O:A*)(\\w:N)(?>\\c:C:R:A*\\S)",
					"(?<\\S\\c:L:O:A*)(\\w:N<2,>)(?>\\c:C:R:A*\\S)"
				  ],
		"def": 0,
		"cols": 0,
		"type": "radiobutton"
	};
	var nucleusTypeParam;
	this.nucleusTypeParameter;
	this.nucleusType = { index:nucleusTypeParamInfo.def, toString: function() { return nucleusTypeParamInfo.choices[nucleusTypeParamInfo.def]; } };

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
		"title": "PPC-r",
		"desc": "Phones marked as 'distorted' (\u25cc\u033e) will be considered correct",
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
		
		onsetTypeParam = new EnumScriptParam(
		    onsetTypeParamInfo.id,
		    onsetTypeParamInfo.title,
		    onsetTypeParamInfo.def,
		    onsetTypeParamInfo.choices,
		    onsetTypeParamInfo.type,
		    onsetTypeParamInfo.cols);
		onsetTypeParam.setVisible(false);
		onsetTypeParam.addPropertyChangeListener(onsetTypeParamInfo.id, new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				var idx = e.source.getValue(e.source.paramId).index;
                patternFilter.setPattern(onsetTypeParamInfo.phonex[idx]);
			}
		});
		this.onsetTypeParameter = onsetTypeParam;
	    params.add(onsetTypeParam);
	    
	    codaTypeParam = new EnumScriptParam(
		    codaTypeParamInfo.id,
		    codaTypeParamInfo.title,
		    codaTypeParamInfo.def,
		    codaTypeParamInfo.choices,
		    codaTypeParamInfo.type,
		    codaTypeParamInfo.cols);
		codaTypeParam.setVisible(false);
		codaTypeParam.addPropertyChangeListener(codaTypeParamInfo.id, new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				var idx = e.source.getValue(e.source.paramId).index;
                patternFilter.setPattern(codaTypeParamInfo.phonex[idx]);
			}
		});
		this.codaTypeParameter = codaTypeParam;
	    params.add(codaTypeParam);
	    
	    nucleusTypeParam = new EnumScriptParam(
		    nucleusTypeParamInfo.id,
		    nucleusTypeParamInfo.title,
		    nucleusTypeParamInfo.def,
		    nucleusTypeParamInfo.choices,
		    nucleusTypeParamInfo.type,
		    nucleusTypeParamInfo.cols);
		nucleusTypeParam.setVisible(false);
		nucleusTypeParam.addPropertyChangeListener(nucleusTypeParamInfo.id, new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				var idx = e.source.getValue(e.source.paramId).index;
                patternFilter.setPattern(nucleusTypeParamInfo.phonex[idx]);
			}
		});
		this.nucleusTypeParameter = nucleusTypeParam;
	    params.add(nucleusTypeParam);

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

				if(idx < 8) {
    				patternFilter.setVisible(false);
				    if(idx == 4) {
				        patternFilter.setPattern(clusterTypeParamInfo.phonex[clusterTypeParam.getValue(clusterTypeParam.paramId).index]);
				    } else if(idx == 5) {
				   		patternFilter.setPattern(onsetTypeParamInfo.phonex[onsetTypeParam.getValue(onsetTypeParam.paramId).index]);
				   	} else if(idx == 6) {
				   		patternFilter.setPattern(codaTypeParamInfo.phonex[codaTypeParam.getValue(codaTypeParam.paramId).index]);
				   	} else if(idx == 7) {
				   		patternFilter.setPattern(nucleusTypeParamInfo.phonex[nucleusTypeParam.getValue(nucleusTypeParam.paramId).index]);
				    } else {
    					patternFilter.setPattern(ppcTypeParamInfo.phonex[idx]);
					}
				} else {
					patternFilter.setVisible(true);
				}
				
				clusterTypeParam.setVisible(idx == 4);
				onsetTypeParam.setVisible(idx == 5);
				codaTypeParam.setVisible(idx == 6);
				nucleusTypeParam.setVisible(idx == 7);
			}
		});

		params.add(ignoreDistortedDiacriticParam);
		params.add(diacriticOptionsParam);
		params.add(includePPCNoEpenParam);
	};

};
