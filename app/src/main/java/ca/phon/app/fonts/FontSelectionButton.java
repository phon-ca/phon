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

import java.awt.*;
import java.util.concurrent.atomic.*;

import javax.swing.*;

import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

public class FontSelectionButton extends MultiActionButton {
	
	public final static String FONT_CHANGE_PROP = "_font_changed_";

	private static final long serialVersionUID = -3873581234740139307L;
	
	private AtomicReference<Font> selectedFont = new AtomicReference<Font>();
	
	private String fontProp;
	
	private String defaultVal;

	private String[] suggestedFonts = new String[0];

	final ImageIcon icon =
			IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL);
	final ImageIcon reloadIcon =
			IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);
	final ImageIcon addIcon =
			IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
	final ImageIcon subIcon =
			IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
	final ImageIcon boldIcon =
			IconManager.getInstance().getIcon("actions/format-text-bold", IconSize.SMALL);
	final ImageIcon italicIcon =
			IconManager.getInstance().getIcon("actions/format-text-italic", IconSize.SMALL);

	public FontSelectionButton() {
		super();
		
		setSelectedFont(getFont());
		
		init();
	}

	public void setSuggestedFonts(String[] suggestedFonts) {
		this.suggestedFonts = suggestedFonts;
	}

	public String[] getSuggestedFonts() {
		return this.suggestedFonts;
	}
	
	private void init() {
		final PhonUIAction onIncreaseFontSize = new PhonUIAction(this, "onIncreaseFontSize");
		onIncreaseFontSize.putValue(PhonUIAction.SHORT_DESCRIPTION, "Increase point size by 2");
		onIncreaseFontSize.putValue(PhonUIAction.LARGE_ICON_KEY, addIcon);

		final PhonUIAction onDecreaseFontSize = new PhonUIAction(this, "onDecreaseFontSize");
		onDecreaseFontSize.putValue(PhonUIAction.SHORT_DESCRIPTION, "Decrease point size by 2");
		onDecreaseFontSize.putValue(PhonUIAction.LARGE_ICON_KEY, subIcon);
		
		final PhonUIAction defaultAct = new PhonUIAction(this, "onShowFontMenu");
		defaultAct.putValue(PhonUIAction.NAME, "Select font");
		defaultAct.putValue(PhonUIAction.LARGE_ICON_KEY, icon);
		
		final PhonUIAction reloadAct = new PhonUIAction(this, "onReload");
		reloadAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset to default");
		reloadAct.putValue(PhonUIAction.LARGE_ICON_KEY, reloadIcon);

		final PhonUIAction toggleBoldAct = new PhonUIAction(this, "onToggleStyle", Font.BOLD);
		toggleBoldAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle bold modifier");
		toggleBoldAct.putValue(PhonUIAction.LARGE_ICON_KEY, boldIcon);

		final PhonUIAction toggleItalicAct = new PhonUIAction(this, "onToggleStyle", Font.ITALIC);
		toggleItalicAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle italic modifier");
		toggleItalicAct.putValue(PhonUIAction.LARGE_ICON_KEY, italicIcon);
		
		addAction(onIncreaseFontSize);
		addAction(onDecreaseFontSize);
		addAction(toggleBoldAct);
		addAction(toggleItalicAct);
		addAction(reloadAct);

		setDefaultAction(defaultAct);
		
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public Font getSelectedFont() {
		return selectedFont.get();
	}
	
	public void setSelectedFont(Font font) {
		selectedFont.set(font);
		setBottomLabelText((new FontFormatter()).format(font));
	}
	
	public String getFontProp() {
		return fontProp;
	}

	public void setFontProp(String fontProp) {
		this.fontProp = fontProp;
	}

	public String getDefaultVal() {
		return defaultVal;
	}

	public void setDefaultVal(String defaultVal) {
		this.defaultVal = defaultVal;
	}

	public void onShowFontMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		MenuBuilder builder = new MenuBuilder(popupMenu);

		// reset


		final PhonUIAction toggleBoldAct = new PhonUIAction(this, "onToggleStyle", Font.BOLD);
		toggleBoldAct.putValue(PhonUIAction.NAME, "Bold");
		toggleBoldAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle bold modifier");
		toggleBoldAct.putValue(PhonUIAction.SELECTED_KEY, getSelectedFont().isBold());
		toggleBoldAct.putValue(PhonUIAction.SMALL_ICON, boldIcon);
		builder.addItem(".", new JCheckBoxMenuItem(toggleBoldAct));

		final PhonUIAction toggleItalicAct = new PhonUIAction(this, "onToggleStyle", Font.ITALIC);
		toggleItalicAct.putValue(PhonUIAction.NAME, "Italic");
		toggleItalicAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle italic modifier");
		toggleItalicAct.putValue(PhonUIAction.SELECTED_KEY, getSelectedFont().isItalic());
		toggleItalicAct.putValue(PhonUIAction.SMALL_ICON, italicIcon);
		builder.addItem(".", new JCheckBoxMenuItem(toggleItalicAct));

		final PhonUIAction onIncreaseFontSize = new PhonUIAction(this, "onIncreaseFontSize");
		onIncreaseFontSize.putValue(PhonUIAction.NAME, "Increase size");
		onIncreaseFontSize.putValue(PhonUIAction.SHORT_DESCRIPTION, "Increase point size by 2");
		onIncreaseFontSize.putValue(PhonUIAction.SMALL_ICON, addIcon);
		builder.addItem(".", onIncreaseFontSize);

		final PhonUIAction onDecreaseFontSize = new PhonUIAction(this, "onDecreaseFontSize");
		onDecreaseFontSize.putValue(PhonUIAction.NAME, "Decrease size");
		onDecreaseFontSize.putValue(PhonUIAction.SHORT_DESCRIPTION, "Decrease point size by 2");
		onDecreaseFontSize.putValue(PhonUIAction.SMALL_ICON, subIcon);
		builder.addItem(".", onDecreaseFontSize);

		builder.addSeparator(".", "modifiers");

		final PhonUIAction reloadAct = new PhonUIAction(this, "onReload");
		reloadAct.putValue(PhonUIAction.NAME, "Reset to default");
		reloadAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset to default: " + defaultVal);
		reloadAct.putValue(PhonUIAction.LARGE_ICON_KEY, reloadIcon);
		builder.addItem(".", reloadAct);

		if(getSuggestedFonts().length > 0) {
			builder.addSeparator(".", "suggested-fonts");

			JMenuItem headerItem = new JMenuItem("-- Suggested Fonts --");
			headerItem.setEnabled(false);
			builder.addItem(".", headerItem);

			for(int i = 0; i < getSuggestedFonts().length; i++) {
				String suggestedFont = getSuggestedFonts()[i];
				String fontString = String.format("%s-PLAIN-12", suggestedFont);
				// font not found
				if(Font.decode(fontString).getFamily().equals("Dialog")) continue;

				final PhonUIAction selectSuggestedFont = new PhonUIAction(this, "onSelectSuggestedFont", i);
				selectSuggestedFont.putValue(PhonUIAction.NAME, suggestedFont);
				selectSuggestedFont.putValue(PhonUIAction.SHORT_DESCRIPTION, "Use font: " + suggestedFont);
				builder.addItem(".", selectSuggestedFont);
			}
		}

		builder.addSeparator(".", "font-dialog");
		final PhonUIAction defaultAct = new PhonUIAction(this, "onSelectFont");
		defaultAct.putValue(PhonUIAction.NAME, "Select font....");
		defaultAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select font using font selection dialog");
		defaultAct.putValue(PhonUIAction.LARGE_ICON_KEY, icon);
		builder.addItem(".", defaultAct);

		popupMenu.show(this, 0, getHeight());
	}

	public void onSelectSuggestedFont(PhonActionEvent pae) {
		int idx = Integer.parseInt(pae.getData().toString());
		String suggestedFont = getSuggestedFonts()[idx];

		float currentFontSize = getSelectedFont().getSize();

		Font font = Font.decode(String.format("%s-PLAIN-%d", suggestedFont, (int)currentFontSize));
		PrefHelper.getUserPreferences()
				.put(getFontProp(), (new FontFormatter()).format(font));
		setSelectedFont(font);
	}

	public void onSelectFont() {
		final FontDialogProperties props = new FontDialogProperties();
		props.setRunAsync(true);
		props.setListener(fontDlgListener);
		props.setFontName(getSelectedFont().getName());
		props.setFontSize(getSelectedFont().getSize());
		props.setBold(getSelectedFont().isBold());
		props.setItalic(getSelectedFont().isItalic());
		props.setParentWindow(SwingUtilities.getWindowAncestor(this));
	
		NativeDialogs.showFontDialog(props);
	}

	public void onIncreaseFontSize() {
		Font currentFont = getSelectedFont();
		Font biggerFont = currentFont.deriveFont(Math.min(72.0f, currentFont.getSize() + 2.0f));
		PrefHelper.getUserPreferences()
				.put(getFontProp(), (new FontFormatter()).format(biggerFont));
		setSelectedFont(biggerFont);
	}

	public void onDecreaseFontSize() {
		Font currentFont = getSelectedFont();
		Font smallerFont = currentFont.deriveFont(Math.max(1.0f, currentFont.getSize() - 2.0f));
		PrefHelper.getUserPreferences()
				.put(getFontProp(), (new FontFormatter()).format(smallerFont));
		setSelectedFont(smallerFont);
	}

	public void onToggleStyle(PhonActionEvent pae) {
		int style = Integer.parseInt(pae.getData().toString());

		Font currentFont = getSelectedFont();
		int fontStyle = currentFont.getStyle();
		fontStyle ^= style;
		Font font = currentFont.deriveFont(fontStyle);
		PrefHelper.getUserPreferences()
				.put(getFontProp(), (new FontFormatter()).format(font));
		setSelectedFont(font);
	}

	public void onReload() {
		PrefHelper.getUserPreferences()
			.put(getFontProp(), getDefaultVal());
		setBottomLabelText(getDefaultVal());
	}
	
	private final NativeDialogListener fontDlgListener = new NativeDialogListener() {
		
		@Override
		public void nativeDialogEvent(NativeDialogEvent arg0) {
			if(arg0.getDialogData() != null) {
				final Font font = (Font)arg0.getDialogData();
				setSelectedFont(font);
				
				PrefHelper.getUserPreferences()
					.put(getFontProp(), (new FontFormatter()).format(font));
			}
		}
		
	};
	
}
