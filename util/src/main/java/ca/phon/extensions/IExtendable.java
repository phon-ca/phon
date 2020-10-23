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
package ca.phon.extensions;

import java.util.*;

/**
 * Adds the ability to add/remove capabilites to an
 * object that implements the ICapable interface.
 * 
 * 
 */
public interface IExtendable {

	/**
	 * Return all extension types supported
	 * 
	 */
	public Set<Class<?>> getExtensions();

	/**
	 * Get the requested extension if available.
	 * 
	 * @param class of the requested capability
	 * @return the capability object or <code>null</code> if
	 *  the cability is not available
	 */
	public <T> T getExtension(Class<T> cap);
	
	/**
	 * Add a new extension.
	 * 
	 * @param cap the extension to add
	 * @return the added extension implementation
	 */
	public <T> T putExtension(Class<T> cap, T impl);
	
	/**
	 * Remove a capability.
	 * 
	 * @param cap the capability to remove
	 */
	public <T> T removeExtension(Class<T> cap);
	
}
