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
package ca.phon.app.prefs;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXList;

public class HiddenMessagesPrefsPanel extends PrefsPanel {

	private static final long serialVersionUID = -8956102208686690713L;
	
	/*
	 * UI
	 */
	private JXList hiddenPropsList;
	
	private JButton resetAllButton;
	
	private JButton resetSelectedButton;

	public HiddenMessagesPrefsPanel() {
		super("User-hidden Messages");
		
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		String msgText = "<html>" +
				"<p>Use the list below to reset (i.e., show) user-hidable messages in the application.</p>" +
				"</html>";
		JLabel msgLabel = new JLabel(msgText);
		
		
	}
	
	
}