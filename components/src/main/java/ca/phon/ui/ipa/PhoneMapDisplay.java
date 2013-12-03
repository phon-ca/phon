/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.plaf.ComponentUI;

import ca.phon.ipa.Phone;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.util.Tuple;

/**
 * Display a list of phonemaps for editing.
 * Each phonemap adds to the number of possible
 * phone positions (for alignment.)
 *
 */
public class PhoneMapDisplay extends JComponent {

	/** Alignment change property */
	public static final String ALIGNMENT_CHANGE_PROP = "_aligned_changed_";

	/** Property for drawing colours */
	public static final String PAINT_PHONE_BACKGROUND_PROP = "_paint_phone_background_";

	/** List of phone maps *
	 *
	 */
	private List<PhoneMap> groups =
			new ArrayList<PhoneMap>();

	/**
	 * The currently focused position
	 * 
	 */
	private int currentFocusPosition = 0;

	/**
	 * Phone current focused.
	 */
	private Phone focusedPhone = null;

	private boolean paintPhoneBackground = false;

	
	/** The ui class ID */
	private static final String uiClassId = "PhoneMapDisplayUI";

	public PhoneMapDisplay() {
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
		setUI(new DefaultPhoneMapDisplayUI(this));
	}

	public PhoneMapDisplayUI getUI() {
		return (PhoneMapDisplayUI)ui;
	}

	/**
	 * Get the number of positions in the display.  This includes
	 * indel positions.
	 */
	public int getNumberOfAlignmentPositions() {
		int retVal = 0;

		for(PhoneMap pm:groups) {
			if(pm == null) continue;
			retVal += pm.getAlignmentLength();
		}

		return retVal;
	}

	public int getNumberOfGroups() {
		return groups.size();
	}

	public PhoneMap getPhoneMapForGroup(int gIdx) {
		PhoneMap retVal = null;

		if(gIdx >=0 && gIdx < groups.size()) {
			retVal = groups.get(gIdx);

		}

		return retVal;
	}

	public void setPhoneMapForGroup(int gIdx, PhoneMap pm) {
		if(gIdx < groups.size()) {
			groups.remove(gIdx);
		}
		groups.add(gIdx, pm);

		super.invalidate();
	}

	public void clear() {
		groups.clear();
		repaint();
	}

	public int getFocusedPosition() {
		return this.currentFocusPosition;
	}

	public void setFocusedPosition(int nextFocus) {
		if(nextFocus >= 0 && nextFocus <
				this.getNumberOfAlignmentPositions()) {
			this.currentFocusPosition = nextFocus;
			repaint();
		}
	}

	public Tuple<Integer, Integer> positionToGroupPos(int pos) {
		Tuple<Integer, Integer> retVal = new Tuple<Integer, Integer>(0, 0);

		int gIdx = 0;
		int cPos = pos;
		for(PhoneMap pm:groups) {
			if(pm.getAlignmentLength() <= cPos) {
				cPos -= pm.getAlignmentLength();
				gIdx++;
			} else {
				break;
			}
		}

		retVal.setObj1(gIdx);
		retVal.setObj2(cPos);

		return retVal;
	}

	/**
	 * Get aligned phones at the given position.
	 * Target alignment is obj1, actual is obj2.
	 *
	 * Indels are indicated by null.
	 *
	 * @param idx
	 * @return the aligned phones at the given position
	 */
	public Tuple<Phone, Phone> getAlignedPhones(int idx) {
		Tuple<Phone, Phone> retVal = new Tuple<Phone, Phone>();

		int gIdx = 0;
		int pIdx = 0;
		int currentIdx = 0;

		PhoneMap currentGrp = null;
		while(gIdx < groups.size() && currentIdx <= idx) {

			if(currentGrp == null) {
				currentGrp = groups.get(gIdx);
				pIdx = 0;
			}

			if(pIdx >= currentGrp.getAlignmentLength()) {
				gIdx++;
				currentGrp = null;
			} else if(currentIdx == idx) {

				List<Phone> ps = 
						currentGrp.getAlignedElements(pIdx);
				retVal.setObj1(ps.get(0));
				retVal.setObj2(ps.get(1));

				currentIdx++;
			} else {
				currentIdx++;
				pIdx++;
			}
		}

		return retVal;
	}

	public boolean isPaintPhoneBackground() {
		return this.paintPhoneBackground;
	}

	public void setPaintPhoneBackground(boolean v) {
		boolean oldVal = this.paintPhoneBackground;
		this.paintPhoneBackground = v;
		repaint();

		super.firePropertyChange(PAINT_PHONE_BACKGROUND_PROP, oldVal, v);
	}

	public void togglePaintPhoneBackground() {
		this.setPaintPhoneBackground(!this.isPaintPhoneBackground());
	}

