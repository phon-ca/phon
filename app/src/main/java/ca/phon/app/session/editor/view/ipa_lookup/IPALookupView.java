package ca.phon.app.session.editor.view.ipa_lookup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.ipa_lookup.actions.AutoTranscribeCommand;
import ca.phon.app.session.editor.view.ipa_lookup.actions.ExportIPACommand;
import ca.phon.app.session.editor.view.ipa_lookup.actions.ImportIPACommand;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.ipadictionary.ui.IPALookupContext;
import ca.phon.ipadictionary.ui.IPALookupContextListener;
import ca.phon.ipadictionary.ui.IPALookupPanel;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.util.Language;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class IPALookupView extends EditorView {

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
		final int langIdx = Arrays.binarySearch(langArray, defLang);
		langBox = new JComboBox(langArray);
		langBox.setRenderer(new LanguageCellRenderer());
		langBox.setSelectedIndex(langIdx);
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
			}
			
			@Override
			public void dictionaryRemoved(String dictName) {
				updateLangBox();
			}
			
			@Override
			public void dictionaryChanged(String newDictionary) {
//				updateLangBox();
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
		return new IPALookupViewMenu(this);
	}
	
	private class LanguageComparator implements Comparator<Language> {

		@Override
		public int compare(Language o1, Language o2) {
			return o1.toString().compareTo(o2.toString());
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
