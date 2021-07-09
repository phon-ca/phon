/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.phonex;

import ca.phon.fsa.FSATransition;
import ca.phon.fsa.OffsetType;
import ca.phon.fsa.TransitionType;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.features.Feature;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.phonex.plugins.AnyDiacriticPhoneMatcher;
import ca.phon.phonex.plugins.CombinableMatcher;
import ca.phon.phonexg4.PhonexListener;
import ca.phon.phonexg4.PhonexParser;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.syllable.SyllableStress;
import ca.phon.syllable.phonex.StressMatcher;
import ca.phon.syllable.phonex.SyllableConstituentMatcher;
import ca.phon.syllable.phonex.SyllableTransition;
import ca.phon.util.Tuple;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.*;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.*;
import java.util.regex.PatternSyntaxException;

/**
 * Phonex compiler.
 *
 * This class is used internally by PhonexPattern to compile phonex
 * strings into and FSA which can process IPAElements.
 */
public class  PhonexCompiler2 implements PhonexListener {

	private PhonexFSA primaryFSA;

	/* Variables re-used by rules during compilation */
	private Stack<PhonexFSA> fsaStack = new Stack<>();
	private Stack<Integer> groupIndexStack = new Stack<>();
	private int nextGroupIndex = 0;

	private List<PhonexFSA> orGroup = new ArrayList<>();
	private Stack<List<PhonexFSA>> orGroupStack = new Stack<>();

	private Stack<Stack<PhoneMatcher>> matcherStacks = new Stack<>();
	private Stack<PhoneMatcher> matcherStack = new Stack<>();

	private List<PhoneMatcher> pluginMatcherList = new ArrayList<>();

	private Quantifier quantifier = null;
	private Stack<Quantifier> quantifierStack = new Stack<>();

	private Tuple<SyllableConstituentType, SyllableConstituentType> syllableBounds = null;

	// flag set when processing look-behind groups
	private boolean lookBehind = false;

	int flags = 0;

	private void pushMatcherStack() {
		matcherStacks.push(matcherStack);
		matcherStack = new Stack<>();
	}

	private void popMatcherStack() {
		matcherStack = matcherStacks.pop();
	}

	private void pushQuantifierStack() {
		quantifierStack.push(quantifier);
		quantifier = null;
	}

	private void popQuantifierStack() {
		quantifier = quantifierStack.pop();
	}

	private void pushOrGroup() {
		orGroupStack.push(orGroup);
		orGroup = new ArrayList<>();
		orGroup.add(fsaStack.peek());
	}

	private void popOrGroup() {
		orGroup = orGroupStack.pop();
	}

	private void setupNonCapturingGroup() {
		fsaStack.push(new PhonexFSA());
		groupIndexStack.push(nextGroupIndex);

		pushQuantifierStack();
		pushOrGroup();
	}

	private void setupCapturingGroup() {
		fsaStack.push(new PhonexFSA());
		groupIndexStack.push(nextGroupIndex++);

		pushQuantifierStack();
		pushOrGroup();
	}