	/* Movement */
	/**
	 * This method will move the value at
	 * alignment[0][position] one place right.  The
	 * return value is the mutated alignment.
	 */
	public Integer[][] mutateAlignment(
			Integer[][] alignment, int position) {
		// calculate the new alignment the size
		// is always +1 except when moving into an indel
		Integer newTopAlignment[] = null;
		Integer newBottomAlignment[] = null;

		if(position+1 < alignment[0].length
				&& alignment[0][position+1] == -1) {
			// in this case we can just swap the values
			newTopAlignment = alignment[0];
			int temp = newTopAlignment[position];
			newTopAlignment[position] = -1;
			newTopAlignment[position+1] = temp;

			newBottomAlignment = alignment[1];
		} else {
			int newAlignmentSize = alignment[0].length+1;
			newTopAlignment = new Integer[newAlignmentSize];
			int oldAlignmentIndex = 0;
			for(int i = 0; i < newAlignmentSize; i++) {
				if(i == position) {
					// insert an indel at old position
					newTopAlignment[i] = -1;
				} else {
					newTopAlignment[i] = alignment[0][oldAlignmentIndex++];
				}
			}

			newBottomAlignment = new Integer[newAlignmentSize];
			oldAlignmentIndex = 0;
			for(int i = 0; i < newAlignmentSize; i++) {
				if(oldAlignmentIndex >= alignment[1].length) {
					newBottomAlignment[i] = -1;
				} else {
					newBottomAlignment[i] = alignment[1][oldAlignmentIndex++];
				}
			}
		}

		// remove instances of indels aligned with indels
		ArrayList<Integer> compactPositions = new ArrayList<Integer>();
		for(int i = 0; i < newTopAlignment.length; i++) {
			if(newTopAlignment[i] == -1 && newBottomAlignment[i] == -1)
				compactPositions.add(i);
		}

		Integer tempTop[] = newTopAlignment;
		Integer tempBottom[] = newBottomAlignment;

		newTopAlignment = new Integer[tempTop.length-compactPositions.size()];
		newBottomAlignment = new Integer[tempBottom.length-compactPositions.size()];

		int tempIndex = 0;
		for(int i = 0; i < tempTop.length; i++) {
			// skip aligned indels
			if(!compactPositions.contains(i)) {
				newTopAlignment[tempIndex] = tempTop[i];
				newBottomAlignment[tempIndex] = tempBottom[i];
				tempIndex++;
			}
		}

		Integer[][] newAlignment = new Integer[2][];
		newAlignment[0] = newTopAlignment;
		newAlignment[1] = newBottomAlignment;

		return newAlignment;
	}

	/**
	 * Move specified phone one position right
	 * @param group and alignment index
	 * @param form (top or bottom)
	 */
	public void movePhoneRight(Tuple<Integer, Integer> alignmentPos,
			Form form) {
		PhoneMap pm = groups.get(alignmentPos.getObj1());
		int pos = alignmentPos.getObj2();

		Phone phoneToMove =
				(form == Form.Actual ? pm.getBottomAlignmentElements().get(pos)
						: pm.getTopAlignmentElements().get(pos));

		// can't move indels
		if (phoneToMove == null) return;

		Integer[][] oldAlignment = new Integer[2][];
		oldAlignment[0] =
				(form == Form.Actual ? pm.getBottomAlignment() : pm.getTopAlignment());
		oldAlignment[1] =
				(form == Form.Actual ? pm.getTopAlignment() : pm.getBottomAlignment());

		Integer[][] newAlignment =
				mutateAlignment(oldAlignment, pos);

		pm.setTopAlignment(
				(form == Form.Actual ? newAlignment[1] : newAlignment[0]));
		pm.setBottomAlignment(
				(form == Form.Actual ? newAlignment[0] : newAlignment[1]));

		// check to see if we need to move our focus
		Phone phoneAfterMove = 
				(form == Form.Actual ? pm.getBottomAlignmentElements().get(pos)
					: pm.getTopAlignmentElements().get(pos));
		
		if(phoneAfterMove == null || phoneAfterMove != phoneToMove) {
			setFocusedPosition(getFocusedPosition()+1);
		} else {
			repaint();
		}
		AlignmentChangeData oldData =
				new AlignmentChangeData(alignmentPos.getObj1(), oldAlignment);
		AlignmentChangeData newData =
				new AlignmentChangeData(alignmentPos.getObj1(), newAlignment);

//		super.firePropertyChange(ALIGNMENT_CHANGE_PROP, oldData, newData);
	}

