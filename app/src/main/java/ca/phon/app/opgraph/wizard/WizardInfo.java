package ca.phon.app.opgraph.wizard;import javax.swing.text.html.HTML;

import org.pegdown.PegDownProcessor;

/**
 * Title, message, and message format.
 */
public class WizardInfo {
	
	private String title;
	
	private String message;
	
	private WizardInfoMessageFormat format = WizardInfoMessageFormat.HTML;
	
	public WizardInfo() {
		this("");
	}
	
	public WizardInfo(String title) {
		this(title, "");
	}
	
	public WizardInfo(String title, String message) {
		this(title, message, WizardInfoMessageFormat.HTML);
	}
	
	public WizardInfo(String title, String message, WizardInfoMessageFormat format) {
		super();
		this.title = title;
		this.message = message;
		this.format = format;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public WizardInfoMessageFormat getFormat() {
		return format;
	}

	public void setFormat(WizardInfoMessageFormat format) {
		this.format = format;
	}
	
	private String markdownToHTML(String md) {
		final PegDownProcessor processor = new PegDownProcessor();
		return processor.markdownToHtml(md);
	}
	
	/**
	 * Get message in HTML format
	 * @return message in HTML
	 */
	public String getMessageHTML() {
		final String message = getMessage();
		if(message == null) return "";
		
		switch(getFormat()) {
		case HTML:
			return message;
			
		case MARKDOWN:
			return markdownToHTML(message);
			
		default:
			return message;
		}
	}

}
