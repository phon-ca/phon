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

var filters = {
    "primary": new PatternFilter("filters.primary"),
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

    var resultFilterSection = new SeparatorScriptParam("alignedPhonesHeader", "Aligned Phones", true);
    var targetLbl = new LabelScriptParam("", "<html><b>IPA Target Matcher</b></html>");
    var actualLbl = new LabelScriptParam("", "<html><b>IPA Actual Matcher</b></html>");

    includeAlignedParam = new BooleanScriptParam(
        includeAlignedParamInfo.id,
        includeAlignedParamInfo.desc,
        includeAlignedParamInfo.title,
        includeAlignedParamInfo.def
        );

    params.add(resultFilterSection);
    params.add(includeAlignedParam);
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

function update_sessions(selectedSessions) {

}

/*
 * Globals
 */
var session;

function begin_search(s) {
    session = s;
}


/**
 * query_record
 * @param recordIndex
 * @param record
 */
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
        element: ipa,
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

    // perform query
    for(var i = 0; i < toSearch.length; i++) {
        var element = toSearch[i].element;
        var alignedResults = toSearch[i].alignedResults;
        var alignedMeta = toSearch[i].alignedMeta;
        var matches = filters.primary.find_pattern(element);
        var primaryFilter = (searchTier == "IPA Target" ? filters.targetResultFilter: filters.actualResultFilter);
        var alignedFilter = (searchTier == "IPA Target" ? filters.actualResultFilter: filters.targetResultFilter);

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

            if(primaryFilter.isUseFilter()) {
                if(!primaryFilter.check_filter(new IPATranscript(match.value))) {
                    continue;
                }
            }

            var result = factory.createResult();
            var startIndex = ipa.stringIndexOf(match.value);
            var length = match.value.toString().length();

            result.recordIndex = recordIndex;
            result.schema = "LINEAR";

            var rv = factory.createResultValue();
            rv.tierName = searchTier;
            rv.range = new Range(startIndex, startIndex + length, true);
            rv.data = (match.value != null ? new IPATranscript(match.value) : new IPATranscript());
            result.addResultValue(rv);

            var aligned = (alignment != null ? alignment.getAligned(match.value.audiblePhones()): null);
            var alignedIpaElements = (aligned != null ? new IPATranscript(aligned) : new IPATranscript());

            var alignedStartIdx = (alignedIpaElements.length() > 0 ? alignedIpa.indexOf(alignedIpaElements.elementAt(0)) : 0);
            var alignedEndIdx = (alignedIpaElements.length() > 0 ? alignedIpa.indexOf(alignedIpaElements.elementAt(alignedIpaElements.length() - 1)): 0);
            var alignedSubIpa = (alignedIpaElements.length() > 0 ? alignedIpa.subsection(alignedStartIdx, alignedEndIdx + 1) : new IPATranscript());

            if(alignedFilter.isUseFilter()) {
                if(!alignedFilter.check_filter(alignedSubIpa)) {
                    continue;
                }
            }

            if (includeAligned == true) {
                var targetIPA = (searchTier == "IPA Target" ? match.value : alignedIpaElements);
                var actualIPA = (searchTier == "IPA Target" ? alignedIpaElements : match.value);

                var subAlignment = (alignment != null ? alignment.getSubAlignment(targetIPA, actualIPA) : new PhoneMap(targetIPA, actualIPA));

                var alignedRv = factory.createResultValue();
                alignedRv.tierName = (searchTier == "IPA Target" ? "IPA Actual": "IPA Target");
                if (aligned != null && aligned.size() > 0) {
                    var alignedStart = alignedIpa.stringIndexOf(alignedSubIpa);
                    var alignedLength = alignedSubIpa.toString().length();

                    alignedRv.range = new Range(alignedStart, alignedStart + alignedLength, true);
                    alignedRv.data = alignedSubIpa;
                } else {
                    alignedRv.range = new Range(0, 0, true);
                    alignedRv.data = "";
                }

                result.addResultValue(alignedRv);
                result.schema = "ALIGNED";

                result.metadata.put("Alignment", subAlignment ? subAlignment.toString() : "");
            }

            if(filters.searchBy.includePositionalInfo == true) {
                var searchBy = (filters.searchBy.searchBySyllable == true ? "Syllable" : filters.searchBy.searchBy);
                result.metadata.put("Position in " + searchBy, match.position);
            }

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
