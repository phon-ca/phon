/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

exports.PatternFilter = function (id) {
    
    var filterParamInfo = {
        "id": id + ".filter",
        "title": "Expression:",
        "prompt": "Enter expression",
        "def": ""
    };
    var filterParam;
    this.filter = filterParamInfo.def;
    
    var filterTypeParamInfo = {
        "id": id + ".filterType",
        "title": "Expression type:",
        "desc":[ "Plain text", "Regular expression", "Phonex", "Stress pattern", "CGV pattern"],
        "def": 0
    };
    var filterTypeParam;
    this.filterType = {
        "index": 0, "toString": "Plain text"
    };
    
    var matchGroupParamInfo = {
        "id":[id + ".caseSensitive", id + ".exactMatch"],
        "title": "",
        "desc":[ "Case sensitive", "Exact match"],
        "def":[ false, false],
        "numCols": 2
    };
    var matchGroupParam;
    this.caseSensitive = matchGroupParamInfo.def[0];
    this.exactMatch = matchGroupParamInfo.def[1];
    
    var helpLabelParamInfo = {
        "title": "",
        "desc": ""
    };
    var helpLabelParam;
    
    var filterTypePromptText =[
    "Enter plain text expression",
    "Enter regular expression",
    "Enter phonex",
    "Enter stress pattern",
    "Enter CGV pattern"];
    
    var filterTypeHelpText =[
    HelpText.plainTextHelpText,
    HelpText.regexHelpText,
    HelpText.phonexHelpText,
    HelpText.stressPatternHelpText,
    HelpText.cvPatternHelpText];
    
    this.setEnabled = function (e) {
        var enabled = (e == true);
        filterParam.setEnabled(enabled);
        filterTypeParam.setEnabled(enabled);
        matchGroupParam.setEnabled(enabled);
    };
    
    this.setVisible = function (v) {
        var visible = (v == true);
        filterParam.setVisible(visible);
        filterTypeParam.setVisible(visible);
        matchGroupParam.setVisible(visible);
    };
    
    var setPatternFilterInvalid = function (message, loc) {
        var msg = (loc >= 0 ?
        "Error at index " + loc + ": " + message:
        message);
       	filterParam.setValidate(false);
       	filterParam.setTooltipText(message);
    };
    
    var setPatternFilterOk = function () {
    	filterParam.setValidate(true);
    	filterParam.setTooltipText(null);
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
    this.setSelectedPatternType = function (type) {
        filterTypeParamInfo.def = type;
    };
    
    /**
     * Set pattern value
     * 
     * @param value
     */
    this.setPattern = function (pattern) {
    	filterParamInfo.def = pattern;
    }
    
    /* Check filter */
    var stressPatternRegex = /^([ ABUabu12][?+*]?)+$/;
    var cvPatternRegex = /^([ ABCVGcvg][?+*]?)+$/;
    
    var checkRegexFilter = function (filter) {
        var retVal = {
            valid: false,
            message: "",
            loc: 0
        };
        try {
            var testPattern = java.util.regex.Pattern.compile(filter);
            retVal.valid = true;
        }
        catch (e) {
            retVal.valid = false;
            retVal.message = e.message;
            retVal.loc = e.javaException.index;
        }
        return retVal;
    };
    
    var checkPhonexFilter = function (filter) {
        var retVal = {
            valid: false,
            message: "",
            loc: 0
        };
        try {
            Packages.ca.phon.phonex.PhonexPattern.compile(filter);
            retVal.valid = true;
        }
        catch (e) {
            retVal.valid = false;
            retVal.messge = e.message;
            retVal.loc = e.javaException.index;
        }
        return retVal;
    };
    
    var checkStressPatternFilter = function (filter) {
        var retVal = {
            valid: false,
            message: "",
            loc: 0
        };
        retVal.valid = stressPatternRegex.test(filter);
        if (! retVal.valid) {
            retVal.messgae = "";
            retVal.loc = -1;
        }
        return retVal;
    };
    
    var checkCVPatternFilter = function (filter) {
        var retVal = {
            valid: false,
            message: "",
            loc: 0
        };
        retVal.valid = cvPatternRegex.test(filter);
        if (! retVal.valid) {
            retVal.message = "";
            retVal.loc = -1;
        }
        return retVal;
    };
    
    // check the filter for errors
    // return value will have three properties
    //   valid:boolean, message:string, loc:int
    var checkFilter = function (filter, filterType) {
        var retVal = {
            valid: false,
            message: "",
            loc: 0
        };
        
        
        switch (filterType) {
            case 0:
            retVal.valid = true;
            break;
            // plain text is always ok
            
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
    
    var validatePattern = function () {
        var txt = filterParam.getValue(filterParamInfo.id);
        
        if(txt.trim().length() == 0) {
        	return true;
        }
        
        var filterType = filterTypeParam.getValue(filterTypeParamInfo.id);
        var filterCheck = checkFilter(txt, filterType.index);
        if (! filterCheck.valid) {
            setPatternFilterInvalid(filterCheck.message, filterCheck.loc);
        } else {
            setPatternFilterOk();
        }
    };
    
    this.set_required = function(required) {
    	filterParam.setRequired(required);
    }
    
    this.param_setup = function (params) {
        // don't add a separator as this filter may be used inside a parent filter
        matchGroupParam = new MultiboolScriptParam(
        matchGroupParamInfo.id,
        matchGroupParamInfo.def,
        matchGroupParamInfo.desc,
        matchGroupParamInfo.title,
        matchGroupParamInfo.numCols);
        matchGroupParam.setEnabled(0, false);
        
        filterParam = new StringScriptParam(
        filterParamInfo.id,
        filterParamInfo.title,
        filterParamInfo.def);
        filterParam.setPrompt(filterParamInfo.prompt);
        
        var filterListener = new java.beans.PropertyChangeListener() {
        	propertyChange: function(e) {
        		validatePattern();
        	}
        };
        filterParam.addPropertyChangeListener(filterParamInfo.id, filterListener);
        
        filterTypeParam = new EnumScriptParam(
        filterTypeParamInfo.id,
        filterTypeParamInfo.title,
        filterTypeParamInfo.def,
        filterTypeParamInfo.desc);
        var filterTypeListener = new java.beans.PropertyChangeListener() {
            propertyChange: function(e) {
                // setup help label
                var idx = e.source.getValue(e.source.paramId).index;
                
                // PHONEX
                if(idx >= exports.PatternType.PHONEX) {
                    matchGroupParam.setEnabled(0, false); 
                } else {
                    matchGroupParam.setEnabled(0, true);
                }
                
                var filterPrompt = filterTypePromptText[idx];
                var filterHelp = filterTypeHelpText[idx];
                
                filterParam.setPrompt(filterPrompt);
                helpLabelParam.setText(filterHelp);
            }  
        };
        filterTypeParam.addPropertyChangeListener(filterTypeParamInfo.id, filterTypeListener);
        
        var helpLabelDesc = filterTypeHelpText[filterTypeParamInfo.def];
        helpLabelParam = new LabelScriptParam(
        helpLabelDesc,
        helpLabelParamInfo.title);
        
        params.add(filterTypeParam);
        params.add(filterParam);
        
        params.add(matchGroupParam);
        
        var sepLine = new LabelScriptParam("<html>&nbsp;</html>", "");
        //   params.add(sepLine);
        
        params.add(helpLabelParam);
    };
    
    this.isUseFilter = function () {
        var txt = this.filter;
        
        if (txt.length() > 0) {
            var filterCheck = checkFilter(this.filter, this.filterType.index);
            return filterCheck.valid;
        } else {
            return false;
        }
    };
    
    /* Check for matches (or exact match) of entered filter */
    var checkPlain = function (obj, filter, caseSensitive, exactMatch) {
        var strA = (caseSensitive == true ? obj.toString(): obj.toString().toLowerCase());
        var strB = (caseSensitive == true ? filter: filter.toLowerCase());
        
        if (exactMatch == true) {
            return (strA == strB);
        } else {
            return (strA.indexOf(strB) >= 0);
        }
    };
    
    var checkRegex = function (obj, filter, caseSensitive, exactMatch) {
        var regexPattern = java.util.regex.Pattern.compile(filter, (caseSensitive == true? 0: java.util.regex.Pattern.CASE_INSENSITIVE));
        var regexMatcher = regexPattern.matcher(obj.toString());
        if (exactMatch == true) {
            return regexMatcher.matches();
        } else {
            return regexMatcher.find();
        }
    };
    
    var checkPhonex = function (obj, filter, exactMatch) {
        if (!(obj instanceof IPATranscript)) return false;
        
        if (exactMatch == true) {
            return obj.matches(filter);
        } else {
            return obj.contains(filter);
        }
    };
    
    var checkStressPattern = function (obj, filter, exactMatch) {
    	if (!(obj instanceof IPATranscript)) return false;
    	
    	if(exactMatch == true) {
    		return obj.matchesStressPattern(filter);
    	} else {
    		return obj.containsStressPattern(filter);
    	}
    };
    
    var checkCVPattern = function (obj, filter, exactMatch) {
    	if (!(obj instanceof IPATranscript)) return false;
    	
    	if(exactMatch == true) {
    		return obj.matchesCVPattern(filter);
    	} else {
    		return obj.containsCVPattern(filter);
    	}
    };
    
    /**
     * Check object for occurances (or exact match)
     * of filter.
     *
     * @param obj
     * @return true if filter matches, false otherwise
     */
    this.check_filter = function (obj) {
        var retVal = true;
        
        if (obj == null) {
        	if(this.filterType.index == 2)
        		obj = new IPATranscript();
        	else
        		obj = new String();
        }
        
        switch (this.filterType.index) {
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
    
    var findPlain = function (obj, filter, caseSensitive, exactMatch) {
        var retVal = new Array();
        
        var strA = (caseSensitive == true ? obj.toString(): obj.toString().toLowerCase());
        var strB = (caseSensitive == true ? filter: filter.toLowerCase());
        
        if (exactMatch == true) {
            if (strA.equals(strB)) {
                var v = {
                    start: 0, end: strA.length(), value: obj
                };
                retVal.push(v);
            }
        } else {
            var i = 0;
            while ((i = strA.indexOf(strB, i)) >= 0) {
            	var myValue;
            	if(obj instanceof IPATranscript) {
            		var startIpaIdx = obj.ipaIndexOf(i);
            		var endIpaIdx = obj.ipaIndexOf(i+strB.length()-1);
            		myValue = obj.subsection(startIpaIdx, endIpaIdx+1);
            	} else {
            		myValue = strA.substring(i, i+strB.length());
            	}
                var v = {
                    start: i, end: i + strB.length(), value: myValue
                };
                
                retVal.push(v);
                i += strB.length();
            }
        }
        
        return retVal;
    };
    
    var findRegex = function (obj, filter, caseSensitive, exactMatch) {
        var regexPattern = java.util.regex.Pattern.compile(filter, (caseSensitive == true ? 0: java.util.regex.Pattern.CASE_INSENSITIVE));
        var regexMatcher = regexPattern.matcher(obj.toString());
        var retVal = new Array();
        
        if (exactMatch == true) {
            if (regexMatcher.matches()) {
                v = {
                    start: 0, end: obj.toString().length(), value: obj
                };
                retVal.push(v);
            } else {
                return new Array();
            }
        } else {
            while(regexMatcher.find()) {
            	var myValue;
            	if(obj instanceof IPATranscript) {
            		var startIpaIdx = obj.ipaIndexOf(regexMatcher.start());
            		var endIpaIdx = obj.ipaIndexOf(regexMatcher.end()-1);
            		myValue = obj.subsection(startIpaIdx, endIpaIdx+1);
            	} else {
            		myValue = regexMatcher.group();
            	}
                v = {
                    start: regexMatcher.start(), end: regexMatcher.end(), value: myValue, matcher: regexMatcher
                };
                
                retVal.push(v);
            }
        }
        
        return retVal;
    };
    
    var findPhonex = function (obj, filter, exactMatch) {
        var retVal = new Array();
        
        if (!(obj instanceof IPATranscript)) return retVal;
        
        var phonexPattern = PhonexPattern.compile(filter);
        var phonexMatcher = phonexPattern.matcher(obj);

        if (exactMatch == true) {
            if (phonexMatcher.matches()) {
                v = {
                    start: 0, end: obj.length(), value: obj, matcher: phonexMatcher
                };
                retVal.push(v);
            }
        } else {
            while (phonexMatcher.find()) {
            	var groupData = new Array();
            	
            	for(grpIdx = 1; grpIdx <= phonexMatcher.groupCount(); grpIdx++) {
            		grpName = phonexPattern.groupName(grpIdx);
            		if(phonexMatcher.start(grpIdx) >= 0) {
	            		groupData[grpIdx] = {
	            				start: phonexMatcher.start(grpIdx),
	            				end: phonexMatcher.end(grpIdx),
	            				value: new IPATranscript(phonexMatcher.group(grpIdx))
	            		};
	            		if(grpName)
	            			groupData[grpName] = groupData[grpIdx];
            		} else {
            			if(grpName)
            				groupData[grpName] = { start:-1, end:-1, value: new IPATranscript() };
            		}
            	}
            	
                v = {
                    start: phonexMatcher.start(), end: phonexMatcher.end(), value: new IPATranscript(phonexMatcher.group()),
                    	groups:groupData
                };
                retVal.push(v);
            }
        }
        
        return retVal;
    };
    
    var findCVPattern = function (obj, filter, exactMatch) {
        var retVal = new Array();
        
        if(!(obj instanceof IPATranscript)) return retVal;
        
        if(exactMatch == true) {
        	if(obj.matchesCVPattern(filter)) {
        		v = {
        			start: 0, end: obj.length(), value: obj
        		};
        		retVal.push(v);
        	}
        } else {
        	var found = obj.findCVPattern(filter);
        	
        	for(i = 0; i < found.size(); i++) {
        		var subT = found.get(i);
        		var subTStart = obj.indexOf(subT);
        		v = {
        			start: subTStart, end: subTStart + subT.length(), value: subT
        		};
        		retVal.push(v);
        	}
        }
        
        return retVal;
    };
    
    var findStressPattern = function (obj, filter, exactMatch) {
        var retVal = new Array();
        
        if(!(obj instanceof IPATranscript)) return retVal;
        
        if(exactMatch == true) {
        	if(obj.matchesStressPattern(filter)) {
        		v = {
        			start: 0, end: obj.length(), value: obj
        		};
        		retVal.push(v);
        	}
        } else {
        	var found = obj.findStressPattern(filter);
        	
        	for(i = 0; i < found.size(); i++) {
        		var subT = found.get(i);
        		var subTStart = obj.indexOf(subT);
        		v = {
        			start: subTStart, end: subTStart + subT.length(), value: subT
        		};
        		retVal.push(v);
        	}
        }
        
        return retVal;
    };
    
    /**
     * Returns all occurances of the indicated pattern
     * within the given object.
     *
     * @param obj
     * @return occurances of the pattern.  The results will be a list
     *  of items conforming to the following protocol:
     *
     *  {
     *     start:int // the start index of the match
     *     end:int   // the end index of the match
     *     value:object // the value of the match
     *  }
     */
    this.find_pattern = function (obj) {
        var retVal = new Array();
        
        if (obj == null) return retVal;
        
        switch (this.filterType.index) {
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
    
    /**
     * Filters a list of group objects given a tier name and list of groups.
     *
     * @param groups
     * @return a list of filtered groups based on the setup of this filter
     */
    this.filter_groups = function (groups, tierName) {
        var retVal = new Array();
        
        for (var gIdx = 0; gIdx < groups.length; gIdx++) {
            var group = groups[gIdx];
            var groupVal = group.getTier(tierName);
            if (this.check_filter(groupVal) == true) {
                retVal.push(group);
            }
        }
        
        return retVal;
    };
}