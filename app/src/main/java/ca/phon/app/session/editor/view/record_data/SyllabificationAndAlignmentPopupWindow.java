package ca.phon.app.session.editor.view.record_data;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Tier;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeSupport;
import java.util.Optional;

public class SyllabificationAndAlignmentPopupWindow extends JFrame {

	private int groupIndex = 0;
	private Tier<IPATranscript> ipaTier;
	private Optional<Tier<PhoneMap>> alignmentTier;

	private JLabel label;
	private SyllabificationDisplay syllabificationDisplay;
	private PhoneMapDisplay alignmentDisplay;

	private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	public SyllabificationAndAlignmentPopupWindow(Tier<IPATranscript> ipaTier, int groupIndex) {
		this(ipaTier, Optional.empty(), groupIndex);
	}

	public SyllabificationAndAlignmentPopupWindow(Tier<IPATranscript> ipaTier, Optional<Tier<PhoneMap>> alignmentTierOpt, int groupIndex) {
		super();

		this.ipaTier = ipaTier;
		this.alignmentTier = alignmentTierOpt;
		this.groupIndex = groupIndex;

		setUndecorated(true);
		setResizable(true);
		init();
	}

	public PropertyChangeSupport getSyllabificationAndAlignmentPropSupport() {
		return this.propSupport;
	}

	public SyllabificationDisplay getSyllabificationDisplay() {
		return this.syllabificationDisplay;
	}

	public int getGroupIndex() {
		return this.groupIndex;
	}

	public IPATranscript getGroupValue() {
		return this.ipaTier.getGroup(getGroupIndex());
	}

	private void init() {
		setLayout(new VerticalLayout(0));

		JPanel contentPanel = new JPanel(new VerticalLayout());
		contentPanel.setBackground(Color.WHITE);

		IPATranscript value = getGroupValue();
		if (value != null) {
			syllabificationDisplay = new SyllabificationDisplay();
			syllabificationDisplay.setFont(FontPreferences.getTierFont());
			syllabificationDisplay.setTranscript(value);
			syllabificationDisplay.addPropertyChangeListener(SyllabificationDisplay.SYLLABIFICATION_PROP_ID, propSupport::firePropertyChange);
			syllabificationDisplay.addPropertyChangeListener(SyllabificationDisplay.HIATUS_CHANGE_PROP_ID, propSupport::firePropertyChange);

			contentPanel.add(syllabificationDisplay);
		}
		if (alignmentTier.isPresent()) {
			PhoneMap alignment = alignmentTier.get().getGroup(getGroupIndex());
			if (alignment.getTargetRep().length() > 0
					&& alignment.getActualRep().length() > 0
					&& alignment.getAlignmentLength() > 0) {
				contentPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

				PhoneMap pm = new PhoneMap(alignment.getTargetRep(), alignment.getActualRep());
				pm.setTopAlignment(alignment.getTopAlignment());
				pm.setBottomAlignment(alignment.getBottomAlignment());

				PhoneMapDisplay alignmentDisplay = new PhoneMapDisplay();
				alignmentDisplay.setFont(FontPreferences.getTierFont());
				alignmentDisplay.setPhoneMapForGroup(0, pm);
				alignmentDisplay.addPropertyChangeListener(PhoneMapDisplay.ALIGNMENT_CHANGE_PROP,
						(e) -> {
							revalidate();
							pack();
							propSupport.firePropertyChange(e);
						});
				contentPanel.add(alignmentDisplay);
			}
		}

		add(new JScrollPane(contentPanel));

		label = new JLabel("Press Tab for focus");
		label.setFont(label.getFont().deriveFont(Font.ITALIC, 10.0f));
		add(label);

		addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				label.setVisible(false);
				revalidate();
				pack();
			}

			@Override
			public void windowLostFocus(WindowEvent e) {

			}
		});

		final PhonUIAction closeAct = new PhonUIAction(this, "setVisible", false);
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "close");
		getRootPane().getActionMap().put("close", closeAct);
	}

}
