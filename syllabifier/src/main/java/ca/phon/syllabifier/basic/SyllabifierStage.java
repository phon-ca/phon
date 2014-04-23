package ca.phon.syllabifier.basic;

import java.util.List;

import ca.phon.ipa.IPAElement;

public interface SyllabifierStage {

	/**
	 * Run syllabifier stage on given list of phones.
	 * 
	 * @param phones
	 * @return <code>true</code> if any {@link IPAElement}s have been
	 *  marked, <code>false</code> otherwise
	 */
	public boolean run(List<IPAElement> phones);
	
	/**
	 * Tells the syllabifier if this stage should be executed until
	 * run() returns false.
	 * 
	 * @return <code>true</code> if stage should be repeated, <code>false</code>
	 *  otherwise
	 */
	public boolean repeatWhileChanges();
	
	/**
	 * Return name of the stage.
	 * 
	 * @return stage name
	 */
	public String getName();
	
}
