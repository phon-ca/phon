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
package ca.phon.app;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.OSInfo;
import com.sun.jna.*;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.ptr.IntByReference;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.*;

/**
 * Check if Phon is running in translated mode using Rosetta on macOS computers.
 */
@PhonPlugin(name="startup")
public final class MacOSTranslationCheck implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	private final static String TRANSLATION_TITLE = "Phon - Incorrect architecture detected";

	private final static String TRANSLATION_MESSAGE = """
			This version of phon is intended for use on x86_64 systems. Please
			visit https://phon.ca/ to download Phon for apple silicon computers.""";

	private final static String[] TRANSLATION_OPTIONS = { "Open https://www.phon.ca", "Exit" };

	@Override
	public void startup() throws PluginException {
		if(OSInfo.isMacOs()) {
			final String arch = System.getProperty("os.arch");
			if("x86_64".equals(arch)) {
				if(isTranslated()) {
					LogUtil.info(String.format("Running %s translated", arch));
					SwingUtilities.invokeLater(this::showTranslationMessageDialog);
				} else {
					LogUtil.info(String.format("Running %s untranslated", arch));
				}
			}
		}
	}

	// execute on EDT
	private void showTranslationMessageDialog() {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setParentWindow(null);
		props.setTitle(TRANSLATION_TITLE);
		props.setHeader(TRANSLATION_TITLE);
		props.setMessage(TRANSLATION_MESSAGE);
		props.setOptions(TRANSLATION_OPTIONS);
		props.setRunAsync(false);

		final int selection = NativeDialogs.showMessageDialog(props);
		if(selection == 0) {
			try {
				Desktop.getDesktop().browse(new URL("https://www.phon.ca").toURI());
			} catch (URISyntaxException | IOException e) {}
		}
		System.exit(1);
	}

	private boolean isTranslated() {
		Pointer ptr = new Memory(Native.getNativeSize(Integer.class));
		IntByReference size = new IntByReference();
		final int ret = SystemB.INSTANCE.sysctlbyname("sysctl.proc_translated", ptr, size, null, 0);
		if (ret == -1) {
			if(Native.getLastError() == 2) { // ENOENT
				return false;
			} else {
				throw new RuntimeException("error calling sysctlbyname");
			}
		} else {
			if(ptr.getInt(0) == 0) {
				return false;
			} else {
				return true;
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
