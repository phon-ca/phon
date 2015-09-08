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

package ca.phon.media;

import java.util.logging.Level;
import java.util.logging.Logger;

import uk.co.caprica.vlcj.Info;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.version.LibVlcVersion;
import ca.phon.ui.nativedialogs.OSInfo;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PrefHelper;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

/**
 * Helper methods for using vlc4j
 */
public class VLCHelper {
	
	/**
	 * Property for the user-set location of VLC
	 */
	public final static String VLC_LOCATION = VLCHelper.class.getName() + ".vlcLocation";
	
	private final static String VLC_LOCATION_WIN = System.getenv("ProgramFiles") + "\\VideoLAN\\VLC";
	
	private final static String VLC_LOCATION_MAC = "/Applications/VLC.app/Contents/MacOS/lib";
	
	private final static Logger LOGGER = Logger.getLogger(VLCHelper.class
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
				// attempt to load native libraries
				if(OSInfo.isMacOs()) {
					vlcLocationDefault = VLC_LOCATION_MAC;
				} else if (OSInfo.isWindows()) {
					vlcLocationDefault = VLC_LOCATION_WIN;
				} else if (OSInfo.isNix()) {
					// libvlc should be in /usr/lib and already included in LD_LIBRARY_PATH
					// on most systems
				}
				final String vlcLocation = PrefHelper.get(VLC_LOCATION, vlcLocationDefault);
				NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLocation);
				Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
				isLoaded = true;
				
				// print info to logger
				LOGGER.info("Using vlcj " + Info.getInstance().version());
				LOGGER.info("Found libVLC " + LibVlcVersion.getVersion() + " at " + vlcLocation);
			} catch (UnsatisfiedLinkError e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				if(showError)
					ToastFactory.makeToast(e.getLocalizedMessage()).start();
			}
		}

		return isLoaded;
	}
	
}
