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
