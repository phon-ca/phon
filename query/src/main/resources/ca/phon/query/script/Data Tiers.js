// param setup

var PatternFilter = require("lib/PatternFilter").PatternFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var TierFilter = require("lib/TierFilter").TierFilter;
var TierList = require("lib/TierList").TierList;
var AlignedTierFilter = require("lib/TierFilter").TierFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var SearchByOptions = require("lib/SearchByOptions").SearchByOptions;

/********************************
 * Setup params
 *******************************/

var filters = {
	"primary": new TierFilter("filters.primary"),
	"searchBy": new SearchByOptions("filters.searchBy"),
	"tierFilter": new PatternFilter("filters.tierFilter"),
	"alignedTierFilter": new AlignedTierFilter("filters.alignedTierFilter"),
	"word": new WordFilter("filters.word"),
	"wordPattern": new PatternFilter("filters.wordPattern"),
	"alignedWord": new AlignedWordFilter("filters.alignedWord"),
	"addTiers": new TierList("filters.addTiers"),
	"wordTiers": new TierList("filters.wordTiers"),
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
	filters.primary.param_setup(params);
	filters.primary.set_required(true);
	
	var insertIdx = 1;

	var tierFilterSection = new SeparatorScriptParam("tierFilterHeader", "Tier Options", true);
	params.add(tierFilterSection);
	filters.tierFilter.param_setup(params);

	var alignedTierHeader = new LabelScriptParam("", "<html><b>Aligned Tier Filter</b></html>");
	params.add(alignedTierHeader);
	filters.alignedTierFilter.param_setup(params);

	// change default status of word filter for this query
	filters.word.searchByWord = false;
	filters.word.param_setup(params);
	filters.wordPattern.param_setup(params);
	filters.wordPattern.setEnabled(false);
	
	filters.searchBy.param_setup(params, filters.word.searchByWordParam, null, insertIdx);

	var alignedWordHeader = new LabelScriptParam("", "<html><b>Aligned Word Filter</b></html>");
	params.add(alignedWordHeader);
	filters.alignedWord.param_setup(params);

	var alignedWordListener = new java.beans.PropertyChangeListener {
		propertyChange: function (e) {
			var enabled = e.source.getValue(e.source.paramId);
			filters.wordTiers.tiersParam.setEnabled(enabled == true);
			filters.alignedWord.setEnabled(enabled);
			filters.wordTiers.setEnabled(enabled);
		}
	};
	filters.word.searchByWordParam.addPropertyChangeListener(alignedWordListener);
	filters.wordPattern.setEnabled(filters.word.searchByWord);
	filters.alignedWord.setEnabled(filters.word.searchByWord);

	var otherDataHeader = new SeparatorScriptParam("otherDataHeader", "Additional Tier Data", true);
	params.add(otherDataHeader);
	var sep = new LabelScriptParam("", "<html><b>Add aligned groups</b></html>");
	params.add(sep);
	filters.addTiers.param_setup(params);
	var wordsep = new LabelScriptParam("", "<html><b>Add aligned words</b></html>");
	params.add(wordsep);
	filters.wordTiers.param_setup(params);
	filters.wordTiers.setEnabled(filters.word.searchByWord);

	filters.speaker.param_setup(params);
}

