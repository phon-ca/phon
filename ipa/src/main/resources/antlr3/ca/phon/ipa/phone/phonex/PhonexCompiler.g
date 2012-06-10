tree grammar PhonexCompiler;

options {
	tokenVocab = Phonex;
	ASTLabelType = CommonTree;
}

@header {
package ca.phon.ipa.phone.phonex;

import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import org.apache.commons.lang3.StringEscapeUtils;
import ca.phon.fsa.*;
import ca.phon.syllable.phonex.*;
import ca.phon.syllable.*;

import ca.phon.ipa.phone.*;

import ca.phon.fsa.*;
}

@members {

private final Stack<PhonexFSA> fsaStack = new Stack<PhonexFSA>();

private int groupIndex = 0;
private final Stack<Integer> groupStack = new Stack<Integer>();

private PhonexFSA primaryFSA = null;

public PhonexFSA getPrimaryFSA() {
	return this.primaryFSA;
}

private PhonexFSA getFSA() {
	if(fsaStack.isEmpty()) {
		primaryFSA = new PhonexFSA();
		fsaStack.push(primaryFSA);
	}
	return fsaStack.peek();
}

}

/**
 * Start
 */
expr returns [PhonexFSA fsa]
@init {
	getFSA();
	groupStack.push(groupIndex++);
}
	:	^(EXPR exprele+)
	{
		$fsa = primaryFSA;
	}
	;

/**
 * 
 */
exprele	:	matcher
	|	group
	|	boundary_matchers
	;

group
@init {
	boolean nonCapturing = (input.LA(3) == NON_CAPTURING_GROUP);
	if(!nonCapturing) {
		fsaStack.push(new PhonexFSA());
		groupStack.push(groupIndex++);
	}
}
@after {
	if(!nonCapturing)
		groupStack.pop();
}
	:	^(GROUP NON_CAPTURING_GROUP? exprele+ q=quantifier?)
	{
		String groupName = $GROUP.text;
		int groupIndex = groupStack.peek();
		
		// pop our group fsa, apply quantifier
		// and add it to the fsa now on top
		// of the stack
		PhonexFSA grpFsa = fsaStack.pop();
		
		if(!nonCapturing) {
			grpFsa.setGroupIndex(groupIndex);
		}
		if(q != null) {
			grpFsa.applyQuantifier(q);
		}
		
		// if the expression starts with a group
		// make the group expression the new
		// primary fsa
		if(getFSA() == primaryFSA && getFSA().getFinalStates().length == 0) {
			fsaStack.pop();
			fsaStack.push(grpFsa);
			
			// copy group names
			if(primaryFSA.getNumberOfGroups() > grpFsa.getNumberOfGroups())
				grpFsa.setNumberOfGroups(primaryFSA.getNumberOfGroups());
				
			for(int gIdx = 1; gIdx <= primaryFSA.getNumberOfGroups(); gIdx++) {
				String gName = primaryFSA.getGroupName(gIdx);
				if(gName != null)
					grpFsa.setGroupName(gIdx, gName);
			}
			primaryFSA = grpFsa;
		} else {
			getFSA().appendGroup(grpFsa);
		}
		
		if(!nonCapturing)
			if(groupIndex > primaryFSA.getNumberOfGroups())
					primaryFSA.setNumberOfGroups(groupIndex);
				
		// set group name (if available)
		if(!nonCapturing && !groupName.equals("GROUP")) {
			
			primaryFSA.setGroupName(groupIndex, groupName);
		}
	}
	;

matcher
scope {
	List<PhoneMatcher> pluginMatchers;
}
@init {
	$matcher::pluginMatchers = new ArrayList<PhoneMatcher>();
}
	:	^(MATCHER bm=base_matcher (pluginMatcher=plugin_matcher {$matcher::pluginMatchers.add($pluginMatcher.value);})* q=quantifier?)
	{
		// append matcher to fsa
		PhoneMatcher matcher = bm;
		
		PhoneMatcher[] pMatchers = new PhoneMatcher[$matcher::pluginMatchers.size()];
		for(int i = 0; i < $matcher::pluginMatchers.size(); i++)
			pMatchers[i] = PhoneMatcher.class.cast($matcher::pluginMatchers.get(i));
		
		
		if(q == null)
			getFSA().appendMatcher(matcher, pMatchers);
		else
			getFSA().appendMatcher(matcher, q, pMatchers);
	}
	|	^(groupIndex=back_reference (pluginMatcher=plugin_matcher {$matcher::pluginMatchers.add($pluginMatcher.value);})* q=quantifier?)
	{
		PhoneMatcher[] pMatchers = new PhoneMatcher[$matcher::pluginMatchers.size()];
		for(int i = 0; i < $matcher::pluginMatchers.size(); i++)
			pMatchers[i] = PhoneMatcher.class.cast($matcher::pluginMatchers.get(i));
		
		if(q == null)
			getFSA().appendBackReference(groupIndex, pMatchers);
		else
			getFSA().appendBackReference(groupIndex, q, pMatchers);
	}
	;

