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
package ca.phon.extensions;

/**
 * Utility methods for working with extensions.
 */
public class ExtensionUtils {
	
	/**
	 * Checks if the given object implements {@link IExtendable}
	 * 
	 * @return <code>true</code> if the object implements {@link IExtendable}, <code>false</code>
	 *  otherwise
	 */
	public static boolean isExtendable(Object obj) {
		return (obj instanceof IExtendable);
	}
	
	/**
	 * Check if the given object has the given extension.
	 * 
	 * @param obj
	 * @param ext
	 * @return <code>true</code> if the extension exists, <code>false</code> otherwise
	 */
	public static boolean hasExtension(Object obj, Class<?> ext) {
		boolean retVal = false;
		if(isExtendable(obj)) {
			retVal = (getExtension(obj, ext) != null);
		}
		return retVal;
	}
	
	/**
	 * Get the specified extension if it exists
	 * 
	 * @param obj
	 * @param ext
	 * 
	 * @return the specified extension or <code>null</code> if the
	 *  extension is not available or the object is not extendable
	 */
	public static <T> T getExtension(Object obj, Class<T> ext) {
		T retVal = null;
		if(isExtendable(obj)) {
			final IExtendable extObj = (IExtendable)obj;
			retVal = extObj.getExtension(ext);
		}
		return retVal;
	}

	/**
	 * Put the specified extention in the given object.
	 * 
	 * @param obj
	 * @param ext
	 * @param extObj
	 * 
	 * @return the previous value of the ext or <code>null</code> if not
	 *  found
	 */
	public static <T> T putExtension(Object obj, Class<T> ext, T impl) {
		T retVal = null;
		if(isExtendable(obj)) {
			final IExtendable extObj = (IExtendable)obj;
			retVal = extObj.putExtension(ext, impl);
		}
		return retVal;
	}
	
}
