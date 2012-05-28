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

import java.util.HashSet;
import java.util.Set;

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
	
	/** Matcher groups started by this transition */
	private final Set<Integer> startGroups = new HashSet<Integer>();
	
	/** Matcher groups added to by this transition */
	private final Set<Integer> matcherGroups = new HashSet<Integer>();
	
	/**
	 * Determines if the fsa will follow the transition
	 * given the current running state.
	 * 
	 * @param currentState the current matchine state
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

}
