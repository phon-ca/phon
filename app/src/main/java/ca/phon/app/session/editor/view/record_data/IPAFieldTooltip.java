package ca.phon.app.session.editor.view.record_data;

import ca.phon.app.session.editor.view.common.IPAGroupField;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Tier;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Optional;

public class IPAFieldTooltip {

	private IPAGroupField field;

	private SyllabificationAndAlignmentPopupWindow currentFrame;

	private Optional<Tier<PhoneMap>> alignmentTier;

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

		if(field.getGroupValue().length() == 0) return;

		if(field.hasFocus()) {
			field.validateAndUpdate();
		}

		int groupIndex = this.field.getGroupIndex();
		Tier<IPATranscript> ipaTier = this.field.getTier();

		SyllabificationAndAlignmentPopupWindow window = new SyllabificationAndAlignmentPopupWindow(ipaTier, alignmentTier, groupIndex);
		window.pack();
		window.setFocusableWindowState(false);

		window.getSyllabificationAndAlignmentPropSupport().addPropertyChangeListener( (e) -> {
			if(SyllabificationDisplay.SYLLABIFICATION_PROP_ID.equals(e.getPropertyName())
				|| SyllabificationDisplay.HIATUS_CHANGE_PROP_ID.equals(e.getPropertyName())
				|| PhoneMapDisplay.ALIGNMENT_CHANGE_PROP.equals(e.getPropertyName())) {
				propSupport.firePropertyChange(e);
			}
		});

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
			if(currentFrame == null) {
				return;
			}

			if(event instanceof KeyEvent) {
				if(((KeyEvent) event).getKeyCode() == KeyEvent.VK_TAB) {
					SwingUtilities.invokeLater( () -> {
						final SyllabificationAndAlignmentPopupWindow focusTooltip = currentFrame;
						focusTooltip.setFocusableWindowState(true);
						focusTooltip.requestFocus();

						if(focusTooltip.getSyllabificationDisplay().getNumberOfDisplayedPhones() > 0)
							focusTooltip.getSyllabificationDisplay().setFocusedPhone(0);

						focusTooltip.getSyllabificationDisplay().requestFocusInWindow();
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
				} else if(((KeyEvent) event).getKeyCode() == KeyEvent.VK_ESCAPE) {
					if(currentFrame != null) {
						currentFrame.setVisible(false);
						currentFrame.dispose();
						currentFrame = null;
					}

					((KeyEvent) event).consume();
					Toolkit.getDefaultToolkit().removeAWTEventListener(this);
				}

			}
		}

	};

	private class TooltipMouseListener extends MouseInputAdapter {

		private Timer currentTimer;

		@Override
		public void mouseEntered(MouseEvent e) {
			super.mouseEntered(e);
			if(currentTimer != null) return;
			currentTimer = new Timer(2000, (evt) -> {
				showWindow();
			});
			currentTimer.setRepeats(false);
			currentTimer.start();
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

		@Override
		public void mousePressed(MouseEvent e) { mouseExited(e); }

	}

}
