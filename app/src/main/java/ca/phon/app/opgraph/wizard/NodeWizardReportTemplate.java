/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.wizard;

import java.io.*;
import java.util.*;

import org.apache.velocity.*;
import org.apache.velocity.app.*;
import org.apache.velocity.runtime.resource.loader.*;
import org.apache.velocity.runtime.resource.util.*;

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
			LOGGER.error( e.getLocalizedMessage(), e);
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
