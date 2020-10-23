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
package ca.phon.app.session.editor.view.media_player.actions;

import java.awt.event.*;
import java.text.*;

import org.apache.logging.log4j.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.media_player.*;
import ca.phon.ui.action.*;

public class GoToAction extends MediaPlayerAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(GoToAction.class.getName());

	private static final long serialVersionUID = -2265485841201934953L;
	
	private final static String CMD_NAME = "Go to...";
	
	private final static String SHORT_DESC = "Go to a specific time";

	public GoToAction(SessionEditor editor, MediaPlayerEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		try {
			getMediaPlayerView().onMenuSelectGoto(new PhonActionEvent(e));
		} catch (ParseException e1) {
			LOGGER.error( e1.getLocalizedMessage(), e1);
		}
	}

}
