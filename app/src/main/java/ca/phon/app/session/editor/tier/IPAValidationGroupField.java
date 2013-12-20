package ca.phon.app.session.editor.tier;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.WordVisitor;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;

/**
 * Displays the transcription entered by the given transcriber.  This
 * field does not allow editing.
 */
public class IPAValidationGroupField extends IPAGroupField {
	
	private static final Logger LOGGER = Logger
			.getLogger(IPAValidationGroupField.class.getName());

	private static final long serialVersionUID = 2929922216219766181L;
	
	private volatile int selectedWord = -1;
	
	private List<IPATranscript> wordList = null;

	public IPAValidationGroupField(Tier<IPATranscript> tier, int groupIndex,
			Transcriber transcriber) {
		super(tier, groupIndex, transcriber);
		
		initField();
	}
	
	private void initField() {
		setEditable(false);
		installInputMap();
		addFocusListener(focusListener);
	}
	
	/**
	 * Install a custom input map.
	 * 
	 */
	private void installInputMap() {
		final ActionMap am = new ActionMap();
		final InputMap im = new InputMap();
		
		// next word
		final PhonUIAction nextWordAct = new PhonUIAction(this, "selectNextWord");
		nextWordAct.putValue(PhonUIAction.NAME, "selectNextWord");
		final KeyStroke nextWordKs = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		am.put(nextWordAct.getValue(PhonUIAction.NAME), nextWordAct);
		im.put(nextWordKs, nextWordAct.getValue(PhonUIAction.NAME));
		
		// prev word
		final PhonUIAction prevWordAct = new PhonUIAction(this, "selectPrevWord");
		prevWordAct.putValue(PhonUIAction.NAME, "selectPrevWord");
		final KeyStroke prevWordKs = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		am.put(prevWordAct.getValue(PhonUIAction.NAME), nextWordAct);
		im.put(prevWordKs, prevWordAct.getValue(PhonUIAction.NAME));
		
		// select word
		final PhonUIAction acceptWordAct = new PhonUIAction(this, "acceptCurrentWord");
		acceptWordAct.putValue(PhonUIAction.NAME, "acceptCurrentWord");
		final KeyStroke acceptWordKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		am.put(acceptWordAct.getValue(PhonUIAction.NAME), acceptWordAct);
		im.put(acceptWordKs, acceptWordAct.getValue(PhonUIAction.NAME));
		
		setInputMap(JComponent.WHEN_FOCUSED, im);
		setActionMap(am);
	}
	
	/**
	 * Get the index of the selectd word
	 * @return index of the currently selected
	 *  word
 	 */
	public int getSelectedWordIndex() {
		return this.selectedWord;
	}
	
	/**
	 * Set the selected word
	 * @param selectedWord
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void setSelectedWord(int selectedWord) {
		this.selectedWord = selectedWord;
		final int idx = selectedWordIndex();
		final int length = wordList.get(getSelectedWordIndex()).toString().length();
//		try {
//			getHighlighter().changeHighlight("word",idx, idx + length);
//		} catch (BadLocationException e) {
//			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
//		}
	}
	
	private int selectedWordIndex() {
		int retVal = 0;
		int idx = getSelectedWordIndex();
		for(int i = 0; i < idx; i++) {
			final IPATranscript w = wordList.get(i);
			retVal += (i > 0 ? 1 : 0) + w.toString().length();
		}
		return retVal;
	}
	
	/**
	 * Get the number of words
	 * 
	 * @return number of words
	 */
	public int getNumberOfWords() {
		return getWords().size();
	}
	
	/**
	 * Get the list of words in this group
	 * 
	 * @return list of words
	 */
	public List<IPATranscript> getWords() {
		if(wordList == null) {
			final IPATranscript groupVal = getGroupValue();
			final WordVisitor wordVisitor = new WordVisitor();
			groupVal.accept(wordVisitor);
			wordList = wordVisitor.getWords();
		}
		return wordList;
	}
	
	/*
	 * UI Actions
	 */
	public void selectNextWord(PhonActionEvent pae) {
		final int nextIdx = selectedWord + 1;
		if(nextIdx < getNumberOfWords()) {
			setSelectedWord(nextIdx);
		}
	}
	
	public void selectPrevWord(PhonActionEvent pae) {
		final int prevIdx = selectedWord - 1;
		if(prevIdx >= 0 && prevIdx < getNumberOfWords()) {
			setSelectedWord(prevIdx);
		}
	}
	
	public void acceptCurrentWord(PhonActionEvent pae) {
		// TODO fire some sort of event
	}
	
	/**
	 * Focus listener
	 */
	private final FocusListener focusListener = new FocusListener() {
		
		@Override
		public void focusLost(FocusEvent arg0) {
			// TODO stop drawing highlights
		}
		
		@Override
		public void focusGained(FocusEvent arg0) {
			if(selectedWord == -1 && getNumberOfWords() > 0) {
				setSelectedWord(0);
			}
			// TODO start drawing highlights
		}
	};
	
}
