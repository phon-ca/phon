/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import java.util.List;

import ca.phon.formatter.Formatter;
import ca.phon.util.Language;
import ca.phon.util.LanguageEntry;
import ca.phon.util.LanguageParser;

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
