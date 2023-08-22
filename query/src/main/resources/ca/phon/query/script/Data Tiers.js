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
		alignedResults: tierAlignedResults,
		alignedMeta: tierAlignedMeta
	});
	if(filters.word.isUseFilter()) {
		// search by word
	}
}
