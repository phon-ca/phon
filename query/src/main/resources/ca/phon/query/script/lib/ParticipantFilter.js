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
 * Options for filtering searched records based on
 * participant information.
 */
exports.ParticipantFilter = function (id) {
	
	// form setup
	var sectionTitle = "Participant Filter";
	
	var participantRoleParamInfo = {
		"id": id + ".participantRole",
		"title": "Participant role:",
		"def": 0
	};
	var participantRoleParam;
	this.participantRole = null;
	
	var participantNamesParamInfo = {
		"id": id + ".participantNames",
		"title": "Participant names:",
		"prompt": "Separate names with a ','",
		"def": ""
	};
	var participantNamesParam;
	this.participantNames = participantNamesParamInfo.def;
	
	var age1ParamInfo = {
		"id": id + ".age1String",
		"title": "",
		"prompt": "yy;mm.dd",
		"def": ""
	};
	var age1Param;
	this.age1String = age1ParamInfo.def;
	
	var age2ParamInfo = {
		"id": id + ".age2String",
		"title": "",
		"prompt": "yy;mm.dd",
		"def": ""
	};
	var age2Param;
	this.age2String = age2ParamInfo.def;
	
	var age1ComparatorParamInfo = {
		"id": id + ".age1Comparator",
		"title": "Age:",
		"desc":[ "less than", "equal to", "greater than"],
		"def": 1
	};
	var age1ComparatorParam;
	this.age1Comparator = {
		index: age1ComparatorParamInfo.def
	};
	
	var age2ComparatorParamInfo = {
		"id": id + ".age2Comparator",
		"title": "Age:",
		"desc":[ "less than", "equal to", "greater than"],
		"def": 1
	};
	var age2ComparatorParam;
	this.age2Comparator = {
		index: age2ComparatorParamInfo.def
	};
	
	var ageOperatorParamInfo = {
		"id": id + ".ageOperator",
		"title": "",
		"desc":[ "(select operator)", "and", "or"],
		"def": 0
	};
	var ageOperatorParam;
	this.ageOperator = {
		index: ageOperatorParamInfo.def
	};
	
	var agePattern = /^([0-9]{2});([0-9]{2})\.([0-9]{2})$/;
	
	/**
	 * Check age filter text.
	 *
	 * @param filter
	 * @return true if text matches age pattern,
	 *  false otherwise
	 */
	var checkAgeFilter = function (filter) {
		if (filter == undefined || filter.length() == 0) return true;
		return AgeFormatter.stringToAge(filter) != null;
	};
	
	this.isUseNameFilter = function () {
		return (this.participantNames != undefined && this.participantNames.length() > 0);
	};
	
	this.isUseAge1Filter = function () {
		return this.age1String != undefined && this.age1String.length() > 0 && checkAgeFilter(this.age1String);
	};
	
	this.isUseAge2Filter = function () {
		var opSelected = this.ageOperator.index > 0;
		return this.isUseAge1Filter() && opSelected && checkAgeFilter(this.age2String);
	};
	
	this.isUseRoleFilter = function () {
		return this.participantRole != null && this.participantRole.index > 0;
	};
	
	var setFieldInvalid = function (textField, message, loc) {
		var msg = (loc >= 0 ?
		"Error at index " + loc + ": " + messgae:
		message);
		
		//        textField.setToolTipText(message);
		//        textField.setState("UNDEFINED");
	};
	
	var setFieldOk = function (textField) {
		//	    textField.setToolTipText("");
		//	    textField.setState("INPUT");
	};
	
	var setAgeFilterOk = function(ageParam) {
		ageParam.setTooltipText(null);
		ageParam.setValidate(true);
	};
	
	var setAgeFilterInvalid = function(ageParam, message) {
		ageParam.setTooltipText(message);
		ageParam.setValidate(false);
	};
	
	var validateAgeParam = function(id, ageParam) {
		var txt = ageParam.getValue(id);
		
		if(txt.trim().length() == 0) {
			return true;
		}
		
		try {
			AgeFormatter.stringToAge(txt);
			setAgeFilterOk(ageParam);
		} catch(e) {
			setAgeFilterInvalid(ageParam, e.message);
		}
	};
	
	/**
	 * Add params to form.
	 */
	this.param_setup = function (params) {
		// create a new section (collapsed by default)
		var sep = new SeparatorScriptParam(id+".sectionHeader", sectionTitle, true);
		params.add(sep);
		
		var roleArray = new Array();
		roleArray.push("(select role)");
		var roles = ParticipantRole.values();
		for (i = 0; i < roles.length; i++) {
			role = roles[i];
			roleArray.push(role.toString());
		}
		participantRoleParam = new EnumScriptParam(
		participantRoleParamInfo.id,
		participantRoleParamInfo.title,
		0,
		roleArray);
		params.add(participantRoleParam);
		
		participantNamesParam = new StringScriptParam(
		participantNamesParamInfo.id,
		participantNamesParamInfo.title,
		participantNamesParamInfo.def);
		participantNamesParam.setPrompt(
		participantNamesParamInfo.prompt);
		params.add(participantNamesParam);
		
		age1Param = new StringScriptParam(
		age1ParamInfo.id,
		age1ParamInfo.title,
		age1ParamInfo.def);
		age1Param.setPrompt(
		age1ParamInfo.prompt);
		
		var age1ParamListener = new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				validateAgeParam(age1ParamInfo.id, age1Param);
			}
		};
		age1Param.addPropertyChangeListener(age1ParamInfo.id, age1ParamListener);
		
		age2Param = new StringScriptParam(
		age2ParamInfo.id,
		age2ParamInfo.title,
		age2ParamInfo.def);
		age2Param.setPrompt(age2ParamInfo.prompt);
		age2Param.setEnabled(false);
		
		var age2ParamListener = new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				validateAgeParam(age2ParamInfo.id, age2Param);
			}
		};
		age2Param.addPropertyChangeListener(age2ParamInfo.id, age2ParamListener);
		
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
		age2ComparatorParam.setEnabled(false);
		
		ageOperatorParam = new EnumScriptParam(
		ageOperatorParamInfo.id,
		ageOperatorParamInfo.title,
		ageOperatorParamInfo.def,
		ageOperatorParamInfo.desc);
		var ageOperatorListener = new java.beans.PropertyChangeListener {
			propertyChange: function (e) {
				var idx = e.source.getValue(e.source.paramId).index;
				var enabled = (idx > 0);
				age2ComparatorParam.setEnabled(enabled);
				age2Param.setEnabled(enabled);
			}
		};
		ageOperatorParam.addPropertyChangeListener(ageOperatorListener);
		
		params.add(age1ComparatorParam);
		params.add(age1Param);
		params.add(ageOperatorParam);
		params.add(age2ComparatorParam);
		params.add(age2Param);
	};
	
	this.checkRole = function (participant) {
		if (participant == null || this.participantRole == null) return false;
		return ParticipantRole.fromString(this.participantRole.toString()) == participant.role;
	}
	
	this.compareAges = function(age1, age2) {
		var p1 = age1.normalized();
		var p2 = age2.normalized();
		var retVal = 
			(new java.lang.Integer(p1.years)).compareTo(p2.years);
		if(retVal == 0) {
			retVal = (new java.lang.Integer(p1.months)).compareTo(p2.months);
			if(retVal == 0) {
				retVal = (new java.lang.Integer(p1.days)).compareTo(p2.days);
			}
		}
		return retVal;
	}
	
	this.checkSpeakerAge = function (speaker, sessionDate) {
		if (speaker == null || speaker.getAge(sessionDate) == null)
		return false;
		
		var speakerPeriod = speaker.getAge(sessionDate);
		
		var p1 = AgeFormatter.stringToAge(this.age1String);
		var cmp1 = this.compareAges(speakerPeriod, p1);
		
		// perform first expression
		if (this.age1Comparator.index == 0) {
			retVal = ( cmp1 < 0 );
		} else if (this.age1Comparator.index == 1) {
			retVal = ( cmp1 == 0 );
		} else if (this.age1Comparator.index == 2) {
			retVal = ( cmp1 > 0 );
		}
				
		if (this.isUseAge2Filter()) {
			var age2Result = false;
			var p2 = AgeFormatter.stringToAge(this.age2String);
			var cmp2 = this.compareAges(speakerPeriod, p2);
			
			if (this.age2Comparator.index == 0) {
				age2Result = ( cmp2 < 0 );
			} else if (this.age2Comparator.index == 1) {
				age2Result = ( cmp2 == 0 );
			} else if (this.age2Comparator.index == 2) {
				age2Result = ( cmp2 > 0 );
			}
			
			if (this.ageOperator.index == 2) {
				retVal |= age2Result;
			} else if (this.ageOperator.index == 1) {
				retVal &= age2Result;
			}
		}
		
		return retVal;
	};
	
	this.checkSpeakerName = function (speaker) {
		// split up participant names
		var retVal = false;
		
		if (speaker == null)
		return retVal;
		
		var partNames = this.participantNames.split(",");
		
		for (var i = 0; i < partNames.length; i++) {
			var partName = StringUtils.strip(partNames[i]);
			
			if (partName == speaker.name) {
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
	 * @param sessionDate
	 * @return true if speaker matches the filter
	 *  false otherwise
	 */
	this.check_speaker = function (speaker, sessionDate) {
		var speakerOk = true;
		
		if (this.isUseRoleFilter() == true) {
			speakerOk &= this.checkRole(speaker);
		}
		
		if (this.isUseNameFilter() == true) {
			speakerOk &= this.checkSpeakerName(speaker);
		}
		
		if (this.isUseAge1Filter() == true) {
			speakerOk &= this.checkSpeakerAge(speaker, sessionDate);
		}
		
		return speakerOk;
	};
}