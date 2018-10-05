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
package ca.phon.app.theme;

import javax.swing.UIDefaults;
import javax.swing.plaf.FontUIResource;

import com.jgoodies.looks.FontPolicy;
import com.jgoodies.looks.FontSet;

import ca.phon.ui.fonts.FontPreferences;

public class PhonWindowsFontPolicy implements FontPolicy {

	@Override
	public FontSet getFontSet(String lafName, UIDefaults table) {
		return new FontSet() {
			
			@Override
			public FontUIResource getWindowTitleFont() {
				return new FontUIResource(FontPreferences.getWindowTitleFont());
			}
			
			@Override
			public FontUIResource getTitleFont() {
				return new FontUIResource(FontPreferences.getTitleFont());
			}
			
			@Override
			public FontUIResource getSmallFont() {
				return new FontUIResource(FontPreferences.getSmallFont());
			}
			
			@Override
			public FontUIResource getMessageFont() {
				return new FontUIResource(FontPreferences.getMessageDialogFont());
			}
			
			@Override
			public FontUIResource getMenuFont() {
				return new FontUIResource(FontPreferences.getMenuFont());
			}
			
			@Override
			public FontUIResource getControlFont() {
				return new FontUIResource(FontPreferences.getControlFont());
			}
		};
	}

}
