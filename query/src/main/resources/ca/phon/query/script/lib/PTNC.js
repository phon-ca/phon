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
 * PTNC Library functions for query scripts
 */
importPackage(Packages.ca.phon.ipa.features)
importClass(Packages.ca.phon.query.script.params.DiacriticOptionsScriptParam)
importClass(Packages.ca.phon.syllable.SyllabificationInfo)
importClass(Packages.ca.phon.ipa.alignment.SyllableAligner)

var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;

exports.PTNC = {
	/**
	 * Perform PTNC (percent tone numbers correct) calculation for an aligned pair of
	 * IPA values
	 *
	 * @param word
	 * @param features
	 * @param ignoreDiacritics
	 *
	 */
	calc_ptnc: function (group) {
		var numTarget = 0;
		var numDeleted = 0;
		var syllDeleted = 0;
		var numActual = 0;
		var numSubstituted = 0;
		var numEpenthesized = 0;
		var numCorrect = 0;

		var targetGroup = (group.getIPATarget() == null ? new IPATranscript(): group.getIPATarget());
		SyllabificationInfo.setupSyllabificationInfo(targetGroup);
		var actualGroup = (group.getIPAActual() == null ? new IPATranscript(): group.getIPAActual());
		SyllabificationInfo.setupSyllabificationInfo(actualGroup);
		var alignment = group.getPhoneAlignment();
		var syllAlignment = (new SyllableAligner()).calculateSyllableAlignment(targetGroup, actualGroup, alignment);
		
		numTarget = targetGroup.syllables().size();
		numActual = actualGroup.syllables().size();
		
		for(var i = 0; i < syllAlignment.getAlignmentLength(); i++) {
			var alignedSylls = syllAlignment.getAlignedElements(i);
			
			var targetSyll = alignedSylls.get(0);
			var actualSyll = alignedSylls.get(1);
			
			if(targetSyll == null) {
				numEpenthesized++;
			} else if(actualSyll == null) {
				numDeleted++;
				syllDeleted++;
			} else {
				var targetScInfo = targetSyll.elementAt(0).getExtension(SyllabificationInfo);
				var targetToneNumber = (targetScInfo != null ? targetScInfo.toneNumber : "");
				
				var actualScInfo = actualSyll.elementAt(0).getExtension(SyllabificationInfo);
				var actualToneNumber = (actualScInfo != null ? actualScInfo.toneNumber : "");
				
				if(targetToneNumber == actualToneNumber) {
					numCorrect++;
				} else {
					if(targetToneNumber.length() > 0 && actualToneNumber.length() == 0) {
						numDeleted++;
					} else {
						numSubstituted++;
					}
				}
			}
		}

		var retVal = {
			target: numTarget,
			actual: numActual,
			correct: numCorrect,
			substituted: numSubstituted,
			deleted: numDeleted,
			syllDeleted: syllDeleted,
			epen: numEpenthesized
		};
		return retVal;
	}
	
};

exports.PTNCOptions = function (id, aligned) {

	this.param_setup = function (params) {
	};

};
