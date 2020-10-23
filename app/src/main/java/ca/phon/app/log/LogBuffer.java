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
package ca.phon.app.log;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.text.*;

import org.fife.ui.rsyntaxtextarea.*;

import ca.phon.ui.fonts.*;

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
