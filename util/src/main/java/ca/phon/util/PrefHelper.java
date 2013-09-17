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
package ca.phon.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.Format;
import java.text.ParseException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;



/**
 * <p>Helper class for managing several user preferences.
 * Also contains methods for accessing the {@link Preferences} object
 * for various roots.</p>
 *
 * <p>The <code>get...(key, default)</code> methods available
 * will search for the given key in the following order:</p>
 * 
 * <ol>
 * <li>System properties {@link System#getProperty(String, String)} (for all types of preferences
 *  <em>except</em> byte arrays).  These can be specified on the command line
 *  using the <pre>-D&lt;key&gt;=&lt;value&gt;</pre> command line option.</li>
 * <li>User preferences {@link #getUserPreferences()} </li>
 * <li>System preferences {@link #getSystemPreferences()}</li>
 * <li>Finally, the provide default is returned if the key is not found.</li>
 * </ol>
 */
public class PrefHelper {
	
	private final static Logger LOGGER = 
			Logger.getLogger(PrefHelper.class.getName());
	
	public static String getUserPrefDir() {
		String retVal = System.getProperty("user.home");
		
		if(OSInfo.isMacOs()) {
			retVal += 
				File.separator + "Library" + File.separator + "Application Support"
				+ File.separator + "Phon";
		} else if(OSInfo.isWindows()) {
			retVal = System.getenv("APPDATA") + File.separator + "Phon";
		} else if(OSInfo.isNix()) {
			retVal += File.separator + ".phon";
		} else {
			// should not get here
			LOGGER.info("Unsupported os: " + System.getProperty("os.name"));
		}
		
		return retVal;
	}
	
	/**
	 * Application prefs root node
	 */
	public final static String PREF_ROOT = "ca/phon";
	
//	/**
//	 * UI prefs root node
//	 */
//	public final static String UI_PREF_ROOT = PREF_ROOT + "/ui/prefs";
//	
//	/**
//	 * Application prefs root node
//	 */
//	public final static String APPLICATION_PREF_ROOT = PREF_ROOT + "/app/prefs";
	
	/**
	 * Returns the root user preferences node.
	 * 
	 * @return  a {@link Preferences} instance
	 */
	public static Preferences getUserPreferences() {
		Preferences retVal = 
				Preferences.userRoot().node(PREF_ROOT);
		return retVal;
	}
	
	/**
	 * Returns the root system preferences node.
	 * 
	 * @return Preferences
	 */
	public static Preferences getSystemPreferences() {
		Preferences retVal = 
				Preferences.systemRoot().node(PREF_ROOT);
		return retVal;
	}
	
//	/**
//	 * Return the user UI preferences node.
//	 * 
//	 * @return Preferences
//	 */
//	public static Preferences getUserUIPreferences() {
//		Preferences retVal = 
//				Preferences.userRoot().node(UI_PREF_ROOT);
//		return retVal;
//	}
	
//	/**
//	 * Return the system UI preferences node.
//	 * 
//	 * @return Preferences
//	 */
//	public static Preferences getSystemUIPreferences() {
//		Preferences retVal = 
//				Preferences.systemRoot().node(UI_PREF_ROOT);
//		return retVal;
//	}
	
//	/**
//	 * Return the user application preferences node.
//	 * 
//	 * @return Preferences
//	 */
//	public static Preferences getUserAppPreferences() {
//		Preferences retVal = 
//				Preferences.userRoot().node(APPLICATION_PREF_ROOT);
//		return retVal;
//	}
	
//	/**
//	 * Return the system application preferences node.
//	 * 
//	 * @return Preferences
//	 */
//	public static Preferences getSystemAppPreferences() {
//		Preferences retVal = 
//				Preferences.userRoot().node(APPLICATION_PREF_ROOT);
//		return retVal;
//	}
	
