/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.phonex;

import ca.phon.fsa.TransitionType;

/**
 * Quantifier for the phonex language.
 *
 */
public class Quantifier {
	
	/**
	 * type of quantifier
	 */
	private QuantifierType type;
	
	/**
	 * transition type
	 */
	private TransitionType transitionType = 
			TransitionType.GREEDY;
	
	/**
	 * x-value for bounded quantifiers
	 * 
	 */
	private int xBound = 0;
	
	/**
	 * y-value for bounded quantifier
	 */
	private int yBound = 0;
	
	/**
	 * Constructor
	 */
	public Quantifier(QuantifierType type) {
		this.type = type;
	}
	
	/** 
	 * Constructor - creates a bounded quantifier
	 * 
	 * @param x
	 * @param y
	 */
	public Quantifier(int x, int y) {
		this.type = QuantifierType.BOUNDED;
		this.xBound = x;
		this.yBound = y;
	}

	public QuantifierType getType() {
		return type;
	}

	public void setType(QuantifierType type) {
		this.type = type;
	}

	public int getxBound() {
		return xBound;
	}

	public void setxBound(int xBound) {
		this.xBound = xBound;
	}

	public int getyBound() {
		return yBound;
	}

	public void setyBound(int yBound) {
		this.yBound = yBound;
	}

	public TransitionType getTransitionType() {
		return transitionType;
	}

	public void setTransitionType(TransitionType transitionType) {
		this.transitionType = transitionType;
	}
	
}
