package ca.phon.app.opgraph.wizard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

public class NodeWizardReportTemplate {

	private final static Logger LOGGER = Logger.getLogger(NodeWizardReportTemplate.class.getName());

	private String name;

	private String template;

	public NodeWizardReportTemplate() {
		this(UUID.randomUUID().toString());
	}

	public NodeWizardReportTemplate(String name) {
		this(name, "");
	}

	public NodeWizardReportTemplate(String name, String template) {
		super();

		this.name = name;
		this.template = template;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String merge(NodeWizardReportContext ctx) {
		// TODO move velocity init somewhere else
		final Properties p = new Properties();
		try {
			p.load(getClass().getResourceAsStream("velocity.properties"));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		final VelocityEngine ve = new VelocityEngine();
		ve.init(p);

		StringResourceRepository repository = StringResourceLoader.getRepository();
		repository.putStringResource(getName(), getTemplate(), "UTF-8");

		// load template
		final Template t = ve.getTemplate(getName(), "UTF-8");
		final StringWriter sw = new StringWriter();

		t.merge(ctx.velocityContext(), sw);

		return sw.toString();
	}

}
