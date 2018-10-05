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

import java.io.IOException;
import java.net.URL;

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
