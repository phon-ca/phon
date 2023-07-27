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
 * List of tiers entered by the user.
 */
 
importPackage(Packages.ca.phon.query.script.params)

exports.TierList = function(id) {

	var tiersParamInfo = {
		"id": id + ".tiers",
		"title": "Tier names:",
		"prompt": "Enter tier names, separated by ','",
		"def": ""
	};
	this.tiersParam;
	this.tiers = tiersParamInfo.def;

	this.setEnabled = function(enabled) {
		this.tiersParam.setEnabled(enabled == true);
	};

	this.param_setup = function(params) {
		this.tiersParam = new TierListScriptParam(
    		tiersParamInfo.id,
    		tiersParamInfo.title,
    		tiersParamInfo.def);
    	this.tiersParam.setPrompt(tiersParamInfo.prompt);

		params.add(this.tiersParam);
	};

	this.getTiers = function() {
		var retVal = new Array();

		var splits = this.tiers.split(',');
		for(var i = 0; i < splits.length; i++) {
			var tierName = splits[i].trim();
			if(tierName.length() > 0)
				retVal.push(tierName);
		}

		return retVal;
	};

	this.setTiers = function(tiers) {
		this.tiers = tiers;
	};

	this.setTitle = function(title) {
		tiersParamInfo.title = title;
	};
	
	var coverRegex = /Cover (IPA (Target|Actual)) \(([^;]+);\s?(.+)\)/;

	/**
	 * Returns a tuple of resultValues and metadata
	 *
	 * @param record
	 * @param label to add to result value name
	 *
	 * @return {
	 *     resultValues,
	 *     metadata
	 * }
	 */
	this.getAlignedTiers = function(record, label) {
		var resultValues = new Array();
		var metadata = new java.util.LinkedHashMap();

		var tierList = this.getTiers();
		for(var i = 0; i < tierList.length; i++) {
			var tier = record.getTier(tierList[i]);
			if(tier != null) {
				var rv = factory.createResultValue();
				rv.tierName = tierList[i];
				rv.name = tierList[i] + " (" + label + ")";
				rv.data = tier.hasValue() ? tier.value : "";
				rv.range = new Range(0, rv.data.toString().length(), true);
				resultValues.push(rv);
			}
		}

		return { resultValues: resultValues, metadata: metadata };
	}

	/**
	 * Returns a tuple of resultValues and metadata.
	 *
	 * @param crossTierAlignment
	 * @param topElement
	 * @param label to add to result value name
	 *
	 * @return {
	 *     resultValues,
	 *     metadata
	 * }
	 */
	this.getAlignedTierData = function(crossTierAlignment, topElement, label) {
		var resultValues = new Array();
		var metadata = new java.util.LinkedHashMap();
		
		var extraTiers = this.getTiers()
		var alignedElementMap = crossTierAlignment.getAlignedElements(topElement);
		for(var j = 0; j < extraTiers.length; j++) {
			var alignedTierName = extraTiers[j];
			var alignedTierVal = alignedElementMap.get(alignedTierName);
			if(alignedTierVal != null) {
				var tierResultValue = factory.createResultValue();
				tierResultValue.name = alignedTierName + " ("  + label + ")";
				tierResultValue.tierName = alignedTierName;
				tierResultValue.data = alignedTierVal || "";

				var startIndex = 0;
				var length = (alignedTierVal ? alignedTierVal.toString().length() : 0);
				var tierAlignment = crossTierAlignment.getTierAlignment(alignedTierName);
				var alignedTier = tierAlignment.getBottomTier();
				if(alignedTierVal != null && alignedTier != null) {
					var wordOffset = 0;

					if(alignedTier.declaredType === Orthography) {
						wordOffset = alignedTier.value.stringIndexOf(alignedTierVal);
					} else if(alignedTier.declaredType === IPATranscript) {
						wordOffset = alignedTier.value.stringIndexOf(alignedTierVal);
					} else if(alignedTier.declaredType === PhoneAlignment) {
						// TODO
						wordOffset = 0;
					} else if(alignedTier.declaredType === UserTierData) {
						// TODO
						wordOffset = 0;
					} else {
						// unknown tier type
					}

					startIndex += wordOffset;
				}

				tierResultValue.range = new Range(startIndex, startIndex + length, true);
				resultValues.push(tierResultValue);
			} else {
				if(alignedTierName == "Phone Alignment") {
					var align = obj.phoneAlignment;
					alignedTierVal = (align != null ? align.toString(false) : "");
				} else if(alignedTierName == "Target CV") {
					var ipaT = obj.IPATarget;
					alignedTierVal = (ipaT != null ? ipaT.cvPattern : "");
				} else if(alignedTierName == "Actual CV") {
					var ipaA = obj.IPAActual;
					alignedTierVal = (ipaA != null ? ipaA.cvPattern : "");
				} else if(alignedTierName == "Target Stress") {
					var ipaT = obj.IPATarget;
					alignedTierVal = (ipaT != null ? ipaT.stressPattern : "");
				} else if(alignedTierName == "Actual Stress") {
					var ipaA = obj.IPAActual;
					alignedTierVal = (ipaA != null ? ipaA.stressPattern : "");
				} else if(alignedTierName == "Target Syllabification") {
					var ipaT = obj.IPATarget;
					alignedTierVal = (ipaT != null ? ipaT.toString(true) : "");
				} else if(alignedTierName == "Actual Syllabification") {
					var ipaA = obj.IPAActual;
					alignedTierVal = (ipaA != null ? ipaA.toString(true) : "");
				} else if(alignedTierName.match(coverRegex)) {
				    var groupData = coverRegex.exec(alignedTierName);
				    
				    var phonTier = groupData[1].trim();
				    var reportTier = groupData[3].trim();
				    var symbolMap = groupData[4].trim();
	
	                var ipa = obj.getTier(phonTier);
	                alignedTierVal = (ipa != null ? ipa.cover(symbolMap) : "");
	                
	                alignedTierName = reportTier;
				}
				if(alignedTierVal != null)
					metadata.put(alignedTierName + " (" + label + ")", alignedTierVal.toString());
			}
		}

		return { resultValues: resultValues, metadata: metadata }
	};

};
