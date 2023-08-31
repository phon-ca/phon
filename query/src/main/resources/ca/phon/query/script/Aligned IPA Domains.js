/*
params =
{enum, searchTier, "IPA Target"|"IPA Actual", 0, "Search Tier"}
;
 */

var AlignedTierFilter = require("lib/TierFilter").TierFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var TierList = require("lib/TierList").TierList;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var SyllableFilter = require("lib/SyllableFilter").SyllableFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;
var SearchByOptions = require("lib/SearchByOptions").SearchByOptions;

/********************************
 * Setup params
 *******************************/

var filters = {
	"searchBy": new SearchByOptions("filters.searchBy"),
	"targetResultFilter": new PatternFilter("filters.targetResultFilter"),
	"actualResultFilter": new PatternFilter("filters.actualResultFilter"),
	"tierFilter": new PatternFilter("filters.tierFilter"),
	"alignedTierFilter": new AlignedTierFilter("filters.alignedTierFilter"),
	"word": new WordFilter("filters.word"),
	"wordPattern": new PatternFilter("filters.wordPattern"),
	"addTiers": new TierList("filters.addTiers"),
	"wordTiers": new TierList("filters.wordTiers"),
	"alignedWord": new AlignedWordFilter("filters.alignedWord"),
	"syllable": new SyllableFilter("filters.syllable"),
	"speaker": new ParticipantFilter("filters.speaker")
};

function setup_params(params) {
	var insertIdx = 0;

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

	var tierFilterSection = new SeparatorScriptParam("tierFilterHeader", "Tier Options", true);
	params.add(tierFilterSection);
	filters.tierFilter.param_setup(params);

	var alignedTierHeader = new LabelScriptParam("", "<html><b>Aligned Tier Filter</b></html>");
	params.add(alignedTierHeader);
	filters.alignedTierFilter.param_setup(params);

	filters.word.param_setup(params);
	filters.wordPattern.param_setup(params);
	filters.wordPattern.setEnabled(false);

	var alignedWordHeader = new LabelScriptParam("", "<html><b>Aligned Word Filter</b></html>");
	params.add(alignedWordHeader);
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
	var sep = new LabelScriptParam("", "<html><b>Add aligned tiers</b></html>");
	params.add(sep);
	filters.addTiers.param_setup(params);
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

	const tier = searchTier == "IPA Target" ? record.getIPATargetTier() : record.getIPAActualTier();
	const ipa = tier.hasValue() ? tier.value : new IPATranscript();
	const alignedTier = searchTier == "IPA Target" ? record.getIPAActualTier() : record.getIPATargetTier();
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
		ipa: ipa,
		offset: 0,
		alignedIpa: alignedIpa,
		alignedOffset: 0,
		phoneMap: alignment.fullAlignment,
		alignedResults: tierAlignedResults,
		alignedMeta: tierAlignedMeta
	});
	// search by word
	if(filters.word.isUseFilter()) {
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

			var alignedElement = crossTierAlignment.getAlignedElements(element).get(alignedTier.getName());
			if(alignedElement == null) {
				alignedElement = new IPATranscript();
			}
			var alignedPhoneMap = crossTierAlignment.getAlignedElements(element).get("Alignment");
			if(addWord) {
				toSearch.push({
					ipa: element,
					offset: ipa.stringIndexOf(element),
					alignedIpa: alignedElement,
					alignedOffset: alignedIpa.stringIndexOf(alignedElement),
					phoneMap: alignedPhoneMap,
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
			var obj = toSearch[i].ipa;
			var alignedObj = toSearch[i].alignedIpa;
			var phoneMap = toSearch[i].phoneMap;
			var sylls = filters.syllable.getRequestedSyllables(obj, alignment);

			for(var j = 0; j < sylls.length; j++) {
				var syllData = sylls[j];
				var syll = syllData.syllable;
				var alignedData = new IPATranscript(phoneMap.getAligned(syll));

				var syllMeta = new java.util.LinkedHashMap();
				syllMeta.putAll(toSearch[i].alignedMeta);

				if(filters.searchBy.includePositionalInfo == true) {
					syllMeta.put("Syllable Position", syllData.position);
				}

				syllList.push({
					ipa: syll,
					offset: obj.stringIndexOf(syll),
					alignedIpa: alignedData,
					alignedOffset: alignedObj.stringIndexOf(alignedData),
					phoneMap: phoneMap,
					alignedMeta: syllMeta,
					alignedResults: toSearch[i].alignedResults
				});
			}
		}
		toSearch = syllList;
	}

	for (j = 0; j < toSearch.length; j++) {
		var match = toSearch[j].ipa;
		var oppositeMatch = toSearch[j].alignedIpa;
		var alignment = toSearch[j].phoneMap;
		var alignedResults = toSearch[j].alignedResults;
		var alignedMetadata = toSearch[j].alignedMeta;

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
		var offset = toSearch[j].offset;
		var alignedOffset = toSearch[j].alignedOffset;
		var startIndex = ipa.stringIndexOf(match);
		var length = match.toString().length();

		result.recordIndex = recordIndex;
		result.schema = "LINEAR";

		var rv = factory.createResultValue();
		rv.tierName = searchTier;
		rv.range = new Range(startIndex, startIndex + length, true);
		rv.data = (match != null ? match : new IPATranscript());
		result.addResultValue(rv);

		var alignedRv = factory.createResultValue();
		alignedRv.tierName = (searchTier == "IPA Target" ? "IPA Actual": "IPA Target");
		if (oppositeMatch != null && oppositeMatch.length() > 0) {
			var alignedStart = alignedIpa.stringIndexOf(oppositeMatch);
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
