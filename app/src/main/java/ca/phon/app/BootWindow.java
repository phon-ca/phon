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
import java.util.logging.Level;
import java.util.logging.Logger;

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
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
		
		String vString = VersionInfo.getInstance().getVersion();
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
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	
		try {
			Class.forName(className)
				.getMethod("main", new Class[] { String[].class })
				.invoke(null, new Object[] { args } );
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (SecurityException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (IllegalAccessException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (InvocationTargetException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		
		// plug-ins may want to modify some startup and environment params
		List<PhonBootHook> bootHooks = new ArrayList<PhonBootHook>();
		try {
			bootHooks = PluginManager.getInstance().getExtensions(PhonBootHook.class);
		} catch (PluginException e1) {
			LOGGER.log(Level.WARNING, e1.getMessage(), e1);
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
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