	/**
	 * Called when compiler comes out of a group rule.
	 * Adds the group FSA to the compiler's fsa
	 * and fixes group indices.
	 *
	 * @param groupContext
	 * @param capturing
	 * @param name
	 * @param quantifier
	 * @return
	 */
	private PhonexFSA popGroup(PhonexParser.GroupContext groupContext, boolean capturing, String name, Quantifier quantifier) {
		PhonexFSA grpFsa = fsaStack.pop();
		int grpIdx = groupIndexStack.pop();

		// if we have an or'ed group create combined fsa
		if(orGroup.size() > 1) {
			grpFsa = new PhonexFSA();
			grpFsa.appendOredGroups(grpIdx, orGroup);
		}
		if(capturing) {
			// update group indices inside transitions
			grpFsa.setGroupIndex(grpIdx);
		}
		if(quantifier != null) {
			grpFsa.applyQuantifier(quantifier);
		}

		// make current group fsa primary if the expression starts with this group
		if(fsaStack.peek() == primaryFSA && primaryFSA.getFinalStates().length == 0) {
			fsaStack.pop();
			fsaStack.push(grpFsa);

			// copy group information (if any) from current primaryFSA
			if (primaryFSA.getNumberOfGroups() > grpFsa.getNumberOfGroups()) {
				grpFsa.setNumberOfGroups(primaryFSA.getNumberOfGroups());
			}
			for (int gIdx = 1; gIdx <= primaryFSA.getNumberOfGroups(); gIdx++) {
				String gName = primaryFSA.getGroupName(gIdx);
				if (gName != null)
					grpFsa.setGroupName(gIdx, gName);
			}
			primaryFSA = grpFsa;
		}

		if(capturing) {
			// copy group information from all machines in our current 'orGroup'
			int maxGroupCount = grpIdx;
			for (PhonexFSA fsa : orGroup) {
				maxGroupCount = Math.max(maxGroupCount, fsa.getNumberOfGroups());
			}
			if (maxGroupCount > primaryFSA.getNumberOfGroups()) {
				primaryFSA.setNumberOfGroups(maxGroupCount);
			}

			// setup nextGroupIndex if necessary
			// this may occur when using or'ed group expressions
			if (maxGroupCount >= nextGroupIndex) {
				nextGroupIndex = maxGroupCount + 1;
			}

			for (PhonexFSA fsa : orGroup) {
				for (String gn : fsa.getGroupNames()) {
					int gidx = fsa.getGroupIndex(gn);
					int currentGroupIdx = primaryFSA.getGroupIndex(gn);
					if (currentGroupIdx > 0 && gidx != currentGroupIdx) {
						PhonexParser.NamedGroupContext namedCtx = (PhonexParser.NamedGroupContext) groupContext;
						throw new PhonexPatternException(namedCtx.group_name().start.getLine(), namedCtx.group_name().getStart().getCharPositionInLine(),
								"Duplicate group name: " + gn);
					}
					primaryFSA.setGroupName(gidx, gn);
				}
			}
		} else {
			grpFsa.stripGroups();
			grpFsa.setNumberOfGroups(0);
		}

		if(capturing && name != null) {
			int currentGroupIdx = primaryFSA.getGroupIndex(name);
			if(currentGroupIdx > 0 && grpIdx != currentGroupIdx) {
				PhonexParser.NamedGroupContext namedCtx = (PhonexParser.NamedGroupContext) groupContext;
				throw new PhonexPatternException(namedCtx.group_name().start.getLine(), namedCtx.group_name().getStart().getCharPositionInLine(),
						"Duplicate group name: " + name);
			}
			primaryFSA.setGroupName(grpIdx, name);
		}

		if(!capturing) {
			nextGroupIndex = grpIdx;
		}
		if(capturing && orGroup.size() > 1) {
			nextGroupIndex = Math.max(nextGroupIndex, primaryFSA.getNumberOfGroups()+1);
		}

		popQuantifierStack();
		popOrGroup();

		return grpFsa;
	}

