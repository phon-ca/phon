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
package ca.phon.app.menu.help;

import javax.swing.*;

import ca.phon.plugin.*;

/**
 * Show application help
 */
public class HelpCommand extends PluginAction {

	private static final long serialVersionUID = -2211753524151844715L;

	public HelpCommand() {
		super("Help");
		putValue(Action.NAME, "About Phon");
		putValue(Action.SHORT_DESCRIPTION, "View about dialog and licence agreement");
	}
	
}
