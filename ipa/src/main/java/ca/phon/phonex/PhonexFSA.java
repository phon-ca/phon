/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import ca.phon.fsa.*;
import ca.phon.ipa.IPAElement;

import java.util.*;

/**
 * Implementation of a phonex state machine.
 * Includes methods for modifying the machine
 * design.
 * 
 */
public class PhonexFSA extends SimpleFSA<IPAElement> implements Cloneable {
	
	/**
	 * State index
	 */
	private int stateIndex = 0;

	/**
	 * Constructor (package visiblity)
	 */
	PhonexFSA() {
		super();
		
		setInitialState(appendState());
	}
	
	/**
	 * Returns the next state name
	 * 
	 */
	private String getNextStateName() {
		return "q" + (stateIndex++);
	}
	
	/**
	 * Add a new state and return
	 * the name of the state.
	 * 
	 * 
	 * @return the name of the new state
	 */
	public String appendState() {
		String s = getNextStateName();
		addState(s);
		return s;
	}
	
	/**
	 * Makes all current final states
	 * non-final and returns them as a
	 * list.
	 * 
	 * @return the previous final states
	 */
	public String[] stripFinalStates() {
		String[] currentFinals = getFinalStates();
		for(String finalState:currentFinals)
			removeFinalState(finalState);
		return currentFinals;
	}
	
	/**
	 * Append the given transition to the machine.
	 * 
	 */
	public void appendTransition(PhonexTransition trans) {
		String[] oldFinalStates = stripFinalStates();
		
		String newFinalState = getNextStateName();
		addState(newFinalState);
		addFinalState(newFinalState);
		
		if(oldFinalStates.length == 0) {
			PhonexTransition transition = 
					(PhonexTransition)trans.clone();
			transition.setFirstState(getInitialState());
			transition.setToState(newFinalState);
			addTransition(transition);
		} else {
			for(String oldFinalState:oldFinalStates) {
				PhonexTransition transition = 
						(PhonexTransition)trans.clone();
				transition.setFirstState(oldFinalState);
				transition.setToState(newFinalState);
				
				addTransition(transition);
			}
		}
	}
	
