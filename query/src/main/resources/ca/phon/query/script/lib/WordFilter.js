/*
 * Code for specifying word by position.
 */
 
var PatternFilter = require("lib/PatternFilter").PatternFilter;

exports.WordFilter = function(id) {

	var sectionTitle = "Word Filter";
	
	var singletonParamInfo = {
		"id" : id+".wSingleton",
		"def": true,
		"title": "Singleton words:",
		"desc": "(groups with only one word)" };
		
	var posParamInfo = {
		"id" : [ id+".wInitial", id+".wMedial", id+".wFinal" ],
		"def": [ true, true, true ],
		"title": "Multiple words:",
		"desc": [ "Initial", "Medial", "Final" ],
		"numCols": 3 };
		
	var searchByWordParamInfo = {
		"id": id+".searchByWord",
		"def": false,
		"title": "Search by word:",
		"desc": ""
	};
	
	this.wSingleton = true;
	this.wInitial = true;
	this.wMedial = true;
	this.wFinal = true;
	
	this.searchByWord = true;
	this.searchByWordEnabled = true;
	this.searchByWordOpt;
	
	var singletonGroupOpt;
	var posGroupOpt;
	
		
	/**
	 * Add params for the group, called automatically when needed.
	 *
	 * @param params
	 */
	this.param_setup = function(params) {
		// create a new section (collapsed by default)
		var sep = new SeparatorScriptParam(sectionTitle, true); 
		params.add(sep);
		
		// search singleton groups.
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
		
		if(this.searchByWordEnabled) {
			var searchByWordOpt = new BooleanScriptParam(
				searchByWordParamInfo.id,
				searchByWordParamInfo.desc,
				searchByWordParamInfo.title,
				searchByWordParamInfo.def);
			params.add(searchByWordOpt);
			
//			var searchByWordCheckbox = searchByWordOpt.getEditorComponent();
//			var actionListener = new java.awt.event.ActionListener() {
//				actionPerformed: function(e) { 
//					var enabled = searchByWordCheckbox.isSelected();
//					singletonGroupOpt.getEditorComponent().setEnabled(enabled);
//					posGroupOpt.getEditorComponent().setEnabled(enabled);
//				}
//			};
//			searchByWordCheckbox.addActionListener(actionListener);
//			
//			var enabled = searchByWordCheckbox.isSelected();
//			singletonGroupOpt.getEditorComponent().setEnabled(enabled);
//			posGroupOpt.getEditorComponent().setEnabled(enabled);
			
			this.searchByWordOpt = searchByWordOpt;
		}
		
		params.add(singletonGroupOpt);
		params.add(posGroupOpt);
	};
	
	/**
	 * Returns a list of words for the given
	 * group which match the criteria in the form.
	 *
	 * @param group
	 *
	 * @return array of object conforming to the following
	 *  protocol
	 * 
	 * {
	 *   wordIndex: int,
	 *   start: int,
	 *   end: int,
	 *   word: obj 
	 * }
	 */
	this.getRequestedWords = function(group) {
		var retVal = new java.util.ArrayList();
		var retIdx = 0;
		
		var words = new Array();
		var wordCount = group.alignedWordCount;
		for(var wIndex = 0; wIndex < wordCount; wIndex++)
		{
			var word = group.getAlignedWord(wIndex);

			var posOk = false;
			if(wIndex == 0 && this.wInitial) posOk = true;
			if(wIndex > 0 && wIndex < wordCount-1 && this.wMedial) posOk = true;
			if(wIndex == wordCount-1 && this.wFinal) posOk = true;

            if(wIndex == 0 && wordCount == 1) posOk = this.wSingleton;

			if(posOk)
			{
				retVal.add(word);
			}
		}
	
		return retVal.toArray();
	};
	
	
	this.isUseFilter = function() {
	    if(this.searchByWordEnabled) {
	        return this.searchByWord;
	    } else {
	        return true;
	    }
	};
	

};
