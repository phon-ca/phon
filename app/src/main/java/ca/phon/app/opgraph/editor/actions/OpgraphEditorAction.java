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
package ca.phon.app.opgraph.editor.actions;

import java.lang.ref.*;

import ca.phon.app.opgraph.editor.*;
import ca.phon.opgraph.app.commands.*;

public abstract class OpgraphEditorAction extends HookableCommand {
	
	private static final long serialVersionUID = 2331592911456671778L;

	private WeakReference<OpgraphEditor> editorRef;
	
	public OpgraphEditorAction(OpgraphEditor editor) {
		super();
		
		this.editorRef = new WeakReference<OpgraphEditor>(editor);
	}

	public OpgraphEditor getEditor() {
		return this.editorRef.get();
	}
	
}
