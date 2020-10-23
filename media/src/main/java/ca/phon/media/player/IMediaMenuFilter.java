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

package ca.phon.media.player;

import javax.swing.*;

/**
 * Adds/removes entries from the media menu.
 * Many menu filters can be added to the media
 * player to provide customized functionality
 * in the menu.
 */
public interface IMediaMenuFilter {

	/**
	 * Make changes to the given menu.
	 *
	 * @param menu the menu
	 * @return the modified menu
	 */
	public JPopupMenu makeMenuChanges(JPopupMenu menu);

}
