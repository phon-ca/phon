package ca.phon.app.opgraph.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.phon.app.session.editor.SegmentedButtonBuilder;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.wizard.WizardEvent.WizardEventType;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class WizardNavigationAndSettings extends JPanel {

	private static final long serialVersionUID = -7519196201905887955L;

	private final NodeWizard wizard;
	
	private JComboBox<String> stepList;
	
	private JButton nextButton;
	private JButton prevButton;
	
	private JCheckBox settingsButton;
	
	public WizardNavigationAndSettings(NodeWizard wizard) {
		super();
		
		this.wizard = wizard;
		
		init();
		setOpaque(false);
	}
	
	private void init() {
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 2, 0, 0);
		
		add(new JLabel("Current step:"), gbc);
		
		stepList = new JComboBox<>(new StepListModel());
		stepList.addItemListener( (e) -> {
			wizard.gotoStep(stepList.getSelectedIndex()); 
		});
		
		getWizard().addListener( (e) -> {
			if(e.getType() == WizardEventType.GOTO_STEP) {
				stepList.setSelectedIndex(getWizard().getCurrentStepIndex());
			}
		});
		
		++gbc.gridx;
		add(stepList, gbc);
		
		final SegmentedButtonBuilder<JButton> buttonBuilder = new SegmentedButtonBuilder<>(JButton::new);
		final List<JButton> buttons = buttonBuilder.createSegmentedButtons(2, new ButtonGroup());
		
		final ImageIcon nextIcon = IconManager.getInstance().getIcon("actions/go-next", IconSize.SMALL);
		final PhonUIAction nextAct = new PhonUIAction(this, "onNextStep");
		nextAct.putValue(PhonUIAction.NAME, "Next");
		nextAct.putValue(PhonUIAction.SMALL_ICON, nextIcon);
		nextButton = buttons.get(1);
		nextButton.setAction(nextAct);
		
		final ImageIcon prevIcon = IconManager.getInstance().getIcon("actions/go-previous", IconSize.SMALL);
		final PhonUIAction prevAct = new PhonUIAction(this, "onPreviousStep");
		prevAct.putValue(PhonUIAction.NAME, "Previous");
		prevAct.putValue(PhonUIAction.SMALL_ICON, prevIcon);
		prevButton = buttons.get(0);
		prevButton.setAction(prevAct);
		
		++gbc.gridx;
		gbc.insets = new Insets(0, 0, 0, 0);
		add(prevButton, gbc);
		++gbc.gridx;
		add(nextButton, gbc);
		
		++gbc.gridx;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(Box.createHorizontalGlue(), gbc);

	}
	
	public void onNextStep() {
		int stepIdx = getWizard().getCurrentStepIndex();
		if(stepIdx + 1 < getWizard().numberOfSteps()) {
			getWizard().gotoStep(stepIdx+1);
		}
	}
	
	public void onPrevousStep() {
		int stepIdx = getWizard().getCurrentStepIndex();
		if(stepIdx - 1 >=  0) {
			getWizard().gotoStep(stepIdx-1);
		}
	}
	
	public NodeWizard getWizard() {
		return this.wizard;
	}
	
	private class StepListModel extends DefaultComboBoxModel<String> {

		private static final long serialVersionUID = -431442490740250217L;

		@Override
		public int getSize() {
			return getWizard().numberOfSteps();
		}

		@Override
		public String getElementAt(int index) {
			WizardStep step = getWizard().getWizardStep(index);
			return step.getTitle();
		}
		
	}
	
}
