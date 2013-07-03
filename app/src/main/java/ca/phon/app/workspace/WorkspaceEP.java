/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.workspace;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;


@PhonPlugin(name="default")
public class WorkspaceEP implements IPluginEntryPoint {

	private static WorkspaceDialog __startDialog;
//	
//	private static WorkspaceDialog getDialog() {
//		if(__startDialog == null) {
//			__startDialog = new WorkspaceDialog();
//		}
//		return __startDialog;
//	}
	
	private final static String EP_NAME = "Workspace";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		WorkspaceDialog startDialog = __startDialog;
		if(startDialog == null) {
			startDialog = new WorkspaceDialog();
			startDialog.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosed(WindowEvent e) {
					__startDialog = null;
				}
				
			});
			__startDialog = startDialog;
		}
		
		if(!startDialog.isShowing()) {
			// display start dialog
			startDialog.setSize(new Dimension(725, 600));
//			startDialog.centerWindow();
			startDialog.setLocationByPlatform(true);
			
			if(!startDialog.isVisible())
				startDialog.setVisible(true);
			else
				startDialog.requestFocus();
		} else {
			startDialog.toFront();
			startDialog.requestFocus();
		}
	}

}
