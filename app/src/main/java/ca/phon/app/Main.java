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
package ca.phon.app;

import java.awt.event.*;
import java.io.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import org.apache.commons.io.*;

import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.app.modules.*;
import ca.phon.app.welcome.*;
import ca.phon.plugin.*;
import ca.phon.util.*;
import ca.phon.worker.*;

/**
 * Main entry point for the application.
 *
 */
public final class Main {
	
	private final static String START_ACTION_PROP = Main.class.getName() + ".startAction";
	
	private final static String startAction =
			PrefHelper.get(START_ACTION_PROP, null);
	
	private final static String INITIAL_EP_PROP = 
			Main.class.getName() + ".initialEntryPoint";
	
	private final static String initialEntryPoint = 
			PrefHelper.get(INITIAL_EP_PROP, WelcomeWindowEP.EP_NAME);
	
	public static void main(String[] args) {
		createDataFolder();
		startLogging();
		startWorker();
		initPlugins();
		updateVersion();
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
	
	private static void createDataFolder() {
		final File userDataFolder = new File(PrefHelper.getUserDataFolder());
		if(!userDataFolder.exists()) {
			userDataFolder.mkdirs();
		}
	}
	
	private static void updateVersion() {
		if(VersionInfo.getInstance().isDevVersion()) return;
		
		String currentVersion = VersionInfo.getInstance().getVersion();
		final File userDataFolder = new File(PrefHelper.getUserDataFolder());
		// write application version to 'version' file
		final File versionFile = new File(userDataFolder, "version");
		
		// check value of current version file
		String oldVersion = "0.0.0";
		if(versionFile.exists() && versionFile.canRead()) {
			try {
				oldVersion = FileUtils.readFileToString(versionFile).trim();
			} catch (IOException e1) {
				LogUtil.severe(e1);
			}
		}
		
		// write new version to file
		try (PrintWriter out = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(versionFile)))) {
			out.println(currentVersion);
			out.flush();
		} catch (IOException e) {
			LogUtil.severe(e);
		}
		
		// fire version triggers
		if(!oldVersion.equals(currentVersion)) {
			final List<IPluginExtensionPoint<VersionTrigger>> extPts = 
					PluginManager.getInstance().getExtensionPoints(VersionTrigger.class);
			
			for(var extPt:extPts) {
				try {
					var trigger = extPt.getFactory().createObject();
					trigger.versionChanged(oldVersion, currentVersion);
				} catch (Exception e) {
					// catch unknown exceptions which may occur in services
					LogUtil.severe(e);
				}
			}
		}
	}
	
	private static void startLogging() {
		LogManager.getInstance().setupLogging();
		
		// output some debug info in the log
		LogUtil.info("Phon " + VersionInfo.getInstance());
		if(PhonSplasher.isForked()) {
			LogUtil.info("Running Phon in forked process");
		}
		LogUtil.info("Process " + ManagementFactory.getRuntimeMXBean().getName());
		printEnvInfo();
		printVMInfo();
	}
	
	private static void printEnvInfo() {
		for(Entry<String, String> envEntry:System.getenv().entrySet()) {
			System.out.println("[ENV] " + envEntry.getKey() + " = " + envEntry.getValue());
		}
	}
	
	private static void printVMInfo() {
		final Properties props = System.getProperties();
		for(Object key:props.keySet()) {
			Object val = props.get(key);
			
			LogUtil.info("[VM Property] " + key + " = " + val);
		}
		LogUtil.info("[Other] Locale = " + Locale.getDefault().toString());
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
				LogUtil.severe( pe.getMessage(), pe);
			}
		}
	}
	
	private static void setupShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(PhonShutdownThread.getInstance());
	}
	
	private static void startApp(String[] args) {
		if(startAction != null) {
			// override entry point and use a Swing Action as entry point for application
			try {
				Class<?> actionClazz = Class.forName(startAction, true, PluginManager.class.getClassLoader());
				if(Action.class.isAssignableFrom(actionClazz)) {
					final Action action = (Action)actionClazz.getDeclaredConstructor().newInstance();
					
					final Runnable onEDT = () -> {
						final ActionEvent ae = new ActionEvent(new Object(), 0, "startAction");
						action.actionPerformed(ae);
					};
					SwingUtilities.invokeLater(onEDT);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				LogUtil.severe( e.getLocalizedMessage(), e);
			}
		} else {
			LogUtil.info("Starting " + initialEntryPoint);
			final PluginEntryPointRunner entryPtRunner =
					new PluginEntryPointRunner(initialEntryPoint);
			try {
				final EntryPointArgs entryPointArgs = new EntryPointArgs();
				entryPointArgs.parseArgs(args);
				entryPtRunner.setArgs(entryPointArgs);
				entryPtRunner.executePlugin();
			} catch (PluginException e) {
				LogUtil.severe( e.getMessage(), e);
			}
		}
	}

}
