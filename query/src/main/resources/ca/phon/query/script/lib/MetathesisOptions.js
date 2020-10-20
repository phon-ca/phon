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
 * Options for metathesis detector.
 */
 
exports.MetathesisOptions = function(id) {

    var typeOptionsInfo = {
        "id": [id + ".includeMetathesis", id + ".includeProgressiveMigration", id + ".includeRegressiveMigration"],
        "title": "Metathesis Options",
        "desc": ["Include metathesis (i.e., X\u2026Y ↔ Y\u2026X)",
                 "Include progressive migration (i.e., X\u2026Y ↔ ?\u2026X)",
                 "Include regressive migration (i.e., X\u2026Y ↔ Y\u2026?)"],
        "def": [true, true, true],
        "numCols": 1
    };
    var typeOptionsParam;
    this.includeMetathesis = typeOptionsInfo.def[0];
    this.includeProgressiveMigration = typeOptionsInfo.def[1];
    this.includeRegressiveMigration = typeOptionsInfo.def[2];
    
    var dimensionOptionsInfo = {
		"id":[id + ".includePlace", id + ".includeManner", id + ".includeVoicing"],
		"def":[ true, true, true ],
		"title": "Dimensions",
		"desc":[ "Place", "Manner", "Voicing"],
		"numCols": 3
	};
	var dimensionOptionsParam;
	this.includePlace = dimensionOptionsInfo.def[0];
	this.includeManner = dimensionOptionsInfo.def[1];
	this.includeVoicing = dimensionOptionsInfo.def[2];
	
	this.param_setup = function (params) {
	    typeOptionsParam = new MultiboolScriptParam(
	        typeOptionsInfo.id,
	        typeOptionsInfo.def,
	        typeOptionsInfo.desc,
	        typeOptionsInfo.title,
	        typeOptionsInfo.numCols);
	    params.add(typeOptionsParam);
	
	    dimensionOptionsParam = new MultiboolScriptParam(
            dimensionOptionsInfo.id,
            dimensionOptionsInfo.def,
            dimensionOptionsInfo.desc,  
            dimensionOptionsInfo.title,
            dimensionOptionsInfo.numCols
        );
        params.add(dimensionOptionsParam);
    };
	
};
