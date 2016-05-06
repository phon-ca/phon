package ca.phon.app.opgraph.wizard;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledSeparator;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.GlossPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.PinstripePainter;

import ca.gedge.opgraph.OpNode;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.wizard.WizardEvent;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class WizardStepList extends JPanel {
	
	private static final long serialVersionUID = 6180937906703773985L;

	private final WeakReference<NodeWizard> wizardRef;
	
	private final Map<Integer, JLabel> stepLabels = new LinkedHashMap<>();
	
	public WizardStepList(NodeWizard wizard) {
		super();
		this.wizardRef = new WeakReference<NodeWizard>(wizard);
		
		init();
		getWizard().addListener( e -> { 
			if(e.getType() == WizardEvent.WizardEventType.STEP_ADDED)
				updateLabels(); 
			else
				refreshLabels();
		} );
	}

	public NodeWizard getWizard() {
		return wizardRef.get();
	}

	private void init() {
		if(getWizard() == null) return;
		
		final GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		updateLabels();
		
	}
	
	private void refreshLabels() {
		ImageIcon currentStepIcon = IconManager.getInstance().getIcon("actions/agt_forward", IconSize.SMALL);
		ImageIcon stepIcon = IconManager.getInstance().getDisabledIcon("actions/agt_forward", IconSize.SMALL);
		WizardStep currentStep = getWizard().getCurrentStep();
		for(int stepIdx:stepLabels.keySet()) {
			WizardStep step = getWizard().getWizardStep(stepIdx);
			if(step == currentStep) {
				stepLabels.get(stepIdx).setIcon(currentStepIcon);
				stepLabels.get(stepIdx).setFont(FontPreferences.getControlFont().deriveFont(Font.BOLD));
			} else {
				stepLabels.get(stepIdx).setIcon(stepIcon);
				stepLabels.get(stepIdx).setFont(FontPreferences.getControlFont());
			}
		}
	}
	
	private void updateLabels() {
		removeAll();
		stepLabels.clear();
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 20, 5, 2);

		final WizardExtension ext = getWizard().getGraph().getExtension(WizardExtension.class);
		
		for(int stepIdx = 0; stepIdx < getWizard().numberOfSteps(); stepIdx++) {
			++gbc.gridy;
			final WizardStep step = getWizard().getWizardStep(stepIdx);
			final JLabel stepLbl = new JLabel(step.getTitle());
			stepLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			stepLbl.addMouseListener(new GotoStepListener(stepIdx));
			add(stepLbl, gbc);
			
			final OpNode node = step.getExtension(OpNode.class);
			if(ext != null && node != null && ext.getNodeMessage(node) != null) {
				final String nodeMessage = ext.getNodeMessage(node);
				if(nodeMessage.length() > 0) {
					++gbc.gridy;
					gbc.insets.left = 40;
					final JLabel infoLabel = new JLabel(ext.getNodeMessage(node));
					add(infoLabel, gbc);
					gbc.insets.left = 20;
				}
			}
			
			stepLabels.put(stepIdx, stepLbl);
		}

		++gbc.gridy;
		gbc.weighty = 1.0;
		add(Box.createVerticalGlue(), gbc);
		
		refreshLabels();
	}
	
	private class GotoStepListener extends MouseInputAdapter {

		int step;
		
		public GotoStepListener(int step) {
			this.step = step;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			getWizard().gotoStep(step);
		}
		
	}
	
}
