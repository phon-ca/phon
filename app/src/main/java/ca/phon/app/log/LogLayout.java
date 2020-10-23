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
/* Original License from Log4j2
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package ca.phon.app.log;

import java.io.*;
import java.lang.management.*;
import java.nio.charset.*;
import java.util.*;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.layout.*;
import org.apache.logging.log4j.core.util.*;
import org.apache.logging.log4j.util.*;

import ca.phon.ui.fonts.*;

/**
 * Log4j2 layout for application log messages.
 * 
 * Code adapted from {@link org.apache.logging.log4j.core.layout.HtmlLayout}
 */
public class LogLayout extends AbstractStringLayout {

	public static enum FontSize {
		SMALLER("smaller"), 
		XXSMALL("xx-small"),
		XSMALL("x-small"), 
		SMALL("small"), 
		MEDIUM("medium"), 
		LARGE("large"),
		XLARGE("x-large"), 
		XXLARGE("xx-large"),  
		LARGER("larger");
		
		private final String size;
		
		private FontSize(final String size) {
			this.size = size;
		}
		
		public String getFontSize() {
			return size;
		}
		
		public static FontSize getFontSize(final String size) {
			for (final FontSize fontSize : values()) {
				if (fontSize.size.equals(size)) {
					return fontSize;
				}
			}
			return SMALL;
		}
		
		public FontSize larger() {
			return this.ordinal() < XXLARGE.ordinal() ? FontSize.values()[this.ordinal() + 1] : this;
		}
	}
	
	/**
	 * Default font family: {@value}.
	 */
	public static final String DEFAULT_FONT_FAMILY = FontPreferences.getUIIpaFont().getFontName()  + ",arial,sans-serif";
	
	private static final String TRACE_PREFIX = "<br />&nbsp;&nbsp;&nbsp;&nbsp;";
	private static final String REGEXP = Strings.LINE_SEPARATOR.equals("\n") ? "\n" : Strings.LINE_SEPARATOR + "|\n";
	
	private String fontName = FontPreferences.getUIIpaFont().getFontName();
	
	private static final String DEFAULT_TITLE = "Phon Log Messages";
	private String title = DEFAULT_TITLE;
	
	private static final FontSize DEFAULT_FONTSIZE = FontSize.SMALL;
	private FontSize fontSize = DEFAULT_FONTSIZE;
	private FontSize headerSize = fontSize.larger();
	
	private final long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
	
	public LogLayout() {
		this("UTF-8");
	}
	
	public LogLayout(String charset) {
		this(Charset.forName(charset));
	}
	
	public LogLayout(Charset charset) {
		super(charset);
	}
    
