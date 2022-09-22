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

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.EditorViewAdapter;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.ipa_lookup.actions.*;
import ca.phon.ipadictionary.*;
import ca.phon.ipadictionary.ui.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.util.Language;
import ca.phon.util.icons.*;
import org.apache.commons.logging.Log;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class IPALookupView extends EditorView {
	
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
	
	private JComboBox<IPADictionary> dictBox;
	
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
		
		editor.getEventManager().registerActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		editor.getEventManager().registerActionForEvent(EditorEventType.TierChanged, this::onTierChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
	}
	
	private void setupToolbar() {
		toolbar = new JToolBar();

		dictBox = new JComboBox<>();
		dictBox.setRenderer(new IPADictionaryCellRenderer());
		dictBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) 
					onLanguageSwitch();
			}
			
		});
		updateLangBox();

		autoTranscribeBtn = new JButton(new AutoTranscribeCommand(this));
		
		importIPABtn = new JButton(new ImportIPACommand(this));
		
		exportIPABtn = new JButton(new ExportIPACommand(this));

		toolbar.add(new JLabel("IPA Dictionary:"));
		toolbar.add(dictBox);
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
					LogUtil.warning(e);
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
				Iterator<IPADictionary> dictItr = IPADictionaryLibrary.getInstance().availableDictionaries();
				List<IPADictionary> availableDicts = new ArrayList<>();
				while(dictItr.hasNext()) availableDicts.add(dictItr.next());
				Collections.sort(availableDicts, Comparator.comparing(IPADictionary::getName));
				dictBox.setModel(new DefaultComboBoxModel<>(availableDicts.toArray(new IPADictionary[0])));

				final Language defLang = IPADictionaryLibrary.getInstance().getDefaultLanguage();
				dictBox.setSelectedIndex(availableDicts.stream().map(IPADictionary::getLanguage)
						.collect(Collectors.toList()).indexOf(lookupContext.getDictionary().getLanguage()));
				isUpdatingBox = false;
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEdt.run();
		else
			SwingUtilities.invokeLater(onEdt);
	}

	public Language getSelectedDictionaryLanguage() {
		return ((IPADictionary) this.dictBox.getSelectedItem()).getLanguage();
	}
	
	public void onLanguageSwitch() {
		if(isUpdatingBox) return;
		final IPADictionary dict = (IPADictionary) dictBox.getSelectedItem();
		if(dict == null) return;
		lookupContext.switchDictionary(dict.getLanguage().toString());
		recordLookupPanel.setDictionary(lookupContext.getDictionary());
	}
	
	public IPALookupContext getLookupContext() {
		return this.lookupContext;
	}
	
	/*
	 * Editor actions
	 */
	private void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> ee) {
		if(!getEditor().getViewModel().isShowingInStack(VIEW_NAME)) return;
		
		final Record r = ee.data().record();
		recordLookupPanel.setRecord(r);
	}
	
	private void onTierChanged(EditorEvent<EditorEventType.TierChangeData> ee) {
		if(!getEditor().getViewModel().isShowingInStack(VIEW_NAME)) return;
		
		if(SystemTierType.Orthography.getName().equals(ee.data().tier().getName())) {
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

}
