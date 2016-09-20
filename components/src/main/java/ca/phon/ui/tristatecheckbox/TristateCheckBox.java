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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class TristateCheckBox extends JCheckBox implements Icon {

	private static final long serialVersionUID = 7998392527523539504L;

	/** Do clicks follow three states? */
	private boolean enablePartialCheck = true;
	
	private boolean partialIsSelected = false;
	
	final static Icon defaultIcon = UIManager.getIcon("CheckBox.icon");
	
	private static ImageIcon uncheckedIcon =
			IconManager.getInstance().getIcon("tristatecheckbox/checkbox", IconSize.SMALL);
	
	private static ImageIcon checkedIcon = 
			IconManager.getInstance().getIcon("tristatecheckbox/checkbox-checked", IconSize.SMALL);
	
	private static ImageIcon partiallyCheckedIcon =
			IconManager.getInstance().getIcon("tristatecheckbox/checkbox-partially-checked", IconSize.SMALL);
	
	
	public TristateCheckBox() {
		super();
		setSelectionState(TristateCheckBoxState.UNCHECKED);
		setIcon(this);
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
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Icon icon = defaultIcon;
		switch(getSelectionState()) {
		case CHECKED:
			icon = checkedIcon;
			break;
			
		case UNCHECKED:
			icon = uncheckedIcon;
			break;
			
		case PARTIALLY_CHECKED:
			icon = partiallyCheckedIcon;
			break;
			
		default:
			icon = defaultIcon;
		}
		icon.paintIcon(c, g, x, y);
	}

	@Override
	public int getIconWidth() {
		return IconSize.SMALL.getWidth();
	}

	@Override
	public int getIconHeight() {
		return IconSize.SMALL.getHeight();
	}
}
