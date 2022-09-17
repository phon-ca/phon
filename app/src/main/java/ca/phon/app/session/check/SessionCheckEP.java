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
package ca.phon.app.session.check;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.*;

import java.awt.*;
import java.util.Map;

@PhonPlugin(name="Session Check")
public class SessionCheckEP implements IPluginEntryPoint {

	public final static String EP_NAME = "Session Check";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final EntryPointArgs epArgs = new EntryPointArgs(args);
		var project = epArgs.getProject();
		
		if(project == null) return;
		
		final SessionCheckWizard wizard = SessionCheckWizard.newWizard(project);
		wizard.pack();
		wizard.setSize(new Dimension(1024, 768));
		wizard.centerWindow();
		wizard.setVisible(true);
	}

}
