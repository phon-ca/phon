/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

import vlc4j.VLCInstance;
import ca.phon.gui.CommonModuleFrame;
import ca.phon.util.NativeDialogs;

/**
 * Helper methods for using vlc4j
 */
public class VLCHelper {

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
		boolean retVal = false;

//		try {
		retVal = VLCInstance.isLibraryLoaded();
//		} catch (VLCException e) {
//			PhonLogger.warning(e.getMessage());
//		}
		if(!retVal && showError) {
			String msg1 = "Could not load VLC";
			String msg2 = "Native library for VLC failed to load. Reason given: ";
			msg2 += VLCInstance.getLibraryLoadError();

			NativeDialogs.showMessageDialogBlocking(
					null, null, msg1, msg2);
		}

		return retVal;
	}


}
