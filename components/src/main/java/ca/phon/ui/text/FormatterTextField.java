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

import ca.phon.formatter.*;
import org.apache.logging.log4j.LogManager;

import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class FormatterTextField<T> extends PromptedTextField {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(FormatterTextField.class.getName());

	private static final long serialVersionUID = 4203616792431039321L;
	
	public static <K> FormatterTextField<K> createTextField(Class<K> type) {
		return new FormatterTextField<K>(type);
	}
	
	private final Formatter<T> formatter;

	public final static String VALIDATED_VALUE = Formatter.class.getName() + ".validatedValue";

	private T validatedValue;
	
	private FormatterTextField(Class<T> type) {
		this(getFormatter(type));
	}
	
	public FormatterTextField(Formatter<T> formatter) {
		super();
		this.formatter = formatter;

		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				getDocument().addDocumentListener(docListener);
			}

			@Override
			public void focusLost(FocusEvent e) {
				getDocument().removeDocumentListener(docListener);
			}
		});
	}
	
	private static <R> Formatter<R> getFormatter(Class<R> type) {
		return FormatterFactory.createFormatter(type);
	}
	
	public boolean validateText() {
		if(super.getText().trim().length() == 0 && this.validatedValue != null) {
			T validatedValue = this.validatedValue;
			this.validatedValue = null;
			firePropertyChange(VALIDATED_VALUE, validatedValue, null);
		}
		final T value = getValue();
		if(value == null && getState() == FieldState.INPUT) {
			setForeground(Color.red);
		} else {
			setValue(value);
			setForeground(getState().getColor());
		}
		return value != null;
	}
	
	public T getValue() {
		T retVal = this.validatedValue;
		
		if(getText().length() > 0 && getState() == FieldState.INPUT && hasFocus()) {
			try {
				retVal = formatter.parse(getText());
				setToolTipText(null);
			} catch (Exception e) {
				setToolTipText(e.getMessage());
				LOGGER.info( e.getLocalizedMessage(), e);
			}
		}
		
		return retVal;
	}
	
	public void setValue(T val) {
		T oldVal = this.validatedValue;
		this.validatedValue = val;
		if(val == null) {
			setText("");
			super.setState(FieldState.PROMPT);
		} else {
			if(!hasFocus()) {
				try {
					final String s = formatter.format(val);
					setText(s);
				} catch (Exception e) {
					LOGGER.error(e.getLocalizedMessage(), e);
				}
			}
		}
		firePropertyChange(VALIDATED_VALUE, oldVal, val);
	}
	
	private final DocumentListener docListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			if(hasFocus() && getState() == FieldState.INPUT)
				validateText();
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			if(hasFocus() && getState() == FieldState.INPUT)
				validateText();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			
		}

	};

	public class FormatterDocument extends PlainDocument {

		private static final long serialVersionUID = 3345418733077374861L;

		@Override
		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {
			// don't allow insertions that would allow 
			// bad data
			final StringBuilder sb = new StringBuilder();
			sb.append(super.getText(0, offs));
			sb.append(str);
			sb.append(super.getText(offs, super.getLength() - offs));
			try {
				formatter.parse(sb.toString());
				super.insertString(offs, str, a);
			} catch (Exception e) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
		
	}
	
}
