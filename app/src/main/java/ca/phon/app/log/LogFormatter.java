/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * Formatter for application log messages.
 *
 */
public class LogFormatter extends Formatter {

	
	final private static String LOG_VAR = "log";
	
	final private static String DATE_VAR = "date";
	
	final private static String TEMPLATE = "ca/phon/app/log/phonlog.vm";
	
	final private VelocityEngine ve;
	
	private Template template;
	final VelocityContext context;
	
	public LogFormatter() {
		ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		
		try {
			template = ve.getTemplate(TEMPLATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		context = new VelocityContext();
	}

	@Override
	public String format(LogRecord record) {
		context.put(LOG_VAR, record);
		context.put(DATE_VAR, new Date(record.getMillis()));
		
		final StringWriter sw = new StringWriter();
		template.merge(context, sw);
		
		return sw.toString();
	}

}
