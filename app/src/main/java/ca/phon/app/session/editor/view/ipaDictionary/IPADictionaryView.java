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
package ca.phon.app.session.editor.view.ipaDictionary;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.autotranscribe.AutoTranscribeAction;
import ca.phon.app.session.editor.view.ipaDictionary.actions.*;
import ca.phon.ipadictionary.*;
import ca.phon.ipadictionary.ui.*;
import ca.phon.util.Language;
import ca.phon.util.icons.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class IPADictionaryView extends EditorView {
	
	public final static String VIEW_NAME = "IPA Dictionary";
	
	public final static String VIEW_ICON = IconManager.GoogleMaterialDesignIconsFontName + ":dictionary";
	
	/*
	 * Lookup context
	 */
	private IPALookupContext lookupContext;
	
	/* 
	 * UI
	 */
	private JToolBar toolbar;
	
	private JComboBox<IPADictionary> dictBox;
	
	private JButton autoTranscribeBtn;

	private JButton importIPABtn;

	private JButton exportIPABtn;
	
	private IPALookupPanel lookupPanel;
	
	/**
	 * constructor
	 */
	public IPADictionaryView(SessionEditor editor) {
		super(editor);

		lookupContext = new IPALookupViewContext();
		init();
	}
	
	private void init() {
		setupToolbar();
		
		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		
		setupConsoleTab();
		
		onLanguageSwitch();
		
		add(lookupPanel, BorderLayout.CENTER);
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

		autoTranscribeBtn = new JButton(new AutoTranscribeAction(getEditor().getProject(), getEditor().getSession(), getEditor().getEventManager(), getEditor().getUndoSupport(), getEditor().getDataModel().getTranscriber()));
		
		importIPABtn = new JButton(new ImportIPACommand(this));
		
		exportIPABtn = new JButton(new ExportIPACommand(this));

		toolbar.add(new JLabel("IPA Dictionary:"));
		toolbar.add(dictBox);
		toolbar.add(importIPABtn);
		toolbar.add(exportIPABtn);
		toolbar.addSeparator();
		//toolbar.add(autoTranscribeBtn);
		toolbar.setFloatable(false);
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
	}
	
	public IPALookupContext getLookupContext() {
		return this.lookupContext;
	}
	
	/*
	 * Editor actions
	 */
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		final String[] iconParts = VIEW_ICON.split(":");
		return IconManager.getInstance().getFontIcon(iconParts[0], iconParts[1], IconSize.MEDIUM, Color.darkGray);
	}

	@Override
	public JMenu getMenu() {
		return new IPADictionaryViewMenu(this);
	}

}
