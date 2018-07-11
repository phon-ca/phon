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

import java.awt.Font;
import java.text.ParseException;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;

/**
 * Formatter for {@link Font}s
 */
@FormatterType(value=Font.class)
public class FontFormatter implements Formatter<Font> {

	@Override
	public String format(Font obj) {
		return fontToString(obj);
	}

	@Override
	public Font parse(String text) throws ParseException {
		return Font.decode(text);
	}
	
	/**
	 * Converts the specified font into a string that can be used by
	 * Font.decode.
	 * @param font  the Font to convert to a String
	 * @return      a String
	 */
	private String fontToString(Font font) {
		StringBuilder ret = new StringBuilder();
		ret.append(font.getFamily());
		ret.append("-");
		
		if(font.isBold()) {
			if(font.isItalic())
				ret.append("BOLDITALIC");
			else
				ret.append("BOLD");
		} else if(font.isItalic()) {
			ret.append("ITALIC");
		} else {
			ret.append("PLAIN");
		}
		ret.append("-");
		
		ret.append(font.getSize());
		
		return ret.toString();
	}

}
