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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierViewItem;

@PhonPlugin
public class CorpusTemplateEP implements IPluginEntryPoint {

	public final static String EP_NAME = "CorpusTemplate";
	
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
			template.setName(EP_NAME);
			
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
		
		final Session session = template;
		final Runnable onEDT = new Runnable() {
			
			@Override
			public void run() {
				final CorpusTemplateEditor editor = new CorpusTemplateEditor(project, session);
				editor.pack();
				editor.setVisible(true);
			}
			
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

}