var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var TierList = require("lib/TierList").TierList;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var SyllableFilter = require("lib/SyllableFilter").SyllableFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;
var MetathesisOptions = require("lib/MetathesisOptions").MetathesisOptions;
var DetectorResultFactory = require("lib/DetectorResultFactory").DetectorResultFactory;
var SearchByOptions = require("lib/SearchByOptions").SearchByOptions;

var filters = {
	"metathesisOptions": new MetathesisOptions("filters.metathesisOptions"),
	"searchBy": new SearchByOptions("filters.searchBy"),
	"group": new GroupFilter("filters.group"),
	"groupTiers": new TierList("filters.groupTiers"),
	"groupPattern": new PatternFilter("filters.groupPattern"),
	"alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
	"word": new WordFilter("filters.word"),
	"wordTiers": new TierList("filters.wordTiers"),
	"wordPattern": new PatternFilter("filters.wordPattern"),
	"alignedWord": new AlignedWordFilter("filters.alignedWord"),
	"syllable": new SyllableFilter("filters.syllable"),
	"speaker": new ParticipantFilter("filters.speaker")
};

/*
 * Globals
 */
var session;

function begin_search(s) {
	session = s;
}

function setup_params(params) {
	filters.metathesisOptions.param_setup(params);
	
	filters.group.param_setup(params);
	filters.groupPattern.param_setup(params);
	
	var sep = new LabelScriptParam("", "<html><b>Add Aligned Groups</b></html>");
	params.add(sep);
	filters.groupTiers.param_setup(params);
	var sep2 = new LabelScriptParam("", "<html><b>Aligned Group Filter</b></html>");
	params.add(sep2);
	filters.alignedGroup.param_setup(params);
	
	filters.word.param_setup(params);
	filters.wordPattern.param_setup(params);
	filters.wordPattern.setEnabled(false);
	
	var wordsep = new LabelScriptParam("", "<html><b>Add Aligned Words</b></html>");
	params.add(wordsep);
	filters.wordTiers.param_setup(params);
	
	var wordsep2 = new LabelScriptParam("", "<html><b>Aligned Word Filter</b></html>");
	params.add(wordsep2);
	filters.alignedWord.param_setup(params);
	var searchByWordListener = new java.beans.PropertyChangeListener {
		propertyChange: function (e) {
			var enabled = e.source.getValue(e.source.paramId);
			filters.wordPattern.setEnabled(enabled);
			filters.alignedWord.setEnabled(enabled);
		}
	};
	filters.word.searchByWordParam.addPropertyChangeListener(filters.word.searchByWordParam.paramId, searchByWordListener);
	var enabled = filters.word.searchByWordParam.getValue(filters.word.searchByWordParam.paramId);
	filters.wordPattern.setEnabled(enabled);
	filters.alignedWord.setEnabled(enabled);
	
	filters.syllable.param_setup(params);
	
	filters.searchBy.includeSyllableOption = true;
	filters.searchBy.param_setup(params, filters.word.searchByWordParam, filters.syllable.searchBySyllableParam, 0);
	
	filters.speaker.param_setup(params);
}

