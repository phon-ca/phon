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
