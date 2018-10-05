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
package ca.phon.app;

import javax.swing.SwingUtilities;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class JavaFXStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(JavaFXStartupHook.class.getName());

	@Override
	public void startup() throws PluginException {
		LOGGER.info("Setting JavaFX Platform implicit exit to false");
		Platform.setImplicitExit(false);
		
		Runnable onEDT = () -> {
			LOGGER.info("Initializing JavaFX");
			// initialized JavaFX at application startup
			@SuppressWarnings("unused")
			final JFXPanel panel = new JFXPanel();
		};
		SwingUtilities.invokeLater(onEDT);
	}

	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return (args) -> this;
	}

}
