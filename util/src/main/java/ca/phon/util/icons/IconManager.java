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
package ca.phon.util.icons;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.GrayFilter;
import javax.swing.ImageIcon;

import ca.phon.util.StackTraceInfo;

/**
 * Class for obtaining icons.
 *
 */
public class IconManager {
	
	private final static Logger LOGGER = Logger.getLogger(IconManager.class.getName());
	
	/** The static instance */
	private static IconManager _instance;
	
	/**
	 * Returns the shared IconManager
	 * instance
	 */
	public static IconManager getInstance() {
		if(_instance == null)
			_instance = new IconManager();
		return _instance;
	}
	
	private final String iconDir = System.getProperty("user.dir") + File.separator + "data/icons";
	
	private final String iconURI = "data/icons";

	/** Only load icons once */
	private HashMap<IconTuple, ImageIcon> loadedIcons;
	
	/** Constructor */
	protected IconManager() {
		super();
		
		this.loadedIcons = new HashMap<IconTuple, ImageIcon>();
	}
	
	public ImageIcon getIcon(String iconName, IconSize size) {
		ImageIcon retVal = null;
		if(loadIcon(iconName, size)) {
			IconTuple tuple = new IconTuple();
			tuple.iconName = iconName;
			tuple.size = size;
			
			retVal = loadedIcons.get(tuple);
		}
		return retVal;
	}
	
	public ImageIcon getGrayedIcon(String iconName, IconSize size) {
		final ImageIcon icon = getIcon(iconName, size);
		if(icon == null) return null;
		
		final int w = icon.getIconWidth();
		final int h = icon.getIconHeight();
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage img = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(Color.decode("#00f0f0f0"));
		g2d.fillRect(0, 0, w, h);
		icon.paintIcon(null, g2d, 0, 0);
		
		BufferedImage gray = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		ColorConvertOp op = new ColorConvertOp(
				img.getColorModel().getColorSpace(),
				gray.getColorModel().getColorSpace(), null);
		op.filter(img, gray);
		
		return new ImageIcon(gray);
	}
	
	public ImageIcon getDisabledIcon(String iconName, IconSize size) {
		final ImageIcon icon = getIcon(iconName, size);
		if(icon == null) return null;
		
		final int w = icon.getIconWidth();
		final int h = icon.getIconHeight();
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage img = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
		Graphics2D g2d = img.createGraphics();
		icon.paintIcon(null, g2d, 0, 0);
		Image gray = GrayFilter.createDisabledImage(img);
		
		return new ImageIcon(gray);
	}
	
	/**
	 * Load an icon from our classpath
	 * @param iconName
	 * @param size
	 * @return <CODE>true</CODE> if the icon is loaded
	 */
	private boolean loadIconFromCp(String iconName, IconSize s) {
		boolean found = false;
		URL iconURL = null;
		
		for(int i = s.ordinal(); i < IconSize.values().length; i++) {
			if(found) break;
			IconSize size = IconSize.values()[i];
			
			String icnURL = 
				iconURI + "/" + size.getWidth() + "x" + size.getHeight() + "/" + iconName + ".png";
			iconURL = ClassLoader.getSystemResource(icnURL);
			if(iconURL == null) {
				icnURL = 
					iconURI + "/" + size.getWidth() + "x" + size.getHeight() + "/" + iconName + ".gif";
				iconURL = ClassLoader.getSystemResource(icnURL);
				
				if(iconURL != null) {
					found = true;
				}
			} else {
				found = true;
			}
		}
		
		if(found) {
			ImageIcon iconImage = null;
			iconImage = new ImageIcon(iconURL);
			
			iconImage = new ImageIcon(
					iconImage.getImage().getScaledInstance(s.getWidth(), s.getHeight(), Image.SCALE_SMOOTH));
			IconTuple tuple = new IconTuple();
			tuple.iconName = iconName;
			tuple.size = s;
			
			loadedIcons.put(tuple, iconImage);
		}
		
		return found;
	}
	
	/**
	 * Loads (ensures) an icon.
	 * 
	 * @param iconName
	 * @param size
	 * @return <CODE>true</CODE> if the icon is loaded (including
	 * if it was already loaded.) <CODE>false</CODE> otherwise.
	 */
	private boolean loadIcon(String iconName, IconSize s) {
		boolean found = loadIconFromCp(iconName, s);
		String iconPath = "";
		
		if(!found) {

			iconPath =
					iconDir + "/" + s.getWidth() + "x" + s.getHeight() + "/" + iconName + ".png";
			if(new File(iconPath).exists()) {
				found = true;
			} else {
				for(int i = IconSize.XXLARGE.ordinal(); i >= 0; i--) {
					if(found) break;
					IconSize size = IconSize.values()[i];

					iconPath =
						iconDir + "/" + size.getWidth() + "x" + size.getHeight() + "/" + iconName + ".png";
					if(!(new File(iconPath)).exists()) {
						iconPath =
							iconDir + "/" + size.getWidth() + "x" + size.getHeight() + "/" + iconName + ".gif";

						if(new File(iconPath).exists()) {
							found = true;
						}
					} else {
						found = true;
					}
				}
			}
			
			if(found) {
				ImageIcon iconImage = null;
				iconImage = new ImageIcon(iconPath);
				
				iconImage = new ImageIcon(
						iconImage.getImage().getScaledInstance(s.getWidth(), s.getHeight(), Image.SCALE_SMOOTH));
				
				IconTuple tuple = new IconTuple();
				tuple.iconName = iconName;
				tuple.size = s;
				
				loadedIcons.put(tuple, iconImage);
			} else {
				LOGGER.log(Level.WARNING, "Icon not found: " + iconName, new StackTraceInfo());
			}
		}
		
		return found;
	}
	
	private class IconTuple {
		public String iconName;
		public IconSize size;
		
		@Override
		public int hashCode() {
			return iconName.hashCode() + size.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof IconTuple))
				return false;
			
			IconTuple comp = (IconTuple)o;
			return 
				((comp.iconName.equals(iconName)) &&
						comp.size == size);
		}
	}
}
