/*
 * Constants used in various Phon query scripts
 */
 
exports.HelpText = {

	/* 
	 * Help label values
	 */
	"phonexHelpText": 
	    "<html><b>.</b> = any phone, <b>\\c or {c}</b> = consonant, <b>\\v or {v}</b> = vowel<br/>" 
	    +"<b>.:o</b> = in onset, <b>[\\c\\v]</b> = consonant or vowel, <b>'[bp]'</b> = b or p (regex)<br/>" 
	    +"<b>*</b> = zero or more, <b>+</b> = one or more, <b>?</b> = zero or one</html>",
	
	"cvPatternHelpText": 
	    "<html><b>C</b> = consonant, <b>G</b> = glide,  <b>V</b> = vowel<br/>" 
	    +"<b>B</b> = anything, <b>A</b> = anything but [space], <b>[space]</b> = word boundary<br/>" 
	    +"<b>*</b> = zero or more, <b>+</b> = one or more, <b>?</b> = zero or one</html>",
	
	"stressPatternHelpText": 
	    "<html><b>1</b> = primary stress, <b>2</b> = secondary stress, <b>U</b> = no stress<br/>"
        +"<b>B</b> = anything, <b>A</b> = anything but [space], <b>[space]</b> = word boundary<br/>"
		+"<b>*</b> = zero or more, <b>+</b> = one or more, <b>?</b> = zero or one</html>",
	
	"plainTextHelpText": "",
	
	"regexHelpText": ""

};

exports.ResultType = {
    LINEAR: "LINEAR",
    ALIGNED: "ALIGNED",
    DETECTOR: "DETECTOR"
};