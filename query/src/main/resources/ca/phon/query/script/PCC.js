var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var TierList = require("lib/TierList").TierList;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;
var Pcc = require("lib/Pcc").Pcc;
var PccOptions = require("lib/Pcc").PccOptions;
var ResultType = require("lib/PhonScriptConstants").ResultType;

/********************************
 * Setup params
 *******************************/
var filters = {
	"pcc": new PccOptions("filters.pcc"),
	"group": new GroupFilter("filters.group"),
	"groupTiers": new TierList("filters.groupTiers"),
	"groupPattern": new PatternFilter("filters.groupPattern"),
	"alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
	"word": new WordFilter("filters.word"),
	"wordTiers": new TierList("filters.wordTiers"),
	"wordPattern": new PatternFilter("filters.wordPattern"),
	"alignedWord": new AlignedWordFilter("filters.alignedWord"),
	"speaker": new ParticipantFilter("filters.speaker")
};

function setup_params(params) {
	var insertIdx = 0;

	filters.pcc.param_setup(params);
	
	filters.group.param_setup(params);
	filters.groupPattern.param_setup(params);

	var sep = new LabelScriptParam("", "<html><b>Add Aligned Groups</b></html>");
	params.add(sep);
	filters.groupTiers.param_setup(params);
	var sep2 = new LabelScriptParam("", "<html><b>Aligned Group Filter</b></html>");
	params.add(sep2);
	filters.alignedGroup.param_setup(params);

	filters.word.searchByWordEnabled = false;
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

	filters.speaker.param_setup(params);
}

var session;

function begin_search(s) {
	session = s;
}

var searchTier = "IPA Target";
function query_record(recordIndex, record) {
	var nf = java.text.NumberFormat.getNumberInstance();
	nf.setMinimumFractionDigits(1);
	nf.setMaximumFractionDigits(2);
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
			for(var j = 0; j < alignedGroupData[0].length; j++) {
				groupAlignedResults.push(alignedGroupData[0][j]);
			}
		}

		var toSearch = new Array();
		toSearch.length = 0;
		var selectedWords = filters.word.getRequestedWords(group, searchTier);
		for (j = 0; j < selectedWords.length; j++) {
			var word = selectedWords[j];

			var wordAlignedMeta = new java.util.LinkedHashMap();
			wordAlignedMeta.putAll(groupAlignedMeta);

			var wordAlignedResults = new Array();
			for(var k = 0; k < groupAlignedResults.length; k++) {
				wordAlignedResults.push(groupAlignedResults[k]);
			}

			var wordAlignedData = filters.wordTiers.getAlignedTierData(record, word, "Word");
			for(var k = 0; k < wordAlignedData[0].length; k++) {
				wordAlignedResults.push(wordAlignedData[0][k]);
			}
			wordAlignedMeta.putAll(wordAlignedData[1]);

			var wordIpa = (searchTier == "IPA Target" ? word.IPATarget : word.IPAActual);
			var alignedIpa = (searchTier == "IPA Target" ? word.IPAActual : word.IPATarget);
			var addWord = (wordIpa != null && wordIpa.length() > 0 && alignedIpa != null && alignedIpa.length() > 0);
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
				for(var k = 0; k < alignedWordData[0].length; k++) {
					wordAlignedResults.push(alignedWordData[0][k]);
				}
			}

			if (addWord == true) {
				toSearch.push([word, wordAlignedResults, wordAlignedMeta]);
			}
		}
		
		for(j = 0; j < toSearch.length; j++) {
			var word = toSearch[j][0];
			var alignedResults = toSearch[j][1];
			var alignedMetadata = toSearch[j][2];
			
			var features = (filters.pcc.pcType.index == 0 ? "c" : "v");
			var pc = (filters.pcc.useAlignment == true ? Pcc.calc_pc_aligned(word, features, filters.pcc.ignoreDiacritics == true)
						: Pcc.calc_pc_standard(word, features, filters.pcc.ignoreDiacritics == true));
			
			var ipaT = (word.IPATarget != null ? word.IPATarget : new IPATranscript());
			var ipaA = (word.IPAActual != null ? word.IPAActual : new IPATranscript());
			
			var result = factory.createResult();
		    result.schema = "ALIGNED";
		    result.recordIndex = recordIndex;

		    var rvt = factory.createResultValue();
		    rvt.tierName = "IPA Target";
	    	rvt.groupIndex = i;
	    	var startIndex = word.getIPATargetWordLocation();
	    	var endIndex = startIndex + ipaT.toString().length();
	    	rvt.range = new Range(startIndex, endIndex, false);
	    	rvt.data = ipaT;
	    	result.addResultValue(rvt);

	    	var rva = factory.createResultValue();
	    	rva.tierName = "IPA Actual";
	    	rva.groupIndex = i;
	    	startIndex = word.getIPAActualWordLocation();
	    	endIndex = startIndex + ipaA.toString().length();
	    	rva.range = new Range(startIndex, endIndex, false);
	    	rva.data = ipaA;
	        result.addResultValue(rva);

			result.metadata.put("# Target", pc.target + "");
			result.metadata.put("# Actual", pc.actual + "");
			result.metadata.put("# Correct", pc.correct + "");
			result.metadata.put("# Substitued", (pc.actual - pc.correct) + "");
			result.metadata.put("# Deleted", pc.deleted + "");
			result.metadata.put("# Epenthesized", pc.epen + "");
			
			var pcColName = (filters.pcc.pcType.index == 0 ? "PCC" : "PVC");
			var pcVal = (pc.target > 0 ? pc.correct / pc.target: 0) * 100;
			result.metadata.put(pcColName, nf.format(pcVal));

			for(var alignedResultIdx = 0; alignedResultIdx < alignedResults.length; alignedResultIdx++) {
					result.addResultValue(alignedResults[alignedResultIdx]);
			}
		    result.metadata.putAll(alignedMetadata);
		    
		    results.addResult(result);
		}
	}
}
