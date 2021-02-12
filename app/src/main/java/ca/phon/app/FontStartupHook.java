package ca.phon.app;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Registers all ttf fonts found in 'bin/fonts' with
 * current graphics environment
 */
public class FontStartupHook implements PhonStartupHook,
		IPluginExtensionPoint<PhonStartupHook> {


	private final static String FONT_FOLDER = "bin/fonts";

	@Override
	public void startup() throws PluginException {
		final File fontFolder = new File(FONT_FOLDER);
		if(!fontFolder.exists() || !fontFolder.isDirectory()) {
			LogUtil.info("Custom font folder not found");
			return;
		}

		for(File fontFile:fontFolder.listFiles()) {
			if(!fontFile.getName().endsWith(".ttf")) continue;

			try {
				Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(customFont);

				LogUtil.info("Registered font: " + fontFile.getName());
			} catch (IOException | FontFormatException e) {
				LogUtil.severe(e);
			}
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return (args) -> this;
	}

}