function query_record(recordIndex, record) {
	if (! filters.speaker.check_speaker(record.speaker, session.date)) return;

	const searchTier = filters.primary.tier;
	const tier = record.getTier(searchTier);
	if(tier == null || !tier.hasValue()) return;

	if(filters.tierFilter.isUseFilter()) {
		if(!filters.tierFilter.check_filter(tier.value)) return;
	}

	var tierAlignedData = filters.addTiers.getAlignedTiers(record, "Tier");
	var tierAlignedResults = tierAlignedData.resultValues;
	var tierAlignedMeta = tierAlignedData.metadata;
	if(filters.alignedTierFilter.isUseFilter()) {
		if(!filters.alignedTierFilter.check_filter(record)) return;

		var tierList = new TierList("tier");
		tierList.setTiers(filters.alignedTierFilter.tier);

		var alignedTierData = tierList.getAlignedTiers(record, "Tier");
		for(var i = 0; i < alignedTierData.resultValues.length; i++) {
			tierAlignedResults.push(alignedTierData.resultValues[i]);
		}
		tierAlignedMeta.putAll(alignedTierData.metadata);
	}

	var toSearch = new Array();
	toSearch.push({
		element: tier.value,
		offset: 0,
		alignedResults: tierAlignedResults,
		alignedMeta: tierAlignedMeta
	});
	if(filters.word.isUseFilter()) {
		if(!tier.isExcludeFromAlignment()) {
			toSearch.length = 0;
			const crossTierAlignment = TierAligner.calculateCrossTierAlignment(record, tier);
			const topElements = crossTierAlignment.getTopAlignmentElements();
			const selectedElements = filters.word.getRequestedWords(topElements);
			for(var eleIdx = 0; eleIdx < selectedElements.length; eleIdx++) {
				var elementData = selectedElements[eleIdx];
				var element = elementData.word;

				var elementAlignedMeta = new java.util.LinkedHashMap();
				elementAlignedMeta.putAll(tierAlignedMeta);
				if(filters.searchBy.includePositionalInfo == true) {
					elementAlignedMeta.put("Word Position", elementData.position);
				}

				var elementAlignedResults = new Array();
				for(var i = 0; i < tierAlignedResults.length; i++) {
					elementAlignedResults.push(tierAlignedResults[i]);
				}
				var elementAlignedData = filters.wordTiers.getAlignedTierData(crossTierAlignment, element, "Word");
				for(var i = 0; i < elementAlignedData.resultValues.length; i++) {
					elementAlignedResults.push(elementAlignedData.resultValues[i]);
				}
				elementAlignedMeta.putAll(elementAlignedData.metadata);

				var addWord = (element != null);
				if(filters.wordPattern.isUseFilter()) {
					addWord = filters.wordPattern.check_filter(element);
				}

				// check aligned word pattern if necessary
				if (filters.alignedWord.isUseFilter()) {
					addWord = filters.alignedWord.check_aligned_element(crossTierAlignment, element);

					var tierList = new TierList("word");
					tierList.setTiers(filters.alignedWord.tier);

					var alignedWordData = tierList.getAlignedTierData(crossTierAlignment, element, "Word");
					for(var k = 0; k < alignedWordData.resultValues.length; k++) {
						elementAlignedResults.push(alignedWordData.resultValues[k]);
					}
					elementAlignedMeta.putAll(alignedWordData.metadata);
				}

				// get word offset
				var wordOffset = -1;
				if(tier.declaredType === Orthography || tier.declaredType === IPATranscript || tier.declaredType === UserTierData) {
					wordOffset = tier.value.stringIndexOf(element);
				}

				if(addWord) {
					toSearch.push({
						element: element,
						offset: wordOffset,
						alignedResults: elementAlignedResults,
						alignedMeta: elementAlignedMeta
					});
				}
			}
		}
	}

	for(var i = 0; i < toSearch.length; i++) {
		var element = toSearch[i].element;
		var offset = toSearch[i].offset;
		var alignedResults = toSearch[i].alignedResults;
		var alignedMeta = toSearch[i].alignedMeta;
		var matches = filters.primary.patternFilter.find_pattern(element);

		for(var j = 0; j < matches.length; j++) {
			var match = matches[j];

			if(match.groups) {
				var xgrp = match.groups["X"];
				if(xgrp) {
					var newMatch = {
						start: xgrp.start,
						end: xgrp.end,
						value: xgrp.value,
						groups: match.groups,
						position: match.position
					};
					match = newMatch;
				}
			}

			var result = factory.createResult();
			var startIndex = offset + match.start;
			var length = match.value.toString().length();

			result.recordIndex = recordIndex;
			result.schema = "LINEAR";

			var rv = factory.createResultValue();
			rv.tierName = searchTier;
			rv.range = new Range(startIndex, startIndex + length, true);
			rv.data = (match.value != null ? match.value : "");
			result.addResultValue(rv);

			for(var alignedResultIdx = 0; alignedResultIdx < alignedResults.length; alignedResultIdx++) {
				result.addResultValue(alignedResults[alignedResultIdx]);
			}
			result.metadata.putAll(alignedMeta);

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
