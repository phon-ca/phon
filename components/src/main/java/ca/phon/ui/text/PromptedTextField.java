/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.ui.text;

import java.awt.Color;
import java.awt.SystemColor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

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
		
		
		
		if(this.fieldState == FieldState.PROMPT) {
			if(oldState == FieldState.INPUT && super.getText().length() > 0)
				throw new IllegalStateException("Cannot set state to PROMPT when field has input.");
			super.setText(prompt);
		} else if(this.fieldState == FieldState.INPUT) {
			if(oldState == FieldState.PROMPT)
				super.setText("");
		}
		
		super.setForeground(this.fieldState.getColor());
		
		super.firePropertyChange(STATE_PROPERTY, oldState, this.fieldState);
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
	

	/**
	 * State change on focus
	 * 
	 */
	private static FocusListener focusStateListener = new FocusListener() {

		@Override
		public void focusGained(FocusEvent arg0) {
			PromptedTextField sf = (PromptedTextField)arg0.getSource();
			if(sf.fieldState == FieldState.PROMPT) {
				sf.setState(FieldState.INPUT);
			}
		}

		@Override
		public void focusLost(FocusEvent arg0) {
			PromptedTextField sf = (PromptedTextField)arg0.getSource();
			if(sf.getText().length()==0) {
				sf.setState(FieldState.PROMPT);
			}
		}
	};
}
