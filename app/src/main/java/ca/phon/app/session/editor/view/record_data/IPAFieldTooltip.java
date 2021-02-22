package ca.phon.app.session.editor.view.record_data;

import ca.phon.app.session.editor.view.common.IPAGroupField;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.Phone;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Group;
import ca.phon.ui.ipa.PhoneMapDisplay;
import ca.phon.ui.ipa.SyllabificationDisplay;
import org.jdesktop.swingx.VerticalLayout;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.html.Option;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class IPAFieldTooltip {

	private IPAGroupField field;

	private Optional<PhoneMap> alignment;

	private JFrame currentFrame;

	public IPAFieldTooltip() {
		super();
		this.alignment = Optional.empty();
	}

	public void install(IPAGroupField groupField) {
		this.field = groupField;
		this.field.addMouseListener(new TooltipMouseListener());
	}

	private void showWindow() {
		if(currentFrame != null)
			currentFrame.setVisible(false);

		JFrame window = new JFrame();
		window.setUndecorated(true);

		window.setLayout(new VerticalLayout(0));

		field.validateAndUpdate();
		IPATranscript value = field.getGroupValue();
		if(value != null) {
			SyllabificationDisplay sd = new SyllabificationDisplay();
			sd.setTranscript(value);

			window.add(sd);
		}
		if(alignment.isPresent()) {
			window.add(new JSeparator(SwingConstants.HORIZONTAL));

			PhoneMapDisplay alignmentDisplay = new PhoneMapDisplay();
			alignmentDisplay.setPhoneMapForGroup(0, alignment.get());
			window.add(alignmentDisplay);
		}

		window.pack();
		window.setFocusable(false);

		Point p = field.getLocationOnScreen();
		window.setLocation(p.x, p.y + field.getHeight());
		window.setVisible(true);

		currentFrame = window;
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
