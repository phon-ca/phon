/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.project.actions;

import java.lang.ref.WeakReference;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.project.ProjectWindow;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

public abstract class ProjectWindowAction extends HookableAction {

	private static final long serialVersionUID = 7949135760405306345L;

	private final WeakReference<ProjectWindow> projectWindowRef;
	
	public ProjectWindowAction(ProjectWindow projectWindow) {
		super();
		this.projectWindowRef = new WeakReference<ProjectWindow>(projectWindow);
	}
	
	public ProjectWindow getWindow() {
		return this.projectWindowRef.get();
	}

	protected void showMessage(String msg1, String msg2) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(MessageDialogProperties.okOptions);
		props.setHeader(msg1);
		props.setTitle(msg1);
		props.setMessage(msg2);
		props.setParentWindow(getWindow());
		
		NativeDialogs.showDialog(props);
	}
	
}
