package ca.phon.fsa;

/**
 * FSA transition type.  Transition type affects
 * the order of non-deterministic decision making
 * as well as how/when backtracking is performed.
 *
 */
public enum TransitionType {
	RELUCTANT,
	NORMAL,
	GREEDY,
	POSSESSIVE;

}