	/**
	 * Get the value of the specified {@link String} preference.
	 * 
	 * @param key
	 * @param def
	 * @return the value of the specified <code>key</code>, or
	 *  <code>default</code> if not found in the preferences
	 *  chain.
	 */
	public static String get(String key, String def) {
		String propVal = System.getProperty(key,
							getUserPreferences().get(key, 
									getSystemPreferences().get(key, def)));
		return propVal;
	}
	
	/**
	 * Get the value of the specified {@link Integer} preference. If found
	 * using {@link System#getProperty(String, String)}, the value is
	 * decoded using {@link Integer#decode(String)}.
	 * 
	 * @param key
	 * @param def
	 * @return the value of the specified <code>key</code>, or
	 *  <code>default</code> if not found in the preferences
	 *  chain.
	 */
	public static Integer getInt(String key, Integer def) {
		String propVal = System.getProperty(key);
		Integer retVal = null;
		if(propVal != null) {
			// try to decode integer
			try {
				retVal = Integer.decode(propVal);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		if(retVal == null) {
			// continue to search in preferences chain
			retVal = getUserPreferences().getInt(key, 
						getSystemPreferences().getInt(key, def));
		}
		return retVal;
	}
	
	/**
	 * Get the value of the specified {@link Boolean} preference. If found
	 * using {@link System#getProperty(String, String)}, the value is
	 * decoded using {@link Boolean#parseBoolean(String)}.
	 * 
	 * @param key
	 * @param def
	 * @return the value of the specified <code>key</code>, or
	 *  <code>default</code> if not found in the preferences
	 *  chain.
	 */
	public static Boolean getBoolean(String key, Boolean def) {
		String propVal = System.getProperty(key);
		Boolean retVal = null;
		if(propVal != null) {
			retVal = Boolean.parseBoolean(propVal);
		}
		if(retVal == null) {
			// continue to search in preferences chain
			retVal = getUserPreferences().getBoolean(key, 
						getSystemPreferences().getBoolean(key, def));
		}
		return retVal;
	}
	
	/**
	 * Get the value of the specified {@link Float} preference. If found
	 * using {@link System#getProperty(String, String)}, the value is
	 * decoded using {@link Float#parseFloat(String)}.
	 * 
	 * @param key
	 * @param def
	 * @return the value of the specified <code>key</code>, or
	 *  <code>default</code> if not found in the preferences
	 *  chain.
	 */
	public static Float getFloat(String key, Float def) {
		String propVal = System.getProperty(key);
		Float retVal = null;
		if(propVal != null) {
			try {
				retVal = Float.parseFloat(propVal);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		if(retVal == null) {
			// continue to search in preferences chain
			retVal = getUserPreferences().getFloat(key,
						getSystemPreferences().getFloat(key, def));
		}
		return retVal;
	}
	
	/**
	 * Get the value of the specified {@link Double} preference. If found
	 * using {@link System#getProperty(String, String)}, the value is
	 * decoded using {@link Float#parseDouble(String)}.
	 * 
	 * @param key
	 * @param def
	 * @return the value of the specified <code>key</code>, or
	 *  <code>default</code> if not found in the preferences
	 *  chain.
	 */
	public static Double getDouble(String key, Double def) {
		String propVal = System.getProperty(key);
		Double retVal = null;
		if(propVal != null) {
			try {
				retVal = Double.parseDouble(propVal);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		if(retVal == null) {
			
			// continue to search in preferences chain
			retVal = getUserPreferences().getDouble(key,
						getSystemPreferences().getDouble(key, def));
		}
		return retVal;
	}
	
	/**
	 * Get the value of the specified {@link Long} preference. If found
	 * using {@link System#getProperty(String, String)}, the value is
	 * decoded using {@link Long#parseLong(String)}.
	 * 
	 * @param key
	 * @param def
	 * @return the value of the specified <code>key</code>, or
	 *  <code>default</code> if not found in the preferences
	 *  chain.
	 */
	public static Long getLong(String key, Long def) {
		String propVal = System.getProperty(key);
		Long retVal = null;
		if(propVal != null) {
			try {
				retVal = Long.parseLong(propVal);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		if(retVal == null) {
			
			// continue to search in preferences chain
			retVal = getUserPreferences().getLong(key,
						getSystemPreferences().getLong(key, def));
		}
		return retVal;
	}
	
	/**
	 * Get the value of the specified {@link byte[]} preference.
	 * Unlike other properties, {@link byte[]} preferences cannot be
	 * overridden from the command line.  The byte array contains
	 * a {@link String} in base64 encoding.
	 * 
	 * @param key
	 * @param def
	 * @return the value of the specified <code>key</code>, or
	 *  <code>default</code> if not found in the preferences
	 *  chain.
	 */
	public static byte[] getByteArray(String key, byte[] def) {
		// continue to search in preferences chain
		byte[] retVal = getUserPreferences().getByteArray(key,
						getSystemPreferences().getByteArray(key, def));
		return retVal;
	}
	
	/**
	 * <p>Retrieve a color preference.  Colors are decoded
	 * using {@link Color#decode(String)}.</p>
	 * 
	 * @param key
	 * @param def
	 * @return the color value for the given <code>key</code>
	 *  or <code>def</code> if not found.
	 */
	public static Color getColor(String key, Color def) {
		Color retVal = def;
		
		String colorTxt = get(key, null);
		if(colorTxt != null) {
			try {
				retVal = Color.decode(colorTxt);
			} catch (NumberFormatException nfe) {
				LOGGER.severe(nfe.toString());
				nfe.printStackTrace();
			}
		}
		
		return retVal;
	}
	
	/**
	 * <p>Fonts preferences can be stored using {@link String}s or
	 * {@link byte} arrays.  If stored as a string, the font is
	 * decoded using {@link Font#decode(String)}.  If stored as
	 * a byte array, the font is loaded using the byte[] array as
	 * an binary input stream.</p>
	 * 
	 * <p><b>Note:</b>  It is <em>not</em> recommended to 
	 * store fonts in byte arrays!</p>
	 * 
	 * @param key
	 * @param def
	 * @return the value of the specified <code>key</code>, or
	 *  <code>default</code> if not found in the preferences
	 *  chain.
	 */
	public static Font getFont(String key, Font def) {
		Font retVal = def;
		// check string preference first
		String fontPref = get(key, null);
		if(fontPref != null) {
			retVal = Font.decode(fontPref);
		} else {
			// check for byte[] array
			byte[] fontData = getByteArray(key, new byte[0]);
			if(fontData.length > 0) {
				try {
					String base64 = new String(fontData);
					ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(base64));
					retVal = Font.createFont(Font.TRUETYPE_FONT, in);
				} catch (FontFormatException e) {
					e.printStackTrace();
					LOGGER.severe(e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					LOGGER.severe(e.getMessage());
				}
			}
		}
		return retVal;
	}
	
	/**
	 * 
	 * @param key
	 * @param type
	 * @param def
	 * @return
	 */
	public static <T extends Serializable> T getSerializedObject(String key, Class<T> type, T def) {
		byte[] data = getByteArray(key, new byte[0]);
		T retVal = def;
		if(data.length > 0) {
			try {
				String base64 = new String(data);
				byte[] objData = Base64.decode(base64);
				ObjectInputStream ois = 
						new ObjectInputStream(new ByteArrayInputStream(objData));
				retVal =  type.cast(ois.readObject());
				ois.close();
			} catch (IOException e) {
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
			} catch (ClassCastException e) {
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
			}
		}
		return retVal;
	}
	
	public static Object getFormattedObject(String key, Format format, Object def) {
		Object retVal = def;
		String objText = get(key, null);
		if(objText != null) {
			try {
				retVal = format.parseObject(objText);
			} catch (ParseException e) {
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
			}
		}
		return retVal;
	}
	
//	
//	public static SystemProperties getUserRecentProjects() {
//		SystemProperties recentProjects = 
//			new SystemProperties();
//		
//		File recentProjectsFile = 
//			new File(getUserPrefDir() + File.separator + PrefHelper.recentProjects);
//		
//		if(!recentProjectsFile.exists()) {
////			viewPrefs.addProperty("creation.date", Calendar.getInstance());
//			for(int i = 1; i <= 5; i++)
//				recentProjects.addProperty("ca.phon.project.recent." + i, new String());
//			
//			try {
//				FileOutputStream out = new FileOutputStream(recentProjectsFile);
//				recentProjects.saveToOutputStream(out);
//			} catch (IOException e) {
////				PhonUtilities.printException(e);
//				PhonLogger.warning(e.toString());
//			}
//			
//		} else {
//			// load from file
//			try {
//				FileInputStream in = new FileInputStream(recentProjectsFile);
//				recentProjects.loadFromInputStream(in);
//			} catch (IOException e) {
////				PhonUtilities.printException(null, e);
//				PhonLogger.warning(e.toString());
//			}
//		}
//		
//		return recentProjects;
//	}
//	
//	public static void saveRecentProjects(SystemProperties prefs) {
//		File recentProjectsFile = 
//			new File(getUserPrefDir() + File.separator + PrefHelper.recentProjects);
//		
//		try {
//			FileOutputStream out = new FileOutputStream(recentProjectsFile);
//			prefs.saveToOutputStream(out);
//		} catch (IOException e) {
////			PhonUtilities.printException(null, e);
//			PhonLogger.warning(e.toString());
//		}
//	}
//	
//	public static void clearRecentProjects() {
//		SystemProperties recentProjects = new SystemProperties();
//		for(int i = 1; i <= 5; i++)
//			recentProjects.addProperty("ca.phon.project.recent." + i, new String());
//		saveRecentProjects(recentProjects);
//	}
//	
//	public static void updateRecentProjects(String projectURI) {
//    	SystemProperties recentProjects = 
//    		PrefHelper.getUserRecentProjects();
//    	
//    	// see if the value already exists
//    	int startIndex = 0;
//    	String prefixString = "ca.phon.project.recent.";
//    	for(int i = 1; i <= 5; i++) {
//    		String proj = recentProjects.getProperty(prefixString + i).toString();
//    		
//    		if(proj.equals(projectURI)){
//    			startIndex = i;
//    			break;
//    		}
//    	}
//    	
//    	if(startIndex == 0)
//    		startIndex = 4;
//    	
//		// move everything above down one
//		for(int i = startIndex-1; i > 0; i--) {
//			recentProjects.addProperty(
//					prefixString+(i+1), recentProjects.getProperty(prefixString+i));
//		}
//		
//		recentProjects.addProperty(prefixString + "1", projectURI);
//    	
//		PrefHelper.saveRecentProjects(recentProjects);
//    }
//	
//	public static Font getTranscriptFont() {
//		SystemProperties prefs =
//			getUserPreferences();
//		if(prefs.getProperty("editor_font") != null) {
//			return Font.decode(prefs.getProperty("editor_font").toString());
//		} else {
//			return new Font("Charis SIL", Font.PLAIN, 12);
//		}
//	}
//	
//	public static String getUserPrefDir() {
//		String retVal = System.getProperty("user.home");
//		
//		if(OSUtils.isMacOs()) {
//			retVal += 
//				File.separator + "Library" + File.separator + "Application Support"
//				+ File.separator + "Phon";
//		} else if(OSUtils.isWindows()) {
//			retVal = System.getenv("APPDATA") + File.separator + "Phon";
//		} else if(OSUtils.isLinux()) {
//			retVal += File.separator + ".phon";
//		} else {
//			// should not get here
//			PhonLogger.info("Unsupported os: " + System.getProperty("os.name"));
//		}
//		
//		retVal += File.separator + "Phon";
//		
//		return retVal;
//	}
	
}
