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
