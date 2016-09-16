/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
