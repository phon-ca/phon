package ca.phon.app.log;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;









import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import ca.phon.app.prefs.PhonProperties;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.PrefHelper;

public class LogBuffer extends RSyntaxTextArea {
	
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
		
		setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
		
		setFont(FontPreferences.getMonospaceFont());
	}
	
	public OutputStream getStdOutStream() {
		return stdOutStream;
	}

	public OutputStream getStdErrStream() {
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
			if(b != '\r') {
				buffer.write(b);
			}
			// keep full lines so we don't cut multi-byte characters
			if(b == '\n' && buffer.size() >= 512) {
				writeToDocument(buffer.toString(encoding));
				buffer = new ByteArrayOutputStream();
			}
		}
		
		private void writeToDocument(final String data) throws UnsupportedEncodingException {
			final Runnable onEDT = new Runnable() {
				
				@Override
				public void run() {
					try {
						getDocument().insertString(getDocument().getLength(), data, style);
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
		}
		
		@Override
		public void flush() throws IOException {
			super.flush();
			writeToDocument(buffer.toString(encoding));
			buffer = new ByteArrayOutputStream();
		}
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		final Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		super.paintComponent(g2);
	}
	
}
