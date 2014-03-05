package ca.phon.app.session.editor.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.tier.IPAGroupField;
import ca.phon.app.session.editor.tier.TierEditorListener;
import ca.phon.app.session.editor.tier.layout.TierDataConstraint;
import ca.phon.app.session.editor.tier.layout.TierDataLayout;
import ca.phon.app.session.editor.tier.layout.TierDataLayoutPanel;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoWord;
import ca.phon.orthography.OrthoWordnet;
import ca.phon.orthography.Orthography;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.visitor.Visitor;

/**
 * Provides a UI for selecting IPA transcriptions from 
 * dictionaries for the current record.
 *
 */
public class RecordLookupPanel extends JPanel {
	
	final static Logger LOGGER = Logger
			.getLogger(RecordLookupPanel.class.getName());
	
	private static final long serialVersionUID = 1358496790602657797L;

	private final AtomicReference<Record> recordRef = new AtomicReference<Record>();
	
	final Tier<IPATranscript> lookupTier;
	
	private AtomicReference<IPADictionary> dictRef = new AtomicReference<IPADictionary>();
	
	private final WeakReference<SessionEditor> editorRef;
	
	/*
	 * UI components
	 */
	private JCheckBox overwriteBox;
	private JCheckBox ipaTargetBox;
	private JCheckBox ipaActualBox;
	
	private JButton setButton;
	
	private TierDataLayoutPanel candidatePanel;
	private TierDataLayoutPanel groupPanel;
	
	private JPanel controlPanel;
	
	RecordLookupPanel(SessionEditor editor) {
		super();
		
		final SessionFactory factory = SessionFactory.newFactory();
		lookupTier = factory.createTier("IPA Lookup", IPATranscript.class, true);
		
		editorRef = new WeakReference<SessionEditor>(editor);
		
		init();
	}
	
	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	public Record getRecord() {
		return recordRef.get();
	}
	
	public void setRecord(Record record) {
		recordRef.getAndSet(record);
		update();
	}
	
	public IPADictionary getDictionary() {
		return this.dictRef.get();
	}
	
	public void setDictionary(IPADictionary lookupContext) {
		dictRef.getAndSet(lookupContext);
		update();
	}
	
	private void init() {
		setBackground(Color.white);
		
		setLayout(new BorderLayout());
		
		candidatePanel = new TierDataLayoutPanel();
		groupPanel = new TierDataLayoutPanel();
		final JScrollPane scroller = new JScrollPane(groupPanel);
		
		overwriteBox = new JCheckBox("Overwrite");
		overwriteBox.setSelected(true);
		overwriteBox.setOpaque(false);
		
		ipaTargetBox = new JCheckBox(SystemTierType.IPATarget.getName());
		ipaTargetBox.setOpaque(false);
		ipaTargetBox.setSelected(false);
		
		ipaActualBox = new JCheckBox(SystemTierType.IPAActual.getName());
		ipaActualBox.setOpaque(false);
		ipaActualBox.setSelected(true);
		
		final PhonUIAction setAct = new PhonUIAction(this, "onSetTranscription");
		setAct.putValue(PhonUIAction.NAME, "Set");
		setAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set transcription for selected tiers");
		setButton = new JButton(setAct);
		
		controlPanel = new JPanel(new HorizontalLayout(((TierDataLayout)groupPanel.getLayout()).getHorizontalGap()));
		controlPanel.setOpaque(false);
		controlPanel.add(overwriteBox);
		controlPanel.add(ipaTargetBox);
		controlPanel.add(ipaActualBox);
		
		add(candidatePanel, BorderLayout.NORTH);
		add(scroller, BorderLayout.CENTER);
	}
	
	void update() {
		// reset our tier
		updateLookupTier();
		updatePanel();
	}
	
	void updateLookupTier() {
		lookupTier.removeAll();
		
		final Record r = getRecord();
		if(r == null) return;
		
		for(int i = 0; i < r.numberOfGroups(); i++) {
			final Group g = r.getGroup(i);
			final Orthography ortho = g.getOrthography();
			lookupTier.addGroup(new IPATranscript());
			final WordLookupVisitor visitor = new WordLookupVisitor(this);
			ortho.accept(visitor);
		}
	}
	
