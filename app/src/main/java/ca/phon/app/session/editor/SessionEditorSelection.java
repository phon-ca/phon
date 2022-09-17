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
package ca.phon.app.session.editor;

import ca.phon.extensions.*;
import ca.phon.util.Range;

import javax.swing.text.Highlighter.HighlightPainter;
import java.util.Set;

/**
 * Selection information used for {@link EditorSelectionModel}.
 * Custom highlighting can be obtained by attaching an extension
 * of type {@link HighlightPainter}
 */
public class SessionEditorSelection implements IExtendable {
	
	private final int recordIndex;
	
	private final String tierName;
	
	private final int groupIndex;
	
	private final Range groupRange;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(SessionEditorSelection.class, this);

	public SessionEditorSelection(int recordIndex, String tierName,
			int groupIndex, Range groupRange) {
		super();
		this.recordIndex = recordIndex;
		this.tierName = tierName;
		this.groupIndex = groupIndex;
		this.groupRange = groupRange;
		
		extSupport.initExtensions();
	}

	public int getRecordIndex() {
		return recordIndex;
	}

	public String getTierName() {
		return tierName;
	}

	public int getGroupIndex() {
		return groupIndex;
	}

	public Range getGroupRange() {
		return groupRange;
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
}
