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
package ca.phon.app.menu.tools;

import javax.swing.*;

import ca.phon.app.help.*;
import ca.phon.plugin.*;

/**
 * Show the language code lookup window.
 *
 */
public class LanguageCodesCommand extends PluginAction {

	private static final long serialVersionUID = -7475966189706158219L;

	public LanguageCodesCommand() {
		super(LanguageCodeEP.EP_NAME);
		putValue(Action.NAME, "ISO-639-3 Language Codes");
		putValue(Action.SHORT_DESCRIPTION, "Standard 3 letter language codes");
	}	
	
}
