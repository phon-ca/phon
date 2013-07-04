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
package ca.phon.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * Reads the plugin def xml files and provides methods
 * for adding plugin defined menu entries to window menus.
 *
 */
public class PluginManager extends URLClassLoader {
	
	private final static Logger LOGGER = Logger.getLogger(PluginManager.class.getName());
	
	/**
	 * Plugin folder
	 */
	private final static String PLUGIN_FOLDER = "plugins";
	
	/**
	 * Singleton instance
	 */
	private static PluginManager _instance;
	
	/**
	 * Get singleton instance
	 */
	public static PluginManager getInstance() {
		if(_instance == null) 
			_instance = new PluginManager(new URL[0]);
		return _instance;
	}
	
	/**
	 * Constructor
	 */
	private PluginManager(URL[] urls) {
		super(urls);
	}
	
	/**
	 * Tell the plugin manager to scan plugins folder.
	 * 
	 */
	public void scanPluginFolder() {
		scanPluginFolder(new File(PLUGIN_FOLDER));
	}
	
	public void scanPluginFolder(File pluginFolder) {
		scanFolderForJars(pluginFolder, true);
	}
	
	/**
	 * Add a zip/jar file to the dynamic class loader
	 * 
	 * @param path
	 * @throws MalformedURLException
	 */
	public void addFile (String path) 
		throws MalformedURLException {
		// add jar to classpath
		String urlPath = "jar:file://" + path + "!/";
		LOGGER.info("Adding " + urlPath + " to dynamic classpath");
		addURL (new URL (urlPath));
	}
	
	private void scanFolderForJars(File dir, boolean recursive) {
		if(dir.isDirectory()) {
			for(File f:dir.listFiles()) {
				if(f.isDirectory() && recursive) {
					scanFolderForJars(f, recursive);
				} else if(f.isFile()) {
					if(f.getName().endsWith(".jar") ||
							f.getName().endsWith(".zip")) {
						try {
							LOGGER.info("Adding archive to dynamic class loader: " + f.getAbsolutePath());
							addFile(f.getAbsolutePath());
							
						} catch (MalformedURLException e) {
							LOGGER.severe("Could not add archive to dynamic class loader: " + f.getAbsolutePath());
						}
					}
				}
			}
		}
	}
	
	/**
	 * Get entry point names
	 * 
	 * @return list of entry point ids
	 */
	public List<String> getEntryPoints() {
		List<String> epIds = new ArrayList<String>();
		
		ServiceLoader<IPluginEntryPoint> entryPointProvider = 
			ServiceLoader.load(IPluginEntryPoint.class, this);
		Iterator<IPluginEntryPoint> epItr = entryPointProvider.iterator();
		while(epItr.hasNext()) {
			IPluginEntryPoint pluginEp = epItr.next();
			
			try {
				checkVersionInfo(pluginEp.getClass());
				String epId = pluginEp.getName();
				
				epIds.add(epId);
			} catch (PluginException e) {
				LOGGER.warning(e.getMessage());
				e.printStackTrace();
			}
		}
		
		Collections.sort(epIds);
		return epIds;
	}
	
