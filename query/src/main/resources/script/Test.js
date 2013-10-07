var GroupFilter = require("lib/GroupFilter.js").GroupFilter;
var PatternFilter = require("lib/PatternFilter.js").PatternFilter;
var ParticipantFilter = require("lib/ParticipantFilter.js").ParticipantFilter;

var filters = {
	"pattern": new PatternFilter("filters.pattern"),
	"group": new GroupFilter("filters.group"),
	"participant": new ParticipantFilter("filters.participant")
}

var param_setup = function(params) {
	filters.pattern.param_setup(params);
	filters.group.param_setup(params);	
    filters.participant.param_setup(params);
}

var query_record = function(record) {
    var speaker = record.speaker;
    if(!filters.participant.check_speaker(speaker)) {
        return;
    }
    
	var groups = filters.group.getRequestedGroups(record);
	for(i = 0; i < groups.length; i++) {
		var group = groups[i];
		var ipa = group.IPATarget;

		var matches =
			filters.pattern.find_pattern(ipa);
		for(j = 0; j < matches.length; j++) {
			var match = matches[j];
			var result = factory.createResult();
			result.recordIndex = 0;
			result.schema = "LINEAR";

			var rv = factory.createResultValue();
			rv.tierName = "IPA Target";
			rv.groupIndex = group.groupIndex;
			rv.range = new Range(match.start, match.end);
			rv.data = match.value;
			result.resultValues.add(rv);
			
			results.addResult(result);
		}
	}
}

var end_search = function(session) {
	java.lang.System.out.println(results.size() + "");
}
