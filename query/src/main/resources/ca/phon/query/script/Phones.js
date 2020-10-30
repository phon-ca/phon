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
	"primary": new PatternFilter("filters.primary"),
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

var includeAlignedParamInfo = {
	"id": "includeAligned",
	"title": "",
	"desc": "Include aligned phones",
	"def": true
};
var includeAlignedParam;
var includeAligned = includeAlignedParamInfo.def;

function setup_params(params) {

	filters.primary.setSelectedPatternType(PatternType.PHONEX);
	filters.primary.param_setup(params);
	filters.primary.set_required(true);

	var insertIdx = 1;

	// setup result filter section
	var resultFilterSection = new SeparatorScriptParam("alignedPhonesHeader", "Aligned Phones", true);
	var targetLbl = new LabelScriptParam("", "<html><b>IPA Target Matcher</b></html>");
	var actualLbl = new LabelScriptParam("", "<html><b>IPA Actual Matcher</b></html>");

	includeAlignedParam = new BooleanScriptParam(
	includeAlignedParamInfo.id,
	includeAlignedParamInfo.desc,
	includeAlignedParamInfo.title,
	includeAlignedParamInfo.def);

	params.add(resultFilterSection);
	params.add(includeAlignedParam);
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
	
	var otherDataScriptParamSep = new SeparatorScriptParam("otherDataHeader", "Additional Tier Data", true);
	params.add(otherDataScriptParamSep);
	var sep = new LabelScriptParam("", "<html><b>Add aligned groups</b></html>");
	params.add(sep);
	filters.groupTiers.param_setup(params);
	var wordsep = new LabelScriptParam("", "<html><b>Add aligned words</b></html>");
	params.add(wordsep);
	filters.wordTiers.param_setup(params);
	
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

		var ipa = (searchTier == "IPA Target" ? group.IPATarget: group.IPAActual);
		var phoneMap = group.phoneAlignment;

		var toSearch = new Array();
		toSearch.push([ipa, groupAlignedResults, groupAlignedMeta]);

		// search by word?
		if (filters.word.isUseFilter()) {
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
					for(var k = 0; k < alignedWordData[0].length; k++) {
						wordAlignedResults.push(alignedWordData[0][k]);
					}
				}

				if (addWord == true) {
					toSearch.push([wordIpa, wordAlignedResults, wordAlignedMeta]);
				}
			}
		}

		// search by syllable?
		if (filters.syllable.isUseFilter()) {
			var syllList = new Array();
			for (j = 0; j < toSearch.length; j++) {
				var obj = toSearch[j][0];
				var aligned = (phoneMap != null ? phoneMap: new Packages.ca.phon.ipa.alignment.PhoneMap());
				var sylls = filters.syllable.getRequestedSyllables(obj, aligned);

				for (k = 0; k < sylls.length; k++) {
					syllList.push([sylls[k], toSearch[j][1], toSearch[j][2]]);
				}
			}
			toSearch = syllList;
		}

		for (j = 0; j < toSearch.length; j++) {
			var obj = toSearch[j][0];
			var alignedResults = toSearch[j][1];
			var alignedMetadata = toSearch[j][2];
			var matches = filters.primary.find_pattern(obj);
			var primaryFilter = (searchTier == "IPA Target" ? filters.targetResultFilter: filters.actualResultFilter);
			var alignedFilter = (searchTier == "IPA Target" ? filters.actualResultFilter: filters.targetResultFilter);

			for (k = 0; k < matches.length; k++) {
				var match = matches[k];

				if (match.groups) {
					var xgrp = match.groups[ "X"];
					if (xgrp) {
						var newMatch = {
							start: xgrp.start,
							end: xgrp.end,
							value: xgrp.value,
							groups: match.groups
						};
						match = newMatch;
					}
				}

				if (primaryFilter.isUseFilter()) {
					if (! primaryFilter.check_filter(new IPATranscript(match.value))) {
						continue;
					}
				}

				var result = factory.createResult();
				// calculate start/end positions of data in text
				var startIndex = ipa.stringIndexOf(match.value);
				var length = match.value.toString().length();

				result.recordIndex = recordIndex;
				result.schema = "LINEAR";

				var rv = factory.createResultValue();
				rv.tierName = searchTier;
				rv.groupIndex = group.groupIndex;
				rv.range = new Range(startIndex, startIndex + length, true);
				rv.data = (match.value != null ? new IPATranscript(match.value): new IPATranscript());
				result.addResultValue(rv);

				var alignedGroup = (searchTier == "IPA Target" ? group.getIPAActual(): group.getIPATarget());
				var aligned = (phoneMap != null ? phoneMap.getAligned(match.value.audiblePhones()) : null);
				var alignedIpaElements = (aligned != null ? new IPATranscript(aligned) : new IPATranscript());

				// find location of aligned value in group
				var groupStartIdx =
				(alignedIpaElements.length() > 0 ? alignedGroup.indexOf(alignedIpaElements.elementAt(0)): 0);
				var groupEndIdx =
				(alignedIpaElements.length() > 0 ? alignedGroup.indexOf(alignedIpaElements.elementAt(alignedIpaElements.length() -1)): 0);
				var alignedIpa =
				(alignedIpaElements.length() > 0 ? alignedGroup.subsection(groupStartIdx, groupEndIdx + 1): new IPATranscript());

				if (alignedFilter.isUseFilter()) {
					if (! alignedFilter.check_filter(alignedIpa)) {
						continue;
					}
				}

				if (includeAligned == true) {
				    var targetIPA = (searchTier == "IPA Target" ? match.value : alignedIpaElements);
				    var actualIPA = (searchTier == "IPA Target" ? alignedIpaElements : match.value);

				    var alignment = (phoneMap != null ? phoneMap.getSubAlignment(targetIPA, actualIPA) : new PhoneMap(targetIPA, actualIPA));

					var alignedRv = factory.createResultValue();
					alignedRv.tierName = (searchTier == "IPA Target" ? "IPA Actual": "IPA Target");
					alignedRv.groupIndex = group.groupIndex;
					if (aligned != null && aligned.size() > 0) {
						var alignedStart = alignedGroup.stringIndexOf(alignedIpa);
						var alignedLength = alignedIpa.toString().length();

						alignedRv.range = new Range(alignedStart, alignedStart + alignedLength, true);
						alignedRv.data = alignedIpa;
					} else {
						alignedRv.range = new Range(0, 0, true);
						alignedRv.data = "";
					}

                    result.addResultValue(alignedRv);
					result.schema = "ALIGNED";

                    result.metadata.put("Alignment", alignment.toString(true));
				}

				for(var alignedResultIdx = 0; alignedResultIdx < alignedResults.length; alignedResultIdx++) {
					result.addResultValue(alignedResults[alignedResultIdx]);
				}
				result.metadata.putAll(alignedMetadata);

				// append named-group information (if any)
				if (match.groups) {
					groupKeys = Object.keys(match.groups);
					for (keyIdx = 0; keyIdx < groupKeys.length; keyIdx++) {
						var key = groupKeys[keyIdx];
						if (/^[a-zA-Z]\w*$/.test(key) && key != 'X') {
							result.metadata.put(key, match.groups[key].value.toString());
						}
					}
				}

				results.addResult(result);
			}
		}
	}
}
