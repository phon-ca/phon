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
package ca.phon.util;

/**
 * Utility class for determining host OS.
 */
public class OSInfo {
	
	public static enum OSType {
		MAC,
		NIX,
		WINDOWS,
		OTHER;
	};
	
	/**
	 * Field containing the host OS type
	 */
	public final static OSType OS_TYPE = getOSType();
	
	/**
	 * Get the host os type.
	 * 
	 * @return the host os type, or <code>OSType.OTHER</code>
	 *  is not detected
	 */
	public static OSType getOSType() {
		OSType retVal = OSType.OTHER;
		
		if(isMacOs()) {
			retVal = OSType.MAC;
		} else if(isWindows()) {
			retVal = OSType.WINDOWS;
		} else if(isNix()) {
			retVal = OSType.NIX;
		}
		
		return retVal;
	}
	
	/**
	 * Is the host os Mac OS?
	 * @return <code>true</code> if system property 'mrj.version' 
	 *  is set <code>false</code> otherwise.
	 */
	public static boolean isMacOs() {
		String osname = System.getProperty("os.name");
		
		if(osname.toLowerCase().indexOf("mac") >= 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Is the host os windows?
	 * 
	 * @return <code>true</code> if system property 'os.name'
	 *  contains the string 'windows', <code>false</code> otherwise
	 */
	public static boolean isWindows() {
		String osname = System.getProperty("os.name");
		
		if(osname.toLowerCase().indexOf("windows") >= 0) 
			return true;
		else
			return false;
	}
	
	/**
	 * Is the host os *nix?
	 * @return <code>true</code> if system property 'os.name'
	 *  contains the string 'nix' or 'nux', <code>false</code> otherwise
	 */
	public static boolean isNix() {
		String osname = System.getProperty("os.name");
		
		int nixIdx = osname.indexOf("nix");
		int nuxIdx = osname.indexOf("nux");
		
		if(nixIdx >= 0 || nuxIdx >= 0)
			return true;
		else
			return false;
	}

}
