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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class TristateCheckBox extends JCheckBox {

	private static final long serialVersionUID = 7998392527523539504L;
	
	public static final String CUSTOM_STATE_PROP = "CustomSelectionState";
	
	/* Icons */
	private final static String CHECKBOX_ICON_NAME = "tristatecheckbox/checkbox-unchecked";
	private Icon checkboxIcon;
		
	private final static String PARTIAL_CHECKBOX_ICON_NAME = "tristatecheckbox/checkbox-partialcheck";
	private Icon partiallyCheckedIcon;
	
	private final static String CHECKED_ICON_NAME = "tristatecheckbox/checkbox-checked";
	private Icon checkedIcon;

	/** Do clicks follow three states? */
	private boolean enablePartialCheck = true;
	
	public TristateCheckBox() {
		super();
		loadIcons();
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
	
	private void loadIcons() {
		checkboxIcon = IconManager.getInstance().getIcon(CHECKBOX_ICON_NAME, IconSize.SMALL);
		
		partiallyCheckedIcon = IconManager.getInstance().getIcon(PARTIAL_CHECKBOX_ICON_NAME, IconSize.SMALL);
		
		checkedIcon = IconManager.getInstance().getIcon(CHECKED_ICON_NAME, IconSize.SMALL);
	}
	
	@Override
	public boolean isSelected() {
		return (getSelectionState() == TristateCheckBoxState.CHECKED);
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
		return (getClientProperty(CUSTOM_STATE_PROP) != null ? (TristateCheckBoxState)getClientProperty(CUSTOM_STATE_PROP)
				: (super.isSelected() ? TristateCheckBoxState.CHECKED : TristateCheckBoxState.UNCHECKED));
	}
	
	public void setSelectionState(TristateCheckBoxState state) {
		switch(state) {
		case UNCHECKED:
			setIcon(checkboxIcon);
			super.setSelected(false);
			break;
			
		case PARTIALLY_CHECKED:
			setIcon(partiallyCheckedIcon);
			super.setSelected(false);
			break;
			
		case CHECKED:
			setIcon(checkedIcon);
			super.setSelected(true);
			break;
		}
		putClientProperty(CUSTOM_STATE_PROP, state);
	}
	
	public static void main(String[] args) throws Exception {
		final JFrame testFrame = new JFrame("test");
		testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
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
	
}
