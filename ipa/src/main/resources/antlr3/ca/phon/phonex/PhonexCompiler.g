/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2011-2016 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
tree grammar PhonexCompiler;

options {
	tokenVocab = Phonex;
	ASTLabelType = CommonTree;
}

@header {
package ca.phon.phonex;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.logging.*;

import org.apache.commons.lang3.StringEscapeUtils;
import ca.phon.fsa.*;
import ca.phon.syllable.phonex.*;
import ca.phon.syllable.*;

import ca.phon.ipa.*;
import ca.phon.ipa.features.*;

import ca.phon.fsa.*;

import ca.phon.phonex.plugins.*;
}

@members {

private final static Logger LOGGER =
	Logger.getLogger(PhonexCompiler.class.getName());

private int flags = 0;

public int getFlags() {
	return this.flags;
}

public PhoneMatcher[] filterPluginMatchers(List<PhoneMatcher> pluginMatchers) {
	// only keep one of each of these matchers
	Map<Class<? extends CombinableMatcher>, CombinableMatcher> combinableMatchers = new HashMap<>(); 

	List<PhoneMatcher> pMatchers = new ArrayList<>();
	for(int i = 0; i < pluginMatchers.size(); i++) {
		PhoneMatcher pMatcher = pluginMatchers.get(i);
	
		if(pMatcher instanceof CombinableMatcher) {
			CombinableMatcher cm = combinableMatchers.get(pMatcher.getClass());
			if(cm != null) {
				cm.combineMatcher(pMatcher);
			} else {
				cm = (CombinableMatcher)pMatcher;
				combinableMatchers.put(cm.getClass(), cm);
				pMatchers.add(pMatcher);
			}
		} else {
			pMatchers.add(pMatcher);
		}
	}
	
	return pMatchers.toArray(new PhoneMatcher[0]);
}

}

/**
 * Start
 */
expr returns [PhonexFSA fsa]
	:	^(EXPR e=baseexpr flags?)
	{
		$fsa = $e.fsa;
		
		// if initial state is final, follow empty transitions and make those states final as well
		for(String finalState:$fsa.getFinalStates()) {
			for(FSATransition<IPAElement> transition:$fsa.getTransitionsForState(finalState)) {
				if(transition instanceof EmptyTransition) {
					$fsa.addFinalState(transition.getToState());
				}
			}
		}
		
	}
	;

flags
scope {
	List<CommonTree> myflags;
}
@init {
	$flags::myflags = new ArrayList<>();
}
	:	^(FORWARDSLASH (myFlag=LETTER {$flags::myflags.add($myFlag);})+)
	{
		for(CommonTree flag:$flags::myflags) {
			char flagChar = flag.getText().charAt(0);
			PhonexFlag phonexFlag = PhonexFlag.fromChar(flagChar);
			if(phonexFlag != null) {
				flags |= phonexFlag.getBitmask();
			} else {
				throw new PhonexPatternException(flag.getLine(), flag.getCharPositionInLine(), "Invalid flag " + flagChar);
			}
		}
	}
	;
	