var searchTier = "IPA Target";
function query_record(recordIndex, record) {
	// check participant filter
	if (! filters.speaker.check_speaker(record.speaker)) return;
	
	// check group+groupPattern filters
	var groups = filters.group.getRequestedGroups(record);
	if (filters.groupPattern.isUseFilter()) {
		groups = filters.groupPattern.filter_groups(groups, searchTier);
	}
	
	// check aligned group for each group returned
	if (filters.alignedGroup.isUseFilter()) {
		groups = filters.alignedGroup.filter_groups(record, groups);
	}
	
	// perform searches
	for (var i = 0; i < groups.length; i++) {
		var group = groups[i];
		
		var groupAlignedData = filters.groupTiers.getAlignedTierData(record, group, "Group");
		
		var groupAlignedResults = groupAlignedData[0];
		var groupAlignedMeta = groupAlignedData[1];
		
		if (filters.alignedGroup.isUseFilter()) {
			var tierList = new TierList("group");
			tierList.setTiers(filters.alignedGroup.tier);
			
			var alignedGroupData = tierList.getAlignedTierData(record, group, "Group");
			for (var j = 0; j < alignedGroupData[0].length; j++) {
				groupAlignedResults.push(alignedGroupData[0][j]);
			}
		}
		
		var ipa = (searchTier == "IPA Target" ? group.IPATarget: group.IPAActual);
		var phoneMap = group.phoneAlignment;
		
		var toSearch = new Array();
		toSearch.push([phoneMap, groupAlignedResults, groupAlignedMeta]);
		
		// search by word?
		if (filters.word.isUseFilter()) {
			toSearch.length = 0;
			var selectedWords = filters.word.getRequestedWords(group, searchTier);
			for (j = 0; j < selectedWords.length; j++) {
				var word = selectedWords[j];
				
				var wordAlignedMeta = new java.util.LinkedHashMap();
				wordAlignedMeta.putAll(groupAlignedMeta);
				
				var wordAlignedResults = new Array();
				for (var k = 0; k < groupAlignedResults.length; k++) {
					wordAlignedResults.push(groupAlignedResults[k]);
				}
				
				var wordAlignedData = filters.wordTiers.getAlignedTierData(record, word, "Word");
				for (var k = 0; k < wordAlignedData[0].length; k++) {
					wordAlignedResults.push(wordAlignedData[0][k]);
				}
				wordAlignedMeta.putAll(wordAlignedData[1]);
				
				var wordIpa = (searchTier == "IPA Target" ? word.IPATarget: word.IPAActual);
				var addWord = (wordIpa != null);
				// check word pattern if necessary
				if (filters.wordPattern.isUseFilter()) {
					addWord = filters.wordPattern.check_filter(wordIpa);
				}
				
				// check aligned word pattern if necessary
				if (filters.alignedWord.isUseFilter()) {
					addWord = filters.alignedWord.check_word(word);
					
					var tierList = new TierList("word");
					tierList.setTiers(filters.alignedWord.tier);
					
					var alignedWordData = tierList.getAlignedTierData(record, word, "Word");
					for (var k = 0; k < alignedWordData[0].length; k++) {
						wordAlignedResults.push(alignedWordData[0][k]);
					}
				}
				
				var wordPhoneAlignment = word.phoneAlignment;
				if (addWord == true) {
					toSearch.push([wordPhoneAlignment, wordAlignedResults, wordAlignedMeta]);
				}
			}
		}
		
		// search by syllable?
		if (filters.syllable.isUseFilter()) {
			var syllList = new Array();
			for (j = 0; j < toSearch.length; j++) {
				var obj = toSearch[j][0];
				var aligned = (phoneMap != null ? phoneMap: new Packages.ca.phon.ipa.alignment.PhoneMap());
				var sylls = filters.syllable.getRequestedSyllables(obj.targetRep, aligned);
				
				for (k = 0; k < sylls.length; k++) {
					var alignedEles = aligned.getAligned(sylls[k]);
					var syllPhoneAlignment = aligned.getSubAlignment(sylls[k], new IPATranscript(alignedEles));
					
					syllList.push([syllPhoneAlignment, toSearch[j][1], toSearch[j][2]]);
				}
			}
			toSearch = syllList;
		}
		
		var detectorResultFactory = new DetectorResultFactory();
		for (j = 0; j < toSearch.length; j++) {
			var obj = toSearch[j][0];
			var alignedResults = toSearch[j][1];
			var alignedMetadata = toSearch[j][2];
			
			var metathesisDetector = new Packages.ca.phon.query.detectors.MetathesisDetector(
			filters.metathesisOptions.includePlace == true, filters.metathesisOptions.includeManner == true,
			filters.metathesisOptions.includeVoicing == true);
			var metathesisResults = metathesisDetector.detect(obj);
			
			for (k = 0; k < metathesisResults.size(); k++) {
				var detectorResult = metathesisResults.get(k);
				
				if(!checkResultType(detectorResult)) continue;
				
				var result = detectorResultFactory.createMetathesisResult(recordIndex, group.groupIndex, detectorResult);
				
				for (var alignedResultIdx = 0; alignedResultIdx < alignedResults.length; alignedResultIdx++) {
					result.addResultValue(alignedResults[alignedResultIdx]);
				}
				result.metadata.putAll(alignedMetadata);
				
				results.addResult(result);
			}
		}
	}
}

function checkResultType(detectorResult) {
	if(filters.metathesisOptions.includeMetathsis == true &&
		filters.metathesisOptions.includeProgressiveMigration == true &&
		filters.metathesisOptions.includeRegressiveMigration == true) return true;
	
	var hasMetathesis = false;
	var hasProgressiveMigration = false;
	var hasRegressiveMigration = false;
	
	var dimensions = detectorResult.profile1.dimensions;
	var itr = dimensions.iterator();
	while(itr.hasNext()) {
		var dim = itr.next();
		var type = detectorResult.getType(dim);
		
		if(type.toString() == "Metathesis")
			hasMetathesis = true;
		else if(type.toString() == "Progressive Migration")
			hasProgressiveMigration = true;
		else if(type.toString() == "Regressive Migration")
			hasRegressiveMigration = true;
	}

	if(filters.metathesisOptions.includeMetathesis == true && hasMetathesis) return true;
	else if(filters.metathesisOptions.includeProgressiveMigration == true && hasProgressiveMigration) return true;
	else if(filters.metathesisOptions.includeRegressiveMigration == true && hasRegressiveMigration) return true;
	else return false;
}
