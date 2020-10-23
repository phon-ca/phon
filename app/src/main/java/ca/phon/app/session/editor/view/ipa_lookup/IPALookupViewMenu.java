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
package ca.phon.app.session.editor.view.ipa_lookup;

import javax.swing.*;

import ca.phon.app.session.editor.view.ipa_lookup.actions.*;

public class IPALookupViewMenu extends JMenu {
	
	private static final long serialVersionUID = 3248124841856311448L;

	private final IPALookupView lookupView;
	
	public IPALookupViewMenu(IPALookupView lookupView) {
		super();
		this.lookupView = lookupView;
		
		init();
	}
	
	private void init() {
		final ExportIPACommand exportAct = new ExportIPACommand(lookupView);
		add(exportAct);
		
		final ImportIPACommand importAct = new ImportIPACommand(lookupView);
		add(importAct);
		
		addSeparator();
		final AutoTranscribeCommand autoTranscribeAct = new AutoTranscribeCommand(lookupView);
		add(autoTranscribeAct);
	}

}
