package ca.phon.app;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.hooks.PhonShutdownHook;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.plugin.PluginManager;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

public class PhonShutdownThread extends PhonWorker {
	
	private final static Logger LOGGER = Logger.getLogger(PhonShutdownThread.class.getName());
	
	private static PhonShutdownThread _instance = null;
	
	public static PhonShutdownThread getInstance() {
		if(_instance == null) {
			_instance = new PhonShutdownThread();
		}
		return _instance;
	}
	
	private PhonShutdownThread() {
		super();
		setName("Shutdown Hook");
		super.setFinishWhenQueueEmpty(true);
		super.invokeLater(new PhonShutdownTask());
	}

	private class PhonShutdownTask extends PhonTask {

		@Override
		public void performTask() {
			setStatus(TaskStatus.RUNNING);
			
			final List<IPluginExtensionPoint<PhonShutdownHook>> shutdownHooksPts = 
					PluginManager.getInstance().getExtensionPoints(PhonShutdownHook.class);
			for(IPluginExtensionPoint<PhonShutdownHook> shutdownHookPt:shutdownHooksPts) {
				final PhonShutdownHook hook = shutdownHookPt.getFactory().createObject();
				try {
					hook.shutdown();
				} catch (PluginException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
			
			setStatus(TaskStatus.FINISHED);
			
			// ensure the JVM exits!
			Runtime.getRuntime().halt(0);
		}
		
	}
	
}
