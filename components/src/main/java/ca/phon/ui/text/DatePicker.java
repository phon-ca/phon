package ca.phon.ui.text;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.text.NavigationFilter;
import javax.swing.text.NavigationFilter.FilterBypass;
import javax.swing.text.Position.Bias;

import org.jdesktop.swingx.JXDatePicker;
import org.joda.time.DateTime;

import ca.phon.ui.DateTimeDocument;
import ca.phon.ui.fonts.FontPreferences;

/**
 * Customizations for {@link JXDatePicker}
 *
 */
public class DatePicker extends JXDatePicker {

	private static final long serialVersionUID = -1181731542683596418L;
	
	private DateTimeDocument dateTimeDoc;
	
	public DatePicker() {
		super();
		
		init();
		dateTimeDoc = new DateTimeDocument(new DateTime(getDate()));
		getEditor().setCaret(new OverwriteCaret());
		getEditor().setDocument(dateTimeDoc);
		getEditor().addFocusListener(fl);
		getEditor().setFont(FontPreferences.getMonospaceFont());
	}
	
	private void init() {
		setFormats("yyyy-MM-dd");
	}
	
	private final FocusListener fl = new FocusListener() {
		
		String initialVal = null;
		
		@Override
		public void focusLost(FocusEvent e) {
			final String curVal = getEditor().getText();
			if(initialVal != null && !initialVal.equals(curVal)) {
				final DateTime newDate = dateTimeDoc.getDateTime();
				setDate(newDate.toDate());
				fireActionPerformed(COMMIT_KEY);
			}
		}
		
		@Override
		public void focusGained(FocusEvent e) {
			initialVal = getEditor().getText();
		}
		
	};
	
}
