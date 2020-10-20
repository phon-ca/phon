/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
 * Feature set filter.  Will provide a textField for entering
 * a list of features.
 */

exports.FeatureFilter = function (id) {

	var featureListParamInfo = {
		"id": id + ".featureList",
		"def": "",
		"prompt": "Enter a set of features, separated by ','",
		"title": "Feature list:"
	};
	var featureListParam;
	this.featureList = featureListParamInfo.def;

	this.setFilterTitle = function (title) {
		featureListParamInfo.title = title;
	};

	var setFilterInvalid = function (textField, message, loc) {
		var msg = (loc >= 0 ?
		"Error at index " + loc + ": " + message:
		message);
	};

	var setFilterOk = function (textField) {
	};

	var checkFilter = function (filter) {
		var retVal = {
			"valid": true,
			"message": "",
			"loc": -1
		};
		if (filter == null || filter.length() == 0) return retVal;

		var features = filter.split(",");
		for (var i = 0; i < features.length; i++) {
			var feature = StringUtils.strip(features[i]);

			if (feature.startsWith("-")) {
				feature = feature.substring(1);
			}

			var validFeature = FeatureMatrix.getInstance().getFeature(feature) != null;
			retVal.valid &= validFeature;

			if (! retVal.valid) {
				retVal.message = "Unknown feature: " + feature;
				break;
			}
		}
		return retVal;
	};



	this.param_setup = function (params) {
		var featureListParam = new StringScriptParam(
		featureListParamInfo.id,
		featureListParamInfo.title,
		featureListParamInfo.def);
		featureListParam.setPrompt(featureListParamInfo.prompt);

		params.add(featureListParam);
	};

	/**
	 * Get the feature sets defined by this filter.
	 *
	 * @return an anonymous object with two properties:
	 *  'required' and 'absent'
	 */
	this.parse_features = function () {
		var requiredFeatures = new Array();
		var unwantedFeatures = new Array();

		var featureNames = this.featureList.split(",");
		for (var i = 0; i < featureNames.length; i++) {
			var feature = featureNames[i].trim();

			if (feature.length() == 0) continue;
			if (feature.startsWith("-")) {
				feature = feature.substring(1);
				unwantedFeatures.push(feature);
			} else {
				requiredFeatures.push(feature);
			}
		}

		var retVal = {
			"required": (requiredFeatures.length > 0 ? FeatureSet.fromArray(requiredFeatures) : new FeatureSet()),
			"absent": (unwantedFeatures.length > 0 ? FeatureSet.fromArray(unwantedFeatures) : new FeatureSet())
		};

		return retVal;
	},

	/**
	 * Function to test a featureset for the required
	 * features.
	 *
	 * @param featureSet
	 * @return true if the given feature set has both
	 *  the required features and does not have the unwanted
	 *  features, false otherwise
	 */
	this.check_featurset = function (featureSet) {
		var featureSets = this.parse_features();
		var requiredFeatures = featureSets.required;
		var unwantedFeatures = featureSets.absent;

		var reqIntersection = FeatureSet.intersect(featureSet, requiredFeatures);
		var unwantedIntersection = FeatureSet.intersect(featureSet, unwantedFeatures);

		return (unwantedIntersection.size() == 0 &&
		reqIntersection.equals(requiredFeatures));
	};
}