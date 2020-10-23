/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.jgoodies.forms.layout.*;

import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.layout.*;
import ca.phon.util.*;

/**
 * A dialog for choosing sort options for records
 * in a session.  This dialog displays 3 combo boxes
 * which hold the list of tiers in a session and an 
 * option for choosing 'decending' for each tier selected.
 * 
 * When the 'ok' button is clicked the dialog will first 
 * sort the records and then close.
 */
public class RecordSortDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;

	/** tier Combo boxes */
	private JComboBox comboBoxes[] = new JComboBox[3];
	
	/** Check boxes */
	private JCheckBox checkBoxes[] = new JCheckBox[3];
	
	private DialogHeader header;
	
	private JButton okButton;
	
	private JButton cancelButton;
	
	/** Session */
	private Session transcript;
	
	private boolean wasCanceled = true;
	
	/**
	 * Constructor
	 */
	public RecordSortDialog(Session session) {
		super();
		super.setTitle("Sort Records");
		
		this.transcript = session;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("Sort Records", "Sort records by one or more tiers.");
		
		createComboBoxes();
		createCheckBoxes();
		
		FormLayout layout = new FormLayout(
				"3dlu, pref, fill:pref:grow, pref, 3dlu",
				"pref, pref, pref, pref, 3dlu");
		JPanel formPanel = new JPanel(layout);
		CellConstraints cc = new CellConstraints();
		
		for(int i = 0; i < 3; i++) {
			String lblText = 
				(i == 0 ? "Sort by:" : "then by:");
			formPanel.add(new JLabel(lblText), cc.xy(2,i+1));
			formPanel.add(comboBoxes[i], cc.xy(3,i+1));
			formPanel.add(checkBoxes[i], cc.xy(4,i+1));
		}
		
		PhonUIAction okAct = new PhonUIAction(this, "onOk");
		okAct.putValue(Action.NAME, "Ok");
		okAct.putValue(Action.SHORT_DESCRIPTION, "Sort records and close");
		okButton = new JButton(okAct);
		
		PhonUIAction cancelAct = new PhonUIAction(this, "onCancel");
		cancelAct.putValue(Action.NAME, "Cancel");
		cancelAct.putValue(Action.SHORT_DESCRIPTION, "Close dialog");
		cancelButton = new JButton(cancelAct);
		
		// bind cancel action to ESC key
		KeyStroke escKs = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getRootPane().getActionMap();
		
		actionMap.put("_CANCEL_DIALOG_", cancelAct);
		inputMap.put(escKs, "_CANCEL_DIALOG_");
		
		getRootPane().setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, inputMap);
		getRootPane().setActionMap(actionMap);
		
		JComponent btnBar = 
			ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton);
		formPanel.add(btnBar, cc.xyw(2,4,3));
		
		add(header, BorderLayout.NORTH);
		add(formPanel, BorderLayout.CENTER);
		
		super.getRootPane().setDefaultButton(okButton);
		// setup default button
//		add(btnBar, BorderLayout.SOUTH);
	}
	
	private void createComboBoxes() {
		// get the list of tiers in order
		List<TierViewItem> tierOrder = transcript.getTierView();
		List<String> comboOpts = new ArrayList<String>();
		comboOpts.add(""); // default option
		comboOpts.add("Speaker");
		for(TierViewItem toi:tierOrder)
			comboOpts.add(toi.getTierName());
		
		for(int i = 0; i < 3; i ++) {
			comboBoxes[i] = new JComboBox(comboOpts.toArray(new String[0]));
			if(i == 0)
				comboBoxes[i].setSelectedItem(SystemTierType.Segment.getName());
			
			PhonUIAction onChangeAct = new PhonUIAction(this, "onComboBoxChange", i);
			comboBoxes[i].setAction(onChangeAct);
		}
		comboBoxes[2].setEnabled(false);
	}
	
	private void createCheckBoxes() {
		for(int i = 0; i < 3; i++) {
			checkBoxes[i] = new JCheckBox("descending");
			if(i > 0) checkBoxes[i].setEnabled(false);
		}
	}
	
	public void onComboBoxChange(PhonActionEvent pae) {
		Integer idx = (Integer)pae.getData();
		
		if(idx < 3 && idx >= 0 ) {
			String selectedItem = comboBoxes[idx].getSelectedItem().toString();
			
			checkBoxes[idx].setEnabled(selectedItem.length() > 0);
			
			for(int i = idx+1; i < 3; i++) {
				if(selectedItem.length() == 0) {
					comboBoxes[i].setEnabled(false);
					checkBoxes[i].setEnabled(false);
				}
			}
			
			
			if(selectedItem.length() > 0 && idx+1 < 3) {
				comboBoxes[idx+1].setEnabled(true);
				checkBoxes[idx+1].setEnabled(comboBoxes[idx+1].getSelectedIndex() > 0);
			}
		}
		
	}
	
	public void onOk(PhonActionEvent pae) {
		// sort records
		wasCanceled = false;
		onCancel(pae);
	}
	
	public Comparator<Record> getComparator() {
		return new RecordComparator();
	}
	
	public void onCancel(PhonActionEvent pae) {
		super.setVisible(false);
		super.dispose();
	}
	
	public boolean wasCanceled() {
		return this.wasCanceled;
	}
	
	private class RecordComparator implements Comparator<Record> {

		@Override
		public int compare(Record arg0, Record arg1) {
		
			String tier1 = comboBoxes[0].getSelectedItem().toString();
			String tier2 = comboBoxes[1].getSelectedItem().toString();
			String tier3 = comboBoxes[2].getSelectedItem().toString();
			
			int retVal = 0;
			if(tier1.length() > 0) {
				String u1data = getTierData(arg0, tier1);
				String u2data = getTierData(arg1, tier1);
				
				retVal = compareTierData(u1data, u2data, checkBoxes[0].isSelected());
				
				
				if(retVal == 0 && tier2.length() > 0) {
					u1data = getTierData(arg0, tier2);
					u2data = getTierData(arg1, tier2);
					
					retVal = compareTierData(u1data, u2data, checkBoxes[1].isSelected());
					
					if(retVal == 0 && tier3.length() > 0) {
						u1data = getTierData(arg0, tier3);
						u2data = getTierData(arg1, tier3);
						
						retVal = compareTierData(u1data, u2data, checkBoxes[2].isSelected());
					}
				}
			}
			return retVal;
		}
		
		private String getTierData(Record utt, String tier) {
			String retVal = "";
			
			if(tier.equalsIgnoreCase("Speaker")) {
				final Participant speaker = utt.getSpeaker();
				if(speaker != null) {
					retVal = 
							(speaker.getName() != null ? speaker.getName() : speaker.getId());
				}
			} else {
				retVal = utt.getTier(tier).toString();
			}
			
			return retVal;
		}
		
		private int compareTierData(String tierData1, String tierData2, boolean decending) {
			int retVal = 0;
			
			Collator collator = CollatorFactory.defaultCollator();
			if(!decending)
				retVal = collator.compare(tierData1, tierData2);
			else
				retVal = collator.compare(tierData2, tierData1);
			
			return retVal;
		}
		
	}
}
