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

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;

/**
 * Base class for media layer actions.
 */
public abstract class MediaPlayerAction extends SessionEditorAction {

	private static final long serialVersionUID = 2965979837203474011L;

	private final MediaPlayerEditorView editorView;
	
	public MediaPlayerAction(SessionEditor editor, MediaPlayerEditorView view) {
		super(editor);
		this.editorView = view;
	}
	
	public MediaPlayerEditorView getMediaPlayerView() {
		return this.editorView;
	}
	
}
