/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui;

import java.awt.*;

import javax.swing.UIManager;

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
