package ca.phon.app.session.editor.util;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.ipadictionary.ui.IPALookupContext;
import ca.phon.ipadictionary.ui.IPALookupPanel;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.Language;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class IPALookupView extends EditorView {

	private final static long serialVersionUID = 2932635326993882782L;

	public final static String VIEW_NAME = "IPA Lookup";
	
	public final static String VIEW_ICON = "apps/preferences-desktop-font";
	
	/*
	 * Lookup context
	 */
	private IPALookupContext lookupContext;
	
	/* 
	 * UI
	 */
	private JTabbedPane tabPane;
	
	private JToolBar toolbar;
	
	private JMenu menu;
	
	private JComboBox langBox;
	
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
		
		lookupContext = new IPALookupContext();
		init();
		setupEditorActions();
	}
	
	private void init() {
		setupToolbar();
		setupMenu();
		
		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		
		tabPane = new JTabbedPane();
		setupTierTab();
		setupConsoleTab();
		
		add(tabPane, BorderLayout.CENTER);
	}
	
	private void setupEditorActions() {
		final SessionEditor editor = getEditor();
		
		final DelegateEditorAction recordChangeAct = new DelegateEditorAction(this, "onRecordChanged");
		editor.getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, recordChangeAct);
		
		final DelegateEditorAction tierChangeAct = new DelegateEditorAction(this, "onTierChanged");
		editor.getEventManager().registerActionForEvent(EditorEventType.TIER_CHANGE_EVT, tierChangeAct);
	}
	
	private void setupToolbar() {
		toolbar = new JToolBar();

		Set<Language> langs = IPADictionaryLibrary.getInstance().availableLanguages();
		langBox = new JComboBox(langs.toArray(new Language[0]));
		langBox.setSelectedItem(IPADictionaryLibrary.getInstance().getDefaultLanguage());
		langBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				onLanguageSwitch();
			}
			
		});

		autoTranscribeBtn = new JButton();
		PhonUIAction autoTranscribeAct =
				new PhonUIAction(this, "onAutoTranscribe");
		autoTranscribeAct.putValue(Action.NAME, "Auto Transcribe Session");
		autoTranscribeBtn.setAction(autoTranscribeAct);
		
		importIPABtn = new JButton();
		PhonUIAction importIPAAct = 
			new PhonUIAction(this, "onImportIPA");
		importIPAAct.putValue(Action.NAME, "Import IPA");
		importIPAAct.putValue(Action.SHORT_DESCRIPTION, "Import custom IPA transcriptions...");
		importIPABtn.setAction(importIPAAct);
		
		exportIPABtn = new JButton();
		PhonUIAction exportIPAAct =
			new PhonUIAction(this, "onExportIPA");
		exportIPAAct.putValue(Action.NAME, "Export IPA");
		exportIPAAct.putValue(Action.SHORT_DESCRIPTION, "Export custom IPA transcriptions...");
		exportIPABtn.setAction(exportIPAAct);

		toolbar.add(new JLabel("IPA Dictionary:"));
		toolbar.add(langBox);
		toolbar.add(importIPAAct);
		toolbar.add(exportIPAAct);
		toolbar.addSeparator();
		toolbar.add(autoTranscribeBtn);
		toolbar.setFloatable(false);
	}
	
	private void setupMenu() {
		menu = new JMenu();
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
		tabPane.addTab("Console", lookupPanel);
	}
	
	/*
	 * UI actions
	 */
	public void onImportIPA() {
		
	}
	
	public void onExportIPA() {
		
	}
	
	public void onLanguageSwitch() {
		final Language lang = (Language)langBox.getSelectedItem();
		lookupContext.switchDictionary(lang.toString());
		recordLookupPanel.setDictionary(lookupContext.getDictionary());
	}
	
	public void onAutoTranscribe() {
		
	}
	
	/*
	 * Editor actions
	 */
	@RunOnEDT
	public void onRecordChanged(EditorEvent ee) {
		final Record r = getEditor().currentRecord();
		recordLookupPanel.setRecord(r);
	}
	
	@RunOnEDT
	public void onTierChanged(EditorEvent ee) {
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
		return menu;
	}

}
