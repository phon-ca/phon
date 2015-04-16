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
