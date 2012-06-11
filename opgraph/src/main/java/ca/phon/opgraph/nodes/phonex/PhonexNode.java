package ca.phon.opgraph.nodes.phonex;

import java.awt.Component;
import java.util.Properties;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OperableContext;
import ca.gedge.opgraph.OperableVertex;
import ca.gedge.opgraph.OperableVertexInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.PhonexPattern;
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
