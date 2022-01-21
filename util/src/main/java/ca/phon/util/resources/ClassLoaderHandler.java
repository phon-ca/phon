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
package ca.phon.util.resources;

import java.io.*;
import java.net.*;
import java.util.*;

import ca.phon.extensions.*;
import org.apache.commons.lang3.*;

/**
 * Loads resources from a specified class loader (or the
 * default class loader if not specified.)
 * 
 * Resources are located using {@link ClassLoader#getResources(String)}
 * for each specified resource.
 */
public abstract class ClassLoaderHandler<T> extends URLHandler<T> {
	
	private static final org.apache.logging.log4j.Logger LOGGER = 
			org.apache.logging.log4j.LogManager.getLogger(ClassLoaderHandler.class.getName());
	
	/**
	 * Class loader
	 */
	private ClassLoader classLoader = getClass().getClassLoader();
	
	/**
	 * Resources to load
	 */
	public List<String> resourcePaths = new ArrayList<String>();
	
	/**
	 * Constructor
	 */
	public ClassLoaderHandler() {}
	
	/**
	 * Constructor
	 * 
	 * @param cl the classloader to use
	 */
	public ClassLoaderHandler(ClassLoader cl) {
		this.classLoader = cl;
	}
	
	/**
	 * Add a resource to locate
	 * 
	 * @param res the resource to locate.  Use the naming rules
	 *  outline by {@link ClassLoader#getResource(String)}.
	 */
	public void addResource(String res) {
		this.resourcePaths.add(res);
	}
	
	/**
	 * Load resource list from the give resource file.
	 * 
	 * @param file
	 */
	public void loadResourceFile(String resFile) {
		try {
			final Enumeration<URL> resURLS = getClassLoader().getResources(resFile);
			while(resURLS.hasMoreElements()) {
				final URL resURL = resURLS.nextElement();
				
				// open file and read in one url at a time
				final BufferedReader in =
						new BufferedReader(new InputStreamReader(resURL.openStream()));
				String line = null;
				while((line = in.readLine()) != null) {
					final String resVal = StringUtils.strip(line);
					if(resVal.length() > 0) {
						addResource(resVal);
					}
				}
				in.close();
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}
	
	/**
	 * Remove resource
	 * 
	 * @param res
	 */
	public void removeResource(String res) {
		this.resourcePaths.remove(res);
	}
	
	public List<String> getResourcePaths() {
		return this.resourcePaths;
	}

	@Override
	public Iterator<T> iterator() {
		super.getURLS().clear();
		for(String res:resourcePaths) {
			
			try {
				Enumeration<URL> resURLS = getClassLoader().getResources(res);
				while(resURLS.hasMoreElements()) {
					super.add(resURLS.nextElement());
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage());
			}
			
		}
		
		return super.iterator();
	}
	
	/**
	 * Get the class loader used by this handler.
	 * 
	 * @return ClassLoader
	 */
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}
	
	/**
	 * Set the class loader used by this handler.
	 * 
	 * @param cl the new class loader to use, if <code>null</code>
	 *  the default system classloader is used
	 */
	public void setClassLoader(ClassLoader cl) {
		this.classLoader = 
				(cl == null ? getClass().getClassLoader() : cl);
	}

}
