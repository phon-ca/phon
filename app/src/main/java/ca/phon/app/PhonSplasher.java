/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app;

import java.io.InputStream;

import javax.imageio.ImageIO;

import ca.phon.util.PrefHelper;

/**
 * Starts the Phon application along with a fast Phon splash screen.
 * 
 *
 */
public class PhonSplasher {
	
	/**
	 * Property used to load splash image
	 */
	public final static String SPLASH_IMAGE_PROPERTY = 
			PhonSplasher.class.getName() + ".splashImage";
	
	/**
	 * Default location of splash image
	 */
	private final static String DEFAULT_SPLASH_IMAGE = "data/phonboot.png";
	
	/**
	 * Property for boot class
	 */
	public final static String BOOT_CLASS_PROPERTY =
			PhonSplasher.class.getName() + ".bootClass";
	
	public final static String BOOT_FORK_PROPERTY =
			PhonSplasher.class.getName() + ".fork";
	
	public final static String IS_FORKED_PROPERTY =
			PhonSplasher.class.getName() + ".isForked";
	
	/**
	 * Default boot class 
	 */
	public final static String DEFAULT_BOOT_CLASS = Main.class.getName();
	
	public static void main(String[] args) throws Exception {
		final String splashImage = PrefHelper.get(SPLASH_IMAGE_PROPERTY, DEFAULT_SPLASH_IMAGE);
		final InputStream splashStream = PhonSplasher.class.getClassLoader().getResourceAsStream(splashImage);
		
		final String bootClass = PrefHelper.get(BOOT_CLASS_PROPERTY, DEFAULT_BOOT_CLASS);
		
		BootWindow.splash(ImageIO.read(splashStream));
		final boolean fork = PrefHelper.getBoolean(BOOT_FORK_PROPERTY, Boolean.FALSE);
		if(fork)
			BootWindow.invokeMainInNewProcess(PhonSplasher.class.getName(), args);
		else
			BootWindow.invokeMain(bootClass, args);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {}
		BootWindow.disposeSplash();
		
		if(fork) 
			System.exit(0);
	}

	public static boolean isForked() {
		return System.getProperty(IS_FORKED_PROPERTY, "false").equalsIgnoreCase("true");
	}
}
