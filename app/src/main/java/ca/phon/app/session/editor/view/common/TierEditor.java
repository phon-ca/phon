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
package ca.phon.app.session.editor.view.common;

import java.util.List;

import javax.swing.JComponent;

/**
 * Interface used to load tier editors.
 */
public interface TierEditor {

	/**
	 * Get the editor component
	 * 
	 * @return component
	 */
	public JComponent getEditorComponent();
	
	/**
	 * Add tier editor listener
	 */
	public void addTierEditorListener(TierEditorListener listener);
	
	/**
	 * remove tier editor listener
	 */
	public void removeTierEditorListener(TierEditorListener listener);
	
	/**
	 * Get tier editor listeners
	 */
	public List<TierEditorListener> getTierEditorListeners();
	
}