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

exports.Pcc = {
	/**
	 * Perform PCC (aligned) calculation for an aligned pair of
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
	calc_pc_aligned: function (group, features, ignoreDiacritics) {
		var numTarget = 0;
		var numDeleted = 0;
		var numActual = 0;
		var numEpenthesized = 0;
		var numCorrect = 0;

		var targetGroup = (group.getIPATarget() == null ? new IPATranscript(): group.getIPATarget());
		var actualGroup = (group.getIPAActual() == null ? new IPATranscript(): group.getIPAActual());
		var alignment = group.getPhoneAlignment();

		var featureSet = FeatureSet.fromArray(features.split(","));

		// check target side for numTarget, numDeleted and numCorrect
		for (pIdx = 0; pIdx < targetGroup.length();
		pIdx++) {
			var phone = targetGroup.elementAt(pIdx);

			if (phone.featureSet.intersects(featureSet)) {
				numTarget++;

				// check aligned phone
				var alignedData = alignment[ "getAligned(java.lang.Iterable)"]([phone]);
				if (alignedData.size() > 0) {
					var actualPhone = alignedData.get(0);
					if (actualPhone != null) {
						var targetPhoneString =
						(ignoreDiacritics ? (new IPATranscript([phone])).removePunctuation(true).stripDiacritics().toString(): phone.toString());
						var actualPhoneString =
						(ignoreDiacritics ? (new IPATranscript([actualPhone])).removePunctuation(true).stripDiacritics().toString(): actualPhone.toString());

						if (targetPhoneString == actualPhoneString) {
							numCorrect++;
						}
					} else {
						numDeleted++;
					}
				} else {
					numDeleted++;
				}
			}
		}

		// check actual side for numActual, numEpenthesized
		// check target side for numTarget, numDeleted and numCorrect
		for (pIdx = 0; pIdx < actualGroup.length();
		pIdx++) {
			var phone = actualGroup.elementAt(pIdx);

			if (phone.featureSet.intersects(featureSet)) {
				numActual++;

				// check aligned phone
				var alignedData = alignment[ "getAligned(java.lang.Iterable)"]([phone]);
				if (alignedData.size() > 0) {
					var targetPhone = alignedData.get(0);
					if (targetPhone == null) {
						numEpenthesized++;
					}
				} else {
					numEpenthesized++;
				}
			}
		}

		var retVal = {
			target: numTarget,
			actual: numActual,
			correct: numCorrect,
			deleted: numDeleted,
			epen: numEpenthesized
		};
		return retVal;
	},

	/**
	 * Calculates PCC (standard) for a pair of ipa transcriptions.
	 * In this version, direct phone alignment is not considered.
	 *
	 * @param word
	 * @param features
	 * @param ignoreDiacritics
	 *
	 * @return {
	 *     target: number of elements in target,
	 *     correct: number of elements correct,
	 *     epen: number of epenthesis
	 * }
	 */
	calc_pc_standard: function (group, features, ignoreDiacritics) {
		var numTarget = 0;
		var numActual = 0;
		var targetVals = new Array();
		var numCorrect = 0;
		var numEpenthesized = 0;
		var numProduced = 0;

		var targetGroup = (group.getIPATarget() == null ? new IPATranscript(): group.getIPATarget());
		var actualGroup = (group.getIPAActual() == null ? new IPATranscript(): group.getIPAActual());

		var alignment = group.getPhoneAlignment();

		var featureSet = FeatureSet.fromArray(features.split(","));

		// check target side for numTarget, numDeleted and numCorrect
		for (pIdx = 0; pIdx < targetGroup.length();
		pIdx++) {
			var phone = targetGroup.elementAt(pIdx);

			if (phone.featureSet.intersects(featureSet)) {
				numTarget++;
				var targetPhoneString =
				(ignoreDiacritics == true ? (new IPATranscript([phone])).removePunctuation(true).stripDiacritics().toString(): phone.toString());
				targetVals[targetPhoneString] =
				(targetVals[targetPhoneString] ? targetVals[targetPhoneString] + 1: 1);
			}
		}

		// check actual side for numActual, numEpenthesized
		// check target side for numTarget, numDeleted and numCorrect
		for (pIdx = 0; pIdx < actualGroup.length();
		pIdx++) {
			var phone = actualGroup.elementAt(pIdx);

			if (phone.featureSet.intersects(featureSet)) {
				numActual++;
				var actualPhoneString =
				(ignoreDiacritics == true ? (new IPATranscript([phone])).removePunctuation(true).stripDiacritics().toString(): phone.toString());

				var amountInTarget = targetVals[actualPhoneString];
				if (amountInTarget != null && amountInTarget > 0) {
					numCorrect++;
					targetVals[actualPhoneString] =-- amountInTarget;
				}
			}
		}
		if (numActual > numTarget)
		numEpenthesized = numActual - numTarget;

		numDeleted = (numTarget > numActual ? numTarget - numActual: 0);

		// format PCC string
		var retVal = {
			target: numTarget,
			actual: numActual,
			correct: numCorrect,
			deleted: numDeleted,
			epen: numEpenthesized
		};
		return retVal;
	}
};

exports.PccOptions = function (id, aligned) {

	var pcTypeParamInfo = {
		"id": id + ".pcType",
		"title": "PCC/PVC",
		"choices": ["Percent Consonants Correct (PCC)", "Percent Vowels Correct (PVC)"],
		"def": 0,
		"cols": 1,
		"type": "radiobutton"
	};
	var pcTypeParam;
	this.pcType = { index:0, toString:pcTypeParamInfo.choices[0] };
	
	var useAlignmentParamInfo = {
		"id": id + ".useAlignment",
		"title": "",
		"desc": "Include segment metathesis and migration as errors",
		"def": true,
	};
	var useAlignmentParam;
	this.useAlignment = useAlignmentParamInfo.def;
	
	var ignoreDiacriticsParamInfo = {
		"id": id +(".ignoreDiacritics"),
		"title": "",
		"desc": "Ignore diacritics",
		"def": false
	};
	var ignoreDiacriticsParam;
	this.ignoreDicacritics = ignoreDiacriticsParamInfo.def;

	this.param_setup = function (params) {
		pcTypeParam = new EnumScriptParam(
			pcTypeParamInfo.id,
			pcTypeParamInfo.title,
			pcTypeParamInfo.def,
			pcTypeParamInfo.choices,
			pcTypeParamInfo.type,
			pcTypeParamInfo.cols);
	
		useAlignmentParam = new BooleanScriptParam(
			useAlignmentParamInfo.id,
			useAlignmentParamInfo.desc,
			useAlignmentParamInfo.title,
			useAlignmentParamInfo.def);

		ignoreDiacriticsParam = new BooleanScriptParam(
			ignoreDiacriticsParamInfo.id,
			ignoreDiacriticsParamInfo.desc,
			ignoreDiacriticsParamInfo.title,
			ignoreDiacriticsParamInfo.def);

		params.add(pcTypeParam);
		params.add(ignoreDiacriticsParam);
		params.add(useAlignmentParam);
	};

};