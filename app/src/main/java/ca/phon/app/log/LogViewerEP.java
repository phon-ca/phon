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
package ca.phon.app.log;

import java.util.*;

import javax.swing.*;

import ca.phon.plugin.*;

@PhonPlugin(name="LogViewer")
public class LogViewerEP implements IPluginEntryPoint {
	
	public final static String EP_NAME = "LogViewer";

	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		SwingUtilities.invokeLater( () -> {
			final LogViewer viewer = new LogViewer();
			viewer.setSize(1024, 768);
			viewer.centerWindow();
			viewer.setVisible(true);
		});
	}

}
