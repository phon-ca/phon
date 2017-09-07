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
package ca.phon.ui.tristatecheckbox;

import java.awt.*;

import javax.swing.*;

import ca.phon.util.OSInfo;

public class TristateCheckBox extends JCheckBox {

	private static final long serialVersionUID = 7998392527523539504L;

	/** Do clicks follow three states? */
	private boolean enablePartialCheck = true;
	
	private boolean partialIsSelected = false;
	
	public TristateCheckBox() {
		super();
		setSelectionState(TristateCheckBoxState.UNCHECKED);
		addActionListener( (e) -> {
			final TristateCheckBoxState currentState = getSelectionState();
			switch(currentState) {
			case CHECKED:
				setSelectionState(TristateCheckBoxState.UNCHECKED);
				break;
				
			case PARTIALLY_CHECKED:
				setSelectionState(TristateCheckBoxState.CHECKED);
				break;
			
			case UNCHECKED:
				setSelectionState( (isEnablePartialCheck() ? TristateCheckBoxState.PARTIALLY_CHECKED : TristateCheckBoxState.CHECKED));
				break;
			}
		});
	}
	
	@Override
	public boolean isSelected() {
		return (getSelectionState() == TristateCheckBoxState.CHECKED || 
				(isPartialSelected() && getSelectionState() == TristateCheckBoxState.PARTIALLY_CHECKED));
	}
	
	@Override
	public void setSelected(boolean selected) {
		setSelectionState(TristateCheckBoxState.CHECKED);
	}
	
	public boolean isEnablePartialCheck() {
		return this.enablePartialCheck;
	}
	
	public void setEnablePartialCheck(boolean partialSelection) {
		this.enablePartialCheck = partialSelection;
	}
	
	public void setPartiallySelected(boolean partialSelection) {
		if(partialSelection) setSelectionState(TristateCheckBoxState.PARTIALLY_CHECKED);
		else {
			if(super.isSelected()) {
				setSelectionState(TristateCheckBoxState.CHECKED);
			} else {
				setSelectionState(TristateCheckBoxState.UNCHECKED);
			}
		}
	}
	
	public TristateCheckBoxState getSelectionState() {
		return (getClientProperty("CustomSelectionState") != null ? (TristateCheckBoxState)getClientProperty("CustomSelectionState")
				: (super.isSelected() ? TristateCheckBoxState.CHECKED : TristateCheckBoxState.UNCHECKED));
	}
	
	public boolean isPartialSelected() {
		return this.partialIsSelected;
	}
	
	public void setPartialSelected(boolean partialSelected) {
		this.partialIsSelected = partialSelected;
	}
	
	public void setSelectionState(TristateCheckBoxState state) {
		switch(state) {
		case UNCHECKED:
			super.setSelected(false);
			break;
			
		case PARTIALLY_CHECKED:
			super.setSelected(false);
			break;
			
		case CHECKED:
			super.setSelected(true);
			break;
		}
		putClientProperty("CustomSelectionState", state);
	}
	
	public static void main(String[] args) throws Exception {
		final JFrame testFrame = new JFrame("test");
		
		TristateCheckBox ch1 = new TristateCheckBox();
		ch1.setSelectionState(TristateCheckBoxState.UNCHECKED);
		ch1.setEnablePartialCheck(false);
		ch1.setText("Unselected");
		
		TristateCheckBox ch2 = new TristateCheckBox();
		ch2.setSelectionState(TristateCheckBoxState.PARTIALLY_CHECKED);
		ch2.setEnablePartialCheck(false);
		ch2.setText("Partially Selected");
		
		TristateCheckBox ch3 = new TristateCheckBox();
		ch3.setSelectionState(TristateCheckBoxState.CHECKED);
		ch3.setText("Checked");
		
		final JPanel panel = new JPanel(new FlowLayout());
		panel.add(ch1);
		panel.add(ch2);
		panel.add(ch3);
		
		testFrame.add(panel);
		testFrame.pack();
		testFrame.setVisible(true);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		
		if(getSelectionState() == TristateCheckBoxState.PARTIALLY_CHECKED) {
			if(OSInfo.isMacOs()) {
				int rectSize = 
						getHeight() - (getInsets().top + getMargin().top);
				final Rectangle macOSRect = 
						new Rectangle(getInsets().left+getMargin().left+2, getInsets().top+getMargin().top, 14, 14);
				final int macOSRectArc = 4;
				g.setColor(Color.yellow);
				g.fillRoundRect(macOSRect.x, macOSRect.y, macOSRect.width, macOSRect.height,
						macOSRectArc, macOSRectArc);
				g.setColor(Color.lightGray);
				g.drawRoundRect(macOSRect.x, macOSRect.y, macOSRect.width, macOSRect.height,
						macOSRectArc, macOSRectArc);
			} else {
				// TODO windows
			}
		}
	}
	
}
