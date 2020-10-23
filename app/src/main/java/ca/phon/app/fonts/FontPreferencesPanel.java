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

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.swingx.*;

import com.jgoodies.forms.layout.*;

import ca.phon.app.prefs.*;
import ca.phon.plugin.*;
import ca.phon.ui.*;
import ca.phon.ui.fonts.*;

public class FontPreferencesPanel extends PrefsPanel implements IPluginExtensionPoint<PrefsPanel> {

	private static final long serialVersionUID = 5418907041332250697L;
	
	private HidablePanel restartPanel;
	
	private JSlider fontSizeSlider;
	
	private FontSelectionButton tierFontBtn;
	
	private FontSelectionButton uiIpaFontBtn;
	
	private FontSelectionButton controlFontBtn;
	
	private FontSelectionButton menuFontBtn;
	
	private FontSelectionButton titleFontBtn;
	
	private FontSelectionButton smallFontBtn;
	
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
		tierFontBtn.setSelectedFont(FontPreferences.getTierFont());
		
		uiIpaFontBtn = new FontSelectionButton();
		uiIpaFontBtn.setFontProp(FontPreferences.UI_IPA_FONT);
		uiIpaFontBtn.setDefaultVal(FontPreferences.DEFAULT_UI_IPA_FONT);
		uiIpaFontBtn.setTopLabelText("<html><b>UI Font</b> &#8226; Font used for UI controls which display IPA text</html>");
		uiIpaFontBtn.setSelectedFont(FontPreferences.getUIIpaFont());
		
		final JPanel tierFontsPanel = new JPanel(new VerticalLayout());
		tierFontsPanel.add(tierFontBtn);
		tierFontsPanel.add(uiIpaFontBtn);
		tierFontsPanel.setBorder(BorderFactory.createTitledBorder("IPA Fonts"));
		
		final Hashtable<Integer, JLabel> hashTbl = new Hashtable<Integer, JLabel>();
		hashTbl.put(0, new JLabel("Default"));
		
		fontSizeSlider = new JSlider(-2, 4);
		fontSizeSlider.setPaintTicks(true);
		fontSizeSlider.setMajorTickSpacing(2);
		fontSizeSlider.setSnapToTicks(true);
		fontSizeSlider.setToolTipText("Adjust font size");
		fontSizeSlider.setLabelTable(hashTbl);
		fontSizeSlider.setPaintLabels(true);
		fontSizeSlider.setValue(FontPreferences.getFontSizeIncrease());
		fontSizeSlider.setFocusable(false);
		fontSizeSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				int val = fontSizeSlider.getValue();
				
				if(Math.abs(val) % 2 == 0 && !fontSizeSlider.getValueIsAdjusting()) {
					FontPreferences.setFontSizeIncrease(val);
					
					final Runnable later = new Runnable() {
						
						@Override
						public void run() {
//							FontPreferences.setupFontPreferences();
						}
						
					};
					SwingUtilities.invokeLater(later);
				}
			}
		});
		
		// setup font scaler
		final JLabel smallLbl = new JLabel("A");
		smallLbl.setFont(getFont().deriveFont(12.0f));
		smallLbl.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel largeLbl = new JLabel("A");
		largeLbl.setFont(getFont().deriveFont(24.0f));
		largeLbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		final FormLayout fontSizeLayout = 
				new FormLayout("fill:pref:grow(0.5), pref, fill:pref:grow, pref, fill:pref:grow(0.5)", "pref");
		final CellConstraints cc = new CellConstraints();
		
		final JPanel fontSizePanel = new JPanel(fontSizeLayout);
		fontSizePanel.add(smallLbl, cc.xy(2, 1));
		fontSizePanel.add(fontSizeSlider, cc.xy(3, 1));
		fontSizePanel.add(largeLbl, cc.xy(4, 1));
		
		controlFontBtn = new FontSelectionButton();
		controlFontBtn.setFontProp(FontPreferences.CONTROL_FONT);
		controlFontBtn.setDefaultVal(FontPreferences.DEFAULT_CONTROL_FONT);
		controlFontBtn.setTopLabelText("<html><b>Control font</b> &#8226; Default font for UI controls</html>");
		controlFontBtn.setSelectedFont(FontPreferences.getControlFont());
		
		titleFontBtn = new FontSelectionButton();
		titleFontBtn.setFontProp(FontPreferences.TITLE_FONT);
		titleFontBtn.setDefaultVal(FontPreferences.DEFAULT_TITLE_FONT);
		titleFontBtn.setTopLabelText("<html><b>Title font</b> &#8226; Font used in titles</html>");
		titleFontBtn.setSelectedFont(FontPreferences.getTitleFont());
		
		smallFontBtn = new FontSelectionButton();
		smallFontBtn.setFontProp(FontPreferences.SMALL_FONT);
		smallFontBtn.setDefaultVal(FontPreferences.DEFAULT_SMALL_FONT);
		smallFontBtn.setTopLabelText("<html><b>Small font</b> &#8226; Font used in tool tips</html>");
		smallFontBtn.setSelectedFont(FontPreferences.getSmallFont());
		
		menuFontBtn = new FontSelectionButton();
		menuFontBtn.setFontProp(FontPreferences.MENU_FONT);
		menuFontBtn.setDefaultVal(FontPreferences.DEFAULT_MENU_FONT);
		menuFontBtn.setTopLabelText("<html><b>Menu font</b> &#8226; Font used in menus</html>");
		menuFontBtn.setSelectedFont(FontPreferences.getMenuFont());
		
		monospaceBtn = new FontSelectionButton();
		monospaceBtn.setFontProp(FontPreferences.MONOSPACE_FONT);
		monospaceBtn.setDefaultVal(FontPreferences.DEFAULT_MONOSPACE_FONT);
		monospaceBtn.setTopLabelText("<html><b>Monospace font</b> &#8226; Font used in consoles</html>");
		monospaceBtn.setSelectedFont(FontPreferences.getMonospaceFont());
		
		final JPanel uiFontsPanel = new JPanel(new VerticalLayout());
//		uiFontsPanel.add(fontSizePanel);
		uiFontsPanel.add(controlFontBtn);
		uiFontsPanel.add(menuFontBtn);
		uiFontsPanel.add(titleFontBtn);
		uiFontsPanel.add(smallFontBtn);
		uiFontsPanel.add(monospaceBtn);
		
		uiFontsPanel.setBorder(BorderFactory.createTitledBorder("UI Fonts"));
		
		setLayout(new VerticalLayout());
		add(restartPanel);
		add(tierFontsPanel);
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
