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
package ca.phon.fsa;

import java.util.*;

public abstract class FSATransition<T> {
	
	/** The attached state */
	private String firstState;
	/** The 'to' state */
	private String toState;
	/** The image */
	private String image;
	
	/**
	 * Transition type
	 */
	private TransitionType type = TransitionType.NORMAL;
	
	/** Offset type */
	private OffsetType offsetType = OffsetType.NORMAL;
	
	/** Matcher groups started by this transition */
	private final Set<Integer> startGroups = new HashSet<Integer>();
	
	/** Matcher groups added to by this transition */
	private final Set<Integer> matcherGroups = new HashSet<Integer>();
	
	/**
	 * Determines if the fsa will follow the transition
	 * given the current running state.
	 * 
	 * @param currentState the current machine state
	 * @return <code>true</code> if this transition
	 *  can be followed given the current state, <code>false</code>
	 *  otherwise
	 */
	public abstract boolean follow(FSAState<T> currentState);
	
	public String getFirstState() {
		return firstState;
	}
	public void setFirstState(String firstState) {
		this.firstState = firstState;
	}
	public String getToState() {
		return toState;
	}
	public void setToState(String toState) {
		this.toState = toState;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
	/**
	 * The number of items matched by this transition
	 * default is 1.
	 * 
	 * @return number of items to match on the tape
	 */
	public int getMatchLength() {
		return 1;
	}
	
	/**
	 * Get the matcher groups started
	 * by this transition.  Every time
	 * this transition is followed it will
	 * begin a new match for the indicated
	 * groups.
	 * 
	 * @return the (live) list of matcher 
	 *  groups started by following this
	 *  transition
	 */
	public Set<Integer> getInitGroups() {
		return this.startGroups;
	}
	
	/**
	 * Get the matcher groups that following
	 * this transition modifies.
	 * 
	 * @return the (live) list of matcher
	 *  groups modified by this transition
	 */
	public Set<Integer> getMatcherGroups() {
		return this.matcherGroups;
	}
	
	/**
	 * Get type
	 * 
	 * @return transition type - default <code>NORMAL</code>
	 */
	public TransitionType getType() {
		return this.type;
	}
	
	/**
	 * Set transition type.
	 * 
	 * @param type
	 */
	public void setType(TransitionType type) {
		this.type = type;
	}

	public OffsetType getOffsetType() {
		return offsetType;
	}

	public void setOffsetType(OffsetType offsetType) {
		this.offsetType = offsetType;
	}
	
}
