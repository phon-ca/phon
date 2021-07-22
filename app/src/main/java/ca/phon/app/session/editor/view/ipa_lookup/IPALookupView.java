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
package ca.phon.app.session.editor.view.ipa_lookup;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import ca.phon.app.session.*;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.ipa_lookup.actions.*;
import ca.phon.ipadictionary.*;
import ca.phon.ipadictionary.ui.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.util.*;
import ca.phon.util.icons.*;

public class IPALookupView extends EditorView {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(IPALookupView.class.getName());

	private final static long serialVersionUID = 2932635326993882782L;

	public final static String VIEW_NAME = "IPA Lookup";
	
	public final static String VIEW_ICON = "misc/ipa-dict";
	
	/*
	 * Lookup context
	 */
	private IPALookupContext lookupContext;
	
	/* 
	 * UI
	 */
	private JTabbedPane tabPane;
	
	private JToolBar toolbar;
	
	private JComboBox<Language> langBox;
	
	private JButton autoTranscribeBtn;
	
	private JButton importIPABtn;
	
	private JButton exportIPABtn;
	
	private RecordLookupPanel recordLookupPanel;
	
	private IPALookupPanel lookupPanel;
	
	/**
	 * constructor
	 */
	public IPALookupView(SessionEditor editor) {
		super(editor);
		addEditorViewListener(editorViewListener);
		
		lookupContext = new IPALookupViewContext();
		init();
		setupEditorActions();
	}
	
	private void init() {
		setupToolbar();
		
		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		
		tabPane = new JTabbedPane();
		setupTierTab();
		setupConsoleTab();
		
		onLanguageSwitch();
		
		add(tabPane, BorderLayout.CENTER);
	}
	
	private void setupEditorActions() {
		final SessionEditor editor = getEditor();
		
		final DelegateEditorAction recordChangeAct = new DelegateEditorAction(this, "onRecordChanged");
		editor.getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, recordChangeAct);
		
		final DelegateEditorAction tierChangeAct = new DelegateEditorAction(this, "onTierChanged");
		editor.getEventManager().registerActionForEvent(EditorEventType.TIER_CHANGED_EVT, tierChangeAct);
	}
	
	private void setupToolbar() {
		toolbar = new JToolBar();

		Set<Language> langs = IPADictionaryLibrary.getInstance().availableLanguages();
		Language langArray[] = langs.toArray(new Language[0]);
		Arrays.sort(langArray, new LanguageComparator());
		final Language defLang = IPADictionaryLibrary.getInstance().getDefaultLanguage();
		langBox = new JComboBox<>(langArray);
		langBox.setRenderer(new LanguageCellRenderer());
		langBox.setSelectedItem(defLang);
		langBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) 
					onLanguageSwitch();
			}
			
		});

		autoTranscribeBtn = new JButton(new AutoTranscribeCommand(this));
		
		importIPABtn = new JButton(new ImportIPACommand(this));
		
		exportIPABtn = new JButton(new ExportIPACommand(this));

		toolbar.add(new JLabel("IPA Dictionary:"));
		toolbar.add(langBox);
		toolbar.add(importIPABtn);
		toolbar.add(exportIPABtn);
		toolbar.addSeparator();
		toolbar.add(autoTranscribeBtn);
		toolbar.setFloatable(false);
	}
	
	private void setupTierTab() {
		recordLookupPanel = new RecordLookupPanel(getEditor());
		recordLookupPanel.setRecord(getEditor().currentRecord());
//		final JScrollPane scroller = new JScrollPane(recordLookupPanel);
//		scroller.setAutoscrolls(false);
		tabPane.addTab("Record Lookup", recordLookupPanel);
	}
	
	private void setupConsoleTab() {
		lookupPanel = new IPALookupPanel(lookupContext);
		lookupPanel.getLookupContext().addLookupContextListener(new IPALookupContextListener() {
			
			@Override
			public void handleMessage(String msg) {
			}
			
			@Override
			public void errorOccured(String err) {
				final Document doc = lookupPanel.getConsole().getDocument();
				try {
					doc.insertString(doc.getLength(), "S " + err, null);
				} catch (BadLocationException e) {
					LOGGER.warn( e.getLocalizedMessage(), e);
				}
			}
			
			@Override
			public void dictionaryRemoved(String dictName) {
				updateLangBox();
			}
			
			@Override
			public void dictionaryChanged(String newDictionary) {
				updateLangBox();
			}
			
			@Override
			public void dictionaryAdded(String newDictionary) {
				updateLangBox();
			}
		});
		tabPane.addTab("Console", lookupPanel);
	}
	
	private volatile boolean isUpdatingBox = false;
	public void updateLangBox() {
		final Runnable onEdt = new Runnable() {
			
			@Override
			public void run() {
				isUpdatingBox = true;
				Set<Language> langs = IPADictionaryLibrary.getInstance().availableLanguages();
				Language langArray[] = langs.toArray(new Language[0]);
				Arrays.sort(langArray, new LanguageComparator());
				final Language defLang = IPADictionaryLibrary.getInstance().getDefaultLanguage();
				final int langIdx = Arrays.binarySearch(langArray, defLang);
				langBox.setModel(new DefaultComboBoxModel(langArray));
				langBox.setSelectedItem(lookupContext.getDictionary().getLanguage());
				isUpdatingBox = false;
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEdt.run();
		else
			SwingUtilities.invokeLater(onEdt);
	}

	public Language getSelectedDictionaryLanguage() {
		return (Language) this.langBox.getSelectedItem();
	}
	
	public void onLanguageSwitch() {
		if(isUpdatingBox) return;
		final Language lang = (Language)langBox.getSelectedItem();
		if(lang == null) return;
		lookupContext.switchDictionary(lang.toString());
		recordLookupPanel.setDictionary(lookupContext.getDictionary());
	}
	
	public IPALookupContext getLookupContext() {
		return this.lookupContext;
	}
	
	/*
	 * Editor actions
	 */
	@RunOnEDT
	public void onRecordChanged(EditorEvent ee) {
		if(!getEditor().getViewModel().isShowingInStack(VIEW_NAME)) return;
		
		final Record r = getEditor().currentRecord();
		recordLookupPanel.setRecord(r);
	}
	
	@RunOnEDT
	public void onTierChanged(EditorEvent ee) {
		if(!getEditor().getViewModel().isShowingInStack(VIEW_NAME)) return;
		
		if(ee.getEventData() != null && 
				SystemTierType.Orthography.getName().equals(ee.getEventData())) {
			recordLookupPanel.update();
		}
	}
	
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon(VIEW_ICON, IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		return new IPALookupViewMenu(this);
	}
	
	private final EditorViewAdapter editorViewListener = new EditorViewAdapter() {

		@Override
		public void onFocused(EditorView view) {
			final Record r = getEditor().currentRecord();
			if(r != recordLookupPanel.getRecord()) 
				recordLookupPanel.setRecord(r);
		}
		
	};
	
	private class LanguageComparator implements Comparator<Language> {

		@Override
		public int compare(Language o1, Language o2) {
			String l1 = o1.getPrimaryLanguage().getName() + " (" + o1.toString() + ")";
			String l2 = o2.getPrimaryLanguage().getName() + " (" + o2.toString() + ")";
			return l1.compareTo(l2);
		}
		
	}

	private class LanguageCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -5753923740573333306L;

		@Override
		public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
		
			if(value != null) {
				final Language lang = (Language)value;
				final String text = lang.getPrimaryLanguage().getName() + " (" + lang.toString() + ")";
				retVal.setText(text);
			}
			
			return retVal;
		}
	
	}
}
