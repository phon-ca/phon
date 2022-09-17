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
package ca.phon.ui.tristatecheckbox;

import javax.swing.event.TreeModelEvent;

/**
 * {@link TreeModelEvent} sent when changes to checking paths for a node changes.
 * 
 *
 */
public class TristateCheckBoxTreeModelEvent extends TreeModelEvent {
	
	private static final long serialVersionUID = -1250202341580525562L;

	private TristateCheckBoxState state;
	
	public TristateCheckBoxTreeModelEvent(Object source, Object[] path, TristateCheckBoxState state) {
		this(source, path, null, null, state);
	}
	
	public TristateCheckBoxTreeModelEvent(Object source, Object[] path, int[] childIndices, Object[] children, TristateCheckBoxState state) {
		super(source, path, childIndices, children);
		this.state = state;
	}
	

	public TristateCheckBoxState getState() {
		return this.state;
	}
	
}
