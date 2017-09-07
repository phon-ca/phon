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
package ca.phon.app.corpus;

import java.io.IOException;
import java.util.*;

import javax.swing.SwingUtilities;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.*;
import ca.phon.project.Project;
import ca.phon.session.*;

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
