package ca.phon.app;

import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import ca.phon.app.log.LogManager;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.worker.PhonWorker;

/**
 * Main entry point for the application.
 *
 */
public class Main {
	
	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	public static void main(String[] args) {
		LogManager.getInstance().setupLogging();
		
		// output some debug info in the log
		LOGGER.info("Phon " + VersionInfo.getInstance().getLongVersion());
		printVMInfo();
		
		// start the shared application worker thread
		final PhonWorker appWorker = PhonWorker.getInstance();
		appWorker.start();
		
		// TODO init plug-ins
		
		// display the workspace window
		final Runnable onEDT = new Runnable() {
			
			@Override
			public void run() {
				final PluginEntryPointRunner entryPtRunner =
						new PluginEntryPointRunner("Workspace");
				try {
					entryPtRunner.executePlugin();
				} catch (PluginException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		};
		SwingUtilities.invokeLater(onEDT);
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
