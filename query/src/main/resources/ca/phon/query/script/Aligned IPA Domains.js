/*
params =
{enum, searchTier, "IPA Target"|"IPA Actual", 0, "Search Tier"}
;
 */

var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var TierList = require("lib/TierList").TierList;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var SyllableFilter = require("lib/SyllableFilter").SyllableFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;
var ResultType = require("lib/PhonScriptConstants").ResultType;
var SearchByOptions = require("lib/SearchByOptions").SearchByOptions;

/********************************
 * Setup params
 *******************************/

var filters = {
	"searchBy": new SearchByOptions("filters.searchBy"),
	"targetResultFilter": new PatternFilter("filters.targetResultFilter"),
	"actualResultFilter": new PatternFilter("filters.actualResultFilter"),
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

function setup_params(params) {

	var insertIdx = 0;

	// setup result filter section
	var resultFilterSection = new SeparatorScriptParam("alignedPhonesHeader", "Aligned Phones", true);
	var targetLbl = new LabelScriptParam("", "<html><b>IPA Target Matcher</b></html>");
	var actualLbl = new LabelScriptParam("", "<html><b>IPA Actual Matcher</b></html>");

	params.add(resultFilterSection);
	params.add(targetLbl);
	filters.targetResultFilter.setSelectedPatternType(PatternType.PHONEX);
	filters.targetResultFilter.param_setup(params);
	params.add(actualLbl);
	filters.actualResultFilter.setSelectedPatternType(PatternType.PHONEX);
	filters.actualResultFilter.param_setup(params);

	filters.group.param_setup(params);
	filters.groupPattern.param_setup(params);

	var sep2 = new LabelScriptParam("", "<html><b>Aligned Group Filter</b></html>");
	params.add(sep2);
	filters.alignedGroup.param_setup(params);

	filters.word.param_setup(params);
	filters.wordPattern.param_setup(params);
	filters.wordPattern.setEnabled(false);

	var wordsep2 = new LabelScriptParam("", "<html><b>Aligned Word Filter</b></html>");
	params.add(wordsep2);
	filters.alignedWord.param_setup(params);
	var searchByWordListener = new java.beans.PropertyChangeListener {
		propertyChange: function (e) {
			var enabled = e.source.getValue(e.source.paramId);
			filters.wordPattern.setEnabled(enabled);
			filters.alignedWord.setEnabled(enabled);
			filters.wordTiers.setEnabled(enabled);
		}
	};
	filters.word.searchByWordParam.addPropertyChangeListener(filters.word.searchByWordParam.paramId, searchByWordListener);
	var enabled = filters.word.searchByWordParam.getValue(filters.word.searchByWordParam.paramId);
	filters.wordPattern.setEnabled(enabled);
	filters.alignedWord.setEnabled(enabled);

	filters.syllable.param_setup(params);

	filters.searchBy.includeSyllableOption = true;
	filters.searchBy.param_setup(params, filters.word.searchByWordParam, filters.syllable.searchBySyllableParam, insertIdx);

	var otherDataHeader = new SeparatorScriptParam("otherDataHeader", "Additional Tier Data", true);
	params.add(otherDataHeader);
	var sep = new LabelScriptParam("", "<html><b>Add aligned groups</b></html>");
	params.add(sep);
	filters.groupTiers.param_setup(params);
	var wordsep = new LabelScriptParam("", "<html><b>Add aligned words</b></html>");
	params.add(wordsep);
	filters.wordTiers.param_setup(params);
	filters.wordTiers.setEnabled(enabled);

	filters.speaker.param_setup(params);
}

/*
 * Globals
 */
var session;

function begin_search(s) {
	session = s;
}


/********************************
 * query_record
 * params:
 * 	record - the current record
 *******************************/
function query_record(recordIndex, record) {
	// check participant filter
	if (! filters.speaker.check_speaker(record.speaker, session.date)) return;

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

		var ipa = (searchTier == "IPA Target" ? group.IPATarget: group.IPAActual);
		var oppositeIpa = (searchTier == "IPA Target" ? group.IPAActual : group.IPATarget);
		var phoneMap = group.phoneAlignment;

		var toSearch = new Array();
		toSearch.push([ipa, oppositeIpa, phoneMap, groupAlignedResults, groupAlignedMeta]);

		// search by word?
		if (filters.word.isUseFilter()) {
			toSearch.length = 0;
			var selectedWords = filters.word.getRequestedWords(group, searchTier);
			for (j = 0; j < selectedWords.length; j++) {
				var wordData = selectedWords[j];
				var word = wordData.word;

				var wordAlignedMeta = new java.util.LinkedHashMap();
				wordAlignedMeta.putAll(groupAlignedMeta);

				if(filters.searchBy.includePositionalInfo == true) {
					wordAlignedMeta.put("Word Position", wordData.position);
				}

				var wordAlignedResults = new Array();
				for(var k = 0; k < groupAlignedResults.length; k++) {
					wordAlignedResults.push(groupAlignedResults[k]);
				}

				var wordAlignedData = filters.wordTiers.getAlignedTierData(record, word, "Word");
				for(var k = 0; k < wordAlignedData[0].length; k++) {
					wordAlignedResults.push(wordAlignedData[0][k]);
				}
				wordAlignedMeta.putAll(wordAlignedData[1]);

				var wordIpa = (searchTier == "IPA Target" ? word.IPATarget: word.IPAActual);
				var oppositeWordIpa = (searchTier == "IPA Target" ? word.IPAActual : word.IPATarget);
				var wordAlign = word.phoneAlignment;;
				
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
					for(var k = 0; k < alignedWordData[0].length; k++) {
						wordAlignedResults.push(alignedWordData[0][k]);
					}
				}

				if (addWord == true) {
					toSearch.push([wordIpa, oppositeWordIpa, wordAlign, wordAlignedResults, wordAlignedMeta]);
				}
			}
		}

		// search by syllable?
		if (filters.syllable.isUseFilter()) {
			var syllList = new Array();
			for (j = 0; j < toSearch.length; j++) {
				var parentIpa = toSearch[j][0];
				var parentOppositeIpa = toSearch[j][1] || new IPATranscript();
				var parentAlignment = toSearch[j][2];
				
				var syllableMap = (new Packages.ca.phon.ipa.alignment.SyllableAligner()).calculateSyllableAlignment(parentIpa, parentOppositeIpa, parentAlignment);
				var syllables = syllableMap.getTopElements();
				
				var sylls = filters.syllable.filterSyllables(syllableMap);
				
				for (k = 0; k < sylls.length; k++) {
					var syll = sylls[k];
					var alignedSylls = syllableMap.getAligned(java.util.List.of(syll));

					var syllMeta = new java.util.LinkedHashMap();
					syllMeta.putAll(toSearch[j][4]);

					if(filters.searchBy.includePositionalInfo == true) {
						syllMeta.put("Syllable Position", syllData.position);
					}
					
					if(alignedSylls.size() == 0) {
						syllList.push([syll, new IPATranscript(),
							parentAlignment.getSubAlignment(syll, new IPATranscript()), toSearch[j][3], syllMeta]);
					} else {
						for(var alignIdx = 0; alignIdx < alignedSylls.size(); alignIdx++) {
							syllList.push([syll, alignedSylls.get(alignIdx),
								parentAlignment.getSubAlignment(syll, alignedSylls.get(alignIdx)), toSearch[j][3], syllMeta]);
						}
					}
				}
			}
			toSearch = syllList;
		}

		for (j = 0; j < toSearch.length; j++) {
			var match = toSearch[j][0];
			var oppositeMatch = toSearch[j][1];
			var alignment = toSearch[j][2];
			var alignedResults = toSearch[j][3];
			var alignedMetadata = toSearch[j][4];
			
			var primaryFilter = (searchTier == "IPA Target" ? filters.targetResultFilter: filters.actualResultFilter);
			var alignedFilter = (searchTier == "IPA Target" ? filters.actualResultFilter: filters.targetResultFilter);

			if (primaryFilter.isUseFilter()) {
				if (! primaryFilter.check_filter(match)) {
					continue;
				}
			}
			if (alignedFilter.isUseFilter()) {
				if (! alignedFilter.check_filter(oppositeMatch)) {
					continue;
				}
			}

			var result = factory.createResult();
			// calculate start/end positions of data in text
			var startIndex = ipa.stringIndexOf(match);
			var length = match.toString().length();

			result.recordIndex = recordIndex;
			result.schema = "LINEAR";

			var rv = factory.createResultValue();
			rv.tierName = searchTier;
			rv.groupIndex = group.groupIndex;
			rv.range = new Range(startIndex, startIndex + length, true);
			rv.data = (match != null ? match : new IPATranscript());
			result.addResultValue(rv);

			var alignedRv = factory.createResultValue();
			alignedRv.tierName = (searchTier == "IPA Target" ? "IPA Actual": "IPA Target");
			alignedRv.groupIndex = group.groupIndex;
			if (oppositeMatch != null && oppositeMatch.length() > 0) {
				var alignedStart = oppositeIpa.stringIndexOf(oppositeMatch);
				var alignedLength = oppositeMatch.toString().length();

				alignedRv.range = new Range(alignedStart, alignedStart + alignedLength, true);
				alignedRv.data = oppositeMatch;
			} else {
				alignedRv.range = new Range(0, 0, true);
				alignedRv.data = "";
			}

            result.addResultValue(alignedRv);
			result.schema = "ALIGNED";

            result.metadata.put("Alignment", alignment.toString(true));

            if(filters.searchBy.includePositionalInfo == true) {
				var searchBy = (filters.searchBy.searchBySyllable == true ? "Syllable" : filters.searchBy.searchBy);
				result.metadata.put("Position in " + searchBy, "all");
			}

			for(var alignedResultIdx = 0; alignedResultIdx < alignedResults.length; alignedResultIdx++) {
				result.addResultValue(alignedResults[alignedResultIdx]);
			}
			result.metadata.putAll(alignedMetadata);

			results.addResult(result);
		}
	}
}
