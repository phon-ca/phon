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
package ca.phon.ipadictionary.ui;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Styled document for IPA lookups.
 * 
 *
 */
public class IPALookupDocument extends DefaultStyledDocument {
	
	/* Style names */
	private final String userIPA = "_user_ipa_";
	private final String genIPA = "_gen_ipa_";
	private final String valIPA = "_val_ipa_";
	
	private final String inputSt = "_input_";
	
	public IPALookupDocument() {
		super();
		
		setupStyles();
	}
	
	private void setupStyles() {
		MutableAttributeSet userIPAStyle = addStyle(userIPA, null);
		StyleConstants.setForeground(userIPAStyle, Color.green.darker());
		
		MutableAttributeSet genIPAStyle = addStyle(genIPA, null);
		StyleConstants.setForeground(genIPAStyle, Color.orange.darker());
		
		MutableAttributeSet valIPAStyle = addStyle(valIPA, null);
		StyleConstants.setForeground(valIPAStyle, Color.blue);
		
		MutableAttributeSet inputStyle = addStyle(inputSt, null);
		StyleConstants.setForeground(inputStyle, Color.DARK_GRAY);
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {
		
		Pattern ipaPattern = Pattern.compile("([SUVsuv])\\p{Space}(.*)\\p{Space}?");
		Matcher m = ipaPattern.matcher(str);
		
		if(m.matches()) {
			String type = m.group(1);
			String ipa = m.group(2);
			
			String st = valIPA;
			if(type.equalsIgnoreCase("U"))
				st = userIPA;
			else if(type.equalsIgnoreCase("V"))
				st = valIPA;
			else if(type.equalsIgnoreCase("S"))
				st = genIPA;
			
			super.insertString(offs, "\t" + ipa + "\n", getStyle(st));
		} else if(str.startsWith(">")) {
			super.insertString(offs, str, getStyle(inputSt));
		} else {
			super.insertString(offs, str, a);
		}
		
	}

}
