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

import ca.phon.app.hooks.PhonShutdownHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import javafx.application.Platform;

public class JavaFXShutdownHook implements PhonShutdownHook, IPluginExtensionPoint<PhonShutdownHook> {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(JavaFXShutdownHook.class.getName());
	
	@Override
	public Class<?> getExtensionType() {
		return PhonShutdownHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonShutdownHook> getFactory() {
		return (args) -> this;
	}

	@Override
	public void shutdown() throws PluginException {
		LOGGER.info("Shutdown JavaFX");
		Platform.runLater( Platform::exit );
	}

}
