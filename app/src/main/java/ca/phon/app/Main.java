package ca.phon.app;

import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogManager;
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
		LogManager.getInstance().setupLogging();
		
		// output some debug info in the log
		LOGGER.info("Phon " + VersionInfo.getInstance().getLongVersion());
		printVMInfo();
		
		// start the shared application worker thread
		final PhonWorker appWorker = PhonWorker.getInstance();
		appWorker.start();
		
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
		
		// start initial entry point
		LOGGER.info("Starting " + initialEntryPoint);
		final PluginEntryPointRunner entryPtRunner =
				new PluginEntryPointRunner(initialEntryPoint);
		try {
			// TODO process command line arguments and pass them
			// to the entry point in a map
			entryPtRunner.executePlugin();
		} catch (PluginException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private static void printVMInfo() {
    	
    	Properties props = System.getProperties();
    	for(Object key:props.keySet()) {
    		Object val = props.get(key);
    		
    		LOGGER.info("[VM Property] " + key + " = " + val);
    	}
    	LOGGER.info("[Other] Locale = " + Locale.getDefault().toString());
    }

}
