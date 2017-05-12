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

exports.TierList = function(id) {

	var tiersParamInfo = {
		"id": id + ".tiers",
		"title": "Tier names:",
		"prompt": "Enter tier names, separated by ','",
		"def": ""
	};
	var tiersParam;
	this.tiers = tiersParamInfo.def;

	this.param_setup = function(params) {
		tiersParam = new StringScriptParam(
		tiersParamInfo.id,
		tiersParamInfo.title,
		tiersParamInfo.def);
		tiersParam.setPrompt(tiersParamInfo.prompt);

		params.add(tiersParam);
	};

	this.getTiers = function() {
		var retVal = new Array();

		var splits = this.tiers.split(',');
		for(var i = 0; i < splits.length; i++) {
			retVal.push(splits[i].trim());
		}

		return retVal;
	};

	this.setTitle = function(title) {
		tiersParamInfo.title = title;
	};

	this.getAlignedTierData = function(record, obj, label) {
		var retVal = new java.util.TreeMap();
		if(typeof obj.getTier !== "function") return retVal;

		var extraTiers = this.getTiers();
		for(var j = 0; j < extraTiers.length; j++) {
			var tierName = extraTiers[j];
			var tierVal = null;
			if(record.hasTier(tierName)) {
				var tierVal = obj.getTier(tierName);
			} else {
				if(tierName == "Target CV") {
					var ipaT = obj.IPATarget;
					tierVal = (ipaT != null ? ipaT.cvPattern : "");
				} else if(tierName == "Actual CV") {
					var ipaA = obj.IPAActual;
					tierVal = (ipaA != null ? ipaA.cvPattern : "");
				} else if(tierName == "Target Stress") {
					var ipaT = obj.IPATarget;
					tierVal = (ipaT != null ? ipaT.stressPattern : "");
				} else if(tierName == "Actual Stress") {
					var ipaA = obj.IPAActual;
					tierVal = (ipaA != null ? ipaA.stressPattern : "");
				} else if(tierName == "Target Syllabification") {
					var ipaT = obj.IPATarget;
					tierVal = (ipaT != null ? ipaT.toString(true) : "");
				} else if(tierName == "Actual Syllabification") {
					var ipaA = obj.IPAActual;
					tierVal = (ipaA != null ? ipaA.toString(true) : "");
				}
			}
			if(tierVal != null)
				retVal.put(extraTiers[j] + " (" + label + ")", tierVal.toString());
		}

		return retVal;
	};

};
