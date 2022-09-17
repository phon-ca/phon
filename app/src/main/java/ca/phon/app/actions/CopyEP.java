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
package ca.phon.app.actions;

import ca.phon.plugin.*;

import javax.swing.FocusManager;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.*;
import java.util.Map;

@PhonPlugin(name="default")
public class CopyEP implements IPluginEntryPoint {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(CopyEP.class.getName());

	private final static String EP_NAME = "Copy";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	private void begin() {
		Component focusedComp = FocusManager.getCurrentManager().getFocusOwner();
		if(focusedComp instanceof  JComponent) {
			Action copyAct = ((JComponent) focusedComp).getActionMap().get("copy");
			if(copyAct != null) {
				copyAct.actionPerformed(new ActionEvent(this, -1, "copy"));
				return;
			} else {
				// copy text from the component with keyboard focus
				Component keyboardComp =
						KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
				if(keyboardComp == null) return;

				if(keyboardComp instanceof JTextComponent) {
					JTextComponent textComp = (JTextComponent)keyboardComp;
					textComp.copy();
				} else {
					// if it was not a text component, see if we have the cut
					// method available
					Method copyMethod = null;
					try {
						copyMethod = keyboardComp.getClass().getMethod("copy", new Class[0]);
					} catch (SecurityException ex) {
						LOGGER.error( ex.getMessage(), ex);
					} catch (NoSuchMethodException ex) {
						LOGGER.error( ex.getMessage(), ex);
					}

					if(copyMethod != null) {
						try {
							copyMethod.invoke(keyboardComp, new Object[0]);
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
		}


	}

	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		begin();
	}
}
