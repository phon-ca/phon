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
package ca.phon.app;

import java.util.logging.Logger;

import ca.phon.app.hooks.PhonShutdownHook;
import ca.phon.plugin.*;
import javafx.application.Platform;

public class JavaFXShutdownHook implements PhonShutdownHook, IPluginExtensionPoint<PhonShutdownHook> {

	private final static Logger LOGGER = Logger.getLogger(JavaFXShutdownHook.class.getName());
	
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
