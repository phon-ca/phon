/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.util.icons;

import ca.hedlund.desktopicons.*;
import ca.phon.plugin.PluginManager;
import ca.phon.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

/**
 * Class for obtaining icons.
 *
 */
public class IconManager {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(IconManager.class.getName());
	
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
		
		return getGrayedIcon(icon);
	}
	
	public ImageIcon getGrayedIcon(Icon icon) {
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
		
		return getDisabledIcon(icon);
	}
	
	public ImageIcon getDisabledIcon(Icon icon) {
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
	 * Load system icon for given path.
	 * 
	 * @param path
	 * @param size
	 * @param backupIcon icon name used as backup if system icon not found
	 * @return icon for given path or <code>null</code> if not found
	 */
	public ImageIcon getSystemIconForPath(String path, IconSize size) {
		return getSystemIconForPath(path, "mimetypes/text-x-generic", size);
	}
	
	/**
	 * Load system icon for given path.
	 * 
	 * @param path
	 * @param backupIcon icon name used as backup if system icon not found. May be <code>null</code>
	 * @param size

	 * @return icon for given path or backup icon if not found
	 */
	public ImageIcon getSystemIconForPath(String path, String backupIcon, IconSize size) {
		ImageIcon retVal = null;
		
		if(OSInfo.isNix()) {
			retVal = getIcon(backupIcon, size);
		} else {
			try {
				final Image img = DesktopIcons.getIconForPath(path, size.getWidth(), size.getHeight());
				retVal = new ImageIcon(img);
			} catch (DesktopIconException e) {
				LOGGER.warn( e.getLocalizedMessage(), e);
				
				if(backupIcon != null && backupIcon.length() > 0) {
					retVal = getIcon(backupIcon, size);
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Load system icon for given file type.  File type should be an extension
	 * without a leading '.'
	 * 
	 * @param filetype
	 * @param size
	 * 
	 * @return icon for given file type or <code>null</code> if not found
	 */
	public ImageIcon getSystemIconForFileType(String filetype, IconSize size) {
		return getSystemIconForFileType(filetype, "mimetypes/text-x-generic", size);
	}
	
	/**
	 * Load system icon for given file type.  File type should be an extension
	 * without a leading '.'
	 * 
	 * @param filetype
	 * @param size
	 * @param backupIcon icon name used as backup if system icon not found, may be <code>null</code>
	 * 
	 * @return icon for given file type or backup icon if not found
	 */
	public ImageIcon getSystemIconForFileType(String filetype, String backupIcon, IconSize size) {
		ImageIcon retVal = null;
		
		if(OSInfo.isNix()) {
			retVal = getIcon(backupIcon, size);
		} else {
			try {
				final Image img = DesktopIcons.getIconForFileType(filetype, size.getWidth(), size.getHeight());
				retVal = new ImageIcon(img);
			} catch (DesktopIconException e) {
				LOGGER.warn( e.getLocalizedMessage(), e);
				
				if(backupIcon != null && backupIcon.length() > 0) {
					retVal = getIcon(backupIcon, size);
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Load system stock icon.  Use a value from either MacOSStockIcon or
	 * WindowsStockIcon enums.
	 * 
	 * @param stockIcon
	 * @param size
	 * 
	 * @return icon for given file type or backup icon if not found
	 */
	public ImageIcon getSystemStockIcon(StockIcon stockIcon, IconSize size) {
		return getSystemStockIcon(stockIcon, "blank", size);
	}
	
	/**
	 * Load system stock icon.  Use a value from either MacOSStockIcon or
	 * WindowsStockIcon enums.
	 * 
	 * @param stockIcon
	 * @param size
	 * @param backupIcon icon name used as backup if system icon not found
	 * 
	 * @return icon for given file type or backup icon if not found
	 */
	public ImageIcon getSystemStockIcon(StockIcon stockIcon, String backupIcon, IconSize size) {
		ImageIcon retVal = null;
		
		// Phon 3.1 - return default icons on linux
		if(OSInfo.isNix()) {
			retVal = getIcon(backupIcon, size);
		} else {
			try {
				final Image img = DesktopIcons.getStockIcon(stockIcon, size.getWidth(), size.getHeight());
				retVal = new ImageIcon(img);
			} catch (DesktopIconException e) {
				LOGGER.warn( e.getLocalizedMessage(), e);
				
				if(backupIcon != null && backupIcon.length() > 0) {
					retVal = getIcon(backupIcon, size);
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Create a new icon with a single character
	 * using the given foreground, background.  The
	 * size of the icon will be the size of the
	 * bounding rectangle returned by {@link FontMetrics#getStringBounds(String, java.awt.Graphics)}
	 * 
	 * @param c
	 * @param font
	 * @param foreground
	 * @param background
	 */
	public ImageIcon createGlyphIcon(Character c, Font font, Color foreground, Color background) {
		final BufferedImage bufferedImage = new BufferedImage(1, 1,
				BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D)bufferedImage.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setFont(font);
		final FontMetrics fm = g.getFontMetrics(font);
		final Rectangle2D charRect = fm.getStringBounds(c.toString(), g);
		
		final BufferedImage img = new BufferedImage((int)charRect.getWidth()+1, (int)charRect.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g2 = (Graphics2D)img.createGraphics();
		
		// get baseline of character
		g2.setColor(foreground);
		g2.drawString(c.toString(), 0, (int)(charRect.getHeight() - fm.getDescent()));
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		return new ImageIcon(img);
	}
	
	/**
	 * Combine icons into an icon strip.
	 * 
	 * @param icons
	 */
	public ImageIcon createIconStrip(Icon[] icons) {
		return createIconStrip(icons, 0);
	}
	
	public ImageIcon createIconStrip(Icon[] icons, int offset) {
		// determine size
		int width = 0;
		int height = 0;
		
		for(Icon icon:icons) {
			width += icon.getIconWidth();
			height = Math.max(height, icon.getIconHeight());
		}
		width += (icons.length - 1) * offset;
		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D)img.createGraphics();
		
		int x = 0;
		int y = 0;
		for(Icon icon:icons) {
			icon.paintIcon(null, g, x, y);
//			g.drawImage(icon.getImage(), x, y, null);
			x += icon.getIconWidth() + offset;
		}
		
		return new ImageIcon(img);
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
			iconURL = PluginManager.getInstance().getResource(icnURL);
			if(iconURL == null) {
				icnURL = 
					iconURI + "/" + size.getWidth() + "x" + size.getHeight() + "/" + iconName + ".gif";
				iconURL = PluginManager.getInstance().getResource(icnURL);
				
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
				LOGGER.warn( "Icon not found: " + iconName, new StackTraceInfo());
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
