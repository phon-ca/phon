/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.JTextComponent;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;


@PhonPlugin(name="default")
public class CutEP implements IPluginEntryPoint {

	private final static Logger LOGGER = Logger.getLogger(CutEP.class.getName());
	
	private final static String EP_NAME = "Cut";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	private void begin() {
		//		 copy text from the component with keyboard focus
		Component keyboardComp = 
			KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
		
		if(keyboardComp != null && keyboardComp instanceof JTextComponent) {
			JTextComponent textComp = (JTextComponent)keyboardComp;
			textComp.cut();
		} else {
			// if it was not a text component, see if we have the cut
			// method available
			Method cutMethod = null;
			try {
				cutMethod = keyboardComp.getClass().getMethod("cut", new Class[0]);
			} catch (SecurityException ex) {
				LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
			} catch (NoSuchMethodException ex) {
				LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
			}
			
			if(cutMethod != null) {
				try {
					cutMethod.invoke(keyboardComp, new Object[0]);
				} catch (IllegalArgumentException ex) {
					LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
				} catch (IllegalAccessException ex) {
					LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
				} catch (InvocationTargetException ex) {
					LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
				}
			}
		}
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		begin();
	}
}
