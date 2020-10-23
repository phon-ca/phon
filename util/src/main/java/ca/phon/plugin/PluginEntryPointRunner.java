/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.plugin;

import java.util.*;

import ca.phon.util.*;
import ca.phon.worker.*;

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
	public static IPluginEntryPoint executePlugin(IPluginEntryPoint ep)
		throws PluginException {
		
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep);
		runner.executePlugin();
	
		return runner.getEp();
	}
	
	public static IPluginEntryPoint executePlugin(String epName)
		throws PluginException {
		
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName);
		runner.executePlugin();
		
		return runner.getEp();
	}
	
	public static IPluginEntryPoint executePlugin(IPluginEntryPoint ep, Map<String, Object> args)
		throws PluginException {
		
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep, args);
		runner.executePlugin();
		
		return runner.getEp();
	}
	
	public static IPluginEntryPoint executePlugin(String epName, Map<String, Object> args) 
		throws PluginException {
		
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName, args);
		runner.executePlugin();
		
		return runner.getEp();
	}

	
	/**
	 * Execute plugin on the default background
	 * thread.
	 *
	 * @return the plugin task
	 */
	public PluginEntryPointTask executePluginInBackground() {
		PluginEntryPointTask retVal = new PluginEntryPointTask();
		PhonWorker.getInstance().invokeLater(retVal);
		return retVal;
	}
	
	/* Helper static methods */
	public static PluginEntryPointTask executePluginInBackground(String epName) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName);
		return runner.executePluginInBackground();
	}
	
	public static PluginEntryPointTask executePluginInBackground(String epName, Map<String, Object> args) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(epName, args);
		return runner.executePluginInBackground();
	}
	
	public static PluginEntryPointTask executePluginInBackground(IPluginEntryPoint ep) {
		PluginEntryPointRunner runner = new PluginEntryPointRunner(ep);
		return runner.executePluginInBackground();
	}
	
	public static PluginEntryPointTask executePluginInBackground(IPluginEntryPoint ep, Map<String, Object> args) {
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
