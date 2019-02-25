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
package ca.phon.app.menu.tools;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.ui.syllable.BasicSyllabifierTest;

public class BasicSyllabifierTestCommand extends HookableAction {

	private static final long serialVersionUID = -1716015830725094452L;
	
	public BasicSyllabifierTestCommand() {
		putValue(NAME, "Syllabifier Test");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final BasicSyllabifierTest f = new BasicSyllabifierTest();
		f.pack();
		f.setVisible(true);
	}

}