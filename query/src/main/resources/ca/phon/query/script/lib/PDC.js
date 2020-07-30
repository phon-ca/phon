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
 * Percent Diacritics Correct (PDF) Library functions for query scripts
 */
importPackage(Packages.ca.phon.ipa.alignment)
importPackage(Packages.ca.phon.ipa.features)
importClass(Packages.ca.phon.query.script.params.DiacriticOptionsScriptParam)

var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;

exports.PDC = {
	/**
	 * Perform PDC (aligned) calculation for an aligned pair of
	 * IPA values
	 *
	 * @param word
	 * @param features
	 * @param ignoreDiacritics
	 *
	 * @return {
			target: numTarget,
			actual: numActual,
			correct: numCorrect,
			substituted: numSubstituted,
			deleted: numDeleted,
			epen: numEpenthesized
		};
	 */
	calc_pdc: function (group, filter, diacriticOptions) {
		var numTarget = 0;
		var numDeleted = 0;
		var numActual = 0;
		var numSubstituted = 0;
		var numEpenthesized = 0;
		var numCorrect = 0;

		var targetGroup = (group.getIPATarget() == null ? new IPATranscript(): group.getIPATarget());
		targetGroup = targetGroup.cover('\u25cc'.charAt(0), '\u25cc'.charAt(0));
		var actualGroup = (group.getIPAActual() == null ? new IPATranscript(): group.getIPAActual());
		actualGroup = actualGroup.cover('\u25cc'.charAt(0), '\u25cc'.charAt(0));
		var groupAlignment = group.getPhoneAlignment();
		var alignment = new PhoneMap(targetGroup, actualGroup);
		alignment.setTopAlignment(groupAlignment.getTopAlignment());
		alignment.setBottomAlignment(groupAlignment.getBottomAlignment());

		// check target side
		var targetResults = filter.find_pattern(targetGroup);
		for(var i = 0; i < targetResults.length; i++) {
			var targetResult = targetResults[i];
			var audiblePhones = targetResult.value.audiblePhones();
			
			for(var j = 0; j < audiblePhones.length(); j++) {
				var phone = audiblePhones.elementAt(j);
				if(phone.getPrefixDiacritics().length > 0 ||
					phone.getSuffixDiacritics().length > 0 ||
					phone.getCombiningDiacritics().length > 0) {
					++numTarget;						
					var alignedData = alignment["getAligned(java.lang.Iterable)"]([phone]);
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
								numCorrect++;
							} else {
								if(actualPhoneString.length() > 0)
									numSubstituted++;
								else
									numDeleted++;
							}
						} else {
							numDeleted++;
						}
					} else {
						numDeleted++;
					}
				}
			}
		}

		var actualResults = filter.find_pattern(actualGroup);
		for(var i = 0; i < actualResults.length; i++) {
			var actualResult = actualResults[i];
			var audiblePhones = actualResult.value.audiblePhones();
			
			for(var j = 0; j < audiblePhones.length(); j++) {
				var phone = audiblePhones.elementAt(j);
				if(phone.getPrefixDiacritics().length > 0 ||
					phone.getSuffixDiacritics().length > 0 ||
					phone.getCombiningDiacritics().length > 0) {
					numActual++;
					var alignedData = alignment["getAligned(java.lang.Iterable)"]([phone]);
					if(alignedData.size() == 0)
						numEpenthesized++;
				}
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

exports.PDCOptions = function (id, aligned) {

	this.pattern = new PatternFilter(id + ".pattern");

	var diacriticOptionsParamInfo = {
		"id": id +(".diacriticOptions"),
		"desc": "Diacritic Options",
		"def": false,
		"retainDia": new java.util.ArrayList()
	};
	var diacriticOptionsParam;
	this.diacriticOptions = {};
			
	this.param_setup = function (params) {	
		var patternParams = new java.util.ArrayList();
		this.pattern.setSelectedPatternType(PatternType.PHONEX);
		this.pattern.param_setup(patternParams);
		//this.pattern.setExactMatch(true);
		this.pattern.set_required(true);
		params.add(patternParams.get(1));

		diacriticOptionsParam = new DiacriticOptionsScriptParam(
			diacriticOptionsParamInfo.id,
			diacriticOptionsParamInfo.desc,
			diacriticOptionsParamInfo.def,
			diacriticOptionsParamInfo.retainDia
		);		
		params.add(diacriticOptionsParam);
	};

};
