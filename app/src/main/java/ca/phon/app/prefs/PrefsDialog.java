/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
import java.util.List;

import javax.swing.*;

import ca.phon.plugin.*;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;


/**
 * Dialog for application preferences.
 *
 */
public class PrefsDialog extends JDialog {
	
	private static final long serialVersionUID = 6513355180838862607L;
	
	/**
	 * pref panels
	 */
	private GeneralPrefsPanel generalPrefs;
	private EditorPrefsPanel editorPrefs;
	private MediaPrefsPanel mediaPrefs;
	
	private DialogHeader header;
	
	private JTabbedPane tabbedPane;
	
	private JButton okButton;
	
	/**
	 * 
	 */
	public PrefsDialog() {
		super();
		super.setTitle("Phon : Preferences");
		
		init();
	}
	
	public PrefsDialog(JFrame f) {
		super(f);
		super.setTitle("Phon : Preferences");
		
		init();
	}
	
	private void init() {
		generalPrefs = new GeneralPrefsPanel();
		editorPrefs = new EditorPrefsPanel();
		mediaPrefs = new MediaPrefsPanel();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(generalPrefs.getTitle(), generalPrefs);
		tabbedPane.addTab(editorPrefs.getTitle(), editorPrefs);
		tabbedPane.addTab(mediaPrefs.getTitle(), mediaPrefs);
		
		List<IPluginExtensionPoint<PrefsPanel>> prefExtPts = 
			PluginManager.getInstance().getExtensionPoints(PrefsPanel.class);
		for(IPluginExtensionPoint<PrefsPanel> prefExtPt:prefExtPts) {
			IPluginExtensionFactory<PrefsPanel> prefExtPtFactory = 
				prefExtPt.getFactory();
			
			PrefsPanel panel = prefExtPtFactory.createObject();
			
			tabbedPane.addTab(panel.getTitle(), panel);
		}
		
		header = new DialogHeader("Preferences", "Modify application preferences");
		
		setLayout(new BorderLayout());
		add(header, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
		
		PhonUIAction okAct = new PhonUIAction(this, "onOk");
		okAct.putValue(Action.NAME, "Ok");
		okAct.putValue(Action.SHORT_DESCRIPTION, "Save prefs and close dialog");
		
		okButton = new JButton(okAct);
		
		super.getRootPane().setDefaultButton(okButton);
		
		JComponent buttonBar = ButtonBarBuilder.buildOkBar(okButton);
		
		add(buttonBar, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(okButton);
	}
	
	public void setActiveTab(String tabName) {
		for(int tabIdx = 0; tabIdx < tabbedPane.getTabCount(); tabIdx++) {
			if(tabbedPane.getTitleAt(tabIdx).equals(tabName)) {
				tabbedPane.setSelectedIndex(tabIdx);
			}
		}
	}
	
	public void onOk(PhonActionEvent pae) {
		// save prefs and close dialog
		setVisible(false);
		dispose();
	}
	
	public static void main(String[] args) {
		PrefsDialog pd = new PrefsDialog();
		pd.pack();
		pd.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		pd.setVisible(true);
		
		return;
	}

}