base_matcher returns [PhoneMatcher value]
	:	cm=class_matcher
	{	$value = cm;	}
	|	sp=single_phone_matcher
	{	$value = sp;	}
	|	cp=compound_matcher
	{	$value = cp;	}
	;
	
compound_matcher returns [PhoneMatcher value]
	:	^(COMPOUND_MATCHER m1=single_phone_matcher m2=single_phone_matcher)
	{
		$value = new CompoundPhoneMatcher(m1, m2);
	}
	;
	
single_phone_matcher returns [PhoneMatcher value]
	:	fs=feature_set_matcher
	{	$value = fs;	}
	|	bp=base_phone_matcher
	{	$value = bp;	}
	|	rp=regex_matcher
	{	$value = rp;	}
	|	pp=predefined_phone_class
	{	$value = pp;	}
	;
	
class_matcher returns [PhoneMatcher value]
scope {
	List<PhoneMatcher> innerMatchers;
}
@init {
	$class_matcher::innerMatchers = new ArrayList<PhoneMatcher>();
}
	:	^(PHONE_CLASS (innerMatcher=single_phone_matcher {$class_matcher::innerMatchers.add($innerMatcher.value);})+)
	{
		PhoneMatcher[] classMatchers = new PhoneMatcher[$class_matcher::innerMatchers.size()];
		for(int i = 0; i < $class_matcher::innerMatchers.size(); i++) {
			classMatchers[i] = (PhoneMatcher)$class_matcher::innerMatchers.get(i);
		}
		
		$value = new PhoneClassMatcher(classMatchers);
	}
	;

plugin_matcher returns [PhoneMatcher value]
scope {
	List<String> scTypes;
}
@init {
	$plugin_matcher::scTypes = new ArrayList<String>();
}
	:	^(PLUGIN STRING)
	{
		String typeName = $PLUGIN.text;
		PluginMatcher pluginMatcher = PhonexPluginManager.getSharedInstance().getMatcher(typeName);
		if(pluginMatcher != null) {
			$value = pluginMatcher.createMatcher(StringEscapeUtils.unescapeJava($STRING.text.substring(1, $STRING.text.length()-1)));
		}
	}
	|	^(PLUGIN (sc=negatable_identifier {$plugin_matcher::scTypes.add($sc.value);})+)
	{
		SyllabificationInfoMatcher retVal = new SyllabificationInfoMatcher();
		
		for(String scTypeString:$plugin_matcher::scTypes) {
			boolean not = false;
			if(scTypeString.startsWith("-")) {
				not = true;
				scTypeString = scTypeString.substring(1);
			}
			
			// try id chars first
			SyllableConstituentType scType = 
				SyllableConstituentType.fromString(scTypeString);
			if(scType != null) {
				if(not)
					retVal.getDisallowedTypes().add(scType);
				else
					retVal.getAllowedTypes().add(scType);
			}
		}
		$value = retVal;
	}
	;

back_reference returns [Integer groupNumber]
	:	BACK_REF
	{
		$groupNumber = Integer.parseInt($BACK_REF.text);
	}
	;
	
feature_set_matcher returns [FeatureSetMatcher matcher]
scope {
	List<String> features;
}
@init {
	$feature_set_matcher::features = new ArrayList<String>();
}
	:	^(FEATURE_SET (f=negatable_identifier {$feature_set_matcher::features.add($f.value);})*)
	{
		$matcher = new FeatureSetMatcher();
		
		for(String feature:$feature_set_matcher::features) {
			boolean not = false;
			if(feature.startsWith("-")) {
				not = true;
				feature = feature.substring(1);
			}
			if(not)
				matcher.addNotFeature(feature);
			else
				matcher.addRequiredFeature(feature);
		}
		
	}
	;
	
base_phone_matcher returns [PhoneMatcher matcher]
	:	LETTER
	{
		Character c = $LETTER.text.charAt(0);
		$matcher = new BasePhoneMatcher(c);
	}
	;
	
