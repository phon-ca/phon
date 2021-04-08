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
 * PNTC Library functions for query scripts
 */
importPackage(Packages.ca.phon.ipa.features)
importClass(Packages.ca.phon.query.script.params.DiacriticOptionsScriptParam)
importClass(Packages.ca.phon.syllable.SyllabificationInfo)
importClass(Packages.ca.phon.ipa.alignment.SyllableAligner)

var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;

exports.PNTC = {
	/**
	 * Perform PNTC (percent number tones correct) calculation for an aligned pair of
	 * IPA values
	 *
	 * @param word
	 * @param features
	 * @param ignoreDiacritics
	 *
	 */
	calc_pntc: function (group) {
		var numTarget = 0;
		var numDeleted = 0;
		var syllDeleted = 0;
		var numActual = 0;
		var numSubstituted = 0;
		var numEpenthesized = 0;
		var numCorrect = 0;
		var expanded_results = new Array();

		var targetGroup = (group.getIPATarget() == null ? new IPATranscript(): group.getIPATarget());
		SyllabificationInfo.setupSyllabificationInfo(targetGroup);
		var actualGroup = (group.getIPAActual() == null ? new IPATranscript(): group.getIPAActual());
		SyllabificationInfo.setupSyllabificationInfo(actualGroup);
		var alignment = group.getPhoneAlignment();
		var syllAlignment = (new SyllableAligner()).calculateSyllableAlignment(targetGroup, actualGroup, alignment);

		var targetTone = "";
		for(var i = 0; i < targetGroup.syllables().size(); i++) {
			var targetSyll = targetGroup.syllables().get(i);
			var targetScInfo = targetSyll.elementAt(0).getExtension(SyllabificationInfo);
			var targetToneNumber = (targetScInfo != null ? targetScInfo.toneNumber : "0");
			if(targetToneNumber.length() == 0) targetToneNumber = "0";
			targetTone += (targetTone.length > 0 ? "," : "") + targetToneNumber;
		}

		var actualTone = "";
		for(var i = 0; i < actualGroup.syllables().size(); i++) {
			var actualSyll = actualGroup.syllables().get(i);
			var actualScInfo = actualSyll.elementAt(0).getExtension(SyllabificationInfo);
			var actualToneNumber = (actualScInfo != null ? actualScInfo.toneNumber : "0");
			if(actualToneNumber.length() == 0) actualToneNumber = "0";
			actualTone += (actualTone.length > 0 ? "," : "") + actualToneNumber;
		}
		
		numTarget = targetGroup.syllables().size();
		numActual = actualGroup.syllables().size();
		
		for(var i = 0; i < syllAlignment.getAlignmentLength(); i++) {
			var alignedSylls = syllAlignment.getAlignedElements(i);
			
			var targetSyll = alignedSylls.get(0);
			var actualSyll = alignedSylls.get(1);
			
			var wasCorrect = false;
			var wasSub = false;
			var wasDeleted = false;
			var wasEpen = false;
			var wasSyllDeleted = false;
			
			if(targetSyll == null) {
				wasEpen = true;
			} else if(actualSyll == null) {
				wasDeleted = true;
				wasSyllDeleted = true;
			} else {
				var targetScInfo = targetSyll.elementAt(0).getExtension(SyllabificationInfo);
				var targetToneNumber = (targetScInfo != null ? targetScInfo.toneNumber : "");
				
				var actualScInfo = actualSyll.elementAt(0).getExtension(SyllabificationInfo);
				var actualToneNumber = (actualScInfo != null ? actualScInfo.toneNumber : "");
				
				if(targetToneNumber == actualToneNumber) {
					wasCorrect = true;
				} else {
					if(targetToneNumber.length() > 0 && actualToneNumber.length() == 0) {
						wasDeleted = true;
					} else {
						wasSub = true;
					}
				}
			}
			
			var expandedResult = {
				target: targetSyll,
				actual: actualSyll,
				syllDeleted: (wasSyllDeleted == true ? 1 : 0),
				correct: (wasCorrect == true ? 1 : 0),
				substituted: (wasSub == true ? 1 : 0),
				deleted: (wasDeleted == true ? 1 : 0),
				epen: (wasEpen == true ? 1 : 0)
			};
			expanded_results.push(expandedResult);
			
			if(wasCorrect == true) 
				++numCorrect;
			if(wasSub == true)
				++numSubstituted;
			if(wasDeleted == true) 
				++numDeleted;
			if(wasSyllDeleted == true)
				++syllDeleted;
			if(wasEpen == true)
				++numEpenthesized;
		}

		var retVal = {
			target: numTarget,
			actual: numActual,
			targetTone: targetTone,
			actualTone: actualTone,
			correct: numCorrect,
			substituted: numSubstituted,
			deleted: numDeleted,
			syllDeleted: syllDeleted,
			epen: numEpenthesized,
			expanded_results: expanded_results
		};
		return retVal;
	}
	
};

exports.PNTCOptions = function (id, aligned) {

	this.param_setup = function (params) {
	};

};
