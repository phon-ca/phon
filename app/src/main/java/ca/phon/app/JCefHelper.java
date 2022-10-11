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

	private final static String DEFAULT_URL = "about:blank";

	private static JCefHelper INSTANCE;

	private final static boolean useOsr = !OSInfo.isMacOs();

	public static JCefHelper getInstance() {
		if(INSTANCE == null) {
			CefAppBuilder builder = new CefAppBuilder();

			builder.setInstallDir(new File(PrefHelper.getUserDataFolder(), "jcef-bundle"));
			builder.setProgressHandler(new ConsoleProgressHandler());
			if(OSInfo.isWindows())
				builder.addJcefArgs("--disable-gpu");
			builder.addJcefArgs("--allow-file-access-from-files");
			builder.getCefSettings().windowless_rendering_enabled = useOsr;
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

	private JCefHelper(CefApp cefApp) {
		super();

		this.cefApp = cefApp;
	}

	public CefApp getApp() {
		return this.cefApp;
	}

	public CefClient createClient() {
		return cefApp.createClient();
	}

	public Tuple<CefClient, CefBrowser> createClientAndBrowser() {
		final CefClient cefClient = getApp().createClient();
		final CefBrowser cefBrowser = createBrowser(cefClient);
		return new Tuple<>(cefClient, cefBrowser);
	}

	public CefBrowser createBrowser(CefClient cefClient) {
		return createBrowser(cefClient, DEFAULT_URL);
	}

	public CefBrowser createBrowser(CefClient cefClient, String url) {
		final CefBrowser browser =
				cefClient.createBrowser("about:blank", useOsr, false);
		return browser;
	}

}
