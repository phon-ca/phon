importPackage(Packages.ca.phon.ipa)
importPackage(Packages.ca.phon.ipa.relations)

exports.SegmentalRelationsOptions = function(id) {

	var relationTypesInfo = {
		"ids": [
				id+".includeReduplication",
				id+".includeMigration",
				id+".includeMetathesis",
				id+".includeHarmony",
				id+".includeAssimilation"
			],
		"descs": [
				  "Reduplication (X\u2205 \u2194 XX, X\u2026\u2205 \u2194 X\u2026X)",
				  "Migration (X\u2026Y \u2194 \u2205\u2026X, X\u2026\u2205 \u2194 \u2205\u2026X)",
				  "Metathesis (XY \u2194 YX, X\u2026Y \u2194 Y\u2026X)",
				  "Harmony (X\u2026Y \u2194 X\u2026X) ",
				  "Assimilation (XY \u2194 XX)"
			],
		"title": "Relations",
		"def": [true, true, true, true, true],
		"cols": 1
	};
	var relationTypesParam;
	this.includeReduplication = relationTypesInfo.def[0];
	this.includeMigration = relationTypesInfo.def[1];
	this.includeMetathesis = relationTypesInfo.def[2];
	this.includeHarmony = relationTypesInfo.def[3];
	this.includeAssimilation = relationTypesInfo.def[4];

	var directionTypesInfo = {
		"ids": [id+".includeProgressive", id+".includeRegressive"],
		"descs": ["Progressive", "Regressive"],
		"title": "Direction",
		"def": [true, true],
		"cols": 2
	};
	var directionTypesParam;
	this.includeProgressive = directionTypesInfo.def[0];
	this.includeRegressive = directionTypesInfo.def[1];

	var localityTypesInfo = {
		"ids": [id+".includeLocal", id+".includeNonlocal"],
		"descs": ["Local (XY)", "Nonlocal (X\u2026Y)"],
		"title": "Locality",
		"def": [true, true],
		"cols": 2
	};
	var localityParam;
	this.includeLocal = localityTypesInfo.def[0];
	this.includeNonlocal = localityTypesInfo.def[1];

	var includeConsonantsInfo = {
		"id": id+".includeConsonantRelations",
		"desc": "Include consonant relations",
		"title": "<html><b>Consonants</b></html>",
		"def": true,
	};
	var includeConsonantsParam;
	this.includeConsonantRelations = includeConsonantsInfo.def;

	var consonantDimensionsInfo = {
		"ids": [id+".includePlace",id+".includeManner",id+".includeVoicing"],
		"descs": ["Place", "Manner", "Voicing"],
		"title": "Consonant Dimensions",
		"def": [true, true, true],
		"cols": 2
	};
	var consonantDimensionsParam;
	this.includePlace = consonantDimensionsInfo.def[0];
	this.includeManner = consonantDimensionsInfo.def[1];
	this.includeVoicing = consonantDimensionsInfo.def[2];

	var dimensionOptionsInfo = {
		"id": id + ".dimensionsRequiredOption",
		"title": "Dimensions",
		"choices": ["Any selected dimension", "Require all selected dimensions"],
		"def": 0,
		"type": "radiobutton",
		"cols": 1
	};
	var dimensionOptionsParam;
	this.dimensionsRequiredOption = {
		"index": 0,
		"toString": dimensionOptionsInfo.choices[0]
	};

	var includeVowelsInfo = {
		"id": id+".includeVowelRelations",
		"desc": "Include vowel relations",
		"title": "<html><b>Vowels</b></html>",
		"def": false,
	};
	var includeVowelsParam;
	this.includeVowelRelations = includeVowelsInfo.def;

	var vowelDimensionsInfo = {
		"ids": [id+".includeHeight",id+".includeBackness",
				id+".includeTenseness",id+".includeRounding"],
		"descs": ["Height", "Backness", "Tenseness", "Rounding"],
		"title": "Vowel Dimensions",
		"def": [true, true, true, true],
		"cols": 2
	};
	var vowelDimensionsParam;
	this.includeHeight = vowelDimensionsInfo.def[0];
	this.includeBackness = vowelDimensionsInfo.def[1];
	this.includeTenseness = vowelDimensionsInfo.def[2];
	this.includeRounding = vowelDimensionsInfo.def[3];

	this.param_setup = function(params) {
		relationTypesParam = new MultiboolScriptParam(
			relationTypesInfo.ids,
			relationTypesInfo.def,
			relationTypesInfo.descs,
			relationTypesInfo.title,
			relationTypesInfo.cols);
		params.add(relationTypesParam);

		directionTypesParam = new MultiboolScriptParam(
			directionTypesInfo.ids,
			directionTypesInfo.def,
			directionTypesInfo.descs,
			directionTypesInfo.title,
			directionTypesInfo.cols);
		params.add(directionTypesParam);

		localityParam = new MultiboolScriptParam(
			localityTypesInfo.ids,
			localityTypesInfo.def,
			localityTypesInfo.descs,
			localityTypesInfo.title,
			localityTypesInfo.cols);
		params.add(localityParam);

		dimensionOptionsParam = new EnumScriptParam(
			dimensionOptionsInfo.id,
			dimensionOptionsInfo.title,
			dimensionOptionsInfo.def,
			dimensionOptionsInfo.choices,
			dimensionOptionsInfo.type,
			dimensionOptionsInfo.cols);
		params.add(dimensionOptionsParam);

		includeConsonantsParam = new BooleanScriptParam(
			includeConsonantsInfo.id,
			includeConsonantsInfo.desc,
			includeConsonantsInfo.title,
			includeConsonantsInfo.def);
		params.add(includeConsonantsParam);
		includeConsonantsParam.addPropertyChangeListener(includeConsonantsInfo.id, new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				consonantDimensionsParam.setEnabled(includeConsonantsParam.getValue(includeConsonantsInfo.id) == true);
			}
		});

		consonantDimensionsParam = new MultiboolScriptParam(
			consonantDimensionsInfo.ids,
			consonantDimensionsInfo.def,
			consonantDimensionsInfo.descs,
			consonantDimensionsInfo.title,
			consonantDimensionsInfo.cols);
		params.add(consonantDimensionsParam);

		includeVowelsParam = new BooleanScriptParam(
			includeVowelsInfo.id,
			includeVowelsInfo.desc,
			includeVowelsInfo.title,
			includeVowelsInfo.def);
		params.add(includeVowelsParam);
		includeVowelsParam.addPropertyChangeListener(includeVowelsInfo.id, new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				vowelDimensionsParam.setEnabled(includeVowelsParam.getValue(includeVowelsInfo.id) == true);
			}
		})

		vowelDimensionsParam = new MultiboolScriptParam(
			vowelDimensionsInfo.ids,
			vowelDimensionsInfo.def,
			vowelDimensionsInfo.descs,
			vowelDimensionsInfo.title,
			vowelDimensionsInfo.cols);
		params.add(vowelDimensionsParam);
		vowelDimensionsParam.setEnabled(this.includeVowelRelations);
	};

	this.filterRelationType = function(segmentalRelation) {
		if(segmentalRelation.relation == SegmentalRelation.Relation.Reduplication)
			return this.includeReduplication == true;
		else if(segmentalRelation.relation == SegmentalRelation.Relation.Metathesis)
			return this.includeMetathesis == true;
		else if(segmentalRelation.relation == SegmentalRelation.Relation.Migration)
			return this.includeMigration == true;
		else if(segmentalRelation.relation == SegmentalRelation.Relation.Harmony)
			return this.includeHarmony == true;
		else if(segmentalRelation.relation == SegmentalRelation.Relation.Assimilation)
			return this.includeAssimilation == true;
		else
			return false;
	};

	this.filterLocality = function(segmentalRelation) {
		if((segmentalRelation.locality == SegmentalRelation.Locality.Nonlocal && this.includeNonlocal == true)
			|| (segmentalRelation.locality == SegmentalRelation.Locality.Local && this.includeLocal == true) )
			return true;
		else
			return false;
	};

	this.filterDirection = function(segmentalRelation) {
		if((segmentalRelation.direction == SegmentalRelation.Direction.Progressive && this.includeProgressive == true)
			|| (segmentalRelation.direction == SegmentalRelation.Direction.Regressive && this.includeRegressive == true))
			return true;
		else
			return false;
	};

	this.isConsonantRelation = function(segmentalRelation) {
		return segmentalRelation.dimensions.contains(PhoneDimension.PLACE)
			|| segmentalRelation.dimensions.contains(PhoneDimension.MANNER)
			|| segmentalRelation.dimensions.contains(PhoneDimension.VOICING);
	};

	this.filterDimensions = function(segmentalRelation) {
		if(this.dimensionsRequiredOption.index == 0) {
			// already done in detectors
			return true;
		} else {
			var retVal = true;

			if(this.isConsonantRelation(segmentalRelation)) {
				if(this.includePlace == true) {
					retVal &= segmentalRelation.dimensions.contains(PhoneDimension.PLACE);
				}
				if(this.includeManner == true) {
					retVal &= segmentalRelation.dimensions.contains(PhoneDimension.MANNER);
				}
				if(this.includeVoicing == true) {
					retVal &= segmentalRelation.dimensions.contains(PhoneDimension.VOICING);
				}
			} else {
				if(this.includeHeight == true) {
					retVal &= segmentalRelation.dimensions.contains(PhoneDimension.HEIGHT);
				}
				if(this.includeBackness == true) {
					retVal &= segmentalRelation.dimensions.contains(PhoneDimension.BACKNESS);
				}
				if(this.includeTenseness == true) {
					retVal &= segmentalRelation.dimensions.contains(PhoneDimension.TENSENESS);
				}
				if(this.includeRounding == true) {
					retVal &= segmentalRelation.dimensions.contains(PhoneDimension.ROUNDING);
				}
			}

			return retVal;
		}
	};

	this.filterRelation = function(segmentalRelation) {
		return (this.filterRelationType(segmentalRelation)
					&& this.filterLocality(segmentalRelation)
					&& this.filterDirection(segmentalRelation)
					&& this.filterDimensions(segmentalRelation));
	};

	this.setupDetector = function(detector) {
		detector.setIncludePlace(this.includeConsonantRelations == true && this.includePlace == true);
		detector.setIncludeManner(this.includeConsonantRelations == true && this.includeManner == true);
		detector.setIncludeVoicing(this.includeConsonantRelations == true && this.includeVoicing == true);

		detector.setIncludeHeight(this.includeVowelRelations == true && this.includeHeight == true);
		detector.setIncludeBackness(this.includeVowelRelations == true && this.includeBackness == true);
		detector.setIncludeTenseness(this.includeVowelRelations == true && this.includeTenseness == true);
		detector.setIncludeRounding(this.includeVowelRelations == true && this.includeRounding == true);
	};

	this.createDetector = function() {
		var retVal = new SegmentalRelations(this.includeConsonantRelations == true, this.includeVowelRelations == true);

		var detector = new ReduplicationDetector();
		this.setupDetector(detector);
		retVal.addDetector(detector);

		var detector = MetathesisDetector();
		this.setupDetector(detector);
		retVal.addDetector(detector);

		var detector = new MigrationDetector();
		this.setupDetector(detector);
		retVal.addDetector(detector);

		var detector = new HarmonyDetector();
		this.setupDetector(detector);
		retVal.addDetector(detector);

		var detector = new AssimilationDetector();
		this.setupDetector(detector);
		retVal.addDetector(detector);

		return retVal;
	};

	this.createResultValue = function(phoneMap, groupIndex, isTarget, index) {
	    var rv = factory.createResultValue();
		rv.tierName = (isTarget == true ? "IPA Target" : "IPA Actual");
		rv.groupIndex = groupIndex;

		var ipaE = null;
		if(index >= 0) {
		    ipaE = (isTarget == true ? phoneMap.topAlignmentElements.get(index)
		        : phoneMap.bottomAlignmentElements.get(index));
		}

		var ipa = (isTarget == true ? phoneMap.targetRep : phoneMap.actualRep);
		stringIdx = (ipaE == null ? -1: ipa.stringIndexOfElement(ipaE));
		rv.range =
		    (stringIdx < 0 ? new Range(0, 0, true) : new Range(stringIdx, stringIdx+(ipaE.toString().length()), true));
		rv.data = (ipaE == null ? "\u2205": ipaE.text);

		return rv;
	};

	this.createQueryResult = function (recordIndex, groupIndex, relation) {
		var SCHEMA = "DETECTOR";

		var retVal = factory.createResult();
		retVal.schema = SCHEMA;
		retVal.recordIndex = recordIndex;

		var phoneMap = relation.getPhoneMap();
		var ipaT = phoneMap.targetRep;
		var ipaA = phoneMap.actualRep;

		var p1 = Math.min(relation.position1, relation.position2);
		var p2 = Math.max(relation.position1, relation.position2);

		// result values
		var rv1 = this.createResultValue(phoneMap, groupIndex, true, p1);
		var rv2 = this.createResultValue(phoneMap, groupIndex, true, p2);
		var rv3 = this.createResultValue(phoneMap, groupIndex, false, p1);
		var rv4 = this.createResultValue(phoneMap, groupIndex, false, p2);

		retVal.addResultValue(rv1);
		retVal.addResultValue(rv2);
		retVal.addResultValue(rv3);
		retVal.addResultValue(rv4);

		var metadata = retVal.metadata;
		metadata.put("Type", relation.relation.toString());
		metadata.put("Direction", relation.direction.toString());
		metadata.put("Locality", relation.locality.toString());

		var dimTxt = "";
		var featureTxt = "";
		var dimItr = relation.dimensions.iterator();
		while(dimItr.hasNext()) {
			var dim = dimItr.next();
			dimTxt += (dimTxt.length > 0 ? ", " : "") + dim;

			featureTxt += (featureTxt.length > 0 ? ", " : "")
	    		+ dim + " = " + relation.profile1.get(dim) +
	    			(relation.profile2.get(dim).size() > 0 ? " \u2194 " + relation.profile2.get(dim) : "");
		}
		metadata.put("Dimensions", dimTxt);
		metadata.put("Features", featureTxt);

		return retVal;
	};

};
