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
package ca.phon.app.menu.edit;

import javax.swing.Action;

import ca.phon.plugin.PluginAction;

/**
 * Open preferences dialog
 */
public class PreferencesCommand extends PluginAction {

	private static final long serialVersionUID = 4295409487719796945L;
	
	private final static String EP = "Preferences";
	
	public PreferencesCommand() {
		super(EP);
		putValue(Action.NAME, "Preferences...");
		putValue(Action.SHORT_DESCRIPTION, "Edit application preferences");
	}
	
	public PreferencesCommand(String initialPanel) {
		super(EP);
		putValue(Action.NAME, "Preferences...");
		putValue(Action.SHORT_DESCRIPTION, "Edit application preferences");
		
		putArg("prefpanel", initialPanel);
	}
	
}