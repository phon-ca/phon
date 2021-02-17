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
package ca.phon.app.fonts;

import javax.swing.*;

import org.jdesktop.swingx.*;

import ca.phon.app.prefs.*;
import ca.phon.plugin.*;
import ca.phon.ui.*;
import ca.phon.ui.fonts.*;

public class FontPreferencesPanel extends PrefsPanel implements IPluginExtensionPoint<PrefsPanel> {

	private static final long serialVersionUID = 5418907041332250697L;
	
	private HidablePanel restartPanel;

	private FontSelectionButton tierFontBtn;

	private FontSelectionButton monospaceBtn;
	
	public FontPreferencesPanel() {
		super("Fonts");
		
		init();
	}
	
	private void init() {
		restartPanel = new HidablePanel(FontPreferencesPanel.class.getName() + ".restartPanel");
		restartPanel.setTopLabelText("Please re-open Phon");
		restartPanel.getTopLabel().setFont(FontPreferences.getTitleFont());
		restartPanel.setBottomLabelText("It is recommended to restart Phon after making changes to font settings.");
		restartPanel.setVisible(false);
		
		tierFontBtn = new FontSelectionButton();
		tierFontBtn.setFontProp(FontPreferences.TIER_FONT);
		tierFontBtn.setDefaultVal(FontPreferences.DEFAULT_TIER_FONT);
		tierFontBtn.setTopLabelText("<html><b>Tier font</b> &#8226; Default font for tiers</html>");
		tierFontBtn.setSuggestedFonts(FontPreferences.SUGGESTED_IPA_FONT_NAMES);
		tierFontBtn.setSelectedFont(FontPreferences.getTierFont());

		monospaceBtn = new FontSelectionButton();
		monospaceBtn.setFontProp(FontPreferences.MONOSPACE_FONT);
		monospaceBtn.setDefaultVal(FontPreferences.DEFAULT_MONOSPACE_FONT);
		monospaceBtn.setSuggestedFonts(FontPreferences.SUGGESTED_MONOSPACE_FONT_NAMES);
		monospaceBtn.setTopLabelText("<html><b>Monospace font</b> &#8226; Font used in consoles</html>");
		monospaceBtn.setSelectedFont(FontPreferences.getMonospaceFont());
		
		final JPanel uiFontsPanel = new JPanel(new VerticalLayout());
		uiFontsPanel.add(tierFontBtn);
		uiFontsPanel.add(monospaceBtn);
		
		uiFontsPanel.setBorder(BorderFactory.createTitledBorder("UI Fonts"));
		
		setLayout(new VerticalLayout());
		add(restartPanel);
		add(uiFontsPanel);
	}

	@Override
	public Class<?> getExtensionType() {
		return PrefsPanel.class;
	}

	@Override
	public IPluginExtensionFactory<PrefsPanel> getFactory() {
		return factory;
	}
	
	private final IPluginExtensionFactory<PrefsPanel> factory = new IPluginExtensionFactory<PrefsPanel>() {
		
		@Override
		public PrefsPanel createObject(Object... args) {
			return FontPreferencesPanel.this;
		}
	
	};

}
