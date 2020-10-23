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
package ca.phon.plugin;

import java.awt.*;

import javax.swing.*;

/**
 * A plugin extension point for controlling the window menu
 * for all Phon windows.  The provided filter will be called
 * to generate the appropriate window menu entries as needed
 * by the plugin.
 * 
 * 
 */
public interface IPluginMenuFilter {

	/**
	 * Add/remove/edit the window menu
	 * as needed by the implementing plugin.
	 * 
	 * @param owner the window which owns the menu
	 *  (may be null)
	 * @param menu the menu in question
	 */
	public void filterWindowMenu(Window owner, JMenuBar menu);
	
}