	/**
	 * Get the entry point identified by the
	 * given plugin id.
	 * 
	 * @param name the name of the entry point
	 * to retrieve
	 * @return the plugin entry point or NULL if
	 * not found
	 */
	public IPluginEntryPoint getEntryPoint(String name) {
		IPluginEntryPoint retVal = null;
		
		ServiceLoader<IPluginEntryPoint> entryPointProvider = 
			ServiceLoader.load(IPluginEntryPoint.class, this);
		Iterator<IPluginEntryPoint> epItr = entryPointProvider.iterator();
		while(epItr.hasNext()) {
			IPluginEntryPoint pluginEp = epItr.next();
			
			try {
				checkVersionInfo(pluginEp.getClass());
				String epId = pluginEp.getName();
				
				if(epId.equals(name)) {
					retVal = pluginEp;
					break;
				}
			} catch (PluginException e) {
				LOGGER.warning(e.getMessage());
				e.printStackTrace();
			}
		}
		
		return retVal;
	}
	
//	/**
//	 * Get static arguments to an entry point as
//	 * defined in the plugin's xml file.
//	 * 
//	 * @param name of the entry point
//	 * @return map of static arguments
//	 */
//	public Map<String, Object> getEntryPointStaticArgs(String name) {
//		Map<String, Object> retVal = 
//			new HashMap<String, Object>();
//		
//		for(PluginInfoType pinfo:pluginData) {
//			for(EntryPointType entryPt:pinfo.getPluginep()) {
//				if(entryPt.getId().equalsIgnoreCase(name)) {
//					
//					for(ArgType arg:entryPt.getBoolOrIntOrString()) {
//						if(arg instanceof BooleanArgType) {
//							BooleanArgType barg = (BooleanArgType)arg;
//							
//							retVal.put(barg.getName(), new Boolean(barg.isValue()));
//						} else if(arg instanceof IntArgType) {
//							IntArgType iarg = (IntArgType)arg;
//							
//							retVal.put(iarg.getName(), new Integer(iarg.getValue()));
//						} else if(arg instanceof StringArgType) {
//							StringArgType sarg = (StringArgType)arg;
//							
//							retVal.put(sarg.getName(), sarg.getValue());
//						} else {
//							PhonLogger.warning(getClass(), "Invalid plugin argument type: " + arg.getName());
//						}
//					}
//					
//				}
//			}
//		}
//		
//		return retVal;
//	}
	
	/**
	 * Get initilized extension points for the given class.
	 * This method should only be used when there is a default
	 * constructor available for the given class.
	 * 
	 * @param clazz 
	 * 
	 * @throws PluginException if there is a problem with
	 *  creating the list of objects.
	 */
	public <T> List<T> getExtensions(Class<T> clazz) 
		throws PluginException {
		final List<T> retVal = new ArrayList<T>();
		
		final List<IPluginExtensionPoint<T>> extPts = 
				getExtensionPoints(clazz);
		for(IPluginExtensionPoint<T> extPt:extPts) {
			final IPluginExtensionFactory<T> factory = extPt.getFactory();
			try {
				final T obj = factory.createObject();
				retVal.add(obj);
			} catch (Exception e) {
				throw new PluginException(e);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Get the extension points for the given class.
	 * 
	 * @param clazz the class for extensions
	 * @return the list of loaded extension points for the
	 * given class
	 */
	public <T> List<IPluginExtensionPoint<T>> getExtensionPoints(Class<T> clazz) {
		List<IPluginExtensionPoint<T>> retVal = 
			new ArrayList<IPluginExtensionPoint<T>>();
		
		ServiceLoader<IPluginExtensionPoint> extPtProvider = 
			ServiceLoader.load(IPluginExtensionPoint.class, this);
		Iterator<IPluginExtensionPoint> extPtItr = 
			extPtProvider.iterator();
		
		while(extPtItr.hasNext()) {
			IPluginExtensionPoint<?> extPt = 
				(IPluginExtensionPoint<?>)extPtItr.next();
			
			try {
				checkVersionInfo(extPt.getClass());
				Class<?> extPtClass = extPt.getExtensionType();
				
				if(extPtClass == clazz) {
					retVal.add((IPluginExtensionPoint<T>)extPt);
				}
			} catch (PluginException e) {
				LOGGER.warning(e.getMessage());
				e.printStackTrace();
			}
		}
		
		return retVal;
	}
	
	/**
	 * Checks the PhonPlugin annotation of the given
	 * class to ensure minimum version of Phon is met.
	 * 
	 * @param clazz
	 * @throws PhonException if the given class does
	 * not have a PhonPlugin annotation or the version 
	 * information does not match
	 */
	private void checkVersionInfo(Class<?> clazz)
		throws PluginException {
		// TODO fix me
//		// check for the PhonPlugin annotation
//		PhonPlugin pluginAnnotation = clazz.getAnnotation(PhonPlugin.class);
//		if(pluginAnnotation == null) {
//			throw new PluginException("[PluginManager] Class " + 
//					clazz.getName() + " must declare the @PhonPlugin annotation.");
//		}
//		
//		String minVersion = pluginAnnotation.minPhonVersion();
//		String currentVersion = VersionInfo.getInstance().getVersion();
//		
//		if(currentVersion.compareTo(minVersion) < 0) {
//			throw new PluginException("[PluginManager] Class " + 
//					clazz.getName() + " requires Phon version " + minVersion + 
//					". Current version is " + currentVersion + ".");
//		}
	}
}
