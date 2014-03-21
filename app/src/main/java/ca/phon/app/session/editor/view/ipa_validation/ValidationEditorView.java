package ca.phon.app.session.editor.view.ipa_validation;

import java.awt.BorderLayout;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.HorizontalLayout;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.common.IPAGroupField;
import ca.phon.app.session.editor.view.common.TierDataConstraint;
import ca.phon.app.session.editor.view.common.TierDataLayoutPanel;
import ca.phon.app.session.editor.view.common.TierEditorListener;
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.session.Transcribers;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Editor view used to validate double-blind transcriptions.  References
 * the extension {@link AlternativeTranscript} in {@link IPATranscript}
 * objects for the IPA Target and IPA Actual tiers or a {@link Record}.
 */
public class ValidationEditorView extends EditorView {

	private static final long serialVersionUID = -1179165192834735478L;

	public final static String VIEW_NAME = "IPA Validation";
	
	public final static String VIEW_ICON = "misc/validation";
	
	private final TierDataLayoutPanel targetValidationPanel;
	
	private final Tier<IPATranscript> targetCandidateTier;
	
	private final TierDataLayoutPanel actualValidationPanel;
	
	private final Tier<IPATranscript> actualCandidateTier;
	
	private JToolBar toolbar;
	
	private JButton autoValidateButton;

	public ValidationEditorView(SessionEditor editor) {
		super(editor);
		
		final SessionFactory factory = SessionFactory.newFactory();
		targetCandidateTier = factory.createTier("Target Validation", IPATranscript.class, true);
		actualCandidateTier = factory.createTier("Actual Validation", IPATranscript.class, true);
		
		targetValidationPanel = new TierDataLayoutPanel();
		actualValidationPanel = new TierDataLayoutPanel();
		init();
		update();
		setupEditorActions();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(targetValidationPanel, BorderLayout.NORTH);
		panel.add(actualValidationPanel, BorderLayout.CENTER);
		final JScrollPane scroller = new JScrollPane(panel);
		
		add(scroller, BorderLayout.CENTER);
	}
	
	private void setupEditorActions() {
		final DelegateEditorAction recordChangeAct = new DelegateEditorAction(this, "onRecordChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, recordChangeAct);
	}
	
	private void update() {
		final Record record = getEditor().currentRecord();
		if(record == null) return;
		
		updateCandidateTier(record.getIPATarget(), targetCandidateTier);
		updateValidationPanel(targetValidationPanel, record.getIPATarget(), targetCandidateTier);
		
		updateCandidateTier(record.getIPAActual(), actualCandidateTier);
		updateValidationPanel(actualValidationPanel, record.getIPAActual(), actualCandidateTier);
	}
	
	
	private void updateCandidateTier(Tier<IPATranscript> tier, Tier<IPATranscript> candidateTier) {
		candidateTier.removeAll();
		
		for(IPATranscript t:tier) {
			final AlternativeTranscript alts = t.getExtension(AlternativeTranscript.class);
			final IPATranscript candidate = (alts != null && alts.getSelected() != null ? alts.get(alts.getSelected()) : new IPATranscript());
			candidateTier.addGroup(candidate);
		}
	}
	
	private void updateValidationPanel(TierDataLayoutPanel panel, Tier<IPATranscript> tier, Tier<IPATranscript> candidateTier) {
		panel.removeAll();
		
		final Session session = getEditor().getSession();
		final Transcribers transcribers = session.getTranscribers();
		
		int row = 0;
		final JLabel tierLabel = new JLabel("<html><b>" + tier.getName() + " Validation</b></html>");
		final TierDataConstraint tierLabelConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row);
		panel.add(tierLabel, tierLabelConstraint);
		
		int j = row + 1;
		for(Transcriber transcriber:transcribers) {
			final String tStr = (transcriber.getRealName() != null ? transcriber.getRealName() : transcriber.getUsername());
			final JLabel transLabel = new JLabel(tStr);
			transLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			final TierDataConstraint tdc = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, j++);
			panel.add(transLabel, tdc);
		}
		
