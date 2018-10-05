/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ui.tristatecheckbox;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
