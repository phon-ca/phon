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
 * Constants used in various Phon query scripts
 */
 
exports.HelpText = {

	/* 
	 * Help label values
	 */
	"phonexHelpText": 
	    "<html><b>.</b> = any phone, <b>\\c or {c}</b> = consonant, <b>\\v or {v}</b> = vowel<br/>" 
	    +"<b>.:o</b> = in onset, <b>[\\c\\v]</b> = consonant or vowel, <b>'[bp]'</b> = b or p (regex)<br/>" 
	    +"<b>^</b> = beginning of input, <b>$</b> = end of input<br/>" 
	    +"<b>*</b> = zero or more, <b>+</b> = one or more, <b>?</b> = zero or one</html>",
	
	"cvPatternHelpText": 
	    "<html><b>C</b> = consonant, <b>G</b> = glide,  <b>V</b> = vowel<br/>" 
	    +"<b>B</b> = anything, <b>A</b> = anything but [space], <b>[space]</b> = word boundary<br/>" 
	    +"<b>*</b> = zero or more, <b>+</b> = one or more, <b>?</b> = zero or one</html>",
	
	"stressPatternHelpText": 
	    "<html><b>1</b> = primary stress, <b>2</b> = secondary stress, <b>U</b> = no stress<br/>"
        +"<b>A</b> = anything but [space], <b>[space]</b> = word boundary<br/>"
		+"<b>*</b> = zero or more, <b>+</b> = one or more, <b>?</b> = zero or one</html>",
	
	"plainTextHelpText": "",
	
	"regexHelpText": ""

};

exports.ResultType = {
    LINEAR: "LINEAR",
    ALIGNED: "ALIGNED",
    DETECTOR: "DETECTOR"
};