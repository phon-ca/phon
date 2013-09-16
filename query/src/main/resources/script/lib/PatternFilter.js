/**
 * Options for filtering/searching objects based on various patterns.
 *
 * Pattern languages supported:
 *  - plain text
 *  - regex
 *  - phonex
 *  - cv pattern
 *  - stress pattern
 * 
 * phonex, cv, and stress pattern matchers are only 
 * applicable to ipa tier values
 */

var HelpText = require("lib/PhonScriptConstants").HelpText;

exports.PatternType = {
    PLAIN: 0,
    REGEX: 1,
    PHONEX: 2,
    STRESS: 3,
    CV: 4
};
    
exports.PatternFilter = function(id) {

    var filterParamInfo = {
        "id": id+".filter",
        "title": "Expression:",
        "prompt": "Enter expression",
        "def": ""
    };
    var filterParam;
    this.filter = "";
    
    var filterTypeParamInfo = {
        "id": id+".filterType",
        "title": "Expression type:",
        "desc": [ "Plain text", "Regular expression", "Phonex", "Stress pattern", "CGV pattern" ],
        "def": 0
    };
    var filterTypeParam;
    this.filterType = { "index": 0, "toString": "Plain text" };
    
    var matchGroupParamInfo = {
        "id": [ id+".caseSensitive", id+".exactMatch" ],
        "title": "",
        "desc": [ "Case sensitive", "Exact match" ],
        "def": [ false, false ],
        "numCols": 2
    };
    var matchGroupParam;
    this.caseSensitive = false;
    this.exactMatch = false;
    
    var helpLabelParamInfo = {
        "title": "",
        "desc": ""
    };
    var helpLabelParam;
   
    var filterTypePromptText = [
        "Enter plain text expression",
        "Enter regular expression",
        "Enter phonex",
        "Enter stress pattern",
        "Enter CGV pattern"
    ];
    
    var filterTypeHelpText = [
        HelpText.plainTextHelpText,
        HelpText.regexHelpText,
        HelpText.phonexHelpText,
        HelpText.stressPatternHelpText,
        HelpText.cvPatternHelpText
    ];
    
    this.setEnabled = function(enabled) {
        filterParam.getEditorComponent().setEnabled(enabled);
        filterTypeParam.getEditorComponent().setEnabled(enabled);
        matchGroupParam.getEditorComponent().setEnabled(enabled);
        
        var idx = filterTypeParam.getEditorComponent().selectedIndex;
        var csEnabled = enabled && ( idx <= exports.PatternType.REGEX );
        matchGroupParam.getCheckbox(matchGroupParamInfo.id[0]).setEnabled(csEnabled);
    };
    
    var setPatternFilterInvalid = function(textField, message, loc) {
        var msg = (loc >= 0 ?
            "Error at index " + loc  +": " + message :
            message);
       
        textField.setToolTipText(msg);
        textField.setState("UNDEFINED");
    };
    
    var setPatternFilterOk = function(textField) {
        textField.setToolTipText("");
        textField.setState("INPUT");
    };
    
    /**
     * Set selected pattern type
     * 
     * type should be one of
     * PATTERN_TYPE_PLAIN, PATTERN_TYPE_REGEX, PATTERN_TYPE_PHONEX,
     * PATTERN_TYPE_STRESS, PATTERN_TYPE_CV
     *
     * @param type
     */
    this.setSelectedPatternType = function(type) {
        filterTypeParamInfo.def = type;
    };
    
    /* Check filter */
    var stressPatternRegex = /^([ ABUabu12][?+*]?)+$/;
    var cvPatternRegex = /^([ ABCVGcvg][?+*]?)+$/;
    
    var checkRegexFilter = function(filter) {
        var retVal = { 
            valid: false,
            message: "",
            loc: 0
        };
        try {
            var testPattern = java.util.regex.Pattern.compile(filter);
            retVal.valid = true;
        } catch (e) {
            retVal.valid = false;
            retVal.message = e.message;
            retVal.loc = e.javaException.index;
        }
        return retVal;
    };
    
    var checkPhonexFilter = function(filter) {
        var retVal = { 
            valid: false,
            message: "",
            loc: 0
        };
        try {
            Packages.ca.phon.phone.PhoneSequenceMatcher.compile(filter);
            retVal.valid = true;
        } catch (e) {
            retVal.valid = false;
            retVal.messge = e.message;
            retVal.loc = e.javaException.index;
        }
        return retVal;
    };
    
    var checkStressPatternFilter = function(filter) {
        var retVal = { 
            valid: false,
            message: "",
            loc: 0
        };
        retVal.valid = stressPatternRegex.test(filter);
        if(!retVal.valid) {
            retVal.messgae = "";
            retVal.loc = -1;
        }
        return retVal;
    };
    
    var checkCVPatternFilter = function(filter) {
        var retVal = { 
            valid: false,
            message: "",
            loc: 0
        };
        retVal.valid = cvPatternRegex.test(filter);
        if(!retVal.valid) {
            retVal.message = "";
            retVal.loc = -1;
        }
        return retVal;
    };
    
    // check the filter for errors
    // return value will have three properties
    //   valid:boolean, message:string, loc:int
    var checkFilter = function(filter, filterType) {
        var retVal = { 
            valid: false,
            message: "",
            loc: 0
        };
            
        
        switch(filterType) {
        case 0:
            retVal.valid = true;
            break; // plain text is always ok
            
        case 1:
            retVal = checkRegexFilter(filter);
            break;
            
        case 2:
            retVal = checkPhonexFilter(filter);
            break;
            
        case 3:
            retVal = checkStressPatternFilter(filter);
            break;
            
        case 4:
            retVal = checkCVPatternFilter(filter);
            break;
            
        default:
            retVal.valid = false;
            retVal.loc = -1;
            break;
        }
        
        return retVal;
    };
    
    var validateTextField = function(textField) {
        var txt = textField.getText();
        if(textField.getState() == Packages.ca.phon.gui.components.PromptedTextField.FieldState.PROMPT) 
            return;
        
        var filterType = filterTypeParam.getValue(filterTypeParamInfo.id);
        var filterCheck = checkFilter(txt, filterType.index);
        if(!filterCheck.valid) {
            setPatternFilterInvalid(textField, filterCheck.message, filterCheck.loc);
        } else {
            setPatternFilterOk(textField);
        }
    };
    
    this.param_setup = function(params) {
        // don't add a separator as this filter may be used inside a parent filter
        matchGroupParam = new MultiboolScriptParam(
            matchGroupParamInfo.id,
            matchGroupParamInfo.def,
            matchGroupParamInfo.desc,
            matchGroupParamInfo.title,
            matchGroupParamInfo.numCols);
            
        filterParam = new StringScriptParam(
            filterParamInfo.id,
            filterParamInfo.title,
            filterParamInfo.def);
        var textField = filterParam.getEditorComponent();
        textField.setPrompt(filterParamInfo.prompt);
       
        var filterListener = new java.awt.event.KeyListener() {
            keyPressed: function(e) {
            },
            
            keyReleased: function(e) {
                var textField = e.getSource();
                validateTextField(textField);
            },
            
            keyTyped: function(e) {
            }
       };
       filterParam.getEditorComponent().addKeyListener(filterListener);
        
        filterTypeParam = new EnumScriptParam(
            filterTypeParamInfo.id,
            filterTypeParamInfo.title,
            filterTypeParamInfo.def,
            filterTypeParamInfo.desc);
        var filterTypeListener = new java.awt.event.ItemListener() {
            itemStateChanged: function(e) {
                var idx = e.getSource().getSelectedIndex();
                
                var filterPrompt = filterTypePromptText[idx];
                var filterHelp = filterTypeHelpText[idx];
                
                filterParam.getEditorComponent().setPrompt(filterPrompt);
                helpLabelParam.getEditorComponent().setText(filterHelp);
                
                var caseSensitiveCb = matchGroupParam.getCheckbox(matchGroupParamInfo.id[0]);
                var enabled = ( idx  <= exports.PatternType.REGEX );
                caseSensitiveCb.setEnabled(enabled);
                
                validateTextField(filterParam.getEditorComponent());
            }
        };
        filterTypeParam.getEditorComponent().addItemListener(filterTypeListener);
        
        var helpLabelDesc = filterTypeHelpText[filterTypeParamInfo.def];
        helpLabelParam = new LabelScriptParam(
            helpLabelDesc,
            helpLabelParamInfo.title);
        helpLabelParam.getEditorComponent().setForeground(java.awt.Color.gray.darker());
        
        params.add(filterTypeParam);
        params.add(filterParam);
        
        params.add(matchGroupParam);
        
        var sepLine = new LabelScriptParam("<html>&nbsp;</html>", "");
     //   params.add(sepLine);
        
        params.add(helpLabelParam);
    };
    
    this.isUseFilter = function() {
        var txt = this.filter;
        
        if(StringUtils.strip(txt).length() > 0) {
            var filterCheck = checkFilter(this.filter, this.filterType.index);
            return filterCheck.valid;
        } else {
            return false;
        }
    };
    
    /* Check for occurances (or exact match) of entered filter */
    var checkPlain = function(obj, filter, caseSensitive, exactMatch) {
        if(exactMatch) {
            return obj.matchesPlain(filter, caseSensitive);
        } else {
            return obj.containsPlain(filter, caseSensitive);
        }
    };
    
    var checkRegex = function(obj, filter, caseSensitive, exactMatch) {
        if(exactMatch) {
            return obj.matchesRegex(filter, caseSensitive);
        } else {
            return obj.containsRegex(filter, caseSensitive);
        }
    };
    
    var checkPhonex = function(obj, filter, exactMatch) {
        if(exactMatch) {
            if(obj.matchesPhonex != undefined) {
                return obj.matchesPhonex(filter);
            } else
                return false;
        } else {
            if(obj.containsPhonex != undefined) {
                return obj.containsPhonex(filter);
            } else {
                return false;
            }
        }
    };
    
    var checkStressPattern = function(obj, filter, exactMatch) {
        if(exactMatch) {
            if(obj.matchesStressPattern != undefined) {
                return obj.matchesStressPattern(filter);
            } else
                return false;
        } else {
            if(obj.containsStressPattern != undefined) {
                return obj.containsStressPattern(filter);
            } else {
                return false;
            }
        }
    };
    
    var checkCVPattern = function(obj, filter, exactMatch) {
        if(exactMatch) {
            if(obj.matchesCVType != undefined) {
                return obj.matchesCVType(filter);
            } else
                return false;
        } else {
            if(obj.containsCVType != undefined) {
                return obj.containsCVType(filter);
            } else {
                return false;
            }
        }
    };
    
    /**
     * Check object for occurances (or exact match)
     * of filter.
     *
     * @param obj
     * @return true if filter matches, false otherwise
     */
    this.check_filter = function(obj) {
        var retVal = true;
        
        if(obj == null) return false;
        
        switch(this.filterType.index) {
        case 0:
            retVal = checkPlain(obj, this.filter, this.caseSensitive, this.exactMatch);
            break;
            
        case 1:
            retVal = checkRegex(obj, this.filter, this.caseSensitive, this.exactMatch);
            break;
            
        case 2:
            retVal = checkPhonex(obj, this.filter, this.exactMatch);
            break;
            
        case 3:
            retVal = checkStressPattern(obj, this.filter, this.exactMatch);
            break;
            
        case 4:
            retVal = checkCVPattern(obj, this.filter, this.exactMatch);
            break;
            
        default:
            retVal = false;
            break;
        };
        
        return retVal;
    };
    
    var findPlain = function(obj, filter, caseSensitive, exactMatch) {
        if(exactMatch) {
           if(obj.matchesPlain(filter, caseSensitive)) {
               return [ obj ];
           }
        } else {
            return obj.findPlain(filter, caseSensitive);
        }
    };
    
    var findRegex = function(obj, filter, caseSensitive, exactMatch) {
        if(exactMatch) {
            if(obj.matchesRegex(filter, caseSensitive)) {
                return [ obj ];
            } else {
                return new Array(); 
            }
        } else {
            return obj.findRegex(filter, caseSensitive);
        }
    };
    
    var findPhonex = function(obj, filter, exactMatch) {
        if(exactMatch) {
            if(obj.matchesPhonex != undefined) {
                if(obj.matchesPhonex(filter)) {
                    return [ obj ];   
                } else {
                    return new Array();
                }
            } else {
                return new Array();
            }
        } else {
            
            if(obj.findPhonex instanceof Function) {
                return obj.findPhonex(filter);
            } else {
                return new Array();
            }
        }
    };
    
    var findCVPattern = function(obj, filter, exactMatch) {
        if(exactMatch) {
            if(obj.matchesCVType != undefined) {
                if(obj.matchesCVType(filter)) {
                    return [ obj ];   
                } else {
                    return new Array();
                }
            } else {
                return new Array();
            }
        } else {
            if(obj.findCVType != undefined) {
                return obj.findCVType(filter);
            } else {
                return new Array();
            }
        }
    };
    
    var findStressPattern = function(obj, filter, exactMatch) {
        if(exactMatch) {
            if(obj.matchesStressPattern != undefined) {
                if(obj.matchesStressPattern(filter)) {
                    return [ obj ];   
                } else {
                    return new Array();
                }
            } else {
                return new Array();
            }
        } else {
            if(obj.findStressPattern != undefined) {
                return obj.findStressPattern(filter);
            } else {
                return new Array();
            }
        }
    };
    
    /**
     * Returns all occurances of the indicated pattern
     * within the given object.
     *
     * @param obj
     * @return occurances of the pattern
     */
    this.find_pattern = function(obj) {
        var retVal = new Array();
        
        if(obj == null) return retVal;
        
        switch(this.filterType.index) {
        case 0:
            retVal = findPlain(obj, this.filter, this.caseSensitive, this.exactMatch);
            break;
            
        case 1:
            retVal = findRegex(obj, this.filter, this.caseSensitive, this.exactMatch);
            break;
            
        case 2:
            retVal = findPhonex(obj, this.filter, this.exactMatch);
            break;
            
        case 3:
            retVal = findStressPattern(obj, this.filter, this.exactMatch);
            break;
            
        case 4:
            retVal = findCVPattern(obj, this.filter, this.exactMatch);
            break;
            
        default:
            break;
        };
        
        return retVal;
    };
    
}
