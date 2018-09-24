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
package ca.phon.app.opgraph.wizard;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

public class NodeWizardReportTemplate {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(NodeWizardReportTemplate.class.getName());

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
