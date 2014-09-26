package ca.phon.ui.text;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.jdesktop.swingx.JXDatePicker;

/**
 * Customizations for {@link JXDatePicker}
 *
 */
public class DatePicker extends JXDatePicker {

	private static final long serialVersionUID = -1181731542683596418L;
	
	public DatePicker() {
		super();
		
		init();
		getEditor().addFocusListener(fl);
	}
	
	private void init() {
		setFormats("yyyy-MM-dd");
	}
	
	private final FocusListener fl = new FocusListener() {
		
		@Override
		public void focusLost(FocusEvent e) {
			fireActionPerformed(COMMIT_KEY);
		}
		
		@Override
		public void focusGained(FocusEvent e) {
		}
		
	};
	
}
