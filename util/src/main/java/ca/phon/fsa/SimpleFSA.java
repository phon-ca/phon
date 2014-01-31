/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.fsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import ca.phon.fsa.FSAState.RunningState;


/**
 * A basic implementation of a fsa.  The
 * input to the machine must be an array of
 * type <CODE>T</CODE>.
 * 
 */
public class SimpleFSA<T> {
	/** The set of states */
	private Set<String> states;
	/** The initial state */
	private String initialState;
	/** The set of final states */
	private Set<String> finalStates;
	/** The transitions */
	private List<FSATransition<T>> transitions;
	
	public SimpleFSA() {
		super();
		
		this.states = 
			Collections.synchronizedSet(new HashSet<String>());
		this.initialState = null;
		this.finalStates = 
			Collections.synchronizedSet(new HashSet<String>());
		this.transitions = 
			Collections.synchronizedList(new ArrayList<FSATransition<T>>());
	}
	
	/**
	 * Add a new state to the machine.
	 * 
	 * @param stateName
	 */
	public void addState(String stateName) {
		states.add(stateName);
	}
	
	/**
	 * Remove a state (and all transitions to/from it)
	 * 
	 * @param stateName
	 */
	public void removeState(String stateName) {
		if(states.contains(stateName)) {
			states.remove(stateName);
			
			Object[] transList = 
				transitions.toArray();
			for(Object obj:transList) {
				FSATransition<T> currentTransition = (FSATransition<T>)obj;
				if(currentTransition.getFirstState().equals(stateName) ||
						currentTransition.getToState().equals(stateName))
					transitions.remove(currentTransition);
			}
		}
	}
	
	/**
	 * Add a new transition
	 * 
	 * @param transition
	 */
	public void addTransition(FSATransition<T> transition) {
		transitions.add(transition);
	}
	
	/**
	 * Remove a transition
	 * 
	 * @param transition
	 */
	public void removeTransition(FSATransition<T> transition) {
		if(transitions.contains(transition))
			transitions.remove(transition);
	}

	public String getInitialState() {
		return initialState;
	}

	public void setInitialState(String initialState) {
		this.initialState = initialState;
	}

	/**
	 * Add a final state
	 */
	public void addFinalState(String state) {
		finalStates.add(state);
	}
	
	/**
	 * Remove a final state
	 */
	public void removeFinalState(String state) {
		if(finalStates.contains(state))
			finalStates.remove(state);
	}
	
	/**
	 * Get the transitions for a paticular state
	 * 
	 * @param state
	 * @return the transitions for the state
	 */
	public List<FSATransition<T>> getTransitionsForState(String state) {
		ArrayList<FSATransition<T>> retVal = 
			new ArrayList<FSATransition<T>>();
		
//		ArrayList<AnythingTransition<T>> anythingTrans = 
//			new ArrayList<AnythingTransition<T>>();
		
		for(FSATransition<T> transition:transitions) {
			if(transition.getFirstState().equals(state)) {
//				if(transition instanceof AnythingTransition)
//					anythingTrans.add((AnythingTransition<T>)transition);
//				else
					retVal.add(transition);
			}
		}
		
		// add the 'anything' transitions to the end so they are processed last
//		retVal.addAll(anythingTrans);
		
		Collections.sort(retVal, new TransitionComparator());
		return retVal;
	}
	
	/**
	 * Get the transitions for a paticular state
	 * 
	 * @param state
	 * @return the transitions for the state
	 */
	public List<FSATransition<T>> getTransitionsToState(String state) {
		ArrayList<FSATransition<T>> retVal = 
			new ArrayList<FSATransition<T>>();
		
		for(FSATransition<T> transition:transitions) {
			if(transition.getToState().equals(state)) {
				retVal.add(transition);
			}
		}
		
		Collections.sort(retVal, new TransitionComparator());
		return retVal;
	}
	
	public List<FSATransition<T>> getTransitions() {
		return this.transitions;
	}
	
	public FSAState<T> runWithTape(T[] tape) {
		FSAState<T> machineState = new FSAState<T>();
		return runWithTape(tape, machineState);
	}
	
	public FSAState<T> runWithTape(T[] tape, FSAState<T> machineState) {
		return runWithTape(tape, machineState, false);
	}
	
