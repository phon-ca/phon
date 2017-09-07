/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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

package ca.phon.ui.ipa;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import ca.phon.ipa.*;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Tuple;

/**
 */
public class SyllabificationDisplay extends JComponent {

	/** Syllabification prop */
	public static final String SYLLABIFICATION_PROP_ID = "_syllabification_";
	
	public static final String HIATUS_CHANGE_PROP_ID = "_hiatus_change_";
	
	public static final String RESYLLABIFY_PROP_ID = "_resyllabify_";


	/** The ui class ID */
	private static final String uiClassId = "SyllabificationDisplayUI";

	/**
	 * Transcription
	 */
	private IPATranscript transcript = new IPATranscript();
	
	/**
	 * Display transcription
	 */
	private IPATranscript displayedPhones = new IPATranscript();

	/** The focused phone */
	private int focusedPhone = -1;

	/** Phon focus property */
	public final static String PHONE_FOCUS = "_phone_focus_";

	public SyllabificationDisplay() {
		super();
		setOpaque(false);
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
		return getDisplayedPhones().length();
	}
	public IPATranscript getTranscript() {
		return this.transcript;
	}

	public void setTranscript(IPATranscript phones) {
		this.transcript = phones;
		displayedPhones = this.transcript.removePunctuation(true);
		
		repaint();

		super.invalidate();
	}

	public void clear() {
		this.transcript = new IPATranscript();
		this.displayedPhones = new IPATranscript();
		repaint();
	}

	public IPAElement getPhoneAtIndex(int idx) {
		IPAElement retVal = null;
		if(idx >= 0 && idx < getDisplayedPhones().length()) {
			retVal = getDisplayedPhones().elementAt(idx);
		}
		return retVal;
	}

	/**
	 * Set syllabification at given index
	 */
	public void setSyllabificationAtIndex(int pIdx, SyllableConstituentType scType) {
		IPAElement p = getPhoneAtIndex(pIdx);
		if(p != null) {
			final int realPhonexIndex = getTranscript().indexOf(p);
			SyllabificationChangeData oldData =
					new SyllabificationChangeData(realPhonexIndex, p.getScType());
			SyllabificationChangeData newData =
					new SyllabificationChangeData(realPhonexIndex, scType);

			super.firePropertyChange(SYLLABIFICATION_PROP_ID, oldData, newData);
		}
	}

	public void toggleHiatus(int pIdx) {
		final IPAElement ele = getPhoneAtIndex(pIdx);
		final int realIdx = getTranscript().indexOf(ele);
		super.firePropertyChange(HIATUS_CHANGE_PROP_ID, -1, realIdx);
	}

	@Override
	public Dimension getPreferredSize() {
		return getUI().getPreferredSize(this);
	}
	
	public IPATranscript getDisplayedPhones() {
		return displayedPhones;
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