	public void appendTransition(PhonexTransition trans, Quantifier quantifier) {
		switch(quantifier.getType()) {
		case ZERO_OR_MORE:
			makeZeroOrMore(trans);
			break;
			
		case ZERO_OR_ONE:
			makeZeroOrOne(trans);
			break;
			
		case ONE_OR_MORE:
			makeOneOrMore(trans);
			break;
			
		case BOUNDED:
			makeBounded(quantifier.getxBound(), quantifier.getyBound(), trans);
			break;
			
		default:
			break;
		}
		
		// apply transition type to final states
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> fTrans:getTransitionsForState(finalState)) {
				fTrans.setType(quantifier.getTransitionType());
			}
		}
	}
	
	/**
	 * Convienence method for appending matchers.
	 * Same as <code>appendMatcher(matcher, null)</code>
	 * 
	 * @param matcher
	 */
	public void appendMatcher(PhoneMatcher matcher) {
		appendMatcher(matcher, new PhoneMatcher[0]);
	}
	
	/**
	 * <p>Helper method for adding a new simple transition
	 * and making appropriate state changes.</p>
	 * 
	 * <p>This method does the following:
	 * <ul><li>removes all current final states</li>
	 * <li>creates a new state</li>
	 * <li>create a transition from all old final states to the new one</li>
	 * <li>makes the new state final</li>
	 * </ul></p>
	 * 
	 * @param matcher to add
	 * @param secondaryMatchers type matcher - can be <code>null</code>
	 */
	public void appendMatcher(PhoneMatcher matcher, PhoneMatcher ... secondaryMatchers) {
		String[] oldFinalStates = stripFinalStates();
		
		String newFinalState = getNextStateName();
		addState(newFinalState);
		addFinalState(newFinalState);
		
		if(oldFinalStates.length == 0) {
			PhonexTransition transition = 
					new PhonexTransition(matcher, secondaryMatchers);
			transition.setFirstState(getInitialState());
			transition.setToState(newFinalState);
			addTransition(transition);
		} else {
			for(String oldFinalState:oldFinalStates) {
				PhonexTransition transition = 
						new PhonexTransition(matcher, secondaryMatchers);
				transition.setFirstState(oldFinalState);
				transition.setToState(newFinalState);
				
				addTransition(transition);
			}
		}
	}
	
	/**
	 * Append a new back reference
	 * 
	 * @param groupIndex
	 */
	public void appendBackReference(int groupIndex) {
		appendBackReference(groupIndex, new PhoneMatcher[0]);
	}
	
	/**
	 * Append a new back reference to the machine.
	 * This method works in the same manner as 
	 * {@link #appendMatcher(PhoneMatcher)}
	 * 
	 * @param groupIndex
	 * @param secondaryMatchers
	 */
	public void appendBackReference(int groupIndex, PhoneMatcher ... secondaryMatchers) {
		String[] oldFinalStates = stripFinalStates();
		
		String newFinalState = getNextStateName();
		addState(newFinalState);
		addFinalState(newFinalState);
		
		if(oldFinalStates.length == 0) {
			BackReferenceTransition transition = 
					new BackReferenceTransition(groupIndex, secondaryMatchers);
			transition.setFirstState(getInitialState());
			transition.setToState(newFinalState);
			addTransition(transition);
		} else {
			for(String oldFinalState:oldFinalStates) {
				BackReferenceTransition transition = 
						new BackReferenceTransition(groupIndex, secondaryMatchers);
				transition.setFirstState(oldFinalState);
				transition.setToState(newFinalState);
				
				addTransition(transition);
			}
		}
	}
	
	/**
	 * Append the given matcher with the
	 * given quantifier.
	 * 
	 * 
	 */
	public void appendMatcher(PhoneMatcher matcher, Quantifier quantifier, PhoneMatcher ... secondaryMatchers) {
		switch(quantifier.getType()) {
		case ZERO_OR_MORE:
			makeZeroOrMore(matcher, secondaryMatchers);
			break;
			
		case ZERO_OR_ONE:
			makeZeroOrOne(matcher, secondaryMatchers);
			break;
			
		case ONE_OR_MORE:
			makeOneOrMore(matcher, secondaryMatchers);
			break;
			
		case BOUNDED:
			makeBounded(quantifier.getxBound(), quantifier.getyBound(), matcher, secondaryMatchers);
			break;
			
		default:
			break;
		}
		
		// apply transition type to final states
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> fTrans:getTransitionsForState(finalState)) {
				fTrans.setType(quantifier.getTransitionType());
			}
		}
	}
	
	/**
	 * Append a back reference with quantifier
	 */
	public void appendBackReference(int groupIndex, Quantifier quantifier, PhoneMatcher ... secondaryMatchers) {
		switch(quantifier.getType()) {
		case ZERO_OR_MORE:
			makeZeroOrMore(groupIndex, secondaryMatchers);
			break;
			
		case ZERO_OR_ONE:
			makeZeroOrOne(groupIndex, secondaryMatchers);
			break;
			
		case ONE_OR_MORE:
			makeOneOrMore(groupIndex, secondaryMatchers);
			break;
			
		case BOUNDED:
			makeBounded(quantifier.getxBound(), quantifier.getyBound(), groupIndex, secondaryMatchers);
			break;
			
		default:
			break;
		}
		
		// apply transition type to final states
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> fTrans:getTransitionsForState(finalState)) {
				fTrans.setType(quantifier.getTransitionType());
			}
		}
	}
	
	/**
	 * Apply the 'zero or more' quantifier
	 * to the last added matcher.
	 * 
	 * @param matcher
	 */
	private void makeZeroOrMore(PhoneMatcher matcher, PhoneMatcher ... secondaryMatchers) {
		appendMatcher(matcher, secondaryMatchers);
		
		// for each final state, find the transitions to it and
		// make the first state final as well
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> trans:getTransitionsToState(finalState)) {
				addFinalState(trans.getFirstState());
			}
			
			// setup loop-back transitions on final states
			// for the given matcher
			PhonexTransition transition = 
					new PhonexTransition(matcher, secondaryMatchers);
			transition.setFirstState(finalState);
			transition.setToState(finalState);
			addTransition(transition);
		}
	}
	
	private void makeZeroOrMore(PhonexTransition transition) {
		appendTransition(transition);
		
		// for each final state, find the transitions to it and
		// make the first state final as well
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> trans:getTransitionsToState(finalState)) {
				addFinalState(trans.getFirstState());
			}
			
			// setup loop-back transitions on final states
			// for the given matcher
			PhonexTransition ct = (PhonexTransition)transition.clone();
			ct.setFirstState(finalState);
			ct.setToState(finalState);
			addTransition(ct);
		}
	}
	
	private void makeZeroOrMore(int groupIndex, PhoneMatcher ... secondaryMatchers) {
		appendBackReference(groupIndex, secondaryMatchers);
		
		// for each final state, find the transitions to it and
		// make the first state final as well
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> trans:getTransitionsToState(finalState)) {
				addFinalState(trans.getFirstState());
			}
			
			// setup loop-back transitions on final states
			// for the given matcher
			BackReferenceTransition transition = 
					new BackReferenceTransition(groupIndex, secondaryMatchers);
			transition.setFirstState(finalState);
			transition.setToState(finalState);
			addTransition(transition);
		}
	}
	
	/**
	 * Apply the 'zero or one' quantifier
	 * to the last added matcher.
	 * 
	 * @param matcher
	 */
	private void makeZeroOrOne(PhoneMatcher matcher, PhoneMatcher ... secondaryMatchers) {
		appendMatcher(matcher, secondaryMatchers);
		
		// for each final state, find the transitions to it and
		// make the first state final as well
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> trans:getTransitionsToState(finalState)) {
				addFinalState(trans.getFirstState());
			}
		}
	}
	
	private void makeZeroOrOne(PhonexTransition transition) {
		appendTransition(transition);
		
		// for each final state, find the transitions to it and
		// make the first state final as well
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> trans:getTransitionsToState(finalState)) {
				addFinalState(trans.getFirstState());
			}
		}
	}
	
	private void makeZeroOrOne(int groupIndex, PhoneMatcher ... secondaryMatchers) {
		appendBackReference(groupIndex, secondaryMatchers);
		
		// for each final state, find the transitions to it and
		// make the first state final as well
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> trans:getTransitionsToState(finalState)) {
				addFinalState(trans.getFirstState());
			}
		}
	}
	
	/**
	 * Apply the 'one or more' quantifier
	 * to the last added matcher.
	 * 
	 * @param matcher
	 */
	private void makeOneOrMore(PhoneMatcher matcher, PhoneMatcher ... secondaryMatchers) {
		appendMatcher(matcher, secondaryMatchers);
		
		// for each final state, make a 
		// new loop transition
		for(String finalState:getFinalStates()) {
			PhonexTransition transition = 
					new PhonexTransition(matcher, secondaryMatchers);
			transition.setFirstState(finalState);
			transition.setToState(finalState);
			addTransition(transition);
		}
	}
	
	private void makeOneOrMore(PhonexTransition transition) {
		appendTransition(transition);
		
		// for each final state, make a 
		// new loop transition
		for(String finalState:getFinalStates()) {
			PhonexTransition ct = (PhonexTransition)transition.clone();
			ct.setFirstState(finalState);
			ct.setToState(finalState);
			addTransition(ct);
		}
	}
	
	private void makeOneOrMore(int groupIndex, PhoneMatcher ... secondaryMatchers) {
		appendBackReference(groupIndex, secondaryMatchers);
		
		// for each final state, make a 
		// new loop transition
		for(String finalState:getFinalStates()) {
			BackReferenceTransition transition = 
					new BackReferenceTransition(groupIndex, secondaryMatchers);
			transition.setFirstState(finalState);
			transition.setToState(finalState);
			addTransition(transition);
		}
	}
	
	/**
	 * Apply a bounded quantifier to 
	 * the given matcher
	 * 
	 * @param xbound
	 * @param ybound
	 * @param matcher
	 * @param secondaryMatchers
	 */
	public void makeBounded(int xbound, int ybound, PhoneMatcher matcher, PhoneMatcher ... secondaryMatchers) {
		// case <int>
		if(xbound > 0 && ybound < 0) {
			for(int i = 0; i < xbound; i++) {
				appendMatcher(matcher, secondaryMatchers);
			}
		// case <int,>
		} else if(xbound > 0 && ybound == 0) {
			for(int i = 0; i < xbound-1; i++) {
				appendMatcher(matcher, secondaryMatchers);
			}
			appendMatcher(matcher, new Quantifier(QuantifierType.ONE_OR_MORE), secondaryMatchers);
		// case <,int>
		} else if(xbound == 0 && ybound > 0) {
			for(int i = 0; i < ybound; i++) {
				appendMatcher(matcher, new Quantifier(QuantifierType.ZERO_OR_ONE), secondaryMatchers);
			}
		// case <int,int>
		} else if(xbound > 0 && ybound > 0) {
//			PhonexFSA toAdd = new PhonexFSA();
			for(int i = 0; i < xbound; i++) {
				appendMatcher(matcher, secondaryMatchers);
			}
			for(int i = xbound; i < ybound; i++) {
				appendMatcher(matcher, new Quantifier(QuantifierType.ZERO_OR_ONE), secondaryMatchers);
			}
		}
	}
	
	public void makeBounded(int xbound, int ybound, PhonexTransition transition) {
		// case <int>
		if(xbound > 0 && ybound < 0) {
			for(int i = 0; i < xbound; i++) {
				appendTransition(transition);
			}
		// case <int,>
		} else if(xbound > 0 && ybound == 0) {
			for(int i = 0; i < xbound-1; i++) {
				appendTransition(transition);
			}
			appendTransition(transition, new Quantifier(QuantifierType.ONE_OR_MORE));
		// case <,int>
		} else if(xbound == 0 && ybound > 0) {
			for(int i = 0; i < ybound; i++) {
				appendTransition(transition, new Quantifier(QuantifierType.ZERO_OR_ONE));
			}
		// case <int,int>
		} else if(xbound > 0 && ybound > 0) {
//			PhonexFSA toAdd = new PhonexFSA();
			for(int i = 0; i < xbound; i++) {
				appendTransition(transition);
			}
			for(int i = xbound; i < ybound; i++) {
				appendTransition(transition, new Quantifier(QuantifierType.ZERO_OR_ONE));
			}
		}
	}
	
	public void makeBounded(int xbound, int ybound, int groupIndex, PhoneMatcher ... secondaryMatchers) {
		// case <int>
		if(xbound > 0 && ybound < 0) {
			for(int i = 0; i < xbound; i++) {
				appendBackReference(groupIndex, secondaryMatchers);
			}
		// case <int,>
		} else if(xbound > 0 && ybound == 0) {
			for(int i = 0; i < xbound-1; i++) {
				appendBackReference(groupIndex, secondaryMatchers);
			}
			appendBackReference(groupIndex, new Quantifier(QuantifierType.ONE_OR_MORE), secondaryMatchers);
		// case <,int>
		} else if(xbound == 0 && ybound > 0) {
			for(int i = 0; i < ybound; i++) {
				appendBackReference(groupIndex, new Quantifier(QuantifierType.ZERO_OR_ONE), secondaryMatchers);
			}
		// case <int,int>
		} else if(xbound > 0 && ybound > 0) {
//			PhonexFSA toAdd = new PhonexFSA();
			for(int i = 0; i < xbound; i++) {
				appendBackReference(groupIndex, secondaryMatchers);
			}
			for(int i = xbound; i < ybound; i++) {
				appendBackReference(groupIndex, new Quantifier(QuantifierType.ZERO_OR_ONE), secondaryMatchers);
			}
		}
	}
	
	public void appendOredGroups(int parentGroupIndex, List<PhonexFSA> orFsas) {
		String[] oldFinalStates = stripFinalStates();
		
		if(oldFinalStates.length == 0) {
			// use initial state
			oldFinalStates = new String[]{ getInitialState() };
		}
		
		for(PhonexFSA fsa:orFsas) {
			String fsaInitialState = appendState();
			// create new empty transitions to group initial state
			for(String finalState:oldFinalStates) {
				EmptyTransition e = new EmptyTransition();
				e.setFirstState(finalState);
				e.setToState(fsaInitialState);
				addTransition(e);
			}
			
			Map<String, String> stateMap = new HashMap<String, String>();
			stateMap.put(fsa.getInitialState(), fsaInitialState);
			for(String state:fsa.getStates()) {
				if(state.equals(fsa.getInitialState())) continue;
				
				String newState = appendState();
				stateMap.put(state, newState);
				
				if(fsa.isFinalState(state))
					addFinalState(newState);
			}
			
			for(FSATransition<IPAElement> transition:fsa.getTransitions()) {
				PhonexTransition pTrans = 
						PhonexTransition.class.cast(transition);
				PhonexTransition cpyTrans = 
						PhonexTransition.class.cast(pTrans.clone());
				cpyTrans.setFirstState(stateMap.get(pTrans.getFirstState()));
				cpyTrans.setToState(stateMap.get(pTrans.getToState()));
				addTransition(cpyTrans);
			}
		}
	}
	
	/**
	 * Append a machine to this machine
	 * 
	 * @param fsa
	 */
	public void appendGroup(PhonexFSA fsa) {
		// if initial state of given machine is also
		// a final state it will match anything - 
		// do not strip final states
		String[] oldFinalStates = new String[0];
		if(fsa.isFinalState(fsa.getInitialState()))
			oldFinalStates = getFinalStates();
		else
			oldFinalStates = stripFinalStates();
		
		if(oldFinalStates.length == 0) {
			// use initial state
			oldFinalStates = new String[]{ this.getInitialState() };
		}
		
		// copy all states other than the initial state
		// from the given fsa
		Map<String, String> stateMap = new HashMap<String, String>();
		for(String state:fsa.getStates()) {
			if(!state.equals(fsa.getInitialState())) {
				String newState = appendState();
				stateMap.put(state, newState);
				
				if(fsa.isFinalState(state))
					addFinalState(newState);
			}
		}
		
		for(String finalState:oldFinalStates) {
			for(FSATransition<IPAElement> initialTransition:fsa.getTransitionsForState(fsa.getInitialState())) {
				PhonexTransition pTrans = 
						PhonexTransition.class.cast(initialTransition);
				PhonexTransition cpyTrans = 
						PhonexTransition.class.cast(pTrans.clone());
				cpyTrans.setFirstState(finalState);
				cpyTrans.setToState(stateMap.get(pTrans.getToState()));
				cpyTrans.setOffsetType(pTrans.getOffsetType());
				addTransition(cpyTrans);
			}
		}
		
		// copy all transitions from non-initial
		// states in the given fsa
		for(String state:fsa.getStates()) {
			if(!state.equals(fsa.getInitialState())) {
				for(FSATransition<IPAElement> transition:fsa.getTransitionsForState(state)) {
					PhonexTransition pTrans = 
							PhonexTransition.class.cast(transition);
					PhonexTransition cpyTrans = 
							PhonexTransition.class.cast(pTrans.clone());
					cpyTrans.setFirstState(stateMap.get(pTrans.getFirstState()));
					cpyTrans.setToState(stateMap.get(pTrans.getToState()));
					cpyTrans.setOffsetType(pTrans.getOffsetType());
					addTransition(cpyTrans);
				}
			}
		}
	}
	
	/**
	 * Apply the given quantifier to the entire fsa
	 * 
	 * @param quantifier
	 */
	public void applyQuantifier(Quantifier quantifier) {
		switch(quantifier.getType()) {
		case ZERO_OR_MORE:
			makeZeroOrMore();
			break;
			
		case ZERO_OR_ONE:
			makeZeroOrOne();
			break;
			
		case ONE_OR_MORE:
			makeOneOrMore();
			break;
			
		case BOUNDED:
			makeBounded(quantifier.getxBound(), quantifier.getyBound());
			break;
			
		default:
			break;
		}

		// apply transition type to final states
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> fTrans:getTransitionsForState(finalState)) {
				fTrans.setType(quantifier.getTransitionType());
			}
		}
	}

	/**
	 * Apply the 'zero or more' quantifier
	 * to the entire fsa
	 *
	 */
	public void makeZeroOrMore() {
		// copy all transitions from the initial state
		// to the final states
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> iniTrans:getTransitionsForState(getInitialState())) {
				PhonexTransition pTrans = 
						PhonexTransition.class.cast(iniTrans);
				PhonexTransition cpyTrans = 
						PhonexTransition.class.cast(pTrans.clone());
				cpyTrans.setFirstState(finalState);
				addTransition(cpyTrans);
			}
		}
		
		// make the initial state final
		addFinalState(getInitialState());
	}
	
	/**
	 * Apply the 'one or more' quantifier
	 * to the entire fsa
	 */
	public void makeOneOrMore() {
		// copy all transitions from the initial state
		// to the final states
		for(String finalState:getFinalStates()) {
			for(FSATransition<IPAElement> iniTrans:getTransitionsForState(getInitialState())) {
				PhonexTransition pTrans = 
						PhonexTransition.class.cast(iniTrans);
				PhonexTransition cpyTrans = 
						PhonexTransition.class.cast(pTrans.clone());
				cpyTrans.setFirstState(finalState);
				addTransition(cpyTrans);
			}
		}
	}
	
	/**
	 * Apply the 'zero or one' quantifier
	 * 
	 * to the entire fsa
	 */
	public void makeZeroOrOne() {
		addFinalState(getInitialState());
	}
	
	/**
	 * Apply a 'bounded' quantifier.
	 * 
	 * @param xbound if 0, then ybound is
	 *  a maximum
	 * @param ybound if < 0, then xbound
	 *  is expected to be an exact number.
	 *  If 0, then xbound is a miniumum.
	 */
	public void makeBounded(int xbound, int ybound) {
		PhonexFSA cpyFSA = PhonexFSA.class.cast(this.clone());
		// case <int>
		if(xbound > 0 && ybound < 0) {
			for(int i = 1; i < xbound; i++) {
				PhonexFSA fsa = PhonexFSA.class.cast(cpyFSA.clone());
				// append copied machine onto this one
				this.appendGroup(fsa);
			}
		// case <int,>
		// xbound is a minimum, no max
		} else if(xbound > 0 && ybound == 0) {
			for(int i = 1; i < xbound-1; i++) {
				PhonexFSA fsa = PhonexFSA.class.cast(cpyFSA.clone());
				this.appendGroup(fsa);
			}
			PhonexFSA fsa = PhonexFSA.class.cast(cpyFSA.clone());
			fsa.makeOneOrMore();
			this.appendGroup(fsa);
		// case <,int>
		// ybound is a maxiumum, no min
		} else if(xbound == 0 && ybound > 0) {
			for(int i = 1; i < ybound; i++) {
				PhonexFSA fsa = PhonexFSA.class.cast(cpyFSA.clone());
				fsa.makeZeroOrOne();
				this.appendGroup(fsa);
			}
		} else if(xbound > 0 && ybound > 0) {
			PhonexFSA atLeast = new PhonexFSA();
			for(int i = 1; i < xbound; i++) {
				PhonexFSA fsa = PhonexFSA.class.cast(cpyFSA.clone());
				atLeast.appendGroup(fsa);
			}
			
			// up to
			for(int i = xbound; i < ybound; i++) {
				PhonexFSA fsa = PhonexFSA.class.cast(cpyFSA.clone());
				fsa.makeZeroOrOne();
				atLeast.appendGroup(fsa);
			}
			
			this.appendGroup(atLeast);
		}
	}
	
	/**
	 * Set the matcher group for this fsa.
	 * Transitions from the initial state will
	 * initialize the specified group, all
	 * other transitions modify the group.
	 * 
	 * @param groupIndex
	 */
	public void setGroupIndex(int groupIndex) {
		for(FSATransition<IPAElement> trans:getTransitionsForState(getInitialState())) {
			trans.getInitGroups().add(groupIndex);
		}
		for(String state:getStates()) {
			if(!getInitialState().equals(state)) {
				for(FSATransition<IPAElement> trans:getTransitionsForState(state)) {
					trans.getMatcherGroups().add(groupIndex);
				}
			}
		}
	}
	
	/**
	 * Decrement group indices on all transitions by one.
	 */
	public void decrementGroups() {
		for(String state:getStates()) {
			for(FSATransition<IPAElement> trans:getTransitionsForState(state)) {
				Integer[] initGroups = trans.getInitGroups().toArray(new Integer[0]);
				Integer[] matcherGroups = trans.getMatcherGroups().toArray(new Integer[0]);
				

				trans.getInitGroups().clear();
				for(int i = 0; i < initGroups.length; i++) {
					trans.getInitGroups().add(initGroups[i]-1);
				}
				trans.getMatcherGroups().clear();
				for(int i = 0; i < matcherGroups.length; i++) {
					trans.getMatcherGroups().add(matcherGroups[i]-1);
				}
				
			}
		}
	}
	
	public void incrementGroups() {
		for(String state:getStates()) {
			for(FSATransition<IPAElement> trans:getTransitionsForState(state)) {
				Integer[] initGroups = trans.getInitGroups().toArray(new Integer[0]);
				Integer[] matcherGroups = trans.getMatcherGroups().toArray(new Integer[0]);
				

				trans.getInitGroups().clear();
				for(int i = 0; i < initGroups.length; i++) {
					trans.getInitGroups().add(initGroups[i]+1);
				}
				trans.getMatcherGroups().clear();
				for(int i = 0; i < matcherGroups.length; i++) {
					trans.getMatcherGroups().add(matcherGroups[i]+1);
				}
			}
		}
	}
	
	@Override
	public Object clone() {
		PhonexFSA retVal = new PhonexFSA();
		
		for(String state:getStates()) {
			retVal.addState(state);
			if(isFinalState(state)) {
				retVal.addFinalState(state);
			}
			
			for(FSATransition<IPAElement> trans:getTransitionsForState(state)) {
				PhonexTransition pTrans = 
						PhonexTransition.class.cast(trans);
				PhonexTransition cpyTrans = PhonexTransition.class.cast(pTrans.clone());
				retVal.addTransition(cpyTrans);
			}
		}
		
		return retVal;
	}
}
