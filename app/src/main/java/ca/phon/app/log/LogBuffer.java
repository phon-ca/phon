package ca.phon.app.log;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;




import ca.phon.app.prefs.PhonProperties;
import ca.phon.util.PrefHelper;

public class LogBuffer extends JTextPane {
	
	private static final Logger LOGGER = Logger.getLogger(LogBuffer.class
			.getName());

	private static final long serialVersionUID = -7321262414933863183L;

	private final DocumentOutputStream stdOutStream;
	
	private final DocumentOutputStream stdErrStream;
	
	private final String name;
	
	private final String encoding = "UTF-8";
	
	public LogBuffer(String name) {
		super();
	
		this.name = name;
		
		final SimpleAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setForeground(as, Color.red);
		StyleConstants.setBold(as, true);
		stdErrStream = new DocumentOutputStream(as);
		
		stdOutStream = new DocumentOutputStream(null);
		
		final Font font = PrefHelper.getFont(PhonProperties.IPA_UI_FONT, 
				Font.decode(PhonProperties.DEFAULT_IPA_UI_FONT));
		setFont(font);
	}
	
	public DocumentOutputStream getStdOutStream() {
		return stdOutStream;
	}

	public DocumentOutputStream getStdErrStream() {
		return stdErrStream;
	}
	
	public String getBufferName() {
		return name;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	private class DocumentOutputStream extends OutputStream {
		
		private final AttributeSet style;
		
		private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		public DocumentOutputStream(AttributeSet style) {
			this.style = style;
		}
		
		@Override
		public void write(int b) throws IOException {
			if(b == '\n') {
				final String line = buffer.toString(encoding);
				buffer = new ByteArrayOutputStream();
				final Runnable onEDT = new Runnable() {
					
					@Override
					public void run() {
						try {
							getDocument().insertString(getDocument().getLength(), line + "\n", style);
						} catch (BadLocationException e) {
							LOGGER.log(Level.SEVERE,
									e.getLocalizedMessage(), e);
						}
					}
				};
				if(SwingUtilities.isEventDispatchThread())
					onEDT.run();
				else
					SwingUtilities.invokeLater(onEDT);
			} else if(b == '\r') { // ignore
			} else {
				buffer.write(b);
			}
		}
		
	}
	
}
