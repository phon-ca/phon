package ca.phon.app;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogManager;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.plugin.PluginManager;
import ca.phon.util.PrefHelper;
import ca.phon.worker.PhonWorker;

/**
 * Main entry point for the application.
 *
 */
public class Main {
	
	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	private final static String INITIAL_EP_PROP = 
			Main.class.getName() + ".initialEntryPoint";
	
	private final static String initialEntryPoint = 
			PrefHelper.get(INITIAL_EP_PROP, "Workspace");
	
	public static void main(String[] args) {
		startLogging();
		startWorker();
		initPlugins();
		runStartupHooks();
		setupShutdownHooks();
		startApp(args);
	}
	
	private static void initPlugins() {
		// init plug-ins
		final PluginManager pluginManager = PluginManager.getInstance();
		pluginManager.scanPluginFolder();
		
		final String userPluginFolder = 
				PrefHelper.getUserDataFolder() + File.separator + PluginManager.PLUGIN_FOLDER;
		pluginManager.scanPluginFolder(new File(userPluginFolder));
	}
	
	private static void startLogging() {
		LogManager.getInstance().setupLogging();
		
		// output some debug info in the log
		LOGGER.info("Phon " + VersionInfo.getInstance().getLongVersion());
		printVMInfo();
	}
	
	private static void printVMInfo() {
		final Properties props = System.getProperties();
		for(Object key:props.keySet()) {
			Object val = props.get(key);
			
			LOGGER.info("[VM Property] " + key + " = " + val);
		}
		LOGGER.info("[Other] Locale = " + Locale.getDefault().toString());
	}

	private static void startWorker() {
		// start the shared application worker thread
		final PhonWorker appWorker = PhonWorker.getInstance();
		appWorker.start();
	}
	
	private static void runStartupHooks() {
		// run startup hooks
		final List<IPluginExtensionPoint<PhonStartupHook>> startupHookPts =
				PluginManager.getInstance().getExtensionPoints(PhonStartupHook.class);
		for(IPluginExtensionPoint<PhonStartupHook> startupHookPt:startupHookPts) {
			final PhonStartupHook hook = startupHookPt.getFactory().createObject();
			try {
				hook.startup();
			} catch (PluginException pe) {
				LOGGER.log(Level.SEVERE, pe.getMessage(), pe);
			}
		}
	}
	
	private static void setupShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(PhonShutdownThread.getInstance());
	}
	
	private static void startApp(String[] args) {
		LOGGER.info("Starting " + initialEntryPoint);
		final PluginEntryPointRunner entryPtRunner =
				new PluginEntryPointRunner(initialEntryPoint);
		try {
			final EntryPointArgs entryPointArgs = new EntryPointArgs();
			entryPointArgs.parseArgs(args);
			entryPtRunner.setArgs(entryPointArgs);
			entryPtRunner.executePlugin();
		} catch (PluginException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}
