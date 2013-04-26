package ca.phon.opgraph.nodes.phonex;

import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.phon.phonex.PhonexPatternException;

public interface PhonexNode extends NodeSettings {
	
	/**
	 * Set (and compile) phonex expression.
	 * 
	 * @param phonex
	 * @throws PhonexPatternException if the given
	 *  phonex is not valid
	 */
	public void setPhonex(String phonex) throws PhonexPatternException;
	
	/**
	 * Get current phonex 
	 * 
	 * @return phonex
	 * 
	 */
	public String getPhonex();

}
