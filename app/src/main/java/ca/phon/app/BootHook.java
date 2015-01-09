package ca.phon.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.hooks.PhonBootHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.util.OSInfo;

/**
 * Setup appliation environment using resource files
 * found in the META-INF folder.
 *
 */
@PhonPlugin(name="default", minPhonVersion="1.6.2")
public class BootHook implements IPluginExtensionPoint<PhonBootHook>, PhonBootHook {
	
	private final Logger LOGGER = Logger.getLogger(BootHook.class.getName());
	
	/* 
	 * Resource files
	 */
	private final static String PHON_VM_OPTIONS_FILE = "Phon.vmoptions";
	private final static String VM_OPTIONS_FILE = "META-INF/environment/$OS/vmoptions";
	private final static String VM_ENV_FILE = "META-INF/environment/$OS/env";

	private Enumeration<URL> getResourceURLs(String resource) {
		final String os =
				(OSInfo.isWindows() ? "window" : (OSInfo.isMacOs() ? "mac" : "unix"));
		final String respath = 
				resource.replaceAll("\\$OS", os);
		
		Enumeration<URL> retVal = null;
		try {
			retVal = ClassLoader.getSystemClassLoader().getResources(respath);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return retVal;
	}
	
	@Override
	public void setupVMOptions(List<String> cmd) {
		loadFromResourcePath(cmd, PHON_VM_OPTIONS_FILE);
		loadFromResourcePath(cmd, VM_OPTIONS_FILE);
	}
	
	private void loadFromResourcePath(List<String> cmd, String path) {
		final Enumeration<URL> optURLs = getResourceURLs(path);
		while(optURLs.hasMoreElements()) {
			URL url = optURLs.nextElement();
			LOGGER.info("Loading vmoptions from URL " + url.toString());
			
			try {
				final InputStream is = url.openStream();
				final BufferedReader isr = new BufferedReader(new InputStreamReader(is));
				String vmopt = null;
				while((vmopt = isr.readLine()) != null) {
					if(vmopt.startsWith("#")) continue;
					cmd.add(vmopt);
				}
				isr.close();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void setupEnvironment(Map<String, String> environment) {
		final String libPath = System.getProperty("java.library.path");
		final Enumeration<URL> envURLs = getResourceURLs(VM_ENV_FILE);
		while(envURLs.hasMoreElements()) {
			URL url = envURLs.nextElement();
			LOGGER.info("Loading environment settings from URL " + url.toString());
			
			try {
				final InputStream is = url.openStream();
				final BufferedReader isr = new BufferedReader(new InputStreamReader(is));
				String envOpt = null;
				while((envOpt = isr.readLine()) != null) {
					String[] opt = envOpt.split("=");
					if(opt.length != 2) continue;
					String key = opt[0];
					String val = opt[1];
					if(key.endsWith("+")) {
						key = key.substring(0, key.length()-1);
						val = environment.get(key) + val;
					}
					environment.put(key, val);
				}
				isr.close();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		
		// windows needs libPath include in the PATH var
		if(OSInfo.isWindows()) {
			String path = environment.get("PATH");
			path += ";\"" + libPath + "\"";
			environment.put("PATH", path);
		}
	}
	
	@Override
	public Class<?> getExtensionType() {
		return PhonBootHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonBootHook> getFactory() {
		return factory;
	}

	private final IPluginExtensionFactory<PhonBootHook> factory = new IPluginExtensionFactory<PhonBootHook>() {
		
		@Override
		public PhonBootHook createObject(Object... args) {
			return BootHook.this;
		}
		
	};
}
