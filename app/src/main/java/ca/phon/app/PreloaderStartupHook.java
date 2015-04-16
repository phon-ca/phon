/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;

public class PreloaderStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	private static final Logger LOGGER = Logger
			.getLogger(PreloaderStartupHook.class.getName());
	
	private final static String PRELOAD_FILE = "META-INF/preload";
	
	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return factory;
	}

	@Override
	public void startup() throws PluginException {
		try {
			preloadClasses();
		} catch (IOException e) {
			throw new PluginException(e);
		} catch (ClassNotFoundException e) {
			throw new PluginException(e);
		}
	}
	
	private void preloadClasses() throws IOException, ClassNotFoundException {
		final Enumeration<URL> preloadURLS = getClass().getClassLoader().getResources(PRELOAD_FILE);
		while(preloadURLS.hasMoreElements()) {
			final URL preloadURL = preloadURLS.nextElement();
			final InputStream is = preloadURL.openStream();
			final InputStreamReader reader = new InputStreamReader(is);
			final BufferedReader in = new BufferedReader(reader);
			
			String line = null;
			while((line = in.readLine()) != null) {
				LOGGER.info("Preloading class " + line);
				getClass().getClassLoader().loadClass(line);
			}
			in.close();
		}
		
		// preload swingx renderers
		final Runnable onEDT = new Runnable() {
			
			@Override
			public void run() {
				final JXTable tbl = new JXTable();
				final JXList list = new JXList();
			}
			
		};
		SwingUtilities.invokeLater(onEDT);
		
	}
	
	private final IPluginExtensionFactory<PhonStartupHook> factory = new IPluginExtensionFactory<PhonStartupHook>() {
		
		@Override
		public PhonStartupHook createObject(Object... args) {
			return PreloaderStartupHook.this;
		}
	};

}
