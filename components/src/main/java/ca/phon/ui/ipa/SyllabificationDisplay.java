/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.phon.ui.ipa;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.plaf.ComponentUI;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.phone.Phone;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.PrefHelper;
import ca.phon.util.Tuple;

/**
 * Displays groups of phones with their syllabification
 * and allows editing of syllabification.  This component
 * works with a list of phones.  To 'simiulate' word-groups
 * place 2 consecutive word boundary markers in between
 * phones.  E.g.,  'a', 'b', ' ', ' ', 'c', 'd'.
 *
 * While not printed, a small space will be left where
 * the double word-boundary marker is found.
 */
public class SyllabificationDisplay extends JComponent {

	/** Syllabification prop */
	public static final String SYLLABIFICATION_PROP_ID = "_syllabification_";
	
	public static final String RESYLLABIFY_PROP_ID = "_resyllabify_";


	/** The ui class ID */
	private static final String uiClassId = "SyllabificationDisplayUI";

	/**
	 * Groups of phones
	 */
	private List<IPATranscript> groups =
			new ArrayList<IPATranscript>();

	/** The focused phone */
	private int focusedPhone = 0;

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
			Phone p = getPhoneAtIndex(pIdx);
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
		return getDisplayedPhones().size();
	}

	public int getNumberOfGroups() {
		return groups.size();
	}

	public IPATranscript getPhonesForGroup(int gIdx) {
		return groups.get(gIdx);
	}

	public void setPhonesForGroup(int gIdx, List<Phone> phones) {
		IPATranscript currentPhones = null;

		if(gIdx >= 0 && gIdx < getNumberOfGroups())
			currentPhones = getPhonesForGroup(gIdx);
		if(currentPhones == null) {
			currentPhones = new IPATranscript();
			groups.add(gIdx, currentPhones);
		}
		currentPhones.clear();
		currentPhones.addAll(phones);
		repaint();

		super.invalidate();
//		groups.add(gIdx, phones);
	}

	public void clear() {
		groups.clear();
		repaint();
	}

	/**
	 * The list of phones with the non-sound
	 * phones filtered out.  WordBoundaries
	 * are placed between groups.
	 */
	public List<Phone> getDisplayedPhones() {
		List<Phone> retVal =
				new ArrayList<Phone>();

		for(IPATranscript grpPhones:groups) {
//			if(retVal.size() > 0)
//				retVal.add(new Phone(" "));
			retVal.addAll(grpPhones.removePunctuation());
		}

		return retVal;
	}

	public Phone getPhoneAtIndex(int idx) {
		Phone retVal = null;
		List<Phone> soundPhones = getDisplayedPhones();
		if(idx >= 0 && idx < soundPhones.size()) {
			retVal = soundPhones.get(idx);
		}
		return retVal;
	}

	/**
	 * Set syllabification at given index
	 */
	public void setSyllabificationAtIndex(int pIdx, SyllableConstituentType scType) {
		Phone p = getPhoneAtIndex(pIdx);
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

	/**
	 * Convert a phone index to a group index.
	 */
	public int getGroupIndexForPhone(int pIdx) {
		int currentIdx = 0;
		for(int gIdx = 0; gIdx < groups.size(); gIdx++) {
			IPATranscript grpPhones = 
					groups.get(gIdx).removePunctuation();

			for(Phone p:grpPhones) {
				if(currentIdx == pIdx)
					return gIdx;
				currentIdx++;
			}
		}
		
		return -1;
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
