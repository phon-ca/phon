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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXBusyLabel;

import ca.phon.ui.nativedialogs.OSInfo;

public class BootWindow extends Window {
	
	private final static Logger LOGGER = Logger.getLogger(BootWindow.class.getName());
	
	private Image bootImage;
	
	private JXBusyLabel busyLabel;
	
	public BootWindow(Frame p, Image img) {
		super(p);
		
		bootImage = img;
		
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

		busyLabel = new JXBusyLabel(new Dimension(20, 20));
		busyLabel.setBounds(360, 10, 20, 20);
		busyLabel.setBusy(true);
		busyLabel.setBackground(Color.white);
		busyLabel.setOpaque(true);
		
		add(busyLabel);
	}
	
	private static volatile boolean paintCalled = false;
//	public static boolean paintCalled = false;
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		g.drawImage(bootImage, 0, 0, this);
		
		// paint the version string
		Font versionFont = new Font("Arial", Font.PLAIN, 14);
		g.setFont(versionFont);
		
		String vString = "Version: " + VersionInfo.getInstance().getVersion();
		FontMetrics fm = g.getFontMetrics();
		int vWidth = (int)fm.getStringBounds(vString, g).getWidth();
		g.setColor(Color.black);
		g.drawString(vString, 390-vWidth, 250-20);
		
		if(!paintCalled)
			paintCalled = true;
	}
	
	/**
	 * Invoke the main() method of our application.
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

		try {
			final String vmFiles = "META-INF/environment/" + 
				(OSInfo.isWindows() ? "windows" : (OSInfo.isMacOs() ? "mac" : "unix")) + File.separator + 
				"vmoptions";
			final Enumeration<URL> optURLs = 
					ClassLoader.getSystemClassLoader().getResources(vmFiles);
			
			while(optURLs.hasMoreElements()) {
				URL url = optURLs.nextElement();
				LOGGER.info("Loading vmoptions from URL " + url.toString());
				final InputStream is = url.openStream();
				final BufferedReader isr = new BufferedReader(new InputStreamReader(is));
				String vmopt = null;
				while((vmopt = isr.readLine()) != null) {
					fullCmd.add(vmopt);
				}
				isr.close();
			}
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
		}
		
		fullCmd.add(className);
		fullCmd.addAll(Arrays.asList(args));
		
		final ProcessBuilder pb = new ProcessBuilder(fullCmd);
		
		// setup environment
		try {
			final String envFiles = "META-INF/environment/" + 
				(OSInfo.isWindows() ? "windows" : (OSInfo.isMacOs() ? "mac" : "unix")) + File.separator + 
				"env";
			final Enumeration<URL> envURLs = 
					ClassLoader.getSystemClassLoader().getResources(envFiles);
			while(envURLs.hasMoreElements()) {
				URL url = envURLs.nextElement();
				LOGGER.info("Loading environment settings from URL " + url.toString());
				final InputStream is = url.openStream();
				final BufferedReader isr = new BufferedReader(new InputStreamReader(is));
				String envOpt = null;
				while((envOpt = isr.readLine()) != null) {
					String[] opt = envOpt.split("=");
					if(opt.length != 2) continue;
					String key = opt[0];
					String val = opt[1];
					if(key.endsWith("+")) {
						key = key.substring(0, key.length()-1);
						val = pb.environment().get(key) + val;
					}
					pb.environment().put(key, val);
				}
				isr.close();
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		
		// important: windows requires the java.library.path to be in the system path
		if(OSInfo.isWindows()) {
			String path = pb.environment().get("PATH");
			path += ";\"" + libPath + "\"";
			pb.environment().put("PATH", path);
		}
		pb.redirectErrorStream(true);
		
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
					
					_instance.setBounds(xPos, yPos, 400, 250);
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
