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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;

/**
 * Text field with optional prompt.
 * 
 */
public class PromptedTextField extends JTextField {
	
	private static final long serialVersionUID = 3592005178147889933L;

	/**
	 * Text field state
	 */
	public static enum FieldState {
		UNDEFINED(Color.red),
		PROMPT(Color.lightGray),
		INPUT(SystemColor.textText);
		
		private Color color;
		
		private FieldState(Color c) {
			this.color = c;
		}
		
		public Color getColor() {
			return color;
		}
		
		public static FieldState fromString(String s) {
			FieldState retVal = UNDEFINED;
			
			for(FieldState fs:values()) {
				if(fs.toString().equalsIgnoreCase(s)) {
					retVal = fs;
					break;
				}
			}
			
			return retVal;
		}
	};
	
	public final static String STATE_PROPERTY = "_search_field_state_";
	
	/**
	 * Current state
	 */
	private FieldState fieldState = FieldState.UNDEFINED;

	/**
	 * Search field prompt
	 */
	private String prompt = "";
	
	
	/**
	 * Should we keep prompt text when we get focus?
	 */
	private boolean keepPrompt = false;
	
	/**
	 * Constructor
	 */
	public PromptedTextField() {
		this("");
	}
	
	public PromptedTextField(String prompt) {
		super();
		this.prompt = prompt;
		setState(FieldState.PROMPT);
		addFocusListener(focusStateListener);
		addPropertyChangeListener(STATE_PROPERTY, this::onStateChange);
	}
	
	public boolean isKeepPrompt() {
		return this.keepPrompt;
	}
	
	public void setKeepPrompt(boolean keepPrompt) {
		this.keepPrompt = keepPrompt;
	}
	
	public String getPrompt() {
		return this.prompt;
	}
	
	public void setPrompt(String prompt) {
		this.prompt = prompt;
		if(getState() == FieldState.PROMPT) {
			super.setText(prompt);
		}
	}
	
	/**
	 * Set state of field
	 * 
	 * @param state
	 */
	public void setState(FieldState state) {
		if(this.fieldState == state) return;
		FieldState oldState = this.fieldState;
		this.fieldState = state;
		super.setForeground(this.fieldState.getColor());

		if(state == FieldState.PROMPT) {
			if(oldState == FieldState.INPUT && super.getText().length() > 0)
				throw new IllegalStateException("Cannot set state to PROMPT when field has input.");
			super.setText(prompt);
		} else if(this.fieldState == FieldState.INPUT) {
			if(oldState == FieldState.PROMPT)
				super.setText("");
		}

		super.firePropertyChange(STATE_PROPERTY, oldState, this.fieldState);
	}

	private void onStateChange(PropertyChangeEvent evt) {

	}
	
	public void setState(String state) {
		setState(FieldState.fromString(state));
	}
	
	
	public FieldState getState() {
		return this.fieldState;
	}
	@Override
	public String getText() {
		String retVal = super.getText();
		if(this.fieldState == FieldState.PROMPT) {
			retVal = "";
		}
		return retVal;
	}
	
	@Override
	public void setText(String s) {
		if(s == null) s = "";
		
		if(s.length() == 0) {
			if(getState() != FieldState.PROMPT) {
				super.setText(s);
				setState(FieldState.PROMPT);
			}
		} else if(s.length() > 0) {
			setState(FieldState.INPUT);
			super.setText(s);
		}
	}
	
	private void _setText(String text) {
		super.setText(text);
	}

	/**
	 * State change on focus
	 * 
	 */
	private FocusListener focusStateListener = new FocusListener() {

		@Override
		public void focusGained(FocusEvent arg0) {
			PromptedTextField sf = (PromptedTextField)arg0.getSource();
			if(sf.fieldState == FieldState.PROMPT) {
				sf.setState(FieldState.INPUT);
				if(isKeepPrompt()) {
					_setText(prompt);
				}
			}
		}

		@Override
		public void focusLost(FocusEvent arg0) {
			PromptedTextField sf = (PromptedTextField)arg0.getSource();
			if(sf.getText().length()==0) {
				sf.setState(FieldState.PROMPT);
			} else {
				if(sf.getText().equals(prompt) && isKeepPrompt()) {
					sf.setText("");
					sf.setState(FieldState.PROMPT);
				}
			}
		}
	};
}
