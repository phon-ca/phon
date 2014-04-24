/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.phon.ui.ipa;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Tuple;

/**
 */
public class SyllabificationDisplay extends JComponent {

	/** Syllabification prop */
	public static final String SYLLABIFICATION_PROP_ID = "_syllabification_";
	
	public static final String RESYLLABIFY_PROP_ID = "_resyllabify_";


	/** The ui class ID */
	private static final String uiClassId = "SyllabificationDisplayUI";

	/**
	 * Transcription
	 */
	private IPATranscript transcript = new IPATranscript();

	/** The focused phone */
	private int focusedPhone = -1;

	/** Phon focus property */
	public final static String PHONE_FOCUS = "_phone_focus_";

	public SyllabificationDisplay() {
		super();
		updateUI();
	}

	@Override
	public String getUIClassID() {
		return uiClassId;
	}

	@Override
	protected void setUI(ComponentUI newUI) {
		super.setUI(newUI);
	}

	@Override
	public void updateUI() {
		setUI(new DefaultSyllabificationDisplayUI(this));
	}

	public SyllabificationDisplayUI getUI() {
		return (SyllabificationDisplayUI)ui;
	}

	public int getFocusedPhone() {
		return focusedPhone;
	}

	public void setFocusedPhone(int pIdx) {
		int currentFocus = focusedPhone;
		int direction = pIdx - currentFocus;
		if(pIdx >= 0 && pIdx < getNumberOfDisplayedPhones()) {
			IPAElement p = getPhoneAtIndex(pIdx);
			if(p.getText().equals(" ")) {
				if(direction < 0)
					pIdx--;
				else
					pIdx++;
			}
			focusedPhone = pIdx;
			repaint();

			super.firePropertyChange(PHONE_FOCUS, currentFocus, pIdx);
		}
	}

	public int getNumberOfDisplayedPhones() {
		return this.transcript.removePunctuation().length();
	}
	public IPATranscript getTranscript() {
		return this.transcript;
	}

	public void setTranscript(IPATranscript phones) {
		this.transcript = phones;
		repaint();

		super.invalidate();
	}

	public void clear() {
		this.transcript = new IPATranscript();
		repaint();
	}

	public IPAElement getPhoneAtIndex(int idx) {
		IPAElement retVal = null;
		IPATranscript soundPhones = this.transcript.removePunctuation();
		if(idx >= 0 && idx < soundPhones.length()) {
			retVal = soundPhones.elementAt(idx);
		}
		return retVal;
	}

	/**
	 * Set syllabification at given index
	 */
	public void setSyllabificationAtIndex(int pIdx, SyllableConstituentType scType) {
		IPAElement p = getPhoneAtIndex(pIdx);
		if(p != null) {
			SyllabificationChangeData oldData =
					new SyllabificationChangeData(pIdx, p.getScType());
//			p.setScType(scType);
			repaint();
			SyllabificationChangeData newData =
					new SyllabificationChangeData(pIdx, scType);

			super.firePropertyChange(SYLLABIFICATION_PROP_ID, oldData, newData);
		}
	}

//	/**
//	 * Re-syllabifiy using given syllabifier
//	 * @param syllabifier
//	 */
//	public void resyllabifiy(Syllabifier syllabifier) {
////		int pIdx = 0;
////		for(int gIdx = 0; gIdx < groups.size(); gIdx++) {
////			List<Phone> grpPhones = groups.get(gIdx);
////
////			String grpTxt = "";
////			for(Phone grpP:grpPhones) grpTxt += grpP.getPhoneString();
////			List<Phone> unsyllabifiedPhones =
////					Phone.toPhoneList(grpTxt);
////			syllabifier.syllabify(unsyllabifiedPhones);
////
////			// copy syllabification
////			for(int i = 0; i < grpPhones.size(); i++) {
////				if(i < unsyllabifiedPhones.size()) {
////					Phone newSyllabifiedPhone = unsyllabifiedPhones.get(i);
////					grpPhones.get(i).setScType(newSyllabifiedPhone.getScType());
////				}
////			}
////			
////		}
////		repaint();
////		super.firePropertyChange(RESYLLABIFY_PROP_ID, true, false);
//	}

	public void resyllabifiy(String name) {
//		Syllabifier syllabifier = Syllabifier.getInstance(name);
//		if(syllabifier != null) {
//
//			resyllabifiy(syllabifier);
//			repaint();
//		}
	}
	
	public void toggleHiatus(int pIdx) {
//		Phone p = getPhoneAtIndex(pIdx);
//		Phone prevP = getPhoneAtIndex(pIdx - 1);
//
//		if(prevP != null) {
//			if(prevP.getScType() == SyllableConstituentType.NUCLEUS
//					&& p.getScType() == prevP.getScType()) {
//				p.setDiphthongMember(!p.isDiphthongMember());
//				super.firePropertyChange(RESYLLABIFY_PROP_ID, true, false);
//			}
//		}
	}

	@Override
	public Dimension getPreferredSize() {
		return getUI().getPreferredSize(this);
	}

	/**
	 * Syllabification change data.  Sent during syllabification events.
	 */
	public class SyllabificationChangeData
			extends Tuple<Integer, SyllableConstituentType> {
		
		public SyllabificationChangeData(
				Integer pIdx, SyllableConstituentType scType) {
			super(pIdx, scType);
		}
		
		public SyllableConstituentType getScType() {
			return super.getObj2();
		}
		
		public Integer getPosition() {
			return super.getObj1();
		}

	}
	
}
