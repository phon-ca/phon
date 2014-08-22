package ca.phon.app.fonts;

import java.awt.Cursor;
import java.awt.Font;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ImageIcon;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.FontFormatter;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FontDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class FontSelectionButton extends MultiActionButton {
	
	public final static String FONT_CHANGE_PROP = "_font_changed_";

	private static final long serialVersionUID = -3873581234740139307L;
	
	private AtomicReference<Font> selectedFont = new AtomicReference<Font>();
	
	private String fontProp;
	
	private String defaultVal;
	
	public FontSelectionButton() {
		super();
		
		setSelectedFont(getFont());
		
		init();
	}
	
	private void init() {
		final ImageIcon icon = 
			IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL);
		final ImageIcon reloadIcon = 
			IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);
		
		final PhonUIAction defaultAct = new PhonUIAction(this, "onSelectFont");
		defaultAct.putValue(PhonUIAction.NAME, "Select font");
		defaultAct.putValue(PhonUIAction.LARGE_ICON_KEY, icon);
		
		final PhonUIAction reloadAct = new PhonUIAction(this, "onReload");
		reloadAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset to default");
		reloadAct.putValue(PhonUIAction.LARGE_ICON_KEY, reloadIcon);
		
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

	public void onSelectFont() {
		final FontDialogProperties props = new FontDialogProperties();
		props.setRunAsync(true);
		props.setListener(fontDlgListener);
		props.setFontName(getSelectedFont().getName());
		props.setFontSize(getSelectedFont().getSize());
		props.setBold(getSelectedFont().isBold());
		props.setItalic(getSelectedFont().isItalic());
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
	
		NativeDialogs.showFontDialog(props);
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
