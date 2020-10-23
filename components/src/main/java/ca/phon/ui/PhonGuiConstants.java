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
package ca.phon.ui;

import java.awt.*;

import javax.swing.*;

/**
 * List of constants for the phon GUI.
 * @author ghedlund
 *
 * @deprecated
 */
public class PhonGuiConstants {
	/** The default transcript font */
	public static final String DEFAULT_TRANSCRIPT_FONT = "Charis SIL-PLAIN-14";

	/** Some default colors */
	public static final Color PHON_SELECTED = UIManager.getColor("textHighlight");
	public static final Color PHON_BACKGROUND = Color.WHITE;
	public static final Color PHON_SHADED = new Color(243, 202, 79, 50);
	public static final Color PHON_FOCUS = new Color(56, 117, 215);
	public static final Color PHON_ORANGE = new Color(243, 202, 79);

	/** Blue colours */
	public static final Color PHON_DARK_BLUE = new Color(17, 75, 122);
//	public static final Color PHON_LIGHT_BLUE = new Color();

	public static final Color PHON_TIER_FOCUS_BACKGROUND = Color.decode("0xffff99");

	public static final Color PHON_UI_STRIP_COLOR = Color.decode("0xF0F8FF");
			//new Color(240, 240, 240);

	public static final Color PHON_VIEW_TITLE_COLOR = SystemColor.activeCaption;

}
