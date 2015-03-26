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
