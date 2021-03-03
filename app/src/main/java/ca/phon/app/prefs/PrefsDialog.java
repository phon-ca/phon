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
package ca.phon.app.prefs;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.List;


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
	private QueryPrefsPanel queryPrefs;
	
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
		queryPrefs = new QueryPrefsPanel();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(generalPrefs.getTitle(), generalPrefs);
		tabbedPane.addTab(editorPrefs.getTitle(), editorPrefs);
		tabbedPane.addTab(mediaPrefs.getTitle(), mediaPrefs);
		tabbedPane.addTab(queryPrefs.getTitle(), queryPrefs);
		
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
