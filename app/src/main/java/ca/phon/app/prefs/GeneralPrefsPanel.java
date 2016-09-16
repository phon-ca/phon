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
package ca.phon.app.prefs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.ui.HidablePanel;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.PrefHelper;

/**
 * Edit general application prefs such as
 * automatic update.
 * 
 */
public class GeneralPrefsPanel extends PrefsPanel {

	private static final long serialVersionUID = 8761130200130930937L;

	private final String checkForUpdateAtStartupProp = "ca.phon.application.updater.checkOnStartup";
	
	public GeneralPrefsPanel() {
		super("General");
		
		init();
	}

	private void init() {
		CellConstraints cc = new CellConstraints();
		
		// update checking
		boolean doCheckUpdate = PrefHelper.getBoolean(checkForUpdateAtStartupProp, true);
		
		JCheckBox checkForUpdatesBox = new JCheckBox("Check for updates when application starts");
		checkForUpdatesBox.setSelected(doCheckUpdate);
		checkForUpdatesBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox)e.getSource();
				boolean doCheckUpdate = box.isSelected();
				PrefHelper.getUserPreferences().putBoolean(checkForUpdateAtStartupProp, doCheckUpdate);
			}
			
		});
		
		
		JPanel updatesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		FormLayout layout = 
			new FormLayout("fill:pref:grow", "pref, pref");
		JPanel innerPanel = new JPanel(layout);
		updatesPanel.add(checkForUpdatesBox);
		updatesPanel.setBorder(BorderFactory.createTitledBorder("Program Updates"));
		innerPanel.add(updatesPanel, cc.xy(1,1));
		
		// info messages
		PhonUIAction resetInfoMessagesAct = new PhonUIAction(this, "onResetInfoMessages");
		resetInfoMessagesAct.putValue(Action.NAME, "Reset Information Messages");
		JButton resetInfoMessagesBtn = new JButton(resetInfoMessagesAct);
		
		JPanel resetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		resetPanel.add(resetInfoMessagesBtn);
		resetPanel.setBorder(BorderFactory.createTitledBorder("Information Messages"));
		innerPanel.add(resetPanel, cc.xy(1, 2));
		
		// UI theme
		
		
		JScrollPane innerScroller = new JScrollPane(innerPanel);
		setLayout(new BorderLayout());
		add(innerScroller, BorderLayout.CENTER);
	}
	
	/**
	 * Reset hide-able messages.
	 * @param pae
	 */
	public void onResetInfoMessages(PhonActionEvent pae) {
		HidablePanel.clearSavePanelProps();
	}
	
	/**
	 * Set application UI theme.
	 * @param themeClassName
	 */
	public void onSetTheme(String themeClassName) {
		
	}
}