baseexpr returns [PhonexFSA fsa]
scope {
	PhonexFSA primaryFSA;
	Stack<PhonexFSA> fsaStack;
	int groupIndex;
	Stack<Integer> groupStack;
}
@init {
	$baseexpr::primaryFSA = new PhonexFSA();
	$baseexpr::fsaStack = new Stack<>();
	$baseexpr::fsaStack.push($baseexpr::primaryFSA);
	
	$baseexpr::groupIndex = 0;
	$baseexpr::groupStack = new Stack<>();
	if(baseexpr_stack.size() > 1) {
		// use stacked group index
		$baseexpr::groupIndex = baseexpr_stack.get(baseexpr_stack.size()-2).groupIndex;
		$baseexpr::groupStack.push($baseexpr::groupIndex);
	} else {
		$baseexpr::groupStack.push($baseexpr::groupIndex++);
	}
}
@after {
	
}	
	: ^(BASEEXPR exprele+)
	{
		$fsa = $baseexpr::primaryFSA;
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
scope {
	List<PhonexFSA> orList;
}
@init {
	boolean nonCapturing = (input.LA(3) == NON_CAPTURING_GROUP);
	$baseexpr::fsaStack.push(new PhonexFSA());
	
	$baseexpr::groupStack.push($baseexpr::groupIndex);
	if(!nonCapturing) $baseexpr::groupIndex++;
	
	$group::orList = new ArrayList<>();
}
	:	^(GROUP NON_CAPTURING_GROUP? (e=baseexpr {$group::orList.add($e.fsa);})+ q=quantifier?)
	{
		String groupName = $GROUP.text;
		// value pushed to stack during @init
		int groupIndex = $baseexpr::groupStack.pop();

		// pop our group fsa, apply quantifier
		// and add it to the fsa now on top
		// of the stack
		PhonexFSA grpFsa = $baseexpr::fsaStack.pop();
		
		if($group::orList.size() > 1) {
			grpFsa = new PhonexFSA();
			grpFsa.appendOredGroups(groupIndex, $group::orList);
		} else {
			grpFsa = $group::orList.get(0);
		}

		if(!nonCapturing) {
			grpFsa.setGroupIndex(groupIndex);
		}
		if(q != null) {
			grpFsa.applyQuantifier(q);
		}

		// look-ahead/behind
		if(nonCapturing && groupName != null) {
			final OffsetType offsetType =
				(groupName.endsWith(">") ? OffsetType.LOOK_AHEAD :
					(groupName.endsWith("<") ? OffsetType.LOOK_BEHIND : OffsetType.NORMAL));
			grpFsa.getTransitions().forEach( (t) -> t.setOffsetType(offsetType) );
		}

		// if the expression starts with a group
		// make the group expression the new
		// primary fsa
		if($baseexpr::fsaStack.peek() == $baseexpr::primaryFSA && $baseexpr::primaryFSA.getFinalStates().length == 0) {
			$baseexpr::fsaStack.pop();
			$baseexpr::fsaStack.push(grpFsa);

			// copy group names
			if($baseexpr::primaryFSA.getNumberOfGroups() > grpFsa.getNumberOfGroups())
				grpFsa.setNumberOfGroups($baseexpr::primaryFSA.getNumberOfGroups());

			for(int gIdx = 1; gIdx <= $baseexpr::primaryFSA.getNumberOfGroups(); gIdx++) {
				String gName = $baseexpr::primaryFSA.getGroupName(gIdx);
				if(gName != null)
					grpFsa.setGroupName(gIdx, gName);
			}
			$baseexpr::primaryFSA = grpFsa;
		} else {
			$baseexpr::fsaStack.peek().appendGroup(grpFsa);
		}

		if(!nonCapturing) {
			int maxGroupCount = groupIndex;
			for(PhonexFSA fsa:$group::orList) {
				maxGroupCount = Math.max(maxGroupCount, fsa.getNumberOfGroups());
			}
			if(maxGroupCount > $baseexpr::primaryFSA.getNumberOfGroups())
				$baseexpr::primaryFSA.setNumberOfGroups(maxGroupCount);
			// update parent group index
			if(maxGroupCount >= $baseexpr::groupIndex) {
				$baseexpr::groupIndex = maxGroupCount+1;
			}
			
			// copy all named groups
			for(PhonexFSA fsa:$group::orList) {
				for(String gn:fsa.getGroupNames()) {
					int gidx = fsa.getGroupIndex(gn);
					int currentGroupIdx = $baseexpr::primaryFSA.getGroupIndex(gn);
					if(currentGroupIdx > 0 && gidx != currentGroupIdx) {
						throw new PhonexPatternException($GROUP.line, $GROUP.getCharPositionInLine(), "Duplicate group name: " + gn);
					}
					$baseexpr::primaryFSA.setGroupName(fsa.getGroupIndex(gn), gn);
				}
			}
		} else {
			grpFsa.stripGroups();
			grpFsa.setNumberOfGroups(0);
		}

		// set group name (if available)
		if(!nonCapturing && !groupName.equals("GROUP")) {
			int currentGroupIdx = $baseexpr::primaryFSA.getGroupIndex(groupName);
			if(currentGroupIdx > 0 && groupIndex != currentGroupIdx) {
				throw new PhonexPatternException($GROUP.line, $GROUP.getCharPositionInLine(), "Duplicate group name: " + groupName);
			}
			$baseexpr::primaryFSA.setGroupName(groupIndex, groupName);
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
		PhoneMatcher pMatchers[] = filterPluginMatchers($matcher::pluginMatchers);

		if(q == null)
			$baseexpr::fsaStack.peek().appendMatcher(matcher, pMatchers);
		else
			$baseexpr::fsaStack.peek().appendMatcher(matcher, q, pMatchers);
	}
	|	^(groupIndex=back_reference (pluginMatcher=plugin_matcher {$matcher::pluginMatchers.add($pluginMatcher.value);})* q=quantifier?)
	{
		PhoneMatcher[] pMatchers = filterPluginMatchers($matcher::pluginMatchers);

		if(q == null)
			$baseexpr::fsaStack.peek().appendBackReference(groupIndex, pMatchers);
		else
			$baseexpr::fsaStack.peek().appendBackReference(groupIndex, q, pMatchers);
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

		PhoneClassMatcher pcm = new PhoneClassMatcher(classMatchers);
		if($PHONE_CLASS.text.equals("^")) {
			pcm.setNot(true);
		}
		$value = pcm;
	}
	;

plugin_matcher returns [PhoneMatcher value]
scope {
	List<String> scTypes;
}
@init {
	$plugin_matcher::scTypes = new ArrayList<String>();
}
	:	^(PLUGIN OPEN_PAREN argument_list? CLOSE_PAREN)
	{
		String typeName = $PLUGIN.text;
		PluginProvider pluginProvider = PhonexPluginManager.getSharedInstance().getProvider(typeName);
		if(pluginProvider != null) {
			List<String> argList = new ArrayList<String>();
			if($argument_list.args != null) argList = $argument_list.args;
			
			try {
				$value = pluginProvider.createMatcher(argList);
			} catch (Exception createEx) {
				throw new PhonexPatternException($OPEN_PAREN.line, $OPEN_PAREN.getToken().getCharPositionInLine()+1, createEx);
			}
		} else {
			final NoSuchPluginException ex = new NoSuchPluginException($PLUGIN.line, $PLUGIN.getCharPositionInLine(), "Invalid plug-in matcher: " + typeName);
			throw ex;
		}
	}
	|	^(PLUGIN MINUS? sc=sctype)
	{
		SyllableConstituentMatcher retVal = new SyllableConstituentMatcher();
		if($MINUS == null)
			retVal.getAllowedTypes().add($sc.value);
		else
			retVal.getDisallowedTypes().add($sc.value);
		$value = retVal;
	}
	|   ^(PLUGIN spm=single_phone_matcher)
	{
		$value = new AnyDiacriticPhoneMatcher($spm.value);
	}
	|	^(PLUGIN cm=class_matcher)
	{
		$value = new AnyDiacriticPhoneMatcher($cm.value);
	}
	|	^(PLUGIN st=stress_type)
	{
		final StressMatcher stressMatcher = new StressMatcher();
		stressMatcher.addType($st.value);
		$value = stressMatcher;
	}
	|	^(PLUGIN TRIANGULAR_COLON)
	{
		$value = new AnyDiacriticPhoneMatcher("{long}");
	}
	;

sctype returns [SyllableConstituentType value]
	:	SCTYPE
	{
		SyllableConstituentType scType = SyllableConstituentType.fromString($SCTYPE.text);

		if(scType == SyllableConstituentType.NUCLEUS &&
			$SCTYPE.text.equalsIgnoreCase("D")) {
			$matcher::pluginMatchers.add(new DiphthongMatcher(true));
		}

		if(scType == null)
			throw new PhonexPluginException($SCTYPE.getToken().getLine(), $SCTYPE.getToken().getCharPositionInLine(), "Invalid syllable constituent type '" + $SCTYPE.text + "'");
		$value = scType;
	}
	;

stress_type returns [SyllableStress value]
	:	STRESS
	{
		$value = SyllableStress.fromString($STRESS.text);
	}
	;

argument_list returns [List<String> args]
@init {
	$args = new ArrayList<String>();
}
	:	^(ARG_LIST (arg=argument {$args.add(StringEscapeUtils.unescapeJava($arg.value.substring(1, $arg.value.length()-1)));})+)
	;

argument returns [String value]
	:	^(ARG STRING)
	{
		$value = $STRING.text;
	}
	;

back_reference returns [Integer groupNumber]
scope {
	boolean isRelative;
}
@init {
	$back_reference::isRelative = false;
}
	:	^(BACK_REF (MINUS {$back_reference::isRelative = true;})?) 
	{
		if($back_reference::isRelative) {
			$groupNumber = $baseexpr::groupIndex - Integer.parseInt($BACK_REF.text);
		} else {
			$groupNumber = Integer.parseInt($BACK_REF.text);
		}
		
		// test group number
		if($groupNumber >= $baseexpr::groupIndex || $groupNumber < 1) {
			throw new PhonexPatternException($BACK_REF.line, $BACK_REF.getCharPositionInLine(), "Invalid group number " + $groupNumber);
		}
	}
	;

feature_set_matcher returns [FeatureSetMatcher matcher]
scope {
	List<negatable_identifier_return> features;
}
@init {
	$feature_set_matcher::features = new ArrayList<negatable_identifier_return>();
}
	:	^(FEATURE_SET (f=negatable_identifier {$feature_set_matcher::features.add((negatable_identifier_return)f);})*)
	{
		$matcher = new FeatureSetMatcher();

		final FeatureMatrix fm = FeatureMatrix.getInstance();

		for(negatable_identifier_return featureReturn:$feature_set_matcher::features) {
			boolean not = false;
			String feature = featureReturn.value;
			if(feature.startsWith("-")) {
				not = true;
				feature = feature.substring(1);
			}

			// check feature name
			final Feature featureObj = fm.getFeature(feature);
			if(featureObj == null) {
				throw new PhonexPatternException(featureReturn.line, featureReturn.charPositionInLine, "Invalid feature name " + feature);
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
	:	REGEX_STRING
	{
		RegexMatcher retVal = new RegexMatcher(
			StringEscapeUtils.unescapeJava($REGEX_STRING.text.substring(1, $REGEX_STRING.text.length()-1)));
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

negatable_identifier returns [String value, int line, int charPositionInLine]
	:	^(NAME n=MINUS? chars+=LETTER+)
	{
		$value = ($n == null ? "" : "-");
		for(Object obj:$chars) {
			$value += ((CommonTree)obj).getToken().getText();
		}
		$line = ((CommonTree)$chars.get(0)).getToken().getLine();
		$charPositionInLine = ((CommonTree)$chars.get(0)).getToken().getCharPositionInLine();
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

		case 'g':
			FeatureSetMatcher gm = new FeatureSetMatcher();
			gm.addRequiredFeature("glide");
			$value = gm;
			break;

		case 'p':
			IntraWordPauseMatcher pauseMatcher = new IntraWordPauseMatcher();
			$value = pauseMatcher;
			break;

		case 'P':
			RegexMatcher rm = new RegexMatcher("\\(\\.{1,3}\\)");
			$value = rm;
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
			PhoneClassMatcher notWm = new PhoneClassMatcher();
			notWm.setNot(true);
			FeatureSetMatcher notCfsm = new FeatureSetMatcher();
			notCfsm.addRequiredFeature("consonant");
			notWm.addMatcher(notCfsm);
			FeatureSetMatcher notVfsm = new FeatureSetMatcher();
			notVfsm.addRequiredFeature("vowel");
			notWm.addMatcher(notVfsm);
			$value = notWm;
			break;

		case 's':
			$value = new PhoneMatcher() {
				public boolean matches(IPAElement p) {
					return p.getScType().equals(SyllableConstituentType.SYLLABLESTRESSMARKER);
				}

				public boolean matchesAnything() { return false; }
			};
			break;

		default:
			$value = new PhoneMatcher() {
				public boolean matches(IPAElement p) {
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
			$baseexpr::fsaStack.peek().appendTransition(new BeginningOfInputTransition());
			break;

		case '$':
			$baseexpr::fsaStack.peek().appendTransition(new EndOfInputTransition());
			break;

		case 'b':
			$baseexpr::fsaStack.peek().appendTransition(new WordBoundaryTransition());
			break;

		case 'S':
			$baseexpr::fsaStack.peek().appendTransition(new SyllableBoundaryTransition());
			break;

		default:
			$value = new PhoneMatcher() {
				public boolean matches(IPAElement p) {
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