		for(int i = 0; i < tier.numberOfGroups(); i++) {
			// add candidate field object
			final IPAGroupField candidateField = new IPAGroupField(candidateTier, i);
			candidateField.addTierEditorListener(tierListener);
			
			final SetGroupData data = new SetGroupData();
			data.tier = tier;
			data.candidateTier = candidateTier;
			data.group = i;
			
			final PhonUIAction setGrpAct = new PhonUIAction(this, "onSetGroup", data);
			setGrpAct.putValue(PhonUIAction.NAME, "Set");
			setGrpAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set group IPA");
			final JButton btn = new JButton(setGrpAct);

			final JPanel p = new JPanel(new HorizontalLayout(3));
			p.add(candidateField);
			p.add(btn);
			
			final TierDataConstraint candidateRestraint = new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+i, row++);
			panel.add(p, candidateRestraint);

			final IPATranscript ipa = tier.getGroup(i);
			final AlternativeTranscript alts = ipa.getExtension(AlternativeTranscript.class);
			final ButtonGroup btnGrp = new ButtonGroup();
			j = row;
			for(Transcriber t:transcribers) {
				final IPATranscript opt = (alts.containsKey(t.getUsername()) ? alts.get(t.getUsername()) : null);
				if(opt == null) continue;
				
				final SelectIPAData selectData = new SelectIPAData();
				selectData.tier = tier;
				selectData.candidateTier = candidateTier;
				selectData.group = i;
				selectData.transcriber = t.getUsername();
				
				final PhonUIAction optAct = new PhonUIAction(this, "onSelectIPA", selectData);
				optAct.putValue(PhonUIAction.NAME, opt.toString());
				if(alts.getSelected() != null && alts.getSelected().equals(t.getUsername())) {
					optAct.putValue(PhonUIAction.SELECTED_KEY, true);
				}
				
				final JRadioButton optBtn = new JRadioButton(optAct);
				optBtn.setOpaque(false);
				btnGrp.add(optBtn);
				final TierDataConstraint tdc = new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+i, j++);
				panel.add(optBtn, tdc);
			}
		}
	}
	
	/*
	 * UI actions
	 */

	/**
	 * Data passed to onSetGroup method
	 */
	public final class SetGroupData {
		Tier<IPATranscript> tier;
		Tier<IPATranscript> candidateTier;
		int group;
	}
	/**
	 * Set ipa for a group
	 * @param data
	 */
	public void onSetGroup(SetGroupData data) {
		final TierEdit<IPATranscript> edit = new TierEdit<IPATranscript>(getEditor(),
				data.tier, data.group, data.candidateTier.getGroup(data.group));
		getEditor().getUndoSupport().postEdit(edit);
	}
	
	/**
	 * Data passed to onSelectIPA method
	 */
	public final class SelectIPAData {
		Tier<IPATranscript> tier;
		Tier<IPATranscript> candidateTier;
		int group;
		String transcriber;
	}
	/**
	 * Select IPA for specified transcriber
	 * 
	 * @param data
	 */
	public void onSelectIPA(SelectIPAData data) {
		final IPATranscript grp = data.tier.getGroup(data.group);
		final AlternativeTranscript alts = grp.getExtension(AlternativeTranscript.class);
		if(alts != null && alts.containsKey(data.transcriber)) {
			final IPATranscript selected = alts.get(data.transcriber);
			alts.setSelected(data.transcriber);
			
			data.candidateTier.setGroup(data.group, selected);
		}
	}
	
	/*
	 * Editor actions
	 */
	@RunOnEDT
	public void onRecordChange(EditorEvent ee) {
		update();
	}
	
	private final TierEditorListener tierListener = new TierEditorListener() {
		
		@Override
		public <T> void tierValueChanged(Tier<T> tier, int groupIndex, T newValue,
				T oldValue) {
			final TierEdit<T> edit = new TierEdit<T>(getEditor(), tier, groupIndex, newValue);
			getEditor().getUndoSupport().postEdit(edit);
		}
		
	};
	
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
		return null;
	}

	
}
