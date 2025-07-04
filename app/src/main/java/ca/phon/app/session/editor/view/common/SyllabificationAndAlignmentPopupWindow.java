package ca.phon.app.session.editor.view.common;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.*;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeSupport;
import java.util.Optional;

public class SyllabificationAndAlignmentPopupWindow extends JFrame {

	private Tier<IPATranscript> ipaTier;

	private Optional<Tier<PhoneAlignment>> alignmentTier;

	private int wordIndex;

	private JLabel label;
	private SyllabificationDisplay syllabificationDisplay;
	private PhoneMapDisplay alignmentDisplay;

	private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	public SyllabificationAndAlignmentPopupWindow(Tier<IPATranscript> ipaTier, int wordIndex) {
		this(ipaTier, Optional.empty(), wordIndex);
	}

	public SyllabificationAndAlignmentPopupWindow(Tier<IPATranscript> ipaTier, Optional<Tier<PhoneAlignment>> alignmentTierOpt, int wordIndex) {
		super();

		this.ipaTier = ipaTier;
		this.alignmentTier = alignmentTierOpt;
		this.wordIndex = wordIndex;

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

	public int getWordIndex() {
		return this.wordIndex;
	}

	public IPATranscript getValue() {
		return this.ipaTier.getValue();
	}

	private void init() {
		setLayout(new VerticalLayout(0));

		JPanel contentPanel = new JPanel(new VerticalLayout());
		contentPanel.setBackground(Color.WHITE);

		IPATranscript value = getValue();
		if (value != null) {
			syllabificationDisplay = new SyllabificationDisplay();
			syllabificationDisplay.setFont(FontPreferences.getTierFont());
			syllabificationDisplay.setTranscript(value);
			syllabificationDisplay.addPropertyChangeListener(SyllabificationDisplay.SYLLABIFICATION_PROP_ID, propSupport::firePropertyChange);
			syllabificationDisplay.addPropertyChangeListener(SyllabificationDisplay.HIATUS_CHANGE_PROP_ID, propSupport::firePropertyChange);

			contentPanel.add(syllabificationDisplay);
		}
		if (alignmentTier.isPresent()) {
			final PhoneAlignment phoneAlignment = alignmentTier.get().getValue();
			final PhoneMap alignment = wordIndex < phoneAlignment.getAlignments().size() ? phoneAlignment.getAlignments().get(wordIndex) : new PhoneMap();
			if (alignment.getTargetRep().length() > 0
					&& alignment.getActualRep().length() > 0
					&& alignment.getAlignmentLength() > 0) {
				contentPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

				PhoneMap pm = new PhoneMap(alignment.getTargetRep(), alignment.getActualRep());
				pm.setTopAlignment(alignment.getTopAlignment());
				pm.setBottomAlignment(alignment.getBottomAlignment());

				PhoneMapDisplay alignmentDisplay = new PhoneMapDisplay();
				alignmentDisplay.setFont(FontPreferences.getTierFont());
				alignmentDisplay.setPhoneMapForWord(0, pm);
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

		final PhonUIAction<Boolean> closeAct = PhonUIAction.consumer(this::setVisible, false);
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "close");
		getRootPane().getActionMap().put("close", closeAct);
	}

}
