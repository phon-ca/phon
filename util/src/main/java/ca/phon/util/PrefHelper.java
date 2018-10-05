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
package ca.phon.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Format;
import java.text.ParseException;
import java.util.prefs.BackingStoreException;
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
	
	private final static org.apache.logging.log4j.Logger LOGGER = 
			org.apache.logging.log4j.LogManager.getLogger(PrefHelper.class.getName());
	
	/**
	 * Returns the location of the application data folder for
	 * the current platform.
	 * 
	 * @return application support folder location
	 */
	public static String getUserDataFolder() {
		String userHomePath = System.getProperty("user.home");
		
		String retVal = "";
		if(OSInfo.isMacOs()) {
			retVal = 
				userHomePath + File.separator + "Library" + File.separator + "Application Support"
				+ File.separator + "Phon";
		} else if(OSInfo.isWindows()) {
			retVal = System.getenv("APPDATA") + File.separator + "Phon";
		} else {
			retVal = userHomePath + File.separator + ".phon";
		}
		
		return retVal;
	}
	
	/**
	 * Application prefs root node
	 */
	public final static String PREF_ROOT = "/ca/phon/util";
	
	public static String getUserDocumentsFolder() {
		final File userHome = new File(System.getProperty("user.home"));
		final File userDocs = new File(userHome, "Documents");
		return userDocs.getAbsolutePath();
	}
	
	/**
	 * The location of the user 'Documents' folder with 'Phon' appended.
	 * 
	 */
	public static String getUserDocumentsPhonFolder() {
		final File phonDocs = new File(getUserDocumentsFolder(), "Phon");
		return phonDocs.getAbsolutePath();
	}
	
	/**
	 * Returns the root user preferences node.
	 * 
	 * @return  a {@link Preferences} instance
	 */
	public static Preferences getUserPreferences() {
		Preferences retVal = 
				Preferences.userRoot().node(PREF_ROOT);
		try {
			retVal.sync();
		} catch (BackingStoreException e) {
			LOGGER.warn( e.getLocalizedMessage(), e);
		}
		return retVal;
	}
	
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
							getUserPreferences().get(key, def));
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
			retVal = getUserPreferences().getInt(key, def);
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
			retVal = getUserPreferences().getBoolean(key, def);
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
			retVal = getUserPreferences().getFloat(key, def);
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
			retVal = getUserPreferences().getDouble(key, def);
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
			retVal = getUserPreferences().getLong(key, def);
		}
		return retVal;
	}
	
	/**
	 * Get the value of the specified enum preference.
	 * 
	 * @param key
	 * @param def
	 * @param enumClazz
	 * 
	 * @return the value of the specified <code>key</code>
	 */
	public static <T extends Enum<?>> T getEnum(Class<T> enumClazz, String key, T def) {
		T retVal = def;
		
		// check for preference
		final String stringPref = get(key, (def != null ? def.toString() : null));
		if(stringPref != null && stringPref.length() > 0) {
			// check for a 'fromString' method first
			try {
				final Method fromString = enumClazz.getMethod("fromString", String.class);
				retVal = enumClazz.cast(fromString.invoke(enumClazz, stringPref));
			} catch (NoSuchMethodException | SecurityException 
					| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
			// else search using the toString method
			if(retVal == null) {
				for(T constant:enumClazz.getEnumConstants()) {
					if(constant.toString().equals(stringPref)) {
						retVal = constant;
						break;
					}
				}
			}
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
		byte[] retVal = getUserPreferences().getByteArray(key, def);
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
				LOGGER.error(nfe.toString());
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
					LOGGER.error(e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					LOGGER.error(e.getMessage());
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
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			} catch (ClassCastException e) {
				LOGGER.error(e.getMessage());
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
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return retVal;
	}
	
}
