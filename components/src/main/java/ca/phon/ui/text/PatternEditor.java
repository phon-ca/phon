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

import java.awt.event.*;

import javax.swing.*;

import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.*;

/**
 * Editor for query patterns with custom syntax highlighting
 * and autocompletion support for phonex.
 *
 */
public class PatternEditor extends RSyntaxTextArea implements Scrollable {
	
	static {
		installSyntaxStyles();
	}
	
	private static void installSyntaxStyles() {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("text/phonex", PhonexTokenMaker.class.getName());
		
		// TODO regular expressions
	}
	
	private boolean autoCompleteEnabled = true;
	
	private AutoCompletion autoCompletion;
	
	/**
	 * Supported editing formats
	 *
	 */
	public static enum SyntaxStyle {
		PLAIN("text/plain"),
		REGEX("text/regex"),
		PHONEX("text/phonex");
		
		String mimetype;
		
		private SyntaxStyle(String mimetype) {
			this.mimetype = mimetype;
		}
		
		public static SyntaxStyle fromMimetype(String mimetype) {
			SyntaxStyle retVal = SyntaxStyle.PLAIN;
			
			for(SyntaxStyle f:values()) {
				if(f.mimetype.equals(mimetype)) {
					retVal = f;
					break;
				}
			}
			
			return retVal;
		}
		
	};
	
	public PatternEditor() {
		this("", SyntaxStyle.PLAIN);
	}
	
	public PatternEditor(SyntaxStyle format) {
		this("", format);
	}
	
	public PatternEditor(String text, SyntaxStyle format) {
		this(text, format.mimetype);
	}
	
	
	public PatternEditor(String text, String format) {
		super(text);		
		setSyntaxEditingStyle(format);
	}
	
	public SyntaxStyle getSyntaxStyle() {
		return SyntaxStyle.fromMimetype(getSyntaxEditingStyle());
	}
	
	public void setSyntaxStyle(SyntaxStyle format) {
		setSyntaxEditingStyle(format.mimetype);
	}
	
	private void setupAutocompletion() {
		if(autoCompleteEnabled) {
			autoCompletion = new AutoCompletion(createCompletionProvider());
			autoCompletion.setTriggerKey(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
			autoCompletion.setParameterAssistanceEnabled(true);
			autoCompletion.setAutoCompleteEnabled(true);
			autoCompletion.setAutoCompleteSingleChoices(true);
			autoCompletion.install(this);
		} else {
			if(autoCompletion != null) {
				autoCompletion.uninstall();
				autoCompletion = null;
			}
		}
	}
	
	protected CompletionProvider createCompletionProvider() {
		CompletionProvider retVal = new DefaultCompletionProvider();
		
		if(getSyntaxStyle() == SyntaxStyle.PHONEX) {
			retVal = new PhonexAutocompleteProvider();
		}
		
		return retVal;
	}

	@Override
	public void setSyntaxEditingStyle(String arg0) {
		super.setSyntaxEditingStyle(arg0);
		setupAutocompletion();
	}
	
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	
}
