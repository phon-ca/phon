package ca.phon.app;

import ca.phon.app.log.LogUtil;
import ca.phon.plugin.*;
import ca.phon.util.*;
import me.friwi.jcefmaven.*;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.cef.*;
import org.cef.browser.*;
import org.cef.handler.CefLoadHandler;
import org.cef.network.CefRequest;

import java.io.*;

public final class JCefHelper {

	private static JCefHelper INSTANCE;

	public static JCefHelper getInstance() {
		if(INSTANCE == null) {
			CefAppBuilder builder = new CefAppBuilder();

			builder.setInstallDir(new File(PrefHelper.getUserDataFolder(), "jcef-bundle"));
			builder.setProgressHandler(new ConsoleProgressHandler());
			builder.addJcefArgs("--disable-gpu");
			builder.addJcefArgs("--allow-file-access-from-files");
			builder.getCefSettings().windowless_rendering_enabled = false;
			builder.setAppHandler(new MavenCefAppHandlerAdapter() {
				@Override
				public boolean onBeforeTerminate() {
					// java-cef will hijack exiting on macOS using Cmd+Q
					// and prevent our exit handler from executing
					// run exit plug-in here
					if(OSInfo.isMacOs()) {
						try {
							PluginEntryPointRunner.executePlugin("Exit");
						} catch (PluginException e1) {
							LogUtil.severe(e1);
						}
					}
					return false;
				}
			});

			try {
				final CefApp app = builder.build();
				INSTANCE = new JCefHelper(app);
			} catch (InterruptedException | UnsupportedPlatformException | IOException | CefInitializationException e) {
				LogUtil.severe(e);
			}
		}
		return INSTANCE;
	}

	private final CefApp cefApp;

	private final CefClient cefClient;

	private JCefHelper(CefApp cefApp) {
		super();

		this.cefApp = cefApp;
		this.cefClient = cefApp.createClient();
	}

	public CefApp getApp() {
		return this.cefApp;
	}

	public CefBrowser createBrowser() {
		final CefBrowser browser =
				cefClient.createBrowser("about:blank", false, false);
		return browser;
	}

}
