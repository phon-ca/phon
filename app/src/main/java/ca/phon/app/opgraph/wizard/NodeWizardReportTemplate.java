package ca.phon.app.opgraph.wizard;

import java.io.StringWriter;
import java.util.Properties;
import java.util.UUID;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

public class NodeWizardReportTemplate {

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
		final Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "string");
		p.setProperty("string.resource.loader.class", "org.apache.velocity.runtime.resource.loader.StringResourceLoader");
	
		final VelocityEngine ve = new VelocityEngine();
		ve.init(p);
		
		StringResourceRepository repository = StringResourceLoader.getRepository();
		repository.putStringResource(getName(), getTemplate());
		repository.putStringResource(getName(), getTemplate(), "UTF-8");
		
		// load template
		final Template t = ve.getTemplate(getName());
		final StringWriter sw = new StringWriter();
		
		t.merge(ctx.velocityContext(), sw);
		
		return sw.toString();
	}
	
}
