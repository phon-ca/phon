package ca.phon.ui.text;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

/**
 * Editor for query patterns with custom syntax highlighting
 * and autocompletion support for phonex.
 *
 */
public class PatternEditor extends RSyntaxTextArea {
	
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
	
}
