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

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public class OpenFileLauncher {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(OpenFileLauncher.class.getName());

	public static boolean openURL(URL url) {
		if(OSInfo.isMacOs()) {
			try {
				Runtime.getRuntime().exec( new String[] {
				        "open",
				        url.toString()
				     });
			} catch (IOException e) {
				LOGGER.warn(e.getMessage());
				return false;
			}
		} else if(OSInfo.isWindows()) {
			try {
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " +
						url.toString());
			} catch (IOException e) {
				LOGGER.warn(e.getMessage());
				return false;
			}
		} else if(OSInfo.isNix()) {
			try {
				Runtime.getRuntime().exec( new String[] {
						"xdg-open", url.toString() } );
				
			} catch(IOException e) {
				LOGGER.warn(e.getMessage());
				return false;
			}
		} else {
			LOGGER.error("Unsupported OS");
			return false;
		}
		return true;
	}
}