regex_matcher returns [PhoneMatcher matcher]
	:	STRING
	{
		RegexMatcher retVal = new RegexMatcher(
			StringEscapeUtils.unescapeJava($STRING.text.substring(1, $STRING.text.length()-1)));
		$matcher = retVal;
	}
	;
	
identifier returns [String value]
	:	^(NAME chars+=LETTER+)
	{
		$value = new String();
		for(Object obj:$chars) {
			$value += ((CommonTree)obj).getToken().getText();
		}
	}
	;
	
negatable_identifier returns [String value]
	:	^(NAME n=MINUS? chars+=LETTER+)
	{
		$value = ($n == null ? "" : "-");
		for(Object obj:$chars) {
			$value += ((CommonTree)obj).getToken().getText();
		}
	}
	;
	
quantifier returns [Quantifier value]
	:	^(QUANTIFIER quant=SINGLE_QUANTIFIER type=SINGLE_QUANTIFIER?)
	{
		$value = new Quantifier(QuantifierType.fromString($quant.text));
		
		if($type != null) {
			switch($type.text.charAt(0)) {
			case '?':
				$value.setTransitionType(TransitionType.RELUCTANT);
				break;
				
			case '*':
				$value.setTransitionType(TransitionType.GREEDY);
				break;
				
			case '+':
				$value.setTransitionType(TransitionType.POSSESSIVE);
				break;
			}
		}
	}
	|	^(QUANTIFIER bounded_quantifier type=SINGLE_QUANTIFIER?)
	{
		$value = $bounded_quantifier.value;
		
		if($type != null) {
			switch($type.text.charAt(0)) {
			case '?':
				$value.setTransitionType(TransitionType.RELUCTANT);
				break;
				
			case '*':
				$value.setTransitionType(TransitionType.GREEDY);
				break;
				
			case '+':
				$value.setTransitionType(TransitionType.POSSESSIVE);
				break;
			}
		}
	}
	;
	
bounded_quantifier returns [Quantifier value]
	:	^(BOUND_START INT)
	{
		$value = new Quantifier(
			Integer.parseInt($INT.text), -1);
	}
	|	^(BOUND_START x=INT y=INT)
	{
		$value = new Quantifier(
			Integer.parseInt($x.text), Integer.parseInt($y.text));
	}
	;
	
predefined_phone_class returns [PhoneMatcher value]
	:	P_PHONE_CLASS
	{
		char classChar = 
			($P_PHONE_CLASS.text.length() == 2 ? $P_PHONE_CLASS.text.charAt(1) : $P_PHONE_CLASS.text.charAt(0));
		
		switch(classChar) {
		case '.':
			$value = new FeatureSetMatcher();
			break;
			
		case 'c':
			FeatureSetMatcher cm = new FeatureSetMatcher();
			cm.addRequiredFeature("consonant");
			$value = cm;
			break;
		
		case 'v':
			FeatureSetMatcher vm = new FeatureSetMatcher();
			vm.addRequiredFeature("vowel");
			$value = vm;
			break;
			
		case 'w':
			PhoneClassMatcher wm = new PhoneClassMatcher();
			FeatureSetMatcher cfsm = new FeatureSetMatcher();
			cfsm.addRequiredFeature("consonant");
			wm.addMatcher(cfsm);
			FeatureSetMatcher vfsm = new FeatureSetMatcher();
			vfsm.addRequiredFeature("vowel");
			wm.addMatcher(vfsm);
			$value = wm;
			break;
			
		case 'W':
			break;
			
		case 's':
			break;
			
		default:
			$value = new PhoneMatcher() {
				public boolean matches(Phone p) {
					return false;
				}
				
				public boolean matchesAnything() {
					return false;
				}
			};
			break;
		}
	}
	;
	
boundary_matchers returns [PhoneMatcher value]
	:	BOUNDARY_MATCHER
	{
		char bChar = 
			($BOUNDARY_MATCHER.text.length() == 2 ? $BOUNDARY_MATCHER.text.charAt(1) : $BOUNDARY_MATCHER.text.charAt(0));
		
		switch(bChar) {
		case '^':
			getFSA().appendTransition(new BeginningOfInputTransition());
			break;
			
		case '$':
			getFSA().appendTransition(new EndOfInputTransition());
			break;
			
		case 'b':
			getFSA().appendTransition(new WordBoundaryTransition());
			break;
			
		case 'S':
			break;
			
		default:
			$value = new PhoneMatcher() {
				public boolean matches(Phone p) {
					return false;
				}
				
				public boolean matchesAnything() {
					return false;
				}
			};
			break;
		}
	}
	;