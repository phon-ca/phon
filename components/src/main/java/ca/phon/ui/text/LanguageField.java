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

import java.util.*;

import ca.phon.formatter.Formatter;
import ca.phon.util.*;

public class LanguageField extends FormatterTextField<List<Language>> {
	
	private static final long serialVersionUID = 1991509022077011707L;

	public LanguageField() {
		this(new DefaultLanguageListFormatter());
	}
	
	public LanguageField(Formatter<List<Language>> formatter) {
		super(formatter);
		
		setPrompt("Enter language codes separated by space");
		
		final FinalTokenTextCompleterModel completerModel = new FinalTokenTextCompleterModel();
		final TextCompleter completer = new TextCompleter(completerModel);
		final List<LanguageEntry> allLangs = LanguageParser.getInstance().getLanguages();
		for(LanguageEntry lang:allLangs) {
			completerModel.addCompletion(lang.getId(), lang.getId() + " - " + lang.getName());
		}
		completer.install(this);
	}
	
	@Override
	public boolean validateText() {
		final boolean retVal = super.validateText();
		if(retVal) {
			setupTooltipText();
		}
		return retVal;
	}
	
	private void setupTooltipText() {
		final List<Language> languages = getValue();
		final StringBuilder sb = new StringBuilder();
		
		for(Language lang:languages) {
			if(sb.length() > 0)
				sb.append(", ");
			sb.append(lang.toString()).append(" (").append(lang.getPrimaryLanguage().getName()).append(")");
		}
		if(sb.length() > 0) {
			setToolTipText(sb.toString());
		}
	}

}
