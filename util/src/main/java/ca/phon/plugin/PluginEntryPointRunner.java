/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.plugin;

import java.util.HashMap;
import java.util.Map;

import ca.phon.util.StackTraceInfo;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

/**
 * Methods for executing plugin entry points
 * using various methods.
 */
public class PluginEntryPointRunner {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PluginEntryPointRunner.class.getName());
	
	/**
	 * Entry point
	 */
	private IPluginEntryPoint ep;
	
	/**
	 * Arguments
	 */
	private Map<String, Object> args;
	
	/**
	 * Constructor
	 * 
	 * @param epName
	 * @boolean runInBackground
	 */
	public PluginEntryPointRunner(String epName) {
		this(epName, new HashMap<String, Object>());
	}
	
	
	/**
	 * Constructor.
	 * 
	 * @param epName
	 * @param args
	 */
	public PluginEntryPointRunner(String epName, Map<String, Object> args) {
		this(PluginManager.getInstance().getEntryPoint(epName), args);
	}
	
	
	/**
	 * Constructor. 
	 * 
	 * @param ep
	 */
	public PluginEntryPointRunner(IPluginEntryPoint ep) {
		this(ep, new HashMap<String, Object>());
	}
	
	/**
	 * Constructor. 
	 * @param ep
	 * @param args
	 */
	public PluginEntryPointRunner(IPluginEntryPoint ep, Map<String, Object> args) {
		this.ep = ep;
		this.args = args;
	}
	
	public IPluginEntryPoint getEp() {
		return ep;
	}


	public void setEp(IPluginEntryPoint ep) {
		this.ep = ep;
	}


	public Map<String, Object> getArgs() {
		return args;
	}


	public void setArgs(Map<String, Object> args) {
		this.args = args;
	}

	/**
	 * Execute plugin on calling thread.
	 * Calling thread will be blocked until pluginStart
	 * is finished.
	 * 
	 */
	public void executePlugin()
		throws PluginException {
		if(ep == null)
			throw new PluginException(new NullPointerException("entry point not found"));
		Map<String, Object> pluginArgs = new HashMap<String, Object>();
		if(args != null)
			pluginArgs.putAll(args);
		try {
			ep.pluginStart(pluginArgs);
		} catch (Exception e) {
//			LOGGER.error( e.getMessage(), e);
			throw new PluginException(e);
		}
	}
	
	/* Helper static methods */
	public static void executePlugin(IPluginEntryPoint ep)
		throws PluginException {
		
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep);
		runner.executePlugin();
	
	}
	
	public static void executePlugin(String epName)
		throws PluginException {
		
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName);
		runner.executePlugin();
		
	}
	
	public static void executePlugin(IPluginEntryPoint ep, Map<String, Object> args)
		throws PluginException {
		
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep, args);
		runner.executePlugin();
		
	}
	
	public static void executePlugin(String epName, Map<String, Object> args) 
		throws PluginException {
		
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName, args);
		runner.executePlugin();
		
	}

	
	/**
	 * Execute plugin on the default background
	 * thread.
	 *
	 * @return the plugin task
	 */
	public PhonTask executePluginInBackground() {
		PhonTask retVal = new PluginEntryPointTask();
		PhonWorker.getInstance().invokeLater(retVal);
		return retVal;
	}
	
	/* Helper static methods */
	public static PhonTask executePluginInBackground(String epName) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName);
		return runner.executePluginInBackground();
	}
	
	public static PhonTask executePluginInBackground(String epName, Map<String, Object> args) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName, args);
		return runner.executePluginInBackground();
	}
	
	public static PhonTask executePluginInBackground(IPluginEntryPoint ep) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep);
		return runner.executePluginInBackground();
	}
	
	public static PhonTask executePluginInBackground(IPluginEntryPoint ep, Map<String, Object> args) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep, args);
		return runner.executePluginInBackground();
	}
	
	/**
	 * Execute plugin on the given worker thread.
	 * 
	 * @param thread
	 * @return the plugin task
	 */
	public PhonTask executePluginOnThread(PhonWorker thread) {
		PhonTask retVal = new PluginEntryPointTask();
		thread.invokeLater(retVal);
		return retVal;
	}
	
	public static PhonTask executePluginOnThread(String epName, PhonWorker thread) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName);
		return runner.executePluginOnThread(thread);
	}
	
	public static PhonTask executePluginOnThread(String epName, Map<String, Object> args, PhonWorker thread) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName, args);
		return runner.executePluginOnThread(thread);
	}
	
	public static PhonTask executePluginOnThread(IPluginEntryPoint ep, PhonWorker thread) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep);
		return runner.executePluginOnThread(thread);
	}
	
	public static PhonTask executePluginOnThread(IPluginEntryPoint ep, Map<String, Object> args, PhonWorker thread) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep, args);
		return runner.executePluginOnThread(thread);
	}
	
	/**
	 * Execute plugin a new thread
	 * 
	 * @return the plugin task
	 */
	public PhonTask executePluginOnNewThread() {
		PhonTask retVal = new PluginEntryPointTask();
		PhonWorker worker = PhonWorker.createWorker();
		worker.setFinishWhenQueueEmpty(true);
		worker.invokeLater(retVal);
		worker.start();
		return retVal;
	}
	
	public static PhonTask executePluginOnNewThread(String epName) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName);
		return runner.executePluginOnNewThread();
	}
	
	public static PhonTask executePluginOnNewThread(String epName, Map<String, Object> args) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName, args);
		return runner.executePluginOnNewThread();
	}
	
	public static PhonTask executePluginOnNewThread(IPluginEntryPoint ep) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep);
		return runner.executePluginOnNewThread();
	}
	
	public static PhonTask executePluginOnNewThread(IPluginEntryPoint ep, Map<String, Object> args) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep, args);
		return runner.executePluginOnNewThread();
	}

	/**
	 * Run the given entry point with no parameters
	 * on the calling thread.
	 * 
	 * @param ep
	 * 
	 * @throws PluginException
	 */
	
	
	/**
	 * Basic runnable task for executing a plugin.
	 */
	private class PluginEntryPointTask extends PhonTask {

		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			
			if(ep != null) {
				Map<String, Object> pluginArgs = new HashMap<String, Object>();
				if(args != null)
					pluginArgs.putAll(args);
				
				try {
					ep.pluginStart(pluginArgs);
					
					setStatus(TaskStatus.FINISHED);
				} catch (Exception e) {
					final PluginException pe = new PluginException(e);
					LOGGER.error( e.getMessage(), pe);
					super.err = pe;
					setStatus(TaskStatus.ERROR);
				}
			} else {
				LOGGER.error( "Cannot find entry point", new StackTraceInfo());
				super.err = new PluginException("No entry point");
				super.setStatus(TaskStatus.ERROR);
			}
		}
		
	}
	
}
