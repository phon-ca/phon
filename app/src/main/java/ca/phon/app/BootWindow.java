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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import ca.phon.app.hooks.PhonBootHook;
import ca.phon.plugin.PluginException;
import ca.phon.plugin.PluginManager;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.nativedialogs.OSInfo;

public class BootWindow extends Window {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(BootWindow.class.getName());
	
	private Image bootImage;
	
	public BootWindow(Frame p, Image img) {
		super(p);
		
		bootImage = img;
		
		setBackground(new Color(0,0,0,0));
		
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(bootImage, 0);
		
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			LOGGER.error( e.getMessage(), e);
		}
		
		init();
	}

	private void init() {
		setLayout(null);
	}
	
	private static volatile boolean paintCalled = false;
//	public static boolean paintCalled = false;
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		g.drawImage(bootImage, 0, 0, this);
		
		// paint the version string
		Font versionFont = FontPreferences.getControlFont();
		g.setFont(versionFont);
		
		String vString = VersionInfo.getInstance().toString();
		FontMetrics fm = g.getFontMetrics();
		int vWidth = (int)fm.getStringBounds(vString, g).getWidth();
		g.setColor(Color.black);
		g.drawString(vString, 400-vWidth-20, 250-20);
		
		if(!paintCalled)
			paintCalled = true;
	}
	
	/**
	 * Invoke the main() method of our application.
	 * This method will not modify the current JVM
	 * options or environment.
	 * 
	 * @param className
	 * @param args
	 */
	public static void invokeMain(String className, String[] args) {
		while(!paintCalled) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.error( e.getMessage(), e);
			}
		}
	
		try {
			Class.forName(className)
				.getMethod("main", new Class[] { String[].class })
				.invoke(null, new Object[] { args } );
		} catch (IllegalArgumentException e) {
			LOGGER.error( e.getMessage(), e);
		} catch (SecurityException e) {
			LOGGER.error( e.getMessage(), e);
		} catch (IllegalAccessException e) {
			LOGGER.error( e.getMessage(), e);
		} catch (InvocationTargetException e) {
			LOGGER.error( e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			LOGGER.error( e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			LOGGER.error( e.getMessage(), e);
		}
		
	}
	
	/**
	 * Invoke the main() method of our application.
	 * This method spawns a new JVM process.  The method
	 * will use the files:
	 * 
	 *  META-INF/env/$(OS)/vmoptions
	 *  META-INF/env/$(OS)/env
	 *  
	 * to setup the vmoptions and environment variables
	 * for the new JVM process.  Use this method if
	 * you need to modify the application environment.
	 * 
	 * @param className
	 * @param args
	 */
	public static void invokeMainInNewProcess(String className, String[] args) {
		while(!paintCalled) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.error( e.getMessage(), e);
			}
		}
		
		// plug-ins may want to modify some startup and environment params
		List<PhonBootHook> bootHooks = new ArrayList<PhonBootHook>();
		try {
			bootHooks = PluginManager.getInstance().getExtensions(PhonBootHook.class);
		} catch (PluginException e1) {
			LOGGER.warn( e1.getMessage(), e1);
		}
		
		final String javaHome = System.getProperty("java.home");
		final String javaBin = javaHome + File.separator + "bin" + File.separator + "java" + 
				(OSInfo.isWindows() ? ".exe" : "");
		final String cp = System.getProperty("java.class.path");
		final String libPath = System.getProperty("java.library.path");
		
		List<String> fullCmd = new ArrayList<String>();
		String[] cmd = {
				javaBin,
				"-cp", cp,
				"-Djava.library.path=" + libPath
		};
		fullCmd.addAll(Arrays.asList(cmd));

		// add vmoptions to command
		for(PhonBootHook bootHook:bootHooks) {
			bootHook.setupVMOptions(fullCmd);
		}
		
		fullCmd.add(className);
		fullCmd.addAll(Arrays.asList(args));
		
		final ProcessBuilder pb = new ProcessBuilder(fullCmd);
		pb.redirectErrorStream(true);
		
		for(PhonBootHook bootHook:bootHooks) {
			bootHook.setupEnvironment(pb.environment());
		}
		
		final StringBuilder builder = new StringBuilder();
		builder.append("Executing command:\n");
		for(String txt:fullCmd) {
			builder.append("\t" + txt + "\n");
		}
		
		LOGGER.info(builder.toString());
		try {
			final Process p = pb.start();
		} catch (IOException e) {
			LOGGER.error( e.getMessage(), e);
		} 
	}
	
	private static BootWindow _instance;
	
	public static void splash(URL imageURL) {
		if(imageURL != null) {
			splash(Toolkit.getDefaultToolkit().createImage(imageURL));
		} 
	}
	
	public static void splash(final Image image) {
		if(_instance == null) {
			final Runnable onEDT = new Runnable() {
				
				@Override
				public void run() {
					Frame f = new Frame();
					_instance = new BootWindow(f, image);
					
					Dimension ss = 
						Toolkit.getDefaultToolkit().getScreenSize();
					int xPos = ss.width / 2 - 200;
					int yPos = ss.height / 2 - 125;
					
					_instance.setBounds(xPos, yPos, 402, 252);
					_instance.setVisible(true);
				}
			};
			SwingUtilities.invokeLater(onEDT);
		}
	}
	
	public static void disposeSplash() {
		if(_instance != null) {
			_instance.setVisible(false);
		}
	}
	
}
