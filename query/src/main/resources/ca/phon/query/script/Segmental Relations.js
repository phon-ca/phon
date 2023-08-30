var AlignedTierFilter = require("lib/TierFilter").TierFilter;
var SegmentalRelationsOptions = require("lib/SegmentalRelationsOptions").SegmentalRelationsOptions;
var AlignedTierFilter = require("lib/TierFilter").TierFilter;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var TierList = require("lib/TierList").TierList;
var WordFilter = require("lib/WordFilter").WordFilter;
var SyllableFilter = require("lib/SyllableFilter").SyllableFilter;
var PatternFilter = require("lib/PatternFilter").PatternFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var SearchByOptions = require("lib/SearchByOptions").SearchByOptions;

/********************************
 * Setup params
 *******************************/

var filters = {
	"segmentalRelationsOptions": new SegmentalRelationsOptions("filters.segmentalRelationsOptions"),
	"searchBy": new SearchByOptions("filters.searchBy"),
	"tierFilter": new PatternFilter("filters.tierFilter"),
	"alignedTierFilter": new AlignedTierFilter("filters.alignedTierFilter"),
	"word": new WordFilter("filters.word"),
	"wordPattern": new PatternFilter("filters.wordPattern"),
	"addTiers": new TierList("filters.groupTiers"),
	"wordTiers": new TierList("filters.wordTiers"),
	"alignedWord": new AlignedWordFilter("filters.alignedWord"),
	"syllable": new SyllableFilter("filters.syllable"),
	"speaker": new ParticipantFilter("filters.speaker")
};

function setup_params(params) {
	filters.segmentalRelationsOptions.param_setup(params);

	var insertIdx = 0;

	var tierFilterSection = new SeparatorScriptParam("tierFilterHeader", "Tier Options", true);
	params.add(tierFilterSection);
	filters.tierFilter.param_setup(params);

	var alignedTierHeader = new LabelScriptParam("", "<html><b>Aligned Tier Filter</b></html>");
	params.add(alignedTierHeader);
	filters.alignedTierFilter.param_setup(params);

	// change default status of word filter for this query
	filters.word.searchByWord = true;
	filters.word.param_setup(params);
	filters.wordPattern.param_setup(params);

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
	filters.alignedWord.setEnabled(filters.word.searchByWord);

	filters.syllable.param_setup(params);

	filters.searchBy.includeSyllableOption = true;
	filters.searchBy.includePositionalOption = false;
	filters.searchBy.param_setup(params, filters.word.searchByWordParam, filters.syllable.searchBySyllableParam, insertIdx);

	var otherDataHeader = new SeparatorScriptParam("otherDataHeader", "Additional Tier Data", true);
	params.add(otherDataHeader);
	var sep = new LabelScriptParam("", "<html><b>Add tiers</b></html>");
	params.add(sep);
	filters.addTiers.param_setup(params);
	var wordsep = new LabelScriptParam("", "<html><b>Add aligned words</b></html>");
	params.add(wordsep);
	filters.wordTiers.param_setup(params);
	filters.wordTiers.setEnabled(filters.word.searchByWord);

	filters.speaker.param_setup(params);
}

var session;

function begin_search(s) {
	session = s;
}

function query_record(recordIndex, record) {
// check participant filter
	if (! filters.speaker.check_speaker(record.speaker, session.date)) return;

	const tier = record.getIPATargetTier();
	const ipa = tier.hasValue() ? tier.value : new IPATranscript();
	const alignedTier = record.getIPAActualTier();
	const alignedIpa = alignedTier.hasValue() ? alignedTier.getValue() : new IPATranscript();
	var alignment = record.getPhoneAlignment();

	if(filters.tierFilter.isUseFilter()) {
		if(!filters.tierFilter.check_filter(tier.getValue())) return;
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
		element: alignment.fullAlignment,
		alignedResults: tierAlignedResults,
		alignedMeta: tierAlignedMeta
	});

	// search by word
	if(filters.word.isUseFilter()) {
		toSearch.length = 0;
		const crossTierAlignment = TierAligner.calculateCrossTierAlignment(record, record.getPhoneAlignmentTier());
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

			if(addWord) {
				toSearch.push({
					element: element,
					alignedResults: elementAlignedResults,
					alignedMeta: elementAlignedMeta
				});
			}
		}
	}

	// search by syllable
	if(filters.syllable.isUseFilter()) {
		var syllList = new Array();
		for(i = 0; i < toSearch.length; i++) {
			var obj = toSearch[i].element;
			var sylls = filters.syllable.getRequestedSyllables(obj, alignment);

			for(var j = 0; j < sylls.length; j++) {
				var syllData = sylls[j];
				var syll = syllData.syllable;

				var syllMeta = new java.util.LinkedHashMap();
				syllMeta.putAll(toSearch[i].alignedMeta);

				if(filters.searchBy.includePositionalInfo == true) {
					syllMeta.put("Syllable Position", syllData.position);
				}

				syllList.push({
					element: syll,
					alignedMeta: syllMeta,
					alignedResults: toSearch[i].alignedResults
				});
			}
		}
		toSearch = syllList;
	}

	var detector = filters.segmentalRelationsOptions.createDetector();
	for (j = 0; j < toSearch.length; j++) {
		var obj = toSearch[j].element;
		var alignedResults = toSearch[j].alignedResults;
		var alignedMetadata = toSearch[j].alignedMeta;
		var phoneMap = alignment.fullAlignment;

		var relations = detector.detect(obj);
		for(k = 0; k < relations.size(); k++) {
			var relation = relations.get(k);
			var offset = phoneMap.getSubAlignmentIndex(obj.targetRep.audiblePhones(), obj.actualRep.audiblePhones());
			relation.phoneMap = phoneMap;
			relation.position1 += offset;
			relation.position2 += offset;

			var includeRelation = filters.segmentalRelationsOptions.filterRelation(relation);
			if(includeRelation == true) {
				var result = filters.segmentalRelationsOptions.createQueryResult(recordIndex, relation);

				for(var alignedResultIdx = 0; alignedResultIdx < alignedResults.length; alignedResultIdx++) {
					result.addResultValue(alignedResults[alignedResultIdx]);
				}
				result.metadata.putAll(alignedMetadata);

				results.addResult(result);
			}
		}
	}
}
