var GroupFilter = require("lib/GroupFilter").GroupFilter;
var AlignedGroupFilter = require("lib/TierFilter").TierFilter;
var WordFilter = require("lib/WordFilter").WordFilter;
var AlignedWordFilter = require("lib/TierFilter").TierFilter;
var SyllableFilter = require("lib/SyllableFilter").SyllableFilter;
var ParticipantFilter = require("lib/ParticipantFilter").ParticipantFilter;
var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;
var Pcc = require("lib/Pcc").Pcc;
var PccOptions = require("lib/Pcc").PccOptions;
var StressPatternOptions = require("lib/StressPattern").StressPatternOptions;
var CvPatternOptions = require("lib/CvPattern").CvPatternOptions;
var ResultType = require("lib/PhonScriptConstants").ResultType;



/********************************
 * Setup params
 *******************************/

var processes = [
    {
        "name": "Custom",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"\""
            }
        ]
    },
    {
        "name": "Coronal Backing",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"{velar}\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"{cor}\""
            }
        ]
    },
    {
        "name": "Deaffrication",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"{aff}\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"{-aff}\""
            }
        ]
    },
    {
        "name": "Deletion",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"\\\\w\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"^$\""
            }
        ]
    },
    {
        "name": "Devoicing",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"{voiced}\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"{voiceless}\""
            }
        ]
    },
    {
        "name": "Gliding & Vocalization",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"{liquid}\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"[\\\\g\\\\v]\""
            }
        ]
    },
    {
        "name": "Glottalization",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"{-glottal}\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"{glottal}\""
            }
        ]
    },
    {
        "name": "Lateralization",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"{-lat}\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"{lat}\""
            }
        ]
    },
    {
        "name": "Singleton Onset Deletion",
        "params": [ 
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"^(?<\\\\s?\\\\c:L*)\\\\c:o(?>\\\\w:sctype('-Onset'))\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"^$\""
            }
        ]
    },
    {
        "name": "Onset Simplification",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"\\\\w:O<2>\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\".\""
            }
        ]
    },
    {
        "name": "Complex Onset Deletion",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"\\\\w:O<2,>\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"^$\""
            }
        ]
        
    },
    {
        "name": "Stopping",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"{-stop}\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"{stop}\""
            }
        ]
    },
    {
        "name": "Velar & Palatal Fronting",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"[{velar,-cor,-labial}{palatal,-cor,-labial}]\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"{cor}\""
            }
        ]
    },
    {
        "name": "Voicing",
        "params": [
            {
                "object": "filters.primary",
                "function": "setPattern",
                "value": "\"{voiceless}\""
            },
            {
                "object": "filters.column",
                "function": "setCaseSensitive",
                "value": "false"
            },
            {
                "object": "filters.column",
                "function": "setExactMatch",
                "value": "true"
            },
            {
                "object": "filters.column",
                "function": "setPattern",
                "value": "\"{voiced}\""
            }
        ]
    }
];
var processSelectionParamInfo = {
    "id": "processSelection",
    "title": "Process",
    "def": 0,
};
var processSelectionParam;

var filters = {
    "primary": new PatternFilter("filters.primary"),
    "column": new PatternFilter("filters.column"),
    "targetResultFilter": new PatternFilter("filters.targetResultFilter"),
    "actualResultFilter": new PatternFilter("filters.actualResultFilter"),
    "group": new GroupFilter("filters.group"),
    "groupPattern": new PatternFilter("filters.groupPattern"),
    "alignedGroup": new AlignedGroupFilter("filters.alignedGroup"),
    "word": new WordFilter("filters.word"),
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

var processNameParamInfo = {
	"id": "processName",
	"title": "Process Name",
	"def": "Custom",
	"prompt": "Enter process name"
};
var processNameParam;

function setup_params(params) {

    processSelectionParam = createProcessSelection();
    params.add(processSelectionParam);

	processNameParam = new StringScriptParam(
			processNameParamInfo.id,
			processNameParamInfo.title,
			processNameParamInfo.def);
     processNameParam.setPrompt(processNameParamInfo.prompt);
	params.add(processNameParam);

	var ipaTargetSepParam = new SeparatorScriptParam("IPA Target Options", false);
	params.add(ipaTargetSepParam);
	filters.primary.setSelectedPatternType(PatternType.PHONEX);
	filters.primary.param_setup(params);
	filters.primary.set_required(true);

	var ipaActualSepParam = new SeparatorScriptParam("IPA Actual Options", false);
	params.add(ipaActualSepParam);
	filters.column.setSelectedPatternType(PatternType.PHONEX);
	filters.column.param_setup(params);
	
	// setup result filter section
	var resultFilterSection = new SeparatorScriptParam("Aligned Phones", true);
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
	var sep = new LabelScriptParam("", "<html><b>Aligned Group</b></html>");
	params.add(sep);
	filters.alignedGroup.param_setup(params);
	
	filters.word.param_setup(params);
	filters.wordPattern.param_setup(params);
    filters.wordPattern.setEnabled(false);
	var wordsep = new LabelScriptParam("", "<html><b>Aligned Word</b></html>");
    params.add(wordsep);
    filters.alignedWord.param_setup(params);
    var searchByWordListener = new java.beans.PropertyChangeListener {
        propertyChange: function(e) {
            var enabled = e.source.getValue(e.source.paramId);
            filters.wordPattern.setEnabled(enabled);
            filters.alignedWord.setEnabled(enabled);
        }    
    };
    filters.word.searchByWordOpt.addPropertyChangeListener(filters.word.searchByWordOpt.paramId, searchByWordListener);
    var enabled = filters.word.searchByWordOpt.getValue(filters.word.searchByWordOpt.paramId);
    filters.wordPattern.setEnabled(enabled);
    filters.alignedWord.setEnabled(enabled);
    
	filters.syllable.param_setup(params);
	filters.speaker.param_setup(params);
}

function createProcessSelection() {
    // get a list of all process names
    var processNames = [];
    for(i = 0; i < processes.length; i++) {
        name = processes[i]["name"];
        processNames.push(name);
    }
    
    var retVal = new EnumScriptParam(
        processSelectionParamInfo.id,
        processSelectionParamInfo.title,
        processSelectionParamInfo.def,
        processNames);
    
     var processSelectionListener = new java.beans.PropertyChangeListener() {
            propertyChange: function(e) {
                var selectedIdx = processSelectionParam.getValue(processSelectionParamInfo.id).index;
                if(selectedIdx >= 0) {
                    var process = processes[selectedIdx];
                    
                    processNameParam.setValue(processNameParamInfo.id, process["name"]);
                    
                    var params = process["params"];
                    for(i = 0; i < params.length; i++) {
                        var param = params[i];
                        var setParam = param["object"] + "." + param["function"] + "(" + param["value"] + ");";
                        eval(setParam);
                    }
                    
                }
            }
     };
     retVal.addPropertyChangeListener(processSelectionListener);
     
    return retVal;
}

function run(context) {
	
}
