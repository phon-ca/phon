package ca.phon.app.session.editor.view.record_data;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.common.IPAGroupField;
import ca.phon.app.session.editor.view.syllabification_and_alignment.ScTypeEdit;
import ca.phon.app.session.editor.view.syllabification_and_alignment.ToggleDiphthongEdit;
import ca.phon.app.session.editor.view.syllabification_and_alignment.actions.ResetSyllabificationCommand;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.Phone;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Group;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import ca.phon.util.Language;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.VerticalLayout;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.html.Option;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Optional;

public class IPAFieldTooltip {

	private IPAGroupField field;

	private Optional<Tier<PhoneMap>> alignmentTier;

	private ToolTipWindow currentFrame;

	private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	public IPAFieldTooltip() {
		super();
		this.alignmentTier = Optional.empty();
	}

	public void install(IPAGroupField groupField) {
		this.field = groupField;
		this.field.addMouseListener(new TooltipMouseListener());
	}

	public void setAlignmentTier(Tier<PhoneMap> alignment) {
		this.alignmentTier = Optional.of(alignment);
	}

	private void showWindow() {
		if(currentFrame != null)
			currentFrame.setVisible(false);

		ToolTipWindow window = new ToolTipWindow();
		window.pack();
		window.setFocusableWindowState(false);

		Point p = field.getLocationOnScreen();
		window.setLocation(p.x, p.y + field.getHeight());
		window.setVisible(true);

		currentFrame = window;
		Toolkit.getDefaultToolkit().addAWTEventListener(focusListener, AWTEvent.KEY_EVENT_MASK);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return propSupport.getPropertyChangeListeners();
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(propertyName, listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
		return propSupport.getPropertyChangeListeners(propertyName);
	}

	private AWTEventListener focusListener = new AWTEventListener() {

		@Override
		public void eventDispatched(AWTEvent event) {
			// cleanup
			if(currentFrame == null) return;
				Toolkit.getDefaultToolkit().removeAWTEventListener(this);
			if(event instanceof KeyEvent) {
				if(((KeyEvent) event).getKeyCode() == KeyEvent.VK_F2) {
					SwingUtilities.invokeLater( () -> {
						final ToolTipWindow focusTooltip = currentFrame;
						focusTooltip.setFocusableWindowState(true);
						focusTooltip.requestFocus();

						if(focusTooltip.syllabificationDisplay.getNumberOfDisplayedPhones() > 0)
							focusTooltip.syllabificationDisplay.setFocusedPhone(0);

						focusTooltip.syllabificationDisplay.requestFocusInWindow();
						focusTooltip.addWindowFocusListener(new WindowFocusListener() {

							@Override
							public void windowGainedFocus(WindowEvent e) {

							}

							@Override
							public void windowLostFocus(WindowEvent e) {
								focusTooltip.setVisible(false);
							}

						});

						currentFrame = null;
					});
					((KeyEvent) event).consume();
					Toolkit.getDefaultToolkit().removeAWTEventListener(this);
				}
			}
		}

	};

	public class ToolTipWindow extends JFrame {

		private SyllabificationDisplay syllabificationDisplay;

		private PhoneMapDisplay alignmentDisplay;

		private JLabel label;

		public ToolTipWindow() {
			super();

			setUndecorated(true);
			setResizable(true);
			init();
		}

		private void init() {
			setLayout(new VerticalLayout(0));

			JPanel contentPanel = new JPanel(new VerticalLayout());
			contentPanel.setBackground(Color.WHITE);

			if(field.hasFocus())
				field.validateAndUpdate();
			IPATranscript value = field.getGroupValue();
			if(value != null) {
				syllabificationDisplay = new SyllabificationDisplay();
				syllabificationDisplay.setFont(FontPreferences.getTierFont());
				syllabificationDisplay.setTranscript(value);
				syllabificationDisplay.addPropertyChangeListener(SyllabificationDisplay.SYLLABIFICATION_PROP_ID, propSupport::firePropertyChange );
				syllabificationDisplay.addPropertyChangeListener(SyllabificationDisplay.HIATUS_CHANGE_PROP_ID, propSupport::firePropertyChange);

				contentPanel.add(syllabificationDisplay);
			}
			if(alignmentTier.isPresent()) {
				PhoneMap alignment = alignmentTier.get().getGroup(field.getGroupIndex());
				if(alignment.getTargetRep().length() > 0
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

			label = new JLabel("Press F2 for focus");
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
		}

	}

	private class TooltipMouseListener extends MouseInputAdapter {

		private Timer currentTimer;

		@Override
		public void mouseEntered(MouseEvent e) {
			super.mouseEntered(e);
			if(currentTimer != null) return;
//			if((e.getModifiersEx() & MouseEvent. SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) {
				currentTimer = new Timer(2000, (evt) -> {
					showWindow();
				});
				currentTimer.setRepeats(false);
				currentTimer.start();
//			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if(currentFrame != null) {
				currentFrame.setVisible(false);
				currentFrame = null;
				Toolkit.getDefaultToolkit().removeAWTEventListener(focusListener);
			}
			if(currentTimer != null) {
				currentTimer.stop();
			}
			currentTimer = null;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			mouseExited(e);
		}

	}

}