    /**
     * For testing purposes.
     */
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
    	this.title = title;
    }
    
    public void setFontSize(FontSize fontSize) {
    	this.fontSize = fontSize;
    	this.headerSize = fontSize.larger();
    }
    
    public FontSize getFontSize() {
    	return this.fontSize;
    }
    
    public FontSize getHeaderSize() {
    	return this.headerSize;
    }
    
    public String getFontName() {
    	return this.fontName;
    }
    
    public void setFontName(String fontName) {
    	this.fontName = fontName;
    }
    
    private String addCharsetToContentType(final String contentType) {
        if (contentType == null) {
            return "text/html; charset=" + getCharset();
        }
        return contentType.contains("charset") ? contentType : contentType + "; charset=" + getCharset();
    }

    /**
     * Formats as a String.
     *
     * @param event The Logging Event.
     * @return A String containing the LogEvent as HTML.
     */
    @Override
    public String toSerializable(final LogEvent event) {
        final StringBuilder sbuf = getStringBuilder();

        sbuf.append(Strings.LINE_SEPARATOR).append("<tr>").append(Strings.LINE_SEPARATOR);

        sbuf.append("<td>");
        sbuf.append(event.getTimeMillis() - jvmStartTime);
        sbuf.append("</td>").append(Strings.LINE_SEPARATOR);

        final String escapedThread = Transform.escapeHtmlTags(event.getThreadName());
        sbuf.append("<td title=\"").append(escapedThread).append(" thread\">");
        sbuf.append(escapedThread);
        sbuf.append("</td>").append(Strings.LINE_SEPARATOR);

        sbuf.append("<td title=\"Level\">");
        if (event.getLevel().equals(Level.DEBUG)) {
            sbuf.append("<font color=\"#339933\">");
            sbuf.append(Transform.escapeHtmlTags(String.valueOf(event.getLevel())));
            sbuf.append("</font>");
        } else if (event.getLevel().isMoreSpecificThan(Level.WARN)) {
            sbuf.append("<font color=\"#993300\"><strong>");
            sbuf.append(Transform.escapeHtmlTags(String.valueOf(event.getLevel())));
            sbuf.append("</strong></font>");
        } else {
            sbuf.append(Transform.escapeHtmlTags(String.valueOf(event.getLevel())));
        }
        sbuf.append("</td>").append(Strings.LINE_SEPARATOR);

        String escapedLogger = Transform.escapeHtmlTags(event.getLoggerName());
        if (Strings.isEmpty(escapedLogger)) {
            escapedLogger = LoggerConfig.ROOT;
        }
        sbuf.append("<td title=\"").append(escapedLogger).append(" logger\">");
        sbuf.append(escapedLogger);
        sbuf.append("</td>").append(Strings.LINE_SEPARATOR);

        final StackTraceElement element = event.getSource();
        sbuf.append("<td>");
        sbuf.append(Transform.escapeHtmlTags(element.getFileName()));
        sbuf.append(':');
        sbuf.append(element.getLineNumber());
        sbuf.append("</td>").append(Strings.LINE_SEPARATOR);

        sbuf.append("<td title=\"Message\">");
        sbuf.append(Transform.escapeHtmlTags(event.getMessage().getFormattedMessage()).replaceAll(REGEXP, "<br />"));
        sbuf.append("</td>").append(Strings.LINE_SEPARATOR);
        sbuf.append("</tr>").append(Strings.LINE_SEPARATOR);

        if (event.getContextStack() != null && !event.getContextStack().isEmpty()) {
            sbuf.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : ").append(fontSize);
            sbuf.append(";\" colspan=\"6\" ");
            sbuf.append("title=\"Nested Diagnostic Context\">");
            sbuf.append("NDC: ").append(Transform.escapeHtmlTags(event.getContextStack().toString()));
            sbuf.append("</td></tr>").append(Strings.LINE_SEPARATOR);
        }

        if (event.getContextData() != null && !event.getContextData().isEmpty()) {
            sbuf.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : ").append(fontSize);
            sbuf.append(";\" colspan=\"6\" ");
            sbuf.append("title=\"Mapped Diagnostic Context\">");
            sbuf.append("MDC: ").append(Transform.escapeHtmlTags(event.getContextData().toMap().toString()));
            sbuf.append("</td></tr>").append(Strings.LINE_SEPARATOR);
        }

        final Throwable throwable = event.getThrown();
        if (throwable != null) {
            sbuf.append("<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : ").append(fontSize);
            sbuf.append(";\" colspan=\"6\">");
            appendThrowableAsHtml(throwable, sbuf);
            sbuf.append("</td></tr>").append(Strings.LINE_SEPARATOR);
        }

        return sbuf.toString();
    }

    private void appendThrowableAsHtml(final Throwable throwable, final StringBuilder sbuf) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
        } catch (final RuntimeException ex) {
            // Ignore the exception.
        }
        pw.flush();
        final LineNumberReader reader = new LineNumberReader(new StringReader(sw.toString()));
        final ArrayList<String> lines = new ArrayList<>();
        try {
          String line = reader.readLine();
          while (line != null) {
            lines.add(line);
            line = reader.readLine();
          }
        } catch (final IOException ex) {
            if (ex instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            lines.add(ex.toString());
        }
        boolean first = true;
        for (final String line : lines) {
            if (!first) {
                sbuf.append(TRACE_PREFIX);
            } else {
                first = false;
            }
            sbuf.append(Transform.escapeHtmlTags(line));
            sbuf.append(Strings.LINE_SEPARATOR);
        }
    }

    private StringBuilder appendLs(final StringBuilder sbuilder, final String s) {
        sbuilder.append(s).append(Strings.LINE_SEPARATOR);
        return sbuilder;
    }

    private StringBuilder append(final StringBuilder sbuilder, final String s) {
        sbuilder.append(s);
        return sbuilder;
    }

    /**
     * Returns appropriate HTML headers.
     * @return The header as a byte array.
     */
    @Override
    public byte[] getHeader() {
        final StringBuilder sbuf = new StringBuilder();
        append(sbuf, "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" ");
        appendLs(sbuf, "\"http://www.w3.org/TR/html4/loose.dtd\">");
        appendLs(sbuf, "<html>");
        appendLs(sbuf, "<head>");
        append(sbuf, "<meta charset=\"");
        append(sbuf, getCharset().toString());
        appendLs(sbuf, "\"/>");
        append(sbuf, "<title>").append(title);
        appendLs(sbuf, "</title>");
        appendLs(sbuf, "<style type=\"text/css\">");
        appendLs(sbuf, "<!--");
        append(sbuf, "body, table {font-family:").append(fontName).append("; font-size: ");
        appendLs(sbuf, headerSize.getFontSize()).append(";}");
        appendLs(sbuf, "th {background: #336699; color: #FFFFFF; text-align: left;}");
        appendLs(sbuf, "-->");
        appendLs(sbuf, "</style>");
        appendLs(sbuf, "</head>");
        appendLs(sbuf, "<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">");
        appendLs(sbuf, "<hr size=\"1\" noshade=\"noshade\">");
        appendLs(sbuf, "Log session start time " + new java.util.Date() + "<br>");
        appendLs(sbuf, "<br>");
        appendLs(sbuf,
                "<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">");
        appendLs(sbuf, "<tr>");
        appendLs(sbuf, "<th>Time</th>");
        appendLs(sbuf, "<th>Thread</th>");
        appendLs(sbuf, "<th>Level</th>");
        appendLs(sbuf, "<th>Logger</th>");
        appendLs(sbuf, "<th>File:Line</th>");
        appendLs(sbuf, "<th>Message</th>");
        appendLs(sbuf, "</tr>");
        return sbuf.toString().getBytes(getCharset());
    }

    /**
     * Returns the appropriate HTML footers.
     * @return the footer as a byte array.
     */
    @Override
    public byte[] getFooter() {
        final StringBuilder sbuf = new StringBuilder();
        appendLs(sbuf, "</table>");
        appendLs(sbuf, "<br>");
        appendLs(sbuf, "</body></html>");
        return getBytes(sbuf.toString());
    }
    
}