	/**
	 * Run the machine with the given tape
	 * 
	 * @param tape
	 * @param machineState
	 * @param forceReluctant if <code>true</code> reluctant quantifiers will 
	 *  be forced to match to attempt a full-input match.
	 */
	public FSAState<T> runWithTape(T[] tape, FSAState<T> machineState, boolean forceReluctant) {
		if(machineState.getTape() == null) {
			machineState.setTape(tape);
			machineState.setTapeIndex(0);
		}
		machineState.setCurrentState(initialState);
		machineState.setRunningState(FSAState.RunningState.Running);
		
		// cached state
		FSAState<T> cachedState = new FSAState<T>();
		cachedState.setCurrentState(initialState);
		cachedState.setTape(tape);
		cachedState.setTapeIndex(machineState.getTapeIndex());
		cachedState.setRunningState(RunningState.Running);
		
		// keep track of possible path choices using a stack
		Stack<DecisionTracker> decisions =
			new Stack<DecisionTracker>();
		
		while(machineState.getRunningState() == FSAState.RunningState.Running) {
			FSATransition<T> toFollow = delta(machineState, decisions);
			String nextState = (toFollow != null ? toFollow.getToState() : null);
			
			// if the next state is null...
			if(nextState == null) {
				// if we are at the end of the tape and have a match
				if(machineState.getTapeIndex() >= tape.length
						&& isFinalState(machineState.getCurrentState())) {
					machineState.setRunningState(RunningState.EndOfInput);
					continue;
				} else {
					if(isFinalState(machineState.getCurrentState())) {
						// only keep longest matches
						if(cachedState.getTapeIndex() < machineState.getTapeIndex()) {
							cachedState.setCurrentState(machineState.getCurrentState());
							cachedState.setTapeIndex(machineState.getTapeIndex());
							cachedState.setGroups(Arrays.copyOf(machineState.getGroupStarts(), machineState.numberOfGroups()), 
									Arrays.copyOf(machineState.getGroupLengths(), machineState.numberOfGroups()));
						}
					}
					toFollow = backtrack(machineState, decisions);
					
					if(toFollow != null 
							&& toFollow.getType() == TransitionType.RELUCTANT
							&& !forceReluctant
							&& isFinalState(cachedState.getCurrentState())) {
						toFollow = null;
					}
					nextState = (toFollow != null ? toFollow.getToState() : null);
				}
			}
			
			// if nextState is still null break
			if(nextState != null) {
				// mark groups
				for(int grpIdx:toFollow.getInitGroups()) {
					machineState.markGroup(grpIdx);
				}
				for(int grpIdx:toFollow.getMatcherGroups()) {
					for(int i = 0; i < toFollow.getMatchLength(); i++)
						machineState.incrementGroup(grpIdx);
				}
				
				machineState.setTapeIndex(machineState.getTapeIndex()+toFollow.getMatchLength());
				machineState.setCurrentState(nextState);
			} else {
				machineState.setRunningState(RunningState.Halted);
			}
		}
		
		if(cachedState.getTapeIndex() >= machineState.getTapeIndex()) {
			machineState.setCurrentState(cachedState.getCurrentState());
			machineState.setTapeIndex(cachedState.getTapeIndex());
			machineState.setGroups(Arrays.copyOf(cachedState.getGroupStarts(), cachedState.numberOfGroups()), 
					Arrays.copyOf(cachedState.getGroupLengths(), cachedState.numberOfGroups()));
		}
		
		return machineState;
//		return (cachedState.getTapeIndex() > machineState.getTapeIndex() ? 
//				cachedState : machineState);
	}
	
	/**
	 * This method chooses which state
	 * the machine should move to given a machine state.  If
	 * more than one choice is available they are added to the
	 * decision stack.  Using backtrack will reset the machine
	 * tape index and return the next state the machine should
	 * move to using the next possible path choice.
	 * 
	 * @param machineState
	 * @param decisions
	 * @return the next machine state.  If no transition can be followed,
	 * or we have reached the end of input <CODE>null</CODE> is returned.
	 */
	protected FSATransition<T> delta(FSAState<T> machineState, Stack<DecisionTracker> decisions) {
		FSATransition<T> retVal = null;
		
		// end of input
		if(machineState.getTapeIndex() > machineState.getTape().length)
			return retVal;
		
		// get possible transitions and the tape object
		String currentState = machineState.getCurrentState();
		List<FSATransition<T>> stateTrans = getTransitionsForState(currentState);
//		T currentObj = 
//			machineState.getTape()[machineState.getTapeIndex()];
		
		// create a list of matching transitions (possible paths)
		List<FSATransition<T>> possiblePaths = new ArrayList<FSATransition<T>>();
		for(FSATransition<T> currentTrans:stateTrans) {
			if(currentTrans.follow(machineState)) {
				possiblePaths.add(currentTrans);
			}
		}
		
		boolean possessive = false;
		// follow the first transition if available
		FSATransition<T> trans = null;
		if(possiblePaths.size() == 0) {
			retVal = null;
		} else {
			trans = possiblePaths.get(0);
			possessive = trans.getType() == TransitionType.POSSESSIVE;
		}
		
		if(trans != null) {
			retVal = trans;
		}
		
		// if more than one path is possible, add the
		// other choices to our decision stack.
		if(!possessive && possiblePaths.size() > 1) {
			DecisionTracker tracker = new DecisionTracker();
			tracker.choices = possiblePaths;
			tracker.choiceIndex = 0;
			tracker.tapeIndex = machineState.getTapeIndex();
			tracker.groupStarts = Arrays.copyOf(machineState.getGroupStarts(), machineState.numberOfGroups());
			tracker.groupLengths = Arrays.copyOf(machineState.getGroupLengths(), machineState.numberOfGroups());
			
			decisions.push(tracker);
		}
		
		return retVal;
	}
	