	public void movePhoneLeft(Tuple<Integer, Integer> alignmentPos,
			Form form) {
		PhoneMap pm = groups.get(alignmentPos.getObj1());
		int pos = alignmentPos.getObj2();

		Phone phoneToMove =
				(form == Form.Actual ? pm.getBottomAlignmentElements().get(pos)
						: pm.getTopAlignmentElements().get(pos));

		// can't move indels
		if (phoneToMove == null) return;

		// same as moveTopPhoneRight, but we will flip the arrays before use
		Integer[] _oldTopAlignment = pm.getTopAlignment();
		ArrayList<Integer> oldTopAlignment = new ArrayList<Integer>();
		for(Integer i:_oldTopAlignment) {
			oldTopAlignment.add(i);
		}
		Collections.reverse(oldTopAlignment);
		Integer[] _oldBottomAlignment = pm.getBottomAlignment();
		ArrayList<Integer> oldBottomAlignment = new ArrayList<Integer>();
		for(Integer i:_oldBottomAlignment) {
			oldBottomAlignment.add(i);
		}
		Collections.reverse(oldBottomAlignment);

		Integer[][] reversedAlignment = new Integer[2][];
		reversedAlignment[0] = (form == Form.Actual ? oldBottomAlignment.toArray(new Integer[0]) :
			oldTopAlignment.toArray(new Integer[0]));
		reversedAlignment[1] = (form == Form.Actual ? oldTopAlignment.toArray(new Integer[0]) :
			oldBottomAlignment.toArray(new Integer[0]));

		int reversePos = (pm.getAlignmentLength()-1) - pos;

		Integer[][] reversedNewAlignment =
			mutateAlignment(reversedAlignment, reversePos);

		ArrayList<Integer> newTopAlignment = new ArrayList<Integer>();
		for(int i = reversedNewAlignment[0].length-1; i >= 0; i--)
			newTopAlignment.add(
					(form == Form.Actual ? reversedNewAlignment[1][i] : reversedNewAlignment[0][i])
					);

		ArrayList<Integer> newBottomAlignment = new ArrayList<Integer>();
		for(int i = reversedNewAlignment[1].length-1; i >= 0; i--)
			newBottomAlignment.add(
					(form == Form.Actual ? reversedNewAlignment[0][i] : reversedNewAlignment[1][i])
					);

		pm.setTopAlignment(newTopAlignment.toArray(new Integer[0]));
		pm.setBottomAlignment(newBottomAlignment.toArray(new Integer[0]));

		Integer[][] oldAlignment = new Integer[2][];
		oldAlignment[0] = _oldTopAlignment;
		oldAlignment[1] = _oldBottomAlignment;

		Integer[][] newAlignment = new Integer[2][];
		newAlignment[0] = newTopAlignment.toArray(new Integer[0]);
		newAlignment[1] = newBottomAlignment.toArray(new Integer[0]);

		// check to see if we need to move our focus
		if(pos >= pm.getAlignmentLength()) {
			setFocusedPosition(getFocusedPosition()-1);
		} else {
			Phone phoneAfterMove =
					(form == Form.Actual ? pm.getBottomAlignmentElements().get(pos)
						: pm.getTopAlignmentElements().get(pos));

			if(phoneAfterMove == null || phoneAfterMove != phoneToMove) {
				setFocusedPosition(getFocusedPosition()-1);
			} else {
				repaint();
			}
		}

		AlignmentChangeData oldData =
				new AlignmentChangeData(alignmentPos.getObj1(), oldAlignment);
		AlignmentChangeData newData =
				new AlignmentChangeData(alignmentPos.getObj1(), newAlignment);

//		super.firePropertyChange(ALIGNMENT_CHANGE_PROP, oldData, newData);
	}

	public void fireAlignmentChange(AlignmentChangeData oldValue, AlignmentChangeData newValue) {
		super.firePropertyChange(ALIGNMENT_CHANGE_PROP, oldValue, newValue);
	}

	/** Class for alignment change events */
	public static class AlignmentChangeData extends Tuple<Integer, Integer[][]> {

		public AlignmentChangeData(Integer gIdx, Integer[][] alignment) {
			super(gIdx, alignment);
		}
		
		public int getGroupIndex() {
			return super.getObj1();
		}
		
		public void setGroupIndex(int gIdx) {
			super.setObj1(gIdx);
		}
		
		public Integer[][] getAlignment() {
			return super.getObj2();
		}

		public void setAlignment(Integer[][] alignment) {
			super.setObj2(alignment);
		}
		
	}
	
	public static void main(String[] args) {
		String tStr[] = {"kʌɹˈtuːæn", "of", "nomaragan"};
		String aStr[] = {"ʌrˈtuːn", "o", "omaga" };

		Syllabifier syllabifier = Syllabifier.getInstance();
		Aligner aligner = new Aligner();

		PhoneMapDisplay display = new PhoneMapDisplay();

		for(int i = 0; i < tStr.length; i++) {
			String tst = tStr[i];
			String ast = aStr[i];

			List<Phone> tPhones = Phone.toPhoneList(tst);
			syllabifier.syllabify(tPhones);

			List<Phone> aPhones = Phone.toPhoneList(ast);
			syllabifier.syllabify(aPhones);


			PhoneMap pm = aligner.getPhoneAlignment(tPhones, aPhones);
			display.groups.add(pm);
		}

//		List<Phone> tPhones = Phone.toPhoneList(tStr);
//		syllabifier.syllabify(tPhones);
//		List<Phone> aPhones = Phone.toPhoneList(aStr);
//		syllabifier.syllabify(aPhones);

//		Aligner aligner = new Aligner();

//		System.out.println((new Phone("#")).getFeatureSet());

		JFrame f = new JFrame("Test");
//		PhoneMapDisplay display = new PhoneMapDisplay();
//		display.groups.add(pm);
		display.setFont(UserPrefManager.getUITranscriptFont());
		f.add(display);
		f.pack();
		f.setVisible(true);
	}
}
