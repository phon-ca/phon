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
 * Options for harmony detector.
 */
 
exports.HarmonyOptions = function(id) {
    var includeConsonantHarmonyInfo = {
		"id": id + ".includeConsonantHarmony",
		"def": true,
		"title": "Consonant Harmony Options",
		"desc": "Include consonant harmony"
	};
	this.includeConsonantHarmony = includeConsonantHarmonyInfo.def;

    var consonantHarmonyOptionsInfo = {
		"id":[id + ".includePlace", id + ".includeManner", id + ".includeVoicing"],
		"def":[ true, true, true ],
		"title": "",
		"desc":[ "Place", "Manner", "Voicing"],
		"numCols": 3
	};
	this.includePlace = consonantHarmonyOptionsInfo.def[0];
	this.includeManner = consonantHarmonyOptionsInfo.def[1];
	this.includeVoicing = consonantHarmonyOptionsInfo.def[2];
	
	var includeVowelHarmonyInfo = {
		"id": id + ".includeVowelHarmony",
		"def": false,
		"title": "Vowel Harmony Options",
		"desc": "Include vowel harmony"
	};
	this.includeVowelHarmony = includeVowelHarmonyInfo.def;

    var vowelHarmonyOptionsInfo = {
		"id":[id + ".includeHeight", id + ".includeBackness", id + ".includeTenseness", id + ".includeRounding"],
		"def":[ true, true, true, true],
		"title": "",
		"desc":[ "Height", "Backness", "Tenseness", "Rounding" ],
		"numCols": 2
	};
	this.includePlace = vowelHarmonyOptionsInfo.def[0];
	this.includeManner = vowelHarmonyOptionsInfo.def[1];
	this.includeVoicing = vowelHarmonyOptionsInfo.def[2];
	this.includeRounding = vowelHarmonyOptionsInfo.def[3];
	
	var directionOptionsInfo = {
	    "id":[id + ".includeProgressive", id + ".includeRegressive"],
	    "def":[true, true],
	    "title": "Direction",
	    "desc":["Progressive", "Regressive"],
	    "numCols": 2
	};
	this.includeProgressive = directionOptionsInfo.def[0];
	this.includeRegressive = directionOptionsInfo.def[1];
	
	this.param_setup = function (params) {
	    var includeConsonantHarmonyParam = new BooleanScriptParam(
	        includeConsonantHarmonyInfo.id,
	        includeConsonantHarmonyInfo.desc,
	        includeConsonantHarmonyInfo.title,
	        includeConsonantHarmonyInfo.def);
	    params.add(includeConsonantHarmonyParam);
	    
	    var consonantHarmonyOptionsParam = new MultiboolScriptParam(
            consonantHarmonyOptionsInfo.id,
            consonantHarmonyOptionsInfo.def,
            consonantHarmonyOptionsInfo.desc,  
            consonantHarmonyOptionsInfo.title,
            consonantHarmonyOptionsInfo.numCols
        );
        params.add(consonantHarmonyOptionsParam);
        consonantHarmonyOptionsParam.setEnabled(includeConsonantHarmonyInfo.def);
        
        var includeConsonantsListener = new java.beans.PropertyChangeListener {
			propertyChange: function (e) {
				var enabled = e.source.getValue(e.source.paramId) == true;
				consonantHarmonyOptionsParam.setEnabled(enabled);
			}
		};
		includeConsonantHarmonyParam.addPropertyChangeListener(includeConsonantHarmonyInfo.id, includeConsonantsListener);
        
        var includeVowelHarmonyParam = new BooleanScriptParam(
            includeVowelHarmonyInfo.id,
            includeVowelHarmonyInfo.desc,
            includeVowelHarmonyInfo.title,
            includeVowelHarmonyInfo.def);
        params.add(includeVowelHarmonyParam);
        
        var vowelHarmonyOptionsParam = new MultiboolScriptParam(
            vowelHarmonyOptionsInfo.id,
            vowelHarmonyOptionsInfo.def,
            vowelHarmonyOptionsInfo.desc,
            vowelHarmonyOptionsInfo.title,
            vowelHarmonyOptionsInfo.numCols);
        params.add(vowelHarmonyOptionsParam);
        vowelHarmonyOptionsParam.setEnabled(includeVowelHarmonyInfo.def);
        
        var includeVowelsListener = new java.beans.PropertyChangeListener {
			propertyChange: function (e) {
				var enabled = e.source.getValue(e.source.paramId) == true;
				vowelHarmonyOptionsParam.setEnabled(enabled);
			}
		};
		includeVowelHarmonyParam.addPropertyChangeListener(includeVowelHarmonyInfo.id, includeVowelsListener);
	
	    var directionParam = new MultiboolScriptParam(
	        directionOptionsInfo.id,
	        directionOptionsInfo.def,
	        directionOptionsInfo.desc,
	        directionOptionsInfo.title,
	        directionOptionsInfo.numCols);
	    params.add(directionParam);
	};
	
};
