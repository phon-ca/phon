/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.fsa;

import java.util.Arrays;


/**
 * 
 *
 * @param <T>
 */
public class FSAState<T> {
	
	public static enum RunningState {
		Running,
		Halted,
		EndOfInput
	};
	
	/** The current state */
	private String currentState;
	/** The current tape index */
	private int tapeIndex;
	/** The tape */
	private T[] tape;
	/** The current machine running status */
	private RunningState runningState = RunningState.Halted;
	/** Current look-behind offset */
	private int lookBehindOffset = 1;
	/** Current look-ahead offset */
	private int lookAheadOffset = 0;
	
	/** Group start indices */
	private int[] groupStarts = new int[0];
	/** Group lengths */
	private int[] groupLengths = new int[0];
	
	/** Constructor */
	public FSAState() {
		super();
	}
	
	public String getCurrentState() {
		return currentState;
	}
	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}
	public RunningState getRunningState() {
		return runningState;
	}
	public void setRunningState(RunningState runningState) {
		this.runningState = runningState;
	}
	public T[] getTape() {
		return tape;
	}
	public void setTape(T[] tape) {
		this.tape = tape;
	}
	public int getTapeIndex() {
		return tapeIndex;
	}
	public void setTapeIndex(int tapeIndex) {
		this.tapeIndex = tapeIndex;
	}
	
	public int getLookAheadOffset() {
		return this.lookAheadOffset;
	}
	
	public void setLookAheadOffset(int lookAheadOffset) {
		this.lookAheadOffset = lookAheadOffset;
	}
	
	public int getLookBehindOffset() {
		return this.lookBehindOffset;
	}
	
	public void setLookBehindOffset(int lookBehindOffset) {
		this.lookBehindOffset = lookBehindOffset;
	}
	
//	@SuppressWarnings("unchecked")
	public T[] getMatchedTape() {
//		T[] retVal = (T[])Array.newInstance(T.getCl,capacity);
//		List<T> retVal = new ArrayList<T>();
//		
//		for(int i = 0; i < tapeIndex; i++)
//			retVal.add(tape[i]);
//		return (T[])retVal.toArray();
		return Arrays.copyOfRange(tape, 0, tapeIndex);
	}
	
	/**
	 * Get the indicated group.
	 * 
	 * @param groupIdx
	 * @return the matched contents
	 *  for the group or <code>null</code> if
	 *  not found
	 */
//	@SuppressWarnings("unchecked")
	public T[] getGroup(int grpIdx) {
		T[] retVal = null;
		
		if(grpIdx == 0) {
			retVal = getMatchedTape();
		} else {
			if(grpIdx <= numberOfGroups()) {
				int grpStart = groupStarts[grpIdx-1];
				int grpLen = groupLengths[grpIdx-1];
				
				retVal = Arrays.copyOfRange(tape, grpStart, grpStart+grpLen);
			}
		}
		return retVal;
	}
	
	/**
	 * Sets the given group start index to
	 * the current tape index and the group
	 * lenth to 1.
	 * 
	 * @param groupIndex
	 * 
	 */
	public void markGroup(int groupIndex, int matchLength) {
		// ensure capacity
		if(groupStarts.length < groupIndex) {
			int numberOfGroups = groupIndex;
			groupStarts = Arrays.copyOf(groupStarts, numberOfGroups);
			groupLengths = Arrays.copyOf(groupLengths, numberOfGroups);
		}
		groupStarts[groupIndex-1] = tapeIndex;
		groupLengths[groupIndex-1] = matchLength;
	}
	
	/**
	 * Increment the length of the specified
	 * group
	 * 
	 * @param groupIndex
	 */
	public void incrementGroup(int groupIndex) {
		groupLengths[groupIndex-1]++;
	}
	
	/**
	 * Set group information
	 * 
	 * @param groupStarts
	 * @param groupLengths
	 * @throws IllegalArgumentException if the given arrays
	 *  are not of the same length
	 */
	public void setGroups(int[] groupStarts, int[] groupLengths) {
		if(groupStarts.length != groupLengths.length) {
			throw new IllegalArgumentException("group arrays must have same length");
		}
		this.groupStarts = groupStarts;
		this.groupLengths = groupLengths;
	}
	
	/**
	 * Get group starts
	 * 
	 * @return array of group starts
	 */
	public int[] getGroupStarts() {
		return this.groupStarts;
	}
	
	/**
	 * Get group lenths
	 * 
	 * @return array of group lengths
	 */
	public int[] getGroupLengths() {
		return this.groupLengths;
	}
	
	/** 
	 * Reset group start and length data.
	 * 
	 */
	public void resetGroupData() {
		this.groupStarts = new int[0];
		this.groupLengths = new int[0];
	}
	
	/**
	 * The number of groups
	 * 
	 * @return number of groups
	 */
	public int numberOfGroups() {
		return this.groupStarts.length;
	}
}