	/**
	 * This method is called when the machine fails before end of input
	 * during delta.  The method resets to the last path decision and
	 * attempts to choose a new path through the machine.  The machine tape index
	 * is reset by this method.
	 * 
	 * @param machineState
	 * @param decisions
	 * @return the next state of the machine or <CODE>null</CODE> if no
	 * path choices exist on the decision stack.
	 */
	protected FSATransition<T> backtrack(FSAState<T> machineState, Stack<DecisionTracker> decisions) {
		FSATransition<T> retVal = null;
		
		while(!decisions.isEmpty()) {
			DecisionTracker lastDecision = decisions.pop();
			
			int nextChoice = lastDecision.choiceIndex+1;
			// no more paths to choose from, try previous decision 
			if(nextChoice >= lastDecision.choices.size())
				continue;
			
			// get the next transition choice
			FSATransition<T> nextPath = 
				lastDecision.choices.get(nextChoice);
			retVal = nextPath;
			
			// reset tape index
			machineState.setTapeIndex(lastDecision.tapeIndex);
			machineState.setGroups(lastDecision.groupStarts, lastDecision.groupLengths);
			
//			if(machineState.getTapeIndex() == machineState.getTape().length-1)
//				machineState.setRunningState(RunningState.EndOfInput);
//			else
//				machineState.setRunningState(RunningState.Running);
			
			// if we have not exhausted all choices, push
			// the updated decision back onto the stack
			if(nextChoice < lastDecision.choices.size()-1) {
				lastDecision.choiceIndex = nextChoice;
				
				decisions.push(lastDecision);
			}
			return retVal;
		}
		
		return retVal;
	}

	public String[] getFinalStates() {
		return finalStates.toArray(new String[0]);
	}
	
	public String[] getStates() {
		return states.toArray(new String[0]);
	}
	
	public boolean isFinalState(String q) {
		for(String state:finalStates) {
			if(q.equals(state))
				return true;
		}
		return false;
	}
	
	public void printDef() {
		System.out.println("States: ");
		for(String state:states)
			System.out.println("\t" + state + (isFinalState(state) ? " (final)" : ""));
		System.out.println("\nTransitions: ");
		
		for(FSATransition<T> transition:transitions) {
			System.out.println("\t" + transition.getFirstState()  + " --( " +
					transition.getImage() + " )-> "+ transition.getToState());
		}
	}
	
	/**
	 * Get the dot representation of the machine.
	 *
	 */
	public String getDotText() {
		String retVal = "digraph G {\n";
		
		for(String state:states) {
			String stateDesc = "\t" + state + " [shape=\"" + 
				(isFinalState(state) ? "doublecircle" : "circle") + "\"]\n";
			retVal += stateDesc;
		}
		
		for(FSATransition<T> transition:transitions) {
			String transLbl = new String();
			for(int initGrp:transition.getInitGroups()) {
				transLbl += (transLbl.length() == 0 ? "[" : ",") + "+" + initGrp;
			}
			for(int grpIdx:transition.getMatcherGroups()) {
				transLbl += (transLbl.length() == 0 ? "[" : ",") + grpIdx;
			}
			transLbl += (transLbl.length() > 0 ? "]":"");
			transLbl = transition.getImage() + (transLbl.length() > 0 ? " " + transLbl : "");
			String transDesc = "\t" + transition.getFirstState() + " -> " +
				transition.getToState() + " [label=\"" + transLbl + "\"];\n";
			retVal += transDesc;
		}
		
		retVal += "}\n";
		
		return retVal;
	}
	
	private class TransitionComparator implements Comparator<FSATransition<T>> {

		@Override
		public int compare(FSATransition<T> o1, FSATransition<T> o2) {
			if(o1.getType().ordinal() > o2.getType().ordinal())
				return -1;
			else if(o1.getType().ordinal() < o2.getType().ordinal())
				return 1;
			else
				return 0;
		}

	}
	
	private class DecisionTracker {
		public int tapeIndex;
		public List<FSATransition<T>> choices;
		public int choiceIndex;
		public int[] groupStarts;
		public int[] groupLengths;
	}
}
