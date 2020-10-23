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
package ca.phon.ipadictionary.ui;

import java.awt.*;
import java.util.regex.*;

import javax.swing.text.*;

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
