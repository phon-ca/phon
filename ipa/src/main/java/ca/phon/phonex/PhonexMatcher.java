package ca.phon.phonex;

import java.util.List;

import ca.phon.fsa.FSAState;
import ca.phon.fsa.FSAState.RunningState;
import ca.phon.ipa.IPAElement;

public class PhonexMatcher {
	
	/**
	 * Input
	 */
	private List<IPAElement> input;
	
	/**
	 * phonex Pattern
	 */
	private PhonexPattern pattern;
	
	/**
	 * Last fsa state
	 */
	private FSAState<IPAElement> lastMatchState = new FSAState<IPAElement>();
	
	/**
	 * Last match start index
	 */
	private int lastMatchStart = 0;
	
	/**
	 * Constructor
	 */
	PhonexMatcher(PhonexPattern pattern, List<IPAElement> input) {
		this.pattern = pattern;
		this.input = input;
		reset();
	}
	
	/**
	 * Return this matcher's pattern.
	 * 
	 * @return the pattern for this matcher
	 */
	public PhonexPattern pattern() {
		return this.pattern;
	}
	
	/**
	 * Reset the matcher
	 */
	public void reset() {
		lastMatchState = new FSAState<IPAElement>();
		lastMatchState.setTape(input.toArray(new IPAElement[0]));
		lastMatchState.setTapeIndex(0);
		lastMatchStart = 0;
	}
	
	/**
	 * Reset the matcher with a new input sequence
	 * 
	 * @param input
	 */
	public void reset(List<IPAElement> input) {
		this.input = input;
		reset();
	}
	
	/**
	 * Reset matcher and perform a full input match test.
	 * 
	 * @param <code>true</code> if the entire input matches
	 *  the pattern, <code>false</code> otherwise
	 */
	public boolean matches() {
		reset();
		lastMatchState =
				pattern.getFsa().runWithTape(input.toArray(new IPAElement[0]), lastMatchState, true);
		return pattern.getFsa().isFinalState(lastMatchState.getCurrentState())
				&& lastMatchState.getRunningState() == RunningState.EndOfInput;
	}
	
	/**
	 * Reset matcher and attempt to find an occurance
	 * of the pattern in the given input.
	 * 
	 * Use {@link #group()}, {@link #start()}, and {@link #end()}
	 * for more information.
	 * 
	 * @param <code>true</code> if a sub-sequence of phones
	 *  is found that matches the pattern
	 */
	public boolean find() {
		boolean retVal = false;
		
//		// reset if no previous match
//		try {
//			checkLastMatch();
//		} catch (IllegalStateException ex) {
//			reset();
//		}
		
		int currentIdx = lastMatchState.getTapeIndex();
		while(!retVal && currentIdx < input.size()) {
			
			lastMatchState.setTapeIndex(currentIdx);
			
			lastMatchState =
					pattern.getFsa().runWithTape(input.toArray(new IPAElement[0]), lastMatchState);
			retVal = 
					pattern.getFsa().isFinalState(lastMatchState.getCurrentState()) && (lastMatchState.getTapeIndex() != currentIdx);
			if(retVal)
				lastMatchStart = currentIdx;
			currentIdx++;
		}
		
		return retVal;
	}
	
	/**
	 * Reset matcher and attempt to find the next occurrence 
	 * of the pattern starting at the given index in the input.
	 * 
	 * @param index
	 * @return <code>true</code> if an occurrence of the pattern
	 *  was found, <code>false</code> otherwise
	 */
	public boolean find(int index) {
		reset();
		lastMatchState.setTapeIndex(index);
		return find();
	}
	
	/**
	 * Return the number of capturing groups (excluding
	 * group zero.)
	 * 
	 * @return the number of capturing groups
	 */
	public int groupCount() {
		return pattern.getFsa().getNumberOfGroups();
	}
	
	/**
	 * Return the start index of the last match.
	 * 
	 * @return the start index of the last match
	 *  (i.e., start index of group zero.)
	 */
	public int start() {
		return start(0);
	}
	
	/**
	 * Return the start index of the specified group.
	 * 
	 * @param gIdx
	 * @return the start index of the group <code>gIdx</code>,
	 *  or -1 if group data was not found
	 */
	public int start(int gIdx) {
		checkLastMatch();
		int retVal = 0;
		if(gIdx == 0) 
			retVal = lastMatchStart;
		else {
			retVal =
				(gIdx == 0 ? 0 : lastMatchState.getGroupStarts()[gIdx-1]);
		}
		return retVal;
	}
	
	/**
	 * Return the end index plus one of group zero
	 * 
	 * @return the end index plus one of group zero
	 */
	public int end() {
		return end(0);
	}
	
	/**
	 * Return the end index plus one of the specified
	 * group.
	 * 
	 * @param gIdx
	 * @return the end index of the specified group
	 *  plus one
	 */
	public int end(int gIdx) {
		checkLastMatch();
		int retVal = 0;
		if(gIdx == 0)
			retVal = lastMatchState.getTapeIndex();
		else {
			retVal =
					lastMatchState.getGroupStarts()[gIdx-1] +
					lastMatchState.getGroupLengths()[gIdx-1];
		}
		return retVal;
	}
	
	/**
	 * Check to make sure the match state is good.
	 * 
	 * @throws IllegalStateException
	 */
	private void checkLastMatch() 
		throws IllegalStateException {
		if(lastMatchState.getCurrentState() == null 
				|| !pattern.getFsa().isFinalState(lastMatchState.getCurrentState())) {
			throw new IllegalStateException("No previous match");
		}
	}
	
	/**
	 * Tells if the matcher has a current match.
	 * 
	 * @return <code>true</code> if the last call
	 *  to {@link #matches()} or {@link #find()}
	 *  was successful
	 */
	public boolean hasMatch() {
		boolean retVal = false;
		try {
			checkLastMatch();
			retVal = true;
		} catch (IllegalStateException e) {}
		return retVal;
	}
	
	/**
	 * Return the group value for this match.  
	 * 
	 * @return group value for group zero
	 */
	public List<IPAElement> group() {
		return input.subList(start(), end());
	}
	
	/**
	 * Return the group value for the specified
	 * group
	 * 
	 * @param gIdx
	 * @return group value for the specified group
	 */
	public List<IPAElement> group(int gIdx) {
		return input.subList(start(gIdx), end(gIdx));
	}

}
