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
package ca.phon.app.corpus;

import java.io.*;
import java.util.*;

import ca.phon.app.modules.*;
import ca.phon.app.session.editor.*;
import ca.phon.plugin.*;
import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.session.Record;

@PhonPlugin
public class SessionTemplateEP extends SessionEditorEP {

	public final static String EP_NAME = "SessionTemplate";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final EntryPointArgs epArgs = new EntryPointArgs(args);
		final Project project = epArgs.getProject();
		final String corpus = epArgs.getCorpus();
		
		Session template = null;
		try {
			template = project.getSessionTemplate(corpus);
		} catch(IOException e) {
			final SessionFactory factory = SessionFactory.newFactory();
			template = factory.createSession();
			template.setCorpus(corpus);
			template.setName("__sessiontemplate");
			
			final Record r = factory.createRecord();
			r.addGroup();
			template.addRecord(r);
			
			final List<TierViewItem> tierView = new ArrayList<TierViewItem>();
			tierView.add(factory.createTierViewItem(SystemTierType.Orthography.getName(), true));
			tierView.add(factory.createTierViewItem(SystemTierType.IPATarget.getName(), true));
			tierView.add(factory.createTierViewItem(SystemTierType.IPAActual.getName(), true));
			tierView.add(factory.createTierViewItem(SystemTierType.Notes.getName(), true));
			tierView.add(factory.createTierViewItem(SystemTierType.Segment.getName(), true));
			template.setTierView(tierView);
		}
		
		args.put(EntryPointArgs.SESSION_OBJECT, template);
		super.pluginStart(args);
	}

}
