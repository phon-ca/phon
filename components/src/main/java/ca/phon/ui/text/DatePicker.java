package ca.phon.ui.text;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.event.DateSelectionEvent;
import org.jdesktop.swingx.event.DateSelectionListener;
import org.joda.time.DateTime;

import ca.phon.session.DateFormatter;
import ca.phon.ui.DateTimeDocument;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.text.PromptedTextField.FieldState;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Customizations for {@link JXDatePicker}
 *
 */
public class DatePicker extends JComponent {

	private static final long serialVersionUID = -1181731542683596418L;
	
	public static final String DATETIME_PROP = "dateTime";
	
	private FormatterTextField<DateTime> textField;
	
	private JButton monthViewButton;
	
	private JXMonthView monthView;
	
	public DatePicker() {
		super();
		
		init();
	}
	
	private void init() {
		textField = new FormatterTextField<DateTime>(new DateFormatter());
		textField.setPrompt("YYYY-MM-DD");
		textField.setToolTipText("Enter date in format YYYY-MM-DD");
		textField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				if(textField.getState() == FieldState.INPUT && !textField.validateText()) {
					ToastFactory.makeToast("Date format: " + DateFormatter.DATETIME_FORMAT).start(textField);
					Toolkit.getDefaultToolkit().beep();
					textField.requestFocus();
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				
			}
		});
		
		monthView = new JXMonthView();
		monthView.setTraversable(true);
		
		final ImageIcon calIcon = 
				IconManager.getInstance().getIcon("apps/office-calendar", IconSize.SMALL);
		
		final PhonUIAction monthViewAct = new PhonUIAction(this, "onShowMonthView");
		monthViewAct.putValue(PhonUIAction.SMALL_ICON, calIcon);
		monthViewAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show calendar");
		monthViewButton = new JButton(monthViewAct);
		
		setLayout(new BorderLayout());
		add(textField, BorderLayout.CENTER);
		add(monthViewButton, BorderLayout.EAST);
	}
	
	public DateTime getDateTime() {
		return textField.getValue();
	}
	
	public void setDateTime(DateTime dateTime) {
		textField.setValue(dateTime);
	}
	
	public JXMonthView getMonthView() {
		return this.monthView;
	}
	
	public FormatterTextField<DateTime> getTextField() {
		return this.textField;
	}
	
	public void onShowMonthView() {
		final JXMonthView monthView = getMonthView();
		monthView.setTraversable(true);
		monthView.setBorder(BorderFactory.createEtchedBorder());
		
		if(textField.getValue() != null) {
			monthView.setFirstDisplayedDay(textField.getValue().toDate());
			monthView.setSelectionDate(textField.getValue().toDate());
		}
		
		monthView.getSelectionModel().addDateSelectionListener(new DateSelectionListener() {
			
			@Override
			public void valueChanged(DateSelectionEvent ev) {
				textField.setValue(new DateTime(monthView.getSelectionDate()));
				
			}
		});
		final JPopupMenu popup = new JPopupMenu();
		popup.add(monthView);
		popup.show(monthViewButton, 0, monthViewButton.getHeight());
	}
	
//	
//	private final FocusListener fl = new FocusListener() {
//		
//		String initialVal = null;
//		
//		@Override
//		public void focusLost(FocusEvent e) {
//			final String curVal = getEditor().getText();
//			if(initialVal != null && !initialVal.equals(curVal)) {
//				final DateTime newDate = dateTimeDoc.getDateTime();
//				setDate(newDate.toDate());
//				fireActionPerformed(COMMIT_KEY);
//			}
//		}
//		
//		@Override
//		public void focusGained(FocusEvent e) {
//			initialVal = getEditor().getText();
//		}
//		
//	};
//	
}
