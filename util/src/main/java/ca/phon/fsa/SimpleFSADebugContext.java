package ca.phon.fsa;

import java.util.Arrays;
import java.util.Stack;

import ca.phon.fsa.FSAState.RunningState;
import ca.phon.fsa.SimpleFSA.DecisionTracker;

/**
 * Class for executing a SimleFSA in a step-by-step
 * manner for the purpose of debugging.
 *
 */
public class SimpleFSADebugContext<T> {

	public final SimpleFSA<T> fsa;
	
	/** Current state */
	private FSAState<T> machineState;
	
	/** Cached state */
	private FSAState<T> cachedState;
	
	/** Decision stack */
	private Stack<DecisionTracker<T>> decisions;
	
	public SimpleFSADebugContext(SimpleFSA<T> fsa) {
		super();
		
		this.fsa = fsa;
	}
	
	public SimpleFSA<T> getFSA() {
		return this.fsa;
	}
	
	public FSAState<T> getMachineState() {
		return this.machineState;
	}
	
	public FSAState<T> getCachedState() {
		return this.cachedState;
	}
	
	public Stack<DecisionTracker<T>> getDecisionStack() {
		return this.decisions;
	}
	
	public void reset(T[] tape) {
		// setup initial machine state
		machineState = new FSAState<T>();
		machineState.setTape(tape);
		machineState.setTapeIndex(0);
		machineState.setCurrentState(fsa.getInitialState());
		machineState.setRunningState(RunningState.Running);
	
		// setup cached state
		cachedState = new FSAState<T>();
		cachedState.setTape(tape);
		cachedState.setTapeIndex(0);
		cachedState.setCurrentState(fsa.getInitialState());
		cachedState.setRunningState(RunningState.Running);
		
		decisions = new Stack<>();
	}
	
	public boolean canStep() {
		return (machineState != null && machineState.getRunningState() == FSAState.RunningState.Running);
	}
	
	/**
	 * Step and return the transition followed
	 * 
	 * @return the state followed or <code>null</code> if
	 *  no matching transition found
	 */
	public FSATransition<T> step() {
		if(!canStep()) throw new IllegalStateException();
		
		FSATransition<T> toFollow = fsa.delta(machineState, decisions);
		String nextState = (toFollow != null ? toFollow.getToState() : null);
	
		if(nextState == null) {
			// if we are at the end of the tape and have a match
			if(machineState.getTapeIndex() >= machineState.getTape().length
					&& fsa.isFinalState(machineState.getCurrentState())) {
				machineState.setRunningState(RunningState.EndOfInput);
				return null;
			} else {
				if(fsa.isFinalState(machineState.getCurrentState())) {
					// only keep longest matches
					if(cachedState.getTapeIndex() < machineState.getTapeIndex()) {
						cachedState.setRunningState(RunningState.EndOfInput);
						cachedState.setCurrentState(machineState.getCurrentState());
						cachedState.setTapeIndex(machineState.getTapeIndex());
						cachedState.setGroups(Arrays.copyOf(machineState.getGroupStarts(), machineState.numberOfGroups()), 
								Arrays.copyOf(machineState.getGroupLengths(), machineState.numberOfGroups()));
						cachedState.setLookAheadOffset(machineState.getLookAheadOffset());
						cachedState.setLookBehindOffset(machineState.getLookBehindOffset());
					}
				}
				toFollow = fsa.backtrack(machineState, decisions);
				
				if(toFollow != null 
						&& toFollow.getType() == TransitionType.RELUCTANT
//						&& !forceReluctant
						&& fsa.isFinalState(cachedState.getCurrentState())) {
					toFollow = null;
				}
				nextState = (toFollow != null ? toFollow.getToState() : null);
			}
		}
		
		// if nextState is still null break
		if(nextState != null) {
			// mark groups
			for(int grpIdx:toFollow.getInitGroups()) {
				machineState.markGroup(grpIdx, toFollow.getMatchLength());
			}
			for(int grpIdx:toFollow.getMatcherGroups()) {
				for(int i = 0; i < toFollow.getMatchLength(); i++)
					machineState.incrementGroup(grpIdx);
			}
			
			if(toFollow.getOffsetType() == OffsetType.LOOK_BEHIND) {
				machineState.setLookBehindOffset(machineState.getLookBehindOffset()+toFollow.getMatchLength());
			} else if(toFollow.getOffsetType() == OffsetType.LOOK_AHEAD) {
				machineState.setLookAheadOffset(machineState.getLookAheadOffset()+toFollow.getMatchLength());
			} else {
				machineState.setTapeIndex(machineState.getTapeIndex()+toFollow.getMatchLength());
			}
			machineState.setCurrentState(nextState);
		} else {
			machineState.setRunningState(RunningState.Halted);
			return null;
		}
		
		return toFollow;
	}
	
	/**
	 * Reset execution state
	 *
	 */
	public void reset() {
		machineState = new FSAState<T>();
		cachedState = null;
		decisions = new Stack<DecisionTracker<T>>();
	}
	
}
