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
package ca.phon.app.log.actions;

import java.awt.event.*;

import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.ui.action.*;

public class PrintHTMLBufferAction extends HookableAction {

	private static final long serialVersionUID = 9121919367383017713L;

	private final BufferPanel bufferPanel;
	
	public PrintHTMLBufferAction(BufferPanel bufferPanel) {
		super();
		this.bufferPanel = bufferPanel;
		
		putValue(PhonUIAction.NAME, "Print");
		putValue(PhonUIAction.SHORT_DESCRIPTION, "Print HTML");
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(bufferPanel.isShowingHtml())
			bufferPanel.getBrowser().print();
	}

}
