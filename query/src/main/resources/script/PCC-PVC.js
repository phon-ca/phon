/*
params = {separator, "Information", false},
			{label, "<html><p>This script calculates PCC/PVC (percent consonants/vowels correct)<br/> for each group in the selected sessions.</p></html>", ""},
			{label, "<html><br/><p>Results are reported as:<br/>&lt;# correct&gt;/&lt;# attempted&gt;;&lt;# deleted&gt;;&lt;# epenthesized&gt;</p></html>", ""}
        ;
*/

var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var PccOptions = require("lib/Pcc").PccOptions;
var Pcc = require("lib/Pcc").Pcc;


var pccOptions = {
    "standard": new PccOptions("pccOptions.standard"),
    "aligned": new PccOptions("pccOptions.aligned")
};

var filters = {
	"group": new GroupFilter("filters.group"),
	"alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
	"speaker": new ParticipantFilter("filters.speaker")
};


function param_setup(params) {
    var sep = new SeparatorScriptParam("PCC/PVC Options", false);
    params.add(sep);
    
    var pccStandardLbl = new LabelScriptParam("", "<html><b>PCC/PVC (standard)</b></html>");
    params.add(pccStandardLbl);
    pccOptions.standard.param_setup(params);

	var pccAlignedLbl = new LabelScriptParam("", "<html><b>PCC/PVC (aligned)</b></html>");
	params.add(pccAlignedLbl);
	pccOptions.aligned.param_setup(params);
	
	filters.group.param_setup(params);
	
	var sep = new LabelScriptParam("", "Aligned Group Filter");
	params.add(sep);
	filters.alignedGroup.param_setup(params);
	filters.speaker.param_setup(params);
}

/********************************
 * query_record (required)
 *
 * Called for each record in a session.
 * Perform search operations here.
 *
 * params:
 *	record - current record
 * returns:
 *	void
 *******************************/
function query_record(record)
{
    var searchTier = "IPA Target";
    if(!filters.speaker.check_speaker(record.speaker)) return;
    
	var searchObjects = filters.group.getRequestedGroups(record, searchTier);
	
	// check aligned group for each group returned
	if(filters.alignedGroup.isUseFilter()) {
	    searchObjects = filters.alignedGroup.filter_groups(record, searchObjects);
	}
	
	for(var gIdx = 0; gIdx < searchObjects.length; gIdx++) {
	    var ipaTGroup = searchObjects[gIdx];
	    var ipaAGroup = record.getGroup("IPA Actual", ipaTGroup.groupIndex);
	    
	    var metadata = new Metadata();
	    pccOptions.standard.setup_pcc_standard_metadata(ipaTGroup, ipaAGroup, metadata);
	    pccOptions.aligned.setup_pcc_aligned_metadata(record, ipaTGroup, ipaAGroup, metadata);
	    
	    var result = [ ipaTGroup, ipaAGroup ];
	    results.add(result, metadata, "ALIGNED");
	}
}