	/**
	 * Filter plugin matchers, combining matchers like multiple
	 * constituent type matchers.
	 *
	 * @param pluginMatchers
	 * @return a filtered list of plugin matchers
	 */
	private PhoneMatcher[] filterPluginMatchers(List<PhoneMatcher> pluginMatchers) {
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

	public PhonexFSA getFsa() {
		return this.primaryFSA;
	}

	public int getFlags() {
		return this.flags;
	}

	@Override
	public void enterExpr(PhonexParser.ExprContext ctx) {
		setupCapturingGroup();
		primaryFSA = fsaStack.peek();
	}

	@Override
	public void exitExpr(PhonexParser.ExprContext ctx) {
		if(fsaStack.isEmpty()) return;

		PhonexFSA fsa = fsaStack.pop();

		// if initial state is final, follow empty transitions and make those states final as well
		for(String finalState:fsa.getFinalStates()) {
			for(FSATransition<IPAElement> transition:fsa.getTransitionsForState(finalState)) {
				if(transition instanceof EmptyTransition) {
					fsa.addFinalState(transition.getToState());
				}
			}
		}
	}

	@Override
	public void enterFlags(PhonexParser.FlagsContext ctx) {
		this.flags = 0;
	}

	@Override
	public void exitFlags(PhonexParser.FlagsContext ctx) {
		for (TerminalNode terminalNode : ctx.LETTER()) {
			PhonexFlag flag = PhonexFlag.fromChar(terminalNode.getText().charAt(0));
			if(flag == null) {
				throw new PhonexPatternException(terminalNode.getSymbol().getLine(), terminalNode.getSymbol().getCharPositionInLine(),
						"Invalid flag: " + terminalNode.getText());
			}
			this.flags |= flag.getBitmask();
		}
	}

	@Override
	public void enterBaseexpr(PhonexParser.BaseexprContext ctx) {
		if(ctx.getParent() instanceof PhonexParser.GroupContext) {
			int idx = ctx.getParent().children.indexOf(ctx);
			ParseTree prevTree = ctx.getParent().children.get(idx-1);

			// handle or'ed group expressions
			if(prevTree.getText().equals("|")) {
				fsaStack.pop();

				PhonexFSA nextOr = new PhonexFSA();
				fsaStack.push(nextOr);
				orGroup.add(nextOr);

				nextGroupIndex = groupIndexStack.peek() + 1;
			}
		}
	}

	@Override
	public void exitBaseexpr(PhonexParser.BaseexprContext ctx) {}

	@Override
	public void enterExprele(PhonexParser.ExpreleContext ctx) {}

	@Override
	public void exitExprele(PhonexParser.ExpreleContext ctx) {}

	@Override
	public void enterCapturingGroup(PhonexParser.CapturingGroupContext ctx) {
		setupCapturingGroup();
	}

	@Override
	public void exitCapturingGroup(PhonexParser.CapturingGroupContext ctx) {
		PhonexFSA grpFsa = popGroup(ctx,true, null, quantifier);
		if(grpFsa != primaryFSA && !fsaStack.isEmpty()) {
			fsaStack.peek().appendGroup(grpFsa);
		}
	}

	@Override
	public void enterNonCapturingGroup(PhonexParser.NonCapturingGroupContext ctx) {
		setupNonCapturingGroup();
	}

	@Override
	public void exitNonCapturingGroup(PhonexParser.NonCapturingGroupContext ctx) {
		PhonexFSA grpFsa = popGroup(ctx, false, null, quantifier);
		if(grpFsa != primaryFSA && !fsaStack.isEmpty()) {
			fsaStack.peek().appendGroup(grpFsa);
		}
	}

	@Override
	public void enterNamedGroup(PhonexParser.NamedGroupContext ctx) {
		setupCapturingGroup();
	}

	@Override
	public void exitNamedGroup(PhonexParser.NamedGroupContext ctx) {
		String groupName = ctx.group_name().getText();
		PhonexFSA grpFsa = popGroup(ctx,true, groupName, quantifier);
		if(grpFsa != primaryFSA && !fsaStack.isEmpty()) {
			fsaStack.peek().appendGroup(grpFsa);
		}
	}

	@Override
	public void enterLookBehindGroup(PhonexParser.LookBehindGroupContext ctx) {
		setupNonCapturingGroup();
		lookBehind = true;
	}

	@Override
	public void exitLookBehindGroup(PhonexParser.LookBehindGroupContext ctx) {
		PhonexFSA grpFsa = popGroup(ctx,false, null, quantifier);
		grpFsa.getTransitions().forEach( t -> t.setOffsetType(OffsetType.LOOK_BEHIND) );

		if(grpFsa != primaryFSA && !fsaStack.isEmpty())
			fsaStack.peek().appendGroup(grpFsa);
		lookBehind = false;
	}

	@Override
	public void enterLookAheadGroup(PhonexParser.LookAheadGroupContext ctx) {
		setupNonCapturingGroup();
	}

	@Override
	public void exitLookAheadGroup(PhonexParser.LookAheadGroupContext ctx) {
		PhonexFSA grpFsa = popGroup(ctx,false, null, quantifier);
		grpFsa.getTransitions().forEach( t -> t.setOffsetType(OffsetType.LOOK_AHEAD) );

		if(grpFsa != primaryFSA && !fsaStack.isEmpty())
			fsaStack.peek().appendGroup(grpFsa);
	}

	@Override
	public void enterGroup_name(PhonexParser.Group_nameContext ctx) {	}

	@Override
	public void exitGroup_name(PhonexParser.Group_nameContext ctx) {	}

	@Override
	public void enterBaseMatcher(PhonexParser.BaseMatcherContext ctx) {
		pushMatcherStack();
		pushQuantifierStack();
		pluginMatcherList.clear();
	}

	@Override
	public void exitBaseMatcher(PhonexParser.BaseMatcherContext ctx) {
		PhoneMatcher matcher = matcherStack.pop();
		PhoneMatcher[] pluginMatchers = filterPluginMatchers(pluginMatcherList);

		if(quantifier == null)
			fsaStack.peek().appendMatcher(matcher, pluginMatchers);
		else
			fsaStack.peek().appendMatcher(matcher, quantifier, pluginMatchers);

		popMatcherStack();
		popQuantifierStack();
	}

	@Override
	public void enterBackReference(PhonexParser.BackReferenceContext ctx) {
		pushQuantifierStack();
		pluginMatcherList.clear();
	}

	@Override
	public void exitBackReference(PhonexParser.BackReferenceContext ctx) {
		boolean isRelative = ctx.back_reference().MINUS() != null;
		int groupIndex = Integer.parseInt(ctx.back_reference().NUMBER().getText());

		if(isRelative) {
			groupIndex = nextGroupIndex - groupIndex;
		}

		if(groupIndex >= nextGroupIndex || groupIndex < 1) {
			throw new PhonexPatternException(ctx.back_reference().start.getLine(),
					ctx.back_reference().start.getCharPositionInLine(), "Invalid group reference " + groupIndex);
		}


		PhoneMatcher[] pluginMatchers = filterPluginMatchers(pluginMatcherList);

		if(quantifier == null)
			fsaStack.peek().appendBackReference(groupIndex, pluginMatchers);
		else
			fsaStack.peek().appendBackReference(groupIndex, quantifier, pluginMatchers);

		popQuantifierStack();
	}

	@Override
	public void enterSyllableMatcher(PhonexParser.SyllableMatcherContext ctx) {
		pushQuantifierStack();
		pluginMatcherList.clear();
		syllableBounds = null;
	}

	@Override
	public void exitSyllableMatcher(PhonexParser.SyllableMatcherContext ctx) {
		PhoneMatcher[] pluginMatchers = filterPluginMatchers(pluginMatcherList);

		SyllableTransition transition = new SyllableTransition(pluginMatchers, syllableBounds);

		if(quantifier == null)
			fsaStack.peek().appendTransition(transition);
		else
			fsaStack.peek().appendTransition(transition, quantifier);

		popQuantifierStack();
	}

	@Override
	public void enterPhone_matcher(PhonexParser.Phone_matcherContext ctx) {}

	@Override
	public void exitPhone_matcher(PhonexParser.Phone_matcherContext ctx) {}

	@Override
	public void enterCompoundPhoneMatcher(PhonexParser.CompoundPhoneMatcherContext ctx) {
		pushMatcherStack();
	}

	@Override
	public void exitCompoundPhoneMatcher(PhonexParser.CompoundPhoneMatcherContext ctx) {
		// TODO issue error if matcherStack.size() != 2
		PhoneMatcher p2Matcher = matcherStack.pop();
		PhoneMatcher p1Matcher = matcherStack.pop();

		popMatcherStack();

		CompoundPhoneMatcher matcher = new CompoundPhoneMatcher(p1Matcher, p2Matcher);
		matcherStack.push(matcher);
	}

	@Override
	public void enterSyllable_matcher(PhonexParser.Syllable_matcherContext ctx) {}

	@Override
	public void exitSyllable_matcher(PhonexParser.Syllable_matcherContext ctx) {}

	@Override
	public void enterSingleSyllableBounds(PhonexParser.SingleSyllableBoundsContext ctx) {}

	@Override
	public void exitSingleSyllableBounds(PhonexParser.SingleSyllableBoundsContext ctx) {
		SyllableConstituentType scType = SyllableConstituentType.fromString(ctx.sctype().getText());
		syllableBounds = new Tuple<>(scType, scType);
	}

	@Override
	public void enterFullSyllableBounds(PhonexParser.FullSyllableBoundsContext ctx) {

	}

	@Override
	public void exitFullSyllableBounds(PhonexParser.FullSyllableBoundsContext ctx) {
		SyllableConstituentType fromType = SyllableConstituentType.fromString(ctx.sctype(0).getText());
		SyllableConstituentType toType = SyllableConstituentType.fromString(ctx.sctype(1).getText());
		syllableBounds = new Tuple<>(fromType, toType);
	}

	@Override
	public void enterFromSyllableBounds(PhonexParser.FromSyllableBoundsContext ctx) {

	}

	@Override
	public void exitFromSyllableBounds(PhonexParser.FromSyllableBoundsContext ctx) {
		SyllableConstituentType fromType = SyllableConstituentType.fromString(ctx.sctype().getText());
		SyllableConstituentType toType = SyllableConstituentType.UNKNOWN;
		syllableBounds = new Tuple<>(fromType, toType);
	}

	@Override
	public void enterToSyllableBounds(PhonexParser.ToSyllableBoundsContext ctx) {

	}

	@Override
	public void exitToSyllableBounds(PhonexParser.ToSyllableBoundsContext ctx) {
		SyllableConstituentType fromType = SyllableConstituentType.UNKNOWN;
		SyllableConstituentType toType = SyllableConstituentType.fromString(ctx.sctype().getText());
		syllableBounds = new Tuple<>(fromType, toType);
	}

	@Override
	public void enterCompound_phone_matcher(PhonexParser.Compound_phone_matcherContext ctx) {}

	@Override
	public void exitCompound_phone_matcher(PhonexParser.Compound_phone_matcherContext ctx) {}

	@Override
	public void enterPhoneMatcher(PhonexParser.PhoneMatcherContext ctx) {}

	@Override
	public void exitPhoneMatcher(PhonexParser.PhoneMatcherContext ctx) {}

	@Override
	public void enterBase_phone_matcher(PhonexParser.Base_phone_matcherContext ctx) {}

	@Override
	public void exitBase_phone_matcher(PhonexParser.Base_phone_matcherContext ctx) {
		String basePhone = (ctx.LETTER() != null ? ctx.LETTER().getText() : ctx.DIACRITIC().getText());
		BasePhoneMatcher matcher = new BasePhoneMatcher(basePhone.charAt(0));
		matcherStack.push(matcher);
	}

	@Override
	public void enterHex_value(PhonexParser.Hex_valueContext ctx) {}

	@Override
	public void exitHex_value(PhonexParser.Hex_valueContext ctx) {
		String value = ctx.HEX_CHAR().getText().substring(2);
		char ch = (char)Integer.parseInt(value, 16);
		BasePhoneMatcher matcher = new BasePhoneMatcher(ch);
		matcherStack.push(matcher);
	}

	@Override
	public void enterEscaped_char(PhonexParser.Escaped_charContext ctx) {}

	@Override
	public void exitEscaped_char(PhonexParser.Escaped_charContext ctx) {
		String value = ctx.ESCAPED_PUNCT().getText();
		BasePhoneMatcher matcher = new BasePhoneMatcher(value.charAt(1));
		matcherStack.push(matcher);
	}

	@Override
	public void enterClass_matcher(PhonexParser.Class_matcherContext ctx) {
		pushMatcherStack();
	}

	@Override
	public void exitClass_matcher(PhonexParser.Class_matcherContext ctx) {
		boolean isNot = ctx.CARET() != null;

		PhoneMatcher[] classMatchers = matcherStack.toArray(new PhoneMatcher[0]);
		PhoneClassMatcher pcm = new PhoneClassMatcher(classMatchers);
		pcm.setNot(isNot);

		popMatcherStack();
		matcherStack.push(pcm);
	}

	@Override
	public void enterPluginMatcher(PhonexParser.PluginMatcherContext ctx) {}

	@Override
	public void exitPluginMatcher(PhonexParser.PluginMatcherContext ctx) {
		String pluginName = ctx.identifier().getText();

		PluginProvider pluginProvider = PhonexPluginManager.getSharedInstance().getProvider(pluginName);
		if(pluginProvider != null) {
			List<String> args = new ArrayList<>();
			for(int i = 0; i < ctx.argument_list().argument().size(); i++) {
				String argText = ctx.argument_list().argument(i).getText();
				argText = argText.substring(1, argText.length()-1);
				argText = StringEscapeUtils.unescapeJava(argText);
				args.add(argText);
			}

			try{
				PhoneMatcher pluginMatcher = pluginProvider.createMatcher(args);
				pluginMatcherList.add(pluginMatcher);
			} catch (Exception ex) {
				throw new PhonexPatternException(ctx.start.getLine(), ctx.start.getCharPositionInLine(),
						ex);
			}

		} else {
			throw new NoSuchPluginException(ctx.start.getLine(), ctx.start.getCharPositionInLine(), pluginName);
		}
	}

	@Override
	public void enterScTypePluginMatcher(PhonexParser.ScTypePluginMatcherContext ctx) {}

	@Override
	public void exitScTypePluginMatcher(PhonexParser.ScTypePluginMatcherContext ctx) {
		boolean isNot = ctx.MINUS() != null;
		SyllableConstituentType scType = SyllableConstituentType.fromString(ctx.sctype().getText());
		if(scType == null) {
			throw new PhonexPluginException(ctx.sctype().start.getLine(),
					ctx.sctype().start.getCharPositionInLine(), "Invalid constituent type id: " + ctx.sctype().getText());
		}
		SyllableConstituentMatcher matcher = new SyllableConstituentMatcher();
		if(isNot) {
			matcher.getDisallowedTypes().add(scType);
		} else {
			matcher.getAllowedTypes().add(scType);
		}
		pluginMatcherList.add(matcher);
	}

	@Override
	public void enterDiacriticMatcher(PhonexParser.DiacriticMatcherContext ctx) {
		pushMatcherStack();
	}

	@Override
	public void exitDiacriticMatcher(PhonexParser.DiacriticMatcherContext ctx) {
		if(matcherStack.isEmpty()) {
			// TODO throw exception
		}
		PhoneMatcher matcher = matcherStack.pop();
		popMatcherStack();

		AnyDiacriticPhoneMatcher pluginMatcher = new AnyDiacriticPhoneMatcher(matcher);
		pluginMatcherList.add(pluginMatcher);
	}

	@Override
	public void enterStressTypeMatcher(PhonexParser.StressTypeMatcherContext ctx) {}

	@Override
	public void exitStressTypeMatcher(PhonexParser.StressTypeMatcherContext ctx) {
		StressMatcher stressMatcher = new StressMatcher();
		SyllableStress syllableStress = SyllableStress.fromString(ctx.getText().substring(1));
		stressMatcher.addType(syllableStress);
		pluginMatcherList.add(stressMatcher);
	}

	@Override
	public void enterLongMatcher(PhonexParser.LongMatcherContext ctx) {}

	@Override
	public void exitLongMatcher(PhonexParser.LongMatcherContext ctx) {
		PhoneMatcher matcher = new AnyDiacriticPhoneMatcher("{long}");
		pluginMatcherList.add(matcher);
	}

	@Override
	public void enterHalflongMatcher(PhonexParser.HalflongMatcherContext ctx) {}

	@Override
	public void exitHalflongMatcher(PhonexParser.HalflongMatcherContext ctx) {
		PhoneMatcher matcher = new AnyDiacriticPhoneMatcher("{halflong}");
		pluginMatcherList.add(matcher);
	}

	@Override
	public void enterArgument(PhonexParser.ArgumentContext ctx) {}

	@Override
	public void exitArgument(PhonexParser.ArgumentContext ctx) {}

	@Override
	public void enterArgument_list(PhonexParser.Argument_listContext ctx) {}

	@Override
	public void exitArgument_list(PhonexParser.Argument_listContext ctx) {}

	@Override
	public void enterBack_reference(PhonexParser.Back_referenceContext ctx) {}

	@Override
	public void exitBack_reference(PhonexParser.Back_referenceContext ctx) {}

	@Override
	public void enterFeature_set_matcher(PhonexParser.Feature_set_matcherContext ctx) {}

	@Override
	public void exitFeature_set_matcher(PhonexParser.Feature_set_matcherContext ctx) {
		FeatureSetMatcher matcher = new FeatureSetMatcher();

		FeatureMatrix fm = FeatureMatrix.getInstance();
		for(int i = 0; i < ctx.negatable_identifier().size(); i++) {
			boolean not = false;

			String feature = ctx.negatable_identifier(i).getText();
			if(feature.startsWith("-")) {
				feature = feature.substring(1);
				not = true;
			}

			Feature featureData = fm.getFeature(feature);
			if(featureData == null) {
				throw new PhonexPatternException(ctx.negatable_identifier(i).start.getLine(),
						ctx.negatable_identifier(i).start.getCharPositionInLine(), "Invalid feature: " + feature);
			}

			if(not) {
				matcher.addNotFeature(feature);
			} else {
				matcher.addRequiredFeature(feature);
			}
		}

		matcherStack.push(matcher);
	}

	@Override
	public void enterIdentifier(PhonexParser.IdentifierContext ctx) {}

	@Override
	public void exitIdentifier(PhonexParser.IdentifierContext ctx) {}

	@Override
	public void enterNegatable_identifier(PhonexParser.Negatable_identifierContext ctx) {}

	@Override
	public void exitNegatable_identifier(PhonexParser.Negatable_identifierContext ctx) {}

	@Override
	public void enterSingleQuantifier(PhonexParser.SingleQuantifierContext ctx) {}

	@Override
	public void exitSingleQuantifier(PhonexParser.SingleQuantifierContext ctx) {
		String q1 = ctx.SINGLE_QUANTIFIER(0).getText();
		String q2 = (ctx.SINGLE_QUANTIFIER().size() > 1 ? ctx.SINGLE_QUANTIFIER(1).getText() : null);

		QuantifierType type = QuantifierType.fromString(q1);
		quantifier = new Quantifier(type);
		if(q2 != null) {
			switch(q2) {
			case "?":
				quantifier.setTransitionType(TransitionType.RELUCTANT);
				break;

			case "*":
				quantifier.setTransitionType(TransitionType.GREEDY);
				break;

			case "+":
				quantifier.setTransitionType(TransitionType.POSSESSIVE);
				break;
			}
		}
	}

	@Override
	public void enterBoundedQuantifier(PhonexParser.BoundedQuantifierContext ctx) {}

	@Override
	public void exitBoundedQuantifier(PhonexParser.BoundedQuantifierContext ctx) {}

	@Override
	public void enterExactBoundedQuantifier(PhonexParser.ExactBoundedQuantifierContext ctx) {}

	private void setupBoundedQuantifier(int start, int end) {
		quantifier = new Quantifier(start, end);
	}

	@Override
	public void exitExactBoundedQuantifier(PhonexParser.ExactBoundedQuantifierContext ctx) {
		setupBoundedQuantifier(Integer.parseInt(ctx.NUMBER().getText()), -1);
	}

	@Override
	public void enterAtLeastBoundedQuantifier(PhonexParser.AtLeastBoundedQuantifierContext ctx) {}

	@Override
	public void exitAtLeastBoundedQuantifier(PhonexParser.AtLeastBoundedQuantifierContext ctx) {
		setupBoundedQuantifier(Integer.parseInt(ctx.NUMBER().getText()), 0);
	}

	@Override
	public void enterAtMostBoundedQuantifier(PhonexParser.AtMostBoundedQuantifierContext ctx) {}

	@Override
	public void exitAtMostBoundedQuantifier(PhonexParser.AtMostBoundedQuantifierContext ctx) {
		setupBoundedQuantifier(0, Integer.parseInt(ctx.NUMBER().getText()));
	}

	@Override
	public void enterBetweenBoundedQuantifier(PhonexParser.BetweenBoundedQuantifierContext ctx) {	}

	@Override
	public void exitBetweenBoundedQuantifier(PhonexParser.BetweenBoundedQuantifierContext ctx) {
		setupBoundedQuantifier(Integer.parseInt(ctx.NUMBER(0).getText()),
				Integer.parseInt(ctx.NUMBER(1).getText()));
	}

	@Override
	public void enterAnyElementClass(PhonexParser.AnyElementClassContext ctx) {}

	@Override
	public void exitAnyElementClass(PhonexParser.AnyElementClassContext ctx) {
		matcherStack.push(new FeatureSetMatcher());
	}

	@Override
	public void enterEscapedClass(PhonexParser.EscapedClassContext ctx) {}

	@Override
	public void exitEscapedClass(PhonexParser.EscapedClassContext ctx) {
		String phoneClass = ctx.ESCAPED_PHONE_CLASS().getText();

		switch(phoneClass) {
		case "\\c":
			FeatureSetMatcher cm = new FeatureSetMatcher();
			cm.addRequiredFeature("consonant");
			matcherStack.push(cm);
			break;

		case "\\v":
			FeatureSetMatcher vm = new FeatureSetMatcher();
			vm.addRequiredFeature("vowel");
			matcherStack.push(vm);
			break;

		case "\\g":
			FeatureSetMatcher gm = new FeatureSetMatcher();
			gm.addRequiredFeature("glide");
			matcherStack.push(gm);
			break;

		case "\\p":
			matcherStack.push(new IntraWordPauseMatcher());
			break;

		case "\\P":
			RegexMatcher rm = new RegexMatcher("\\(\\.{1,3}\\)");
			matcherStack.push(rm);
			break;

		case "\\w":
			PhoneClassMatcher wm = new PhoneClassMatcher();
			FeatureSetMatcher cfsm = new FeatureSetMatcher();
			cfsm.addRequiredFeature("consonant");
			wm.addMatcher(cfsm);
			FeatureSetMatcher vfsm = new FeatureSetMatcher();
			vfsm.addRequiredFeature("vowel");
			wm.addMatcher(vfsm);
			matcherStack.push(wm);
			break;

		case "\\W":
			PhoneClassMatcher notWm = new PhoneClassMatcher();
			notWm.setNot(true);
			FeatureSetMatcher notCfsm = new FeatureSetMatcher();
			notCfsm.addRequiredFeature("consonant");
			notWm.addMatcher(notCfsm);
			FeatureSetMatcher notVfsm = new FeatureSetMatcher();
			notVfsm.addRequiredFeature("vowel");
			notWm.addMatcher(notVfsm);
			matcherStack.push(notWm);
			break;

		case "\\s":
			SyllableConstituentMatcher scm = new SyllableConstituentMatcher();
			scm.getAllowedTypes().add(SyllableConstituentType.SYLLABLESTRESSMARKER);
			matcherStack.push(scm);
			break;

		default:
			throw new PhonexPatternException(ctx.start.getLine(),
					ctx.start.getCharPositionInLine(), "Invalid phone class");
		}
	}

	@Override
	public void enterBoundary_matcher(PhonexParser.Boundary_matcherContext ctx) {}

	@Override
	public void exitBoundary_matcher(PhonexParser.Boundary_matcherContext ctx) {
		String txt = ctx.getText();

		switch(txt) {
		case "^":
			fsaStack.peek().appendTransition(new BeginningOfInputTransition());
			break;

		case "$":
			fsaStack.peek().appendTransition(new EndOfInputTransition());
			break;

		case "\\b":
			fsaStack.peek().appendTransition(new WordBoundaryTransition());
			break;

		case "\\S":
			fsaStack.peek().appendTransition(new SyllableBoundaryTransition());
			break;

		default:
			// TODO throw exception, invalid escape sequence
			break;
		}
	}

	@Override
	public void enterStress_type(PhonexParser.Stress_typeContext ctx) {}

	@Override
	public void exitStress_type(PhonexParser.Stress_typeContext ctx) {}

	@Override
	public void enterSctype(PhonexParser.SctypeContext ctx) {}

	@Override
	public void exitSctype(PhonexParser.SctypeContext ctx) {}

	@Override
	public void enterRegex_matcher(PhonexParser.Regex_matcherContext ctx) {}

	@Override
	public void exitRegex_matcher(PhonexParser.Regex_matcherContext ctx) {
		String regex = (ctx.QUOTED_STRING() != null ? ctx.QUOTED_STRING().getText() :
				(ctx.SINGLE_QUOTED_STRING() != null ? ctx.SINGLE_QUOTED_STRING().getText() : null));
		if(regex == null) {
			// TODO throw exception
		}
		regex = regex.substring(1, regex.length()-1);
		regex = StringEscapeUtils.unescapeJava(regex);

		try {
			RegexMatcher matcher = new RegexMatcher(regex);
			matcherStack.push(matcher);
		} catch (PatternSyntaxException pse) {
			throw new PhonexPatternException(ctx.start.getLine(), ctx.start.getCharPositionInLine(),
					pse.getMessage());
		}
	}

	@Override
	public void visitTerminal(TerminalNode node) {

	}

	@Override
	public void visitErrorNode(ErrorNode node) {

	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {

	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {

	}

	/**
	 * Walk the parse tree produced by PhonexParser
	 *
	 */
	public void walkTree(ParseTree tree) {
		treeWalker.walk(this, tree);
	}

	private ParseTreeWalker treeWalker = new ParseTreeWalker() {

		@Override
		public void walk(ParseTreeListener listener, ParseTree t) {
			if ( t instanceof ErrorNode) {
				listener.visitErrorNode((ErrorNode)t);
				return;
			}
			else if ( t instanceof TerminalNode) {
				listener.visitTerminal((TerminalNode)t);
				return;
			}
			RuleNode r = (RuleNode)t;
			enterRule(listener, r);
			int n = r.getChildCount();

			// reverse expreles in baseexprs when in lookBehind mode
			if(lookBehind && t instanceof PhonexParser.BaseexprContext) {
				for (int i = n-1; i >=0; i--) {
					walk(listener, r.getChild(i));
				}
			} else {
				for (int i = 0; i < n; i++) {
					walk(listener, r.getChild(i));
				}
			}

			exitRule(listener, r);
		}

	};

}
