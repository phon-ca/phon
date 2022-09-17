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
