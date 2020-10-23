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
package ca.phon.app.actions;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.text.*;

import ca.phon.plugin.*;

@PhonPlugin(name="default")
public class PasteEP implements IPluginEntryPoint {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PasteEP.class.getName());

	private final static String EP_NAME = "Paste";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	private void begin() {
		Component keyboardComp = 
			KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
		if(keyboardComp == null) return;
		
		if(keyboardComp instanceof JTextComponent) {
			JTextComponent textComp = (JTextComponent)keyboardComp;
			textComp.paste();
		} else {
			// if it was not a text component, see if we have the cut
			// method available
			Method pasteMethod = null;
			try {
				pasteMethod = keyboardComp.getClass().getMethod("paste", new Class[0]);
			} catch (SecurityException ex) {
				LOGGER.error( ex.getMessage(), ex);
			} catch (NoSuchMethodException ex) {
				LOGGER.error( ex.getMessage(), ex);
			}
			
			if(pasteMethod != null) {
				try {
					pasteMethod.invoke(keyboardComp, new Object[0]);
				} catch (IllegalArgumentException ex) {
					LOGGER.error( ex.getMessage(), ex);
				} catch (IllegalAccessException ex) {
					LOGGER.error( ex.getMessage(), ex);
				} catch (InvocationTargetException ex) {
					LOGGER.error( ex.getMessage(), ex);
				}
			}
		}
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		begin();
	}

}
