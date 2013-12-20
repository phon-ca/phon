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
package ca.phon.app;

import java.io.File;

import javax.imageio.ImageIO;

import ca.phon.ui.nativedialogs.OSInfo;

/**
 * Starts the Phon application along with a fast Phon splash screen.
 * 
 *
 */
public class PhonSplasher {
	
	public static void main(String[] args) throws Exception {
		BootWindow.splash(ImageIO.read(new File("data/phonboot.png")));
		if(!OSInfo.isMacOs())
			BootWindow.invokeMainInNewProcess("ca.phon.application.main.Phon", args);
		else
			BootWindow.invokeMain("ca.phon.application.main.Phon", args);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
		BootWindow.disposeSplash();
		
		if(!OSInfo.isMacOs())
			System.exit(0);
	}

}
