/**
 * Options for filtering searched records based on
 * participant information.
 */

exports.ParticipantFilter = function(id) {

    // form setup
    var sectionTitle = "Participant Filter";
	
	var participantNamesParamInfo = {
		"id": id+".participantNames",
		"title": "Participant names:",
		"prompt": "Separate names with a ','",
        "def": ""
    };
	var participantNamesParam;
	this.participantNames = "";
	
	var age1ParamInfo = {
		"id": id+".age1String",
		"title": "",
		"prompt": "yy;mm.dd",
		"def": ""
	};
	var age1Param;
	this.age1String;
	
	var age2ParamInfo = {
		"id": id+".age2String",
		"title": "",
		"prompt": "yy;mm.dd",
		"def": ""
	};
	var age2Param;
	this.age2String;
	
	var age1ComparatorParamInfo = {
		"id": id+".age1Comparator",
		"title": "Age:",
		"desc": [ "less than", "equal to", "greater than" ],
		"def": 1
	};
	var age1ComparatorParam;
	this.age1Comparator = {
	    index: 1
	};
	
	var age2ComparatorParamInfo = {
		"id": id+".age2Comparator",
		"title": "Age:",
		"desc": [ "less than", "equal to", "greater than" ],
		"def": 1
	};
	var age2ComparatorParam;
	this.age2Comparator = {
	    index: 1
	};
	
	var ageOperatorParamInfo = {
	    "id": id+".ageOperator",
	    "title": "",
	    "desc": [ "(select operator)", "and", "or" ],
	    "def": 0
	};
	var ageOperatorParam;
	this.ageOperator = {
	    index: 0
	};
	
	var agePattern = /^([0-9]{2});([0-9]{2})\.([0-9]{2})$/;
	
	/**
	 * Check age filter text.
	 *
	 * @param filter
	 * @return true if text matches age pattern,
	 *  false otherwise
	 */
    var checkAgeFilter = function(filter) {
        if(filter == undefined || filter.length == 0) return true;
        return filter.match(agePattern);
    };

	this.isUseNameFilter = function() {
		return (this.participantNames != undefined && this.participantNames.length > 0);
	};
	
	this.isUseAge1Filter = function() {
	    return this.age1String && this.age1String.length > 0 && checkAgeFilter(this.age1String);
	};
	
	this.isUseAge2Filter = function() {
	    var opSelected = this.ageOperator.index > 0;
	    return this.isUseAge1Filter() && opSelected && checkAgeFilter(this.age2String);
	};
	
	var setFieldInvalid = function(textField, message, loc) {
	    var msg = (loc >= 0 ?
            "Error at index " + loc  +": " + messgae :
            message);
       
        textField.setToolTipText(message);
        textField.setState("UNDEFINED");
	};
	
	var setFieldOk = function(textField) {
	    textField.setToolTipText("");
	    textField.setState("INPUT");
	};
	
	/** 
	 * Add params to form.
	 */
	this.param_setup = function(params) {
	    // create a new section (collapsed by default)
		var sep = new SeparatorScriptParam(sectionTitle, true); 
		params.add(sep);
		
		participantNamesParam = new StringScriptParam(
		    participantNamesParamInfo.id,
		    participantNamesParamInfo.title,
		    participantNamesParamInfo.def);
	    // set tooltip for text field
	    participantNamesParam.getEditorComponent().setPrompt(
	        participantNamesParamInfo.prompt);
	    params.add(participantNamesParam);   
	    
	    age1Param = new StringScriptParam(
	        age1ParamInfo.id,
	        age1ParamInfo.title,
	        age1ParamInfo.def);
	    age1Param.getEditorComponent().setPrompt(
	        age1ParamInfo.prompt);
	    
	    age2Param = new StringScriptParam(
	        age2ParamInfo.id,
	        age2ParamInfo.title,
	        age2ParamInfo.def);
	    age2Param.getEditorComponent().setPrompt(
	        age2ParamInfo.prompt);
	    age2Param.getEditorComponent().setEnabled(false);
	    
	    age1ComparatorParam = new EnumScriptParam(
	        age1ComparatorParamInfo.id,
	        age1ComparatorParamInfo.title,
	        age1ComparatorParamInfo.def,
	        age1ComparatorParamInfo.desc);
	        
	    age2ComparatorParam = new EnumScriptParam(
	        age2ComparatorParamInfo.id,
	        age2ComparatorParamInfo.title,
	        age2ComparatorParamInfo.def,
	        age2ComparatorParamInfo.desc);
	    age2ComparatorParam.getEditorComponent().setEnabled(false);
	    
	    ageOperatorParam = new EnumScriptParam(
	        ageOperatorParamInfo.id,
	        ageOperatorParamInfo.title,
	        ageOperatorParamInfo.def,
	        ageOperatorParamInfo.desc);
	    var ageOperatorListener = new java.awt.event.ItemListener() {
	        itemStateChanged: function(e) {
                var idx = e.getSource().getSelectedIndex();
                var enabled = (idx > 0);
                age2ComparatorParam.getEditorComponent().setEnabled(enabled);
                age2Param.getEditorComponent().setEnabled(enabled);
            }
	    };
	    ageOperatorParam.getEditorComponent().addItemListener(ageOperatorListener);
	    
	    
	    var ageValidatorListener = new java.awt.event.KeyListener() {
	        keyReleased: function(e) {
	            var textField = e.getSource();
	            var txt = textField.getText();
                
                if(!checkAgeFilter(txt)) {
                    setFieldInvalid(textField, "", -1);
                } else {
                    setFieldOk(textField);
                }
	        },
	        
	        keyPressed: function(e) {},
	        
	        keyTyped: function(e) {}
	    };
	    age1Param.getEditorComponent().addKeyListener(ageValidatorListener);
	    age2Param.getEditorComponent().addKeyListener(ageValidatorListener);
	    
	    params.add(age1ComparatorParam);
	    params.add(age1Param);
	    params.add(ageOperatorParam);
	    params.add(age2ComparatorParam);
	    params.add(age2Param);
	};
	
	this.checkSpeakerAge = function(speaker) {
    	if(speaker == null || speaker.ageTo == null)
    		return false;
    
    	var age1Result = false;
    	var age2Result = false;
    	
    	var speakerPeriod = speaker.ageTo;
    	var ageFormatter = new Packages.org.joda.time.format.PeriodFormatterBuilder()
    	        .minimumPrintedDigits(2)
                .appendYears()
    	        .appendSuffix(";")
    	        .minimumPrintedDigits(2)
    	        .appendMonths()
    	        .appendSuffix(".")
    	        .minimumPrintedDigits(2)
    	        .appendDays()
    	        .toFormatter();
    	var speakerAge = ageFormatter.print(speakerPeriod);
    	
    	// perform first expression
    	if(this.age1Comparator.index == 0) {
    		age1Result = speakerAge < this.age1String;
    	} else if(this.age1Comparator.index == 1) {
    		age1Result = speakerAge == this.age1String;
    	} else if(this.age1Comparator.index == 2) {
    		age1Result = speakerAge > this.age1String;
    	}
    
    	var retVal = age1Result;
    
    	if(this.isUseAge2Filter()) {
    		if(this.age2Comparator.index == 0) {
    			age2Result = speakerAge < this.age2String;
    		} else if(this.age2Comparator.index == 1) {
    			age2Result = speakerAge == this.age2String;
    		} else if(this.age2Comparator.index == 2) {
    			age2Result = speakerAge > this.age2String;
    		}
    
    		if(this.ageOperator.index == 2) {
    			retVal = age1Result || age2Result;
    		} else if(this.ageOperator.index == 1) {
    			retVal = age1Result && age2Result;
    		}
    	}
    
    	return retVal;
	};
	
	this.checkSpeakerName = function(speaker) {
    	// split up participant names
    	var retVal = false;
    
    	if(speaker == null)
    		return retVal;
    
    	var partNames = this.participantNames.split(",");
    
    	for(var i = 0; i < partNames.length; i++) {
    		var partName = StringUtils.strip(partNames[i]);
    
    		if(partName == speaker.name) {
    			retVal = true;
    			break;
    		}
    	}
    
    	return retVal;
	};
	
	/**
	 * Check if given speaker matches filter
	 * 
	 * @param speaker
	 * @return true if speaker matches the filter
	 *  false otherwise
	 */
	this.check_speaker = function(speaker) {
	    var speakerOk = true;
	    
	    if (this.isUseNameFilter()) {
	        speakerOk &= this.checkSpeakerName(speaker);
	    }
	    
	    if(this.isUseAge1Filter()) {
	        speakerOk &= this.checkSpeakerAge(speaker);
	    }
	    
	    return speakerOk;
	};
	
}

