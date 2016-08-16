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
/**
 * Create a listing of aligned words from the Orthography/IPA Target/IPA Actual
 * tiers.
 */

var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;

var filters = {
		"group": new GroupFilter("filters.group"),
		"alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
		"word": new WordFilter("filters.word"),
		"alignedWord": new AlignedWordFilter("filters.alignedWord"),
		"speaker": new ParticipantFilter("filters.speaker")
};

var session;

function begin_search(s) {
	session = s;
}

function setup_params(params) {
	filters.group.param_setup(params);
	var sep = new LabelScriptParam("", "Aligned Group Filter");
	params.add(sep);
	filters.alignedGroup.param_setup(params);
	
	filters.word.searchByWordEnabled = false;
	filters.word.param_setup(params);
	var wordsep = new LabelScriptParam("", "<html><b>Aligned Word</b></html>");
	params.add(wordsep);
	filters.alignedWord.param_setup(params);

	filters.speaker.param_setup(params);
}

function query_record(recordIndex, record) {
	if(!filters.speaker.check_speaker(record.speaker)) return;
    
	var searchObjects = filters.group.getRequestedGroups(record);
	// check aligned group for each group returned
	if(filters.alignedGroup.isUseFilter()) {
	    searchObjects = filters.alignedGroup.filter_groups(record, searchObjects);
	}
	
	for(var gIdx = 0; gIdx < searchObjects.length; gIdx++) {
		var group = searchObjects[gIdx];
		
		// use 'IPA Target' as our source tier
		var words = filters.word.getRequestedWords(group, "IPA Target");
		
		for(var wIdx = 0; wIdx < words.length; wIdx++) {
			var word = words[wIdx];
			var ortho = word.orthography || new Orthography();
			var ipaT = word.IPATarget || new IPATranscript();
			var ipaA = word.IPAActual || new IPATranscript();
			
			var result = factory.createResult();
			result.schema = "ALIGNED";
			result.recordIndex = recordIndex;
			
			var rv1 = factory.createResultValue();
			rv1.tierName = "Orthography";
	    	rv1.groupIndex = gIdx;
	    	var startIndex = (ortho.toString().length() > 0 ? word.getOrthographyWordLocation() : 0);
	    	var endIndex = startIndex + ortho.toString().length();
	    	rv1.range = new Range(startIndex, endIndex, false);
	    	rv1.data = ortho;
	    	result.addResultValue(rv1);
	    	
	    	var rvt = factory.createResultValue();
		    rvt.tierName = "IPA Target";
	    	rvt.groupIndex = gIdx;
	    	var startIndex = (ipaT.length() > 0 ? word.getIPATargetWordLocation() : 0);
	    	var endIndex = startIndex + ipaT.toString().length();
	    	rvt.range = new Range(startIndex, endIndex, false);
	    	rvt.data = ipaT;
	    	result.addResultValue(rvt);
	    	
	    	var rva = factory.createResultValue();
	    	rva.tierName = "IPA Actual";
	    	rva.groupIndex = gIdx;
	    	startIndex = (ipaA.length() > 0 ? word.getIPAActualWordLocation() : 0);
	    	endIndex = startIndex + ipaA.toString().length();
	    	rva.range = new Range(startIndex, endIndex, false);
	    	rva.data = ipaA;
	        result.addResultValue(rva);
	        
	        results.addResult(result);
		}
	}
}
