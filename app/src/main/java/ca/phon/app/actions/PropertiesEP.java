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
package ca.phon.app.actions;

import java.util.Map;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.ui.CommonModuleFrame;

/**
 * Application properties module.
 * 
 */
@PhonPlugin(name="default")
public class PropertiesEP implements IPluginEntryPoint {
	
	private final static String EP_NAME = "Properties";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	@Override
	public void pluginStart(Map<String, Object> mi) {
		CommonModuleFrame currentFrame = CommonModuleFrame.getCurrentFrame();
		
		PrefsDialog dlg = null;
		if(currentFrame != null)
			dlg = new PrefsDialog(currentFrame);
		else
			dlg = new PrefsDialog();
		dlg.setVisible(true);
		dlg.setModal(true);

		dlg.pack();
		if(currentFrame != null)
			dlg.setLocationRelativeTo(currentFrame);
		
		if(mi.containsKey("prefpanel")) {
			dlg.setActiveTab(mi.get("prefpanel").toString());
		}
		
		dlg.setVisible(true);
	}
}