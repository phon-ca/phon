/**
 * Code for filtering based on syllable position and stress.
 */
 
exports.SyllableFilter = function(id) {

    var sectionTitle = "Syllable Filter";
                           
	var searchBySyllableParamInfo = {
		"id": id+".searchBySyllable",
		"def": false,
		"title": "Search by syllable:",
		"desc": "",
		"enbled": true 
	};
	
	var ignoreTruncatedParamInfo = {
	    "id": id+".ignoreTruncated",
	    "def": false,
	    "title": "Truncated syllables:",
	    "desc": "Ignore results from truncated syllables"
    };
		
	var singletonParamInfo = {
		"id" : id+".sSingleton",
		"def": true,
		"title": "Singleton syllables:",
		"desc": "(words with only one syllable)" 
	};
		
	var posParamInfo = {
		"id" : [ id+".sInitial", id+".sMedial", id+".sFinal" ],
		"def": [ true, true, true ],
		"title": "Multiple syllables:",
		"desc": [ "Initial", "Medial", "Final" ],
		"numCols": 3 
	};
		
	var stressParamInfo = {
		"id": [ id+".sPrimary", id+".sSecondary", id+".sNone" ],
		"def": [ true, true, true ],
		"title": "Syllable stress:",
		"desc": [ "Primary", "Secondary", "Unstressed" ],
		"numCols": 3 
	};
	
	this.searchBySyllable = true;
	this.ignoreTruncated = false;
	this.searchBySyllableEnabled = true;
	this.searchBySyllOpt;
	
	this.sSingleton = true;
	this.sInitial = true;
	this.sMedial = true;
	this.sFinal = true;
	
	this.sPrimary = true;
	this.sSecondary = true;
	this.sNone = true;

	var singletonGroupOpt;
	var posGroupOpt;
	var stressGroupOpt;
	
	/**
	 * Setup parameters
	 * 
	 * @param params
	 */
	this.param_setup = function(params) {
		// create a new section (collapsed by default)
		var sep = new SeparatorScriptParam(sectionTitle, true); 
		params.add(sep);

        ignoreTruncatedOpt = new BooleanScriptParam(
		    ignoreTruncatedParamInfo.id,
		    ignoreTruncatedParamInfo.desc,
		    ignoreTruncatedParamInfo.title,
		    ignoreTruncatedParamInfo.def);

		// search singleton groups
		singletonGroupOpt = new BooleanScriptParam(
			singletonParamInfo.id,
			singletonParamInfo.desc,
			singletonParamInfo.title,
			singletonParamInfo.def);
		
		posGroupOpt = new MultiboolScriptParam(
			posParamInfo.id,
			posParamInfo.def,
			posParamInfo.desc,
			posParamInfo.title,
			posParamInfo.numCols);
		
		stressGroupOpt = new MultiboolScriptParam(
			stressParamInfo.id,
			stressParamInfo.def,
			stressParamInfo.desc,
			stressParamInfo.title,
			stressParamInfo.numCols);
			
		if(this.searchBySyllableEnabled) {
			var searchBySyllOpt = new BooleanScriptParam(
				searchBySyllableParamInfo.id,
				searchBySyllableParamInfo.desc,
				searchBySyllableParamInfo.title,
				searchBySyllableParamInfo.def);
			params.add(searchBySyllOpt);

			var searchBySyllCheckbox = searchBySyllOpt.getEditorComponent();
			var pf = this.patternFilter;
			var actionListener = new java.awt.event.ActionListener() {
				actionPerformed: function(e) { 
					var enabled = searchBySyllCheckbox.isSelected();
					ignoreTruncatedOpt.getEditorComponent().setEnabled(enabled);
					singletonGroupOpt.getEditorComponent().setEnabled(enabled);
					posGroupOpt.getEditorComponent().setEnabled(enabled);
					stressGroupOpt.getEditorComponent().setEnabled(enabled);
					pf.setEnabled(enabled);
				}
			};
			searchBySyllCheckbox.addActionListener(actionListener);
			
			var enabled = searchBySyllCheckbox.isSelected();
			ignoreTruncatedOpt.getEditorComponent().setEnabled(enabled);
			singletonGroupOpt.getEditorComponent().setEnabled(enabled);
			posGroupOpt.getEditorComponent().setEnabled(enabled);
			stressGroupOpt.getEditorComponent().setEnabled(enabled);
		
		    this.searchBySyllOpt = searchBySyllOpt;
		}
		
		params.add(ignoreTruncatedOpt);
		params.add(singletonGroupOpt);
		params.add(posGroupOpt);
		params.add(stressGroupOpt);
	};

	this.checkStress = function(syll) {
		var stressOk =
			(this.sNone && syll.syllableStress == "NoStress") ||
			(this.sPrimary && syll.syllableStress == "PrimaryStress") ||
			(this.sSecondary && syll.syllableStress == "SecondaryStress");
		return stressOk;
	};
	
	/**
	 * Return a list of syllables with the requested position
	 * and stress.
	 * 
	 * @param obj
	 * @return list of syllables
	 */
	this.getRequestedSyllables = function(ipaObj) {
		var retVal = new java.util.ArrayList();
		var retIdx = 0;
		
		if(ipaObj.syllables === undefined) return retIdx;
		
		var syllables = ipaObj.syllables();
		for(var sIndex = 0; sIndex < syllables.size(); sIndex++)
		{
			var syll = syllables.get(sIndex);
			var stressOk = this.checkStress(syll);

			var posOk = false;
			if(sIndex == 0 && this.sInitial) posOk = true;
			if(sIndex > 0 && sIndex < syllables.size()-1 && this.sMedial) posOk = true;
			if(sIndex == syllables.size()-1 && this.sFinal) posOk = true;
			
			// take care of singleton cases
			if(sIndex == 0 && syllables.size() == 1) posOk = this.sSingleton;
			
			var truncatedOk = true;
			if(this.ignoreTruncated) {
			    var aligned = record.getAlignedPhones(syll);
			    if(aligned.numberOfPhones == 0) truncatedOk = false;
			}
			
			if(posOk && stressOk && truncatedOk)
			{
				retVal.add(syll);
			}
		}
	
		return retVal.toArray();
	};
	
	this.filter = function(record, ipaObjs) {
	    var retVal = new Array();
	    for(var i = 0; i < ipaObjs.length; i++) {
	        var ipaObj = ipaObjs[i];
	        var sylls = this.getRequestedSyllables(record, ipaObj);
	        
	        for(var j = 0; j < sylls.length; j++) 
	            retVal = retVal.concat(sylls[j]);
	    }
	    return retVal;
	};
	
	this.isUseFilter = function() {
	    if(this.searchBySyllableEnabled) {
	        return this.searchBySyllable;
	    } else {
	        return true;
	    }
	};

};
