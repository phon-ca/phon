package ca.phon.app.session.editor.tier;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.tier.layout.TierDataConstraint;
import ca.phon.app.session.editor.tier.layout.TierDataLayout;
import ca.phon.app.session.editor.tier.layout.TierDataLayoutButtons;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ValidationEditorView extends EditorView {
	
	private static final long serialVersionUID = 8217005057844862790L;

	private final static String VIEW_NAME = "Transcript Validation";
	
	private JPanel contentPane = null;
	
	private JPanel topPanel;
	
	private JButton autoValidateButton;

	public ValidationEditorView(SessionEditor editor) {
		super(editor);
		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		
		contentPane = new JPanel();
		contentPane.setBackground(Color.white);
		final TierDataLayout layout = new TierDataLayout();
		contentPane.setLayout(layout);
		
		final FormLayout topLayout = new FormLayout("pref, fill:pref:grow, right:pref", "pref");
		topPanel = new JPanel(topLayout);
		
		final PhonUIAction autoValidateAct = new PhonUIAction(this, "onAutoValidate");
		autoValidateAct.putValue(PhonUIAction.NAME, "Auto Validate");
		autoValidateAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Perform automatic validation");
		autoValidateButton = new JButton(autoValidateAct);
		
		final TierDataLayoutButtons tdlb = new TierDataLayoutButtons(contentPane, layout);
		
		final CellConstraints cc = new CellConstraints();
		topPanel.add(autoValidateButton, cc.xy(1,1));
		topPanel.add(tdlb, cc.xy(3,1));
		
		add(topPanel, BorderLayout.NORTH);

		final JScrollPane scroller = new JScrollPane(contentPane);
		add(scroller, BorderLayout.CENTER);
		
		update(); 
	}
	
	public void update() {
		contentPane.removeAll();
		
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final Record record = editor.currentRecord();
		
		int currentRow = 0;
		int actualOffset = 2 + session.getTranscriberCount();
		
		final JLabel ipaTargetLabel = new JLabel(
				"<html><b>" + SystemTierType.IPATarget.getName() + "</b></html>");
		ipaTargetLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		ipaTargetLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		final TierDataConstraint ipaTargetLabelConstraint =
				new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, currentRow);
		contentPane.add(ipaTargetLabel, ipaTargetLabelConstraint);
		
		final JLabel ipaActualLabel = new JLabel(
				"<html><b>" + SystemTierType.IPAActual.getName() + "</b></html>");
		ipaActualLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		ipaActualLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		final TierDataConstraint ipaActualTierDataConstraint = 
				new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, currentRow + actualOffset);
		contentPane.add(ipaActualLabel, ipaActualTierDataConstraint);
		
		final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		final TierDataConstraint separatorConstraint = 
				new TierDataConstraint(TierDataConstraint.FULL_TIER_COLUMN, currentRow + session.getTranscriberCount() + 1);
		contentPane.add(separator, separatorConstraint);
		
		currentRow++;
		final Tier<IPATranscript> ipaTarget = record.getIPATarget();
		final Tier<IPATranscript> ipaActual = record.getIPAActual();
		for(int i = 0; i < session.getTranscriberCount(); i++) {
			final Transcriber transcriber = session.getTranscriber(i);
			
			final JLabel targetTranscriberLabel = new JLabel(transcriber.getUsername());
			targetTranscriberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			targetTranscriberLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
			final TierDataConstraint targetTranscriberConstraint =
					new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, currentRow);
			contentPane.add(targetTranscriberLabel, targetTranscriberConstraint);
			
			final JLabel actualTranscriberLabel = new JLabel(transcriber.getUsername());
			actualTranscriberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			actualTranscriberLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
			final TierDataConstraint actualTranscriberConstraint =
					new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, currentRow + actualOffset);
			contentPane.add(actualTranscriberLabel, actualTranscriberConstraint);
			
			for(int j = 0; j < record.numberOfGroups(); j++) {
				final IPAValidationGroupField targetField = new IPAValidationGroupField(ipaTarget, j, transcriber);
				final TierDataConstraint targetFieldConstraint = 
						new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+j, currentRow);
				contentPane.add(targetField, targetFieldConstraint);
				
				final IPAValidationGroupField actualField = new IPAValidationGroupField(ipaActual, j, transcriber);
				final TierDataConstraint actualFieldConstraint =
						new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+j, currentRow + actualOffset);
				contentPane.add(actualField, actualFieldConstraint);
			}
			currentRow++;
		}
	}
	
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("misc/validation", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		return null;
	}

}
