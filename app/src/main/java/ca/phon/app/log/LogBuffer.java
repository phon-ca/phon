/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.app.log;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import ca.phon.ui.fonts.FontPreferences;

/**
 * A buffer for general output.
 * 
 */
public class LogBuffer extends RSyntaxTextArea {
	
	private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(LogBuffer.class
			.getName());
	
	public static final String ESCAPE_CODE_PREFIX = "\u0000\u0000";

	private static final long serialVersionUID = -7321262414933863183L;

	private final DocumentOutputStream stdOutStream;
	
	private final DocumentOutputStream stdErrStream;
	
	private String name;
	
	private final String encoding = "UTF-8";
	
	private final List<LogEscapeCodeHandler> escapeCodeHandlers = 
			Collections.synchronizedList(new ArrayList<LogEscapeCodeHandler>());
	
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
	
	public void addEscapeCodeHandler(LogEscapeCodeHandler escapeCodeHandler) {
		if(!this.escapeCodeHandlers.contains(escapeCodeHandler))
			this.escapeCodeHandlers.add(escapeCodeHandler);
	}
	
	public void removeEscapeCodeHandler(LogEscapeCodeHandler escapeCodeHandler) {
		this.escapeCodeHandlers.remove(escapeCodeHandler);
	}
	
	public List<LogEscapeCodeHandler> getEscapeCodeHandlers() {
		return Collections.unmodifiableList(escapeCodeHandlers);
	}
	
	public void fireEscapeCodeEvent(String code) {
		for(LogEscapeCodeHandler handler:getEscapeCodeHandlers()) {
			handler.handleEscapeCode(code);
		}
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
	
	public void writeEscapeCode(String code) {
		try {
			stdOutStream.flush();
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		
		final StringBuffer buffer = new StringBuffer();
		buffer.append(ESCAPE_CODE_PREFIX).append(code);
		
		try {
			stdOutStream.write(buffer.toString().getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		
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
						LOGGER.error(
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
			final String toWrite = buffer.toString(encoding);
			if(toWrite.startsWith(ESCAPE_CODE_PREFIX)) {
				fireEscapeCodeEvent(toWrite.substring(2));
			} else {
				writeToDocument(buffer.toString(encoding));
			}
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

	public void setBufferName(String string) {
		this.name = string;
	}
	
}