	private void updatePanel() {
		candidatePanel.removeAll();
		groupPanel.removeAll();
		
		final Record r = getRecord();
		if(r == null) return;
		
		int row = 0;
		int col = 0;
		candidatePanel.add(controlPanel, new TierDataConstraint(TierDataConstraint.FLAT_TIER_PREF_COLUMN, row));
		
		++row;
		final JLabel transLbl = new JLabel("Transcription");
		transLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		candidatePanel.add(transLbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row));
		for(int i = 0; i < lookupTier.numberOfGroups(); i++) {
			final IPAGroupField ipaField = new IPAGroupField(lookupTier, i);
			ipaField.addTierEditorListener(tierListener);
			candidatePanel.add(ipaField, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+i, row));
		}
		candidatePanel.add(setButton, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+lookupTier.numberOfGroups(), row));
		
		row = 0;
		col = 0;
		// create group sections
		final Tier<Orthography> orthoTier = r.getOrthography();
		final TierDataLayout groupLayout = (TierDataLayout)groupPanel.getLayout();
		for(int i = 0; i < lookupTier.numberOfGroups(); i++) {
			if(i > 0) {
				final JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
				groupPanel.add(sep, new TierDataConstraint(TierDataConstraint.FULL_TIER_COLUMN, row++));
			}
			final JLabel groupLabel = new JLabel("<html><b>Group #" + (i+1) + "</b></html>");
			final JLabel tLbl = new JLabel("Transcription");
			tLbl.setHorizontalAlignment(SwingConstants.RIGHT);
			final JPanel pnl = new JPanel(new BorderLayout());
			pnl.setOpaque(false);
			pnl.add(groupLabel, BorderLayout.WEST);
			pnl.add(tLbl, BorderLayout.EAST);
			groupPanel.add(pnl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row));
			
			final IPAGroupField grpField = new IPAGroupField(lookupTier, i);
			grpField.addTierEditorListener(tierListener);
			
			final PhonUIAction setGrpAct = new PhonUIAction(this, "onSetGroup", i);
			setGrpAct.putValue(PhonUIAction.NAME, "Set");
			setGrpAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set transcription for group " + (i+1));
			final JButton setGrpBtn = new JButton(setGrpAct);
			
			final JPanel grpPanel = new JPanel(new HorizontalLayout(groupLayout.getHorizontalGap()));
			grpPanel.setOpaque(false);
			grpPanel.add(grpField);
			grpPanel.add(setGrpBtn);
			
			groupPanel.add(grpPanel, new TierDataConstraint(TierDataConstraint.FLAT_TIER_PREF_COLUMN, row));
			
			++row;
			final int startRows = groupLayout.getRowCount();
			final Orthography ortho = orthoTier.getGroup(i);
			final OptionBoxVisitior visitor = new OptionBoxVisitior(this, groupPanel, row);
			ortho.accept(visitor);
			final int endRows = groupLayout.getRowCount();
			
			row += (endRows - startRows);
		}
		
		repaint();
	}
	
	public void onSetTranscription() {
		final Record r = getRecord();
		if(r == null) return;
		
		final SessionEditor editor = getEditor();
		if(editor == null) return;

		final Tier<IPATranscript> ipaTarget = r.getIPATarget();
		final Tier<IPATranscript> ipaActual = r.getIPAActual();
		
		boolean groupsNumsMatch = (r.numberOfGroups() == lookupTier.numberOfGroups());
		if(!groupsNumsMatch) return;
		
		for(int i = 0; i < r.numberOfGroups(); i++) {
			final IPATranscript ipa = lookupTier.getGroup(i);
			
			// TODO syllabify ipa
			
			if(ipaTargetBox.isSelected()) {
				final TierEdit<IPATranscript> ipaTargetEdit = new TierEdit<IPATranscript>(editor, ipaTarget, i, ipa);
				editor.getUndoSupport().postEdit(ipaTargetEdit);
			}
			if(ipaActualBox.isSelected()) {
				final TierEdit<IPATranscript> ipaActualEdit = new TierEdit<IPATranscript>(editor, ipaActual, i, ipa);
				editor.getUndoSupport().postEdit(ipaActualEdit);
			}
		}
	}
	
	public void onSetGroup(Integer i) {
		final Record r = getRecord();
		if(r == null) return;
		
		final SessionEditor editor = getEditor();
		if(editor == null) return;

		final Tier<IPATranscript> ipaTarget = r.getIPATarget();
		final Tier<IPATranscript> ipaActual = r.getIPAActual();
		
		final IPATranscript ipa = lookupTier.getGroup(i);
		
		// TODO syllabify ipa
		
		if(ipaTargetBox.isSelected()) {
			final TierEdit<IPATranscript> ipaTargetEdit = new TierEdit<IPATranscript>(editor, ipaTarget, i, ipa);
			editor.getUndoSupport().postEdit(ipaTargetEdit);
		}
		if(ipaActualBox.isSelected()) {
			final TierEdit<IPATranscript> ipaActualEdit = new TierEdit<IPATranscript>(editor, ipaActual, i, ipa);
			editor.getUndoSupport().postEdit(ipaActualEdit);
		}
	}
	
	private final TierEditorListener tierListener = new TierEditorListener() {
		
		@Override
		public <T> void tierValueChanged(Tier<T> tier, int groupIndex, T newValue,
				T oldValue) {
			final TierEdit<T> edit = new TierEdit<T>(getEditor(), tier, groupIndex, newValue);
			getEditor().getUndoSupport().postEdit(edit);
		}
		
	};
}
