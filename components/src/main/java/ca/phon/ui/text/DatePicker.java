/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import ca.phon.session.format.DateFormatter;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.text.PromptedTextField.FieldState;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.event.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.Date;

/**
 * Customizations for {@link JXDatePicker}
 *
 */
public class DatePicker extends JComponent {

	private static final long serialVersionUID = -1181731542683596418L;
	
	public static final String DATETIME_PROP = "dateTime";
	
	private FormatterTextField<LocalDate> textField;

	private LocalDate promptDate;
	
	private JButton monthViewButton;
	
	private boolean valueIsAdjusting;
	
	public DatePicker() {
		this(LocalDate.now());
	}

	public DatePicker(LocalDate promptDate) {
		super();

		this.promptDate = promptDate;

		init();
	}

	public LocalDate getPromptDate() {
		return promptDate;
	}

	public void setPromptDate(LocalDate promptDate) {
		this.promptDate = promptDate;
	}

	public boolean isValueAdjusting() {
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

		textField.addPropertyChangeListener(FormatterTextField.VALIDATED_VALUE, (e) -> {
			firePropertyChange(DATETIME_PROP, e.getOldValue(), e.getNewValue());
		});

		final ImageIcon calIcon = 
				IconManager.getInstance().getIcon("apps/office-calendar", IconSize.SMALL);
		
		final PhonUIAction monthViewAct = PhonUIAction.runnable(this::onShowMonthView);
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
		final LocalDate currentDateTime = getDateTime();
		textField.setValue(dateTime);
	}
	
	public FormatterTextField<LocalDate> getTextField() {
		return this.textField;
	}
	
	public void onShowMonthView() {
		final JXMonthView monthView = new JXMonthView();
		monthView.setTraversable(true);

		monthView.setBorder(BorderFactory.createEtchedBorder());
		
		JComboBox<Integer> yearSelectionBox = new JComboBox<>(new YearComboBoxModel());
		if(textField.getValue() != null) {
			final Date date = Date.from(textField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
			monthView.setFirstDisplayedDay(date);
			monthView.setSelectionDate(date);
			yearSelectionBox.setSelectedItem(getDateTime().getYear());
		} else {
			final Date date = Date.from(promptDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			monthView.setFirstDisplayedDay(date);

			yearSelectionBox.setSelectedItem(promptDate.getYear());
		}

		yearSelectionBox.addItemListener((e) -> {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				final Date javaDate = monthView.getSelectionDate();
				if(javaDate == null) return;
				final LocalDate localDate = javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				final LocalDate newDate = LocalDate.of((int)yearSelectionBox.getSelectedItem(), localDate.getMonth(), localDate.getDayOfMonth());
				final Date newJavaDate = Date.from(newDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
				monthView.setFirstDisplayedDay(newJavaDate);
			}
		});
		
		monthView.getSelectionModel().addDateSelectionListener(new DateSelectionListener() {
			
			@Override
			public void valueChanged(DateSelectionEvent ev) {
				if(ev.getEventType() == DateSelectionEvent.EventType.DATES_SET) {
					final Date javaDate = monthView.getSelectionDate();
					if (javaDate == null) return;
					final LocalDate localDate = javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					setValueIsAdjusting(true);
					setDateTime(localDate);
					setValueIsAdjusting(false);
				}
			}
			
		});


		final JFrame popupFrame = new JFrame();
		popupFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		popupFrame.setUndecorated(true);

		final JPanel content = new JPanel(new VerticalLayout());
		content.add(yearSelectionBox);
		content.add(monthView);
		final PhonUIAction closePopupAct = PhonUIAction.runnable(() -> popupFrame.setVisible(false));
		content.getActionMap().put("close_popup", closePopupAct);
		monthView.getActionMap().put("close_popup", closePopupAct);
		monthView.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close_popup");
		content.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close_popup");
		popupFrame.add(content);

		popupFrame.pack();
		popupFrame.setLocation(monthViewButton.getLocationOnScreen().x, monthViewButton.getLocationOnScreen().y + monthViewButton.getHeight());
		popupFrame.setVisible(true);

		yearSelectionBox.requestFocus();
		popupFrame.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowGainedFocus(WindowEvent e) {

			}

			@Override
			public void windowLostFocus(WindowEvent e) {
				popupFrame.setVisible(false);
			}
		});
	}

	private class YearComboBoxModel extends DefaultComboBoxModel<Integer> {

		private int numYears;

		private int selectedYear = 0;

		public YearComboBoxModel() {
			this(150);
		}

		public YearComboBoxModel(int numYears) {
			this.numYears = numYears;
		}

		@Override
		public int getSize() {
			return this.numYears;
		}

		@Override
		public Integer getElementAt(int index) {
			return LocalDate.now().getYear() - ((this.numYears-1)-index);
		}

		@Override
		public void setSelectedItem(Object anItem) {
			this.selectedYear = (Integer)anItem;
		}

		@Override
		public Object getSelectedItem() {
			return this.selectedYear;
		}
	}

}
