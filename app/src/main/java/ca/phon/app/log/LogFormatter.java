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
