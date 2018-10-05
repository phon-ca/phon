/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ui.text;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.event.DateSelectionEvent;
import org.jdesktop.swingx.event.DateSelectionListener;

import ca.phon.session.DateFormatter;
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
	
	private FormatterTextField<LocalDate> textField;
	
	private JButton monthViewButton;
	
	private JXMonthView monthView;
	
	private boolean valueIsAdjusting;
	
	public DatePicker() {
		super();
		
		init();
	}

	public boolean isValueAdjusing() {
		return this.valueIsAdjusting;
	}
	
	public void setValueIsAdjusting(boolean valueIsAdjusing) {
		this.valueIsAdjusting = valueIsAdjusing;
	}
	
	private void init() {
		textField = new FormatterTextField<LocalDate>(new DateFormatter());
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
		
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		setLayout(layout);
		
		add(textField, gbc);
		gbc.gridx++;
		gbc.weightx = 0.0;
		add(monthViewButton, gbc);
	}
	
	
	
	public LocalDate getDateTime() {
		return textField.getValue();
	}
	
	public void setDateTime(LocalDate dateTime) {
		textField.setValue(dateTime);
	}
	
	public JXMonthView getMonthView() {
		return this.monthView;
	}
	
	public FormatterTextField<LocalDate> getTextField() {
		return this.textField;
	}
	
	public void onShowMonthView() {
		final JXMonthView monthView = getMonthView();
		monthView.setTraversable(true);
		monthView.setBorder(BorderFactory.createEtchedBorder());
		
		if(textField.getValue() != null) {
			final Date date = Date.from(textField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
			monthView.setFirstDisplayedDay(date);
			monthView.setSelectionDate(date);
		}
		
		monthView.getSelectionModel().addDateSelectionListener(new DateSelectionListener() {
			
			@Override
			public void valueChanged(DateSelectionEvent ev) {
				final Date javaDate = monthView.getSelectionDate();
				if(javaDate == null) return;
				final LocalDate localDate = javaDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDate();
				
				textField.setValue(localDate);
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
