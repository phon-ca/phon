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

package ca.phon.media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import ca.phon.ui.nativedialogs.OSInfo;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PrefHelper;
import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.support.Info;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

/**
 * Helper methods for using vlc4j
 */
public class VLCHelper {
	
	/**
	 * Property for the user-set location of VLC
	 */
	public final static String VLC_LOCATION = VLCHelper.class.getName() + ".vlcLocation";
	
	public final static String VLC_PLUGIN_PATH = VLCHelper.class.getName() + ".vlcPluginPath";
	
	private final static String VLC_LOCATION_WIN = System.getenv("ProgramFiles") + "\\VideoLAN\\VLC";
	
	private final static String VLC_PLUGIN_PATH_WIN = VLC_LOCATION_WIN + "\\plugins";
	
	private final static String VLC_LOCATION_MAC = "/Applications/VLC.app/Contents/MacOS/lib";
	
	private final static String VLC_PLUGIN_PATH_MAC = "/Applications/VLC.app/Contents/MacOS/plugins";
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(VLCHelper.class
			.getName());
	
	private static volatile boolean isLoaded = false;

	/**
	 * Check loading of the native library.
	 *
	 * @param showError if <code>true</code> will display
	 * a message to the user in a dialog if the library
	 * failed to load
	 *
	 * @return <code>true</code> if the library was
	 * loaded correctly.  <code>false</code> otherwise.
	 *
	 */
	public static boolean checkNativeLibrary(boolean showError) {
		if(!isLoaded) {
			try {
				String vlcLocationDefault = new String();
				String vlcPluginPathDefault = null;
				// attempt to load native libraries
				if(OSInfo.isMacOs()) {
					vlcLocationDefault = VLC_LOCATION_MAC;
					vlcPluginPathDefault = VLC_PLUGIN_PATH_MAC;
				} else if (OSInfo.isWindows()) {
					vlcLocationDefault = VLC_LOCATION_WIN;
					vlcPluginPathDefault = VLC_PLUGIN_PATH_WIN;
				} else if (OSInfo.isNix()) {
					// libvlc should be in /usr/lib and already included in LD_LIBRARY_PATH
					// on most systems
				}
				final String vlcLocation = PrefHelper.get(VLC_LOCATION, vlcLocationDefault);
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLocation);
				
				final String vlcPluginPath = PrefHelper.get(VLC_PLUGIN_PATH, vlcPluginPathDefault);

				if(vlcPluginPath != null && !OSInfo.isWindows()) {
					LibC.INSTANCE.setenv("VLC_PLUGIN_PATH", vlcPluginPath, 1);
				}
				
				// print info to logger
				LOGGER.info("Using vlcj " + Info.getInstance().vlcjVersion());
				LibVlcVersion libvlcVersion = new LibVlcVersion();
				LOGGER.info("Using libvlc " + libvlcVersion.getVersion());
				isLoaded = true;
			} catch (UnsatisfiedLinkError e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				if(showError)
					ToastFactory.makeToast(e.getLocalizedMessage()).start();
			}
		}

		return isLoaded;
	}
	
}
