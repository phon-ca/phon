package ca.phon.app;

import ca.phon.util.PrefHelper;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.cef.CefApp;
import org.cef.browser.CefBrowser;

import java.io.File;

public final class JCefHelper {

	private static JCefHelper INSTANCE;

	public static JCefHelper getInstance() {
		if(INSTANCE == null) {
			CefAppBuilder builder = new CefAppBuilder();

			builder.setInstallDir(new File(PrefHelper.getUserDataFolder(), "jcef-bundle"));
			builder.setProgressHandler(new ConsoleProgressHandler());
			builder.addJcefArgs("--disable-gpu");
			builder.getCefSettings().windowless_rendering_enabled = true;

			final CefApp app = builder.build();
			INSTANCE = new JCefHelper(app);
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

	public CefBrowser createBrowser() {

	}

}
