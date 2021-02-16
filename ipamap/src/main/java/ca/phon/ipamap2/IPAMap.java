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
package ca.phon.ipamap2;

import ca.phon.util.OSInfo;

import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

public class IPAMap  {

	/**
	 * Custom font created by merging Noto Sans with Noto Sans Mono
	 * (for the empty set and arrow characters) using pyftmerge
	 */
	private final static String NOTO_SANS_REGULAR = "data/fonts/NotoSans-Regular.ttf";

	public final static float DEFAULT_FONT_SIZE = 14.0f;

	private final static Font IPA_MAP_FONT = loadDefaultIPAMapFont();

	/**
	 * Load default IPA map font
	 * @return default font for IPA map
	 */
	public static Font loadDefaultIPAMapFont() {
		String defaultFontName =
				(OSInfo.isMacOs() ? "Lucida Grande" : (OSInfo.isWindows() ? "Segoe UI" : "Dialog"));
		Font retVal = null;

		try {
			retVal = Font.createFont(Font.TRUETYPE_FONT,
					IPAMap.class.getClassLoader()
							.getResourceAsStream(NOTO_SANS_REGULAR)).deriveFont(DEFAULT_FONT_SIZE);
		} catch (IOException | FontFormatException e) {
			Logger.getLogger(IPAMap.class.getName()).log(Level.WARNING, e.getLocalizedMessage(), e);
			retVal = Font.decode(defaultFontName + "-14-PLAIN");
		}

		return retVal;
	}

	public static Font getDefaultIPAMapFont() {
		return IPA_MAP_FONT;
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame("test");
		
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new DiacriticSelector(), BorderLayout.CENTER);
		
		f.pack();
		f.setVisible(true);
	}

}
