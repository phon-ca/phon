/**
 * Code for filtering based on syllable position and stress.
 */
 
var PatternFilter = require("lib/PatternFilter").PatternFilter;

exports.SyllableFilter = function(id) {

    var sectionTitle = "Syllable Filter";
                           
	var searchBySyllableParamInfo = {
		"id": id+".searchBySyllable",
		"def": false,
		"title": "Search by syllable:",
		"desc": "",
		"enbled": true 
	};
	this.searchBySyllable = searchBySyllableParamInfo.def;
	
	var ignoreTruncatedParamInfo = {
	    "id": id+".ignoreTruncated",
	    "def": false,
	    "title": "Truncated syllables:",
	    "desc": "Ignore results from truncated syllables"
    };
	this.ignoreTruncated = ignoreTruncatedParamInfo.def;
		
	var singletonParamInfo = {
		"id" : id+".sSingleton",
		"def": true,
		"title": "Singleton syllables:",
		"desc": "(words with only one syllable)" 
	};
	this.sSingleton = singletonParamInfo.def;
		
	var posParamInfo = {
		"id" : [ id+".sInitial", id+".sMedial", id+".sFinal" ],
		"def": [ true, true, true ],
		"title": "Multiple syllables:",
		"desc": [ "Initial", "Medial", "Final" ],
		"numCols": 3 
	};
	this.sInitial = posParamInfo.def[0];
	this.sMedial = posParamInfo.def[1];
	this.sFinal = posParamInfo.def[2];
		
	var stressParamInfo = {
		"id": [ id+".sPrimary", id+".sSecondary", id+".sNone" ],
		"def": [ true, true, true ],
		"title": "Syllable stress:",
		"desc": [ "Primary", "Secondary", "Unstressed" ],
		"numCols": 3 
	};
	this.sPrimary = stressParamInfo.def[0];
	this.sSecondary = stressParamInfo.def[1];
	this.sNone = stressParamInfo.def[2];
	
    var syllableTypeExprs = [ 
    	"\\s?.:sctype(\"O|OEHS|LA\")*.:N+",
    	"\\s?.:sctype(\"O|OEHS|LA\")*.:N+.:C+.*",
    	"\\s?.:N+.*"
    ];
	
	var anySyllableTypeParamInfo = {
		"id": id+".anySyll",
		"def": true,
		"title": "Syllable type:",
		"desc": "Any syllable",
	};
	var anySyllableTypeParam;
	this.anySyll = anySyllableTypeParamInfo.def;
	
	var syllableTypeParamInfo = {
		"id": [ id+".openSylls", id+".closedSylls", id+".onsetlessSylls", id+".otherSylls" ],
		"def": [ true, true, true, true ],
		"title": "",
		"desc": ["Open", "Closed", "Onsetless","Other (specify below)"],
		"numCols": 1	
	};
	var syllableTypeParam;
	this.openSylls = syllableTypeParamInfo.def[0];
	this.closedSylls = syllableTypeParamInfo.def[1];
	this.onsetlessSylls = syllableTypeParamInfo.def[2];
	this.otherSylls = syllableTypeParamInfo.def[3];
	
	// filter for other syllables
	this.otherSyllTypePattern = new PatternFilter(id+".otherSyllTypePattern");
	var patternFilter = this.otherSyllTypePattern;
	
	this.searchBySyllableEnabled = true;
	this.searchBySyllOpt;

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
			
		if(this.searchBySyllableEnabled == true) {
			var searchBySyllOpt = new BooleanScriptParam(
				searchBySyllableParamInfo.id,
				searchBySyllableParamInfo.desc,
				searchBySyllableParamInfo.title,
				searchBySyllableParamInfo.def);
			params.add(searchBySyllOpt);

            var searchBySyllListener = new java.beans.PropertyChangeListener {
                propertyChange: function(e) {
                    var enabled = e.source.getValue(e.source.paramId) == true;
                    ignoreTruncatedOpt.setEnabled(enabled);
                    singletonGroupOpt.setEnabled(enabled);
                    posGroupOpt.setEnabled(enabled);
                    stressGroupOpt.setEnabled(enabled);
                }    
            };
            var enabled = searchBySyllOpt.getValue(searchBySyllOpt.paramId) == true;
            ignoreTruncatedOpt.setEnabled(enabled);
            singletonGroupOpt.setEnabled(enabled);
            posGroupOpt.setEnabled(enabled);
            stressGroupOpt.setEnabled(enabled);
            searchBySyllOpt.addPropertyChangeListener(searchBySyllOpt.paramId, searchBySyllListener);
            
		    this.searchBySyllOpt = searchBySyllOpt;
		}
		
		anySyllableTypeParam = new BooleanScriptParam(
			anySyllableTypeParamInfo.id,
			anySyllableTypeParamInfo.desc,
			anySyllableTypeParamInfo.title,
			anySyllableTypeParamInfo.def);
		var filterTypeParamListener = new java.beans.PropertyChangeListener {
			propertyChange: function(e) {
				var enabled = e.source.getValue(anySyllableTypeParamInfo.id) == false;
				syllableTypeParam.setEnabled(enabled);
				patternFilter.setEnabled(enabled);
			}
		};
		anySyllableTypeParam.addPropertyChangeListener(anySyllableTypeParamInfo.id, filterTypeParamListener);
		
		syllableTypeParam = new MultiboolScriptParam(
			syllableTypeParamInfo.id,
			syllableTypeParamInfo.def,
			syllableTypeParamInfo.desc,
			syllableTypeParamInfo.title,
			syllableTypeParamInfo.numCols);
		var syllableTypeParamListener = new java.beans.PropertyChangeListener {
            propertyChange: function(e) {
            	patternFilter.setEnabled(
            		e.source.getValue(syllableTypeParamInfo.id[3]));
            }
        };
        syllableTypeParam.setEnabled(this.anySyllableType == false);
		syllableTypeParam.addPropertyChangeListener(syllableTypeParamInfo.id[3], syllableTypeParamListener);
		
		params.add(ignoreTruncatedOpt);
		params.add(singletonGroupOpt);
		params.add(posGroupOpt);
		params.add(anySyllableTypeParam);
		params.add(syllableTypeParam);
		
		this.otherSyllTypePattern.param_setup(params);
		this.otherSyllTypePattern.setEnabled(this.anySyllableType == false);
		
		params.add(stressGroupOpt);
	};

	this.checkStress = function(syll) {
		var stressOk =
			(this.sNone == true && syll.syllableStress == "NoStress") ||
			(this.sPrimary == true && syll.syllableStress == "PrimaryStress") ||
			(this.sSecondary == true && syll.syllableStress == "SecondaryStress");
		return stressOk;
	};
	
	this.checkType = function(syll) {
		var typeOk = 
			(this.openSylls == true && syll.matches(syllableTypeExprs[0])) ||
			(this.closedSylls == true && syll.matches(syllableTypeExprs[1])) ||
			(this.onsetlessSylls == true && syll.matches(syllableTypeExprs[2])) ||
			(this.otherSylls == true && this.otherSyllTypePattern.isUseFilter() && this.otherSyllTypePattern.check_filter(syll));
		return typeOk;
	};
	
	/**
	 * Return a list of syllables with the requested position
	 * and stress.
	 * 
	 * @param obj
	 * @return list of syllables
	 */
	this.getRequestedSyllables = function(ipaObj, aligned) {
		var retVal = new java.util.ArrayList();
		var retIdx = 0;
		
		if(ipaObj.syllables === undefined) return retIdx;
		
		var syllables = ipaObj.syllables();
		for(var sIndex = 0; sIndex < syllables.size(); sIndex++)
		{
			var syll = syllables.get(sIndex);
			var stressOk = this.checkStress(syll);

			var posOk = false;
			if(sIndex == 0 && this.sInitial == true) posOk = true;
			if(sIndex > 0 && sIndex < syllables.size()-1 && this.sMedial == true) posOk = true;
			if(sIndex == syllables.size()-1 && this.sFinal == true) posOk = true;
			
			// take care of singleton cases
			if(sIndex == 0 && syllables.size() == 1) posOk = this.sSingleton;
			
			var truncatedOk = true;
			if(this.ignoreTruncated == true) {
				truncatedOk = (aligned != null && aligned.length() > 0);
			}
			
			var typeOk = true;
			if(this.anySyll == false) {
				typeOk = this.checkType(syll);
			}
			
			if(posOk == true && stressOk == true && truncatedOk == true && typeOk)
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
	    if(this.searchBySyllableEnabled == true) {
	        return this.searchBySyllable == true;
	    } else {
	        return true;
	    }
	};

};
