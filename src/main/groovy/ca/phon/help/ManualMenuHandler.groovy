package ca.phon.help

import java.awt.Window;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ca.phon.app.VersionInfo;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.plugin.PhonPlugin;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.OpenFileLauncher;

/**
 * Adds menu entries for opening Phon pdf manuals.
 *
 */
class ManualMenuHandler implements IPluginMenuFilter {

	private final static APP_MANUAL = "META-INF/doc/pdf/ApplicationManual.pdf";
	
	private final static IPA_MANUAL = "META-INF/doc/pdf/src/main/dita/phon-ipa.pdf";
	
	private final static API_URL = "https://www.phon.ca/phon_" +
		VersionInfo.getInstance().getVersion().replaceAll("\\.", "_") + "_apidocs";
	
	public void filterWindowMenu(Window owner, JMenuBar menuBar) {
		JMenu menu = null;
		for(int i = 0; i < menuBar.getMenuCount(); i++) {
			if(menuBar.getMenu(i).getText().equals("Help")) {
				menu = menuBar.getMenu(i);
				break;
			}
		}
		assert menu != null;
		
		PhonUIAction action = new PhonUIAction(ManualMenuHandler.class, "onShowManual");
		action.setData(APP_MANUAL);
		action.putValue(PhonUIAction.NAME, "Phon Manual");
		JMenuItem manualItem = new JMenuItem(action);
		
		action = new PhonUIAction(ManualMenuHandler.class, "onShowManual");
		action.setData(IPA_MANUAL);
		action.putValue(PhonUIAction.NAME, "IPA & Phonex Reference");
		JMenuItem ipaItem = new JMenuItem(action);
		
		action = new PhonUIAction(ManualMenuHandler.class, "onShowAPI");
		action.setData(API_URL);
		action.putValue(PhonUIAction.NAME, "Phon " + VersionInfo.getInstance().getVersion() + " API");
		action.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open Phon api in a new browser (required online access)");
		JMenuItem apiItem = new JMenuItem(action);
		
		menu.add(manualItem, 0);
		menu.add(ipaItem, 1);
		menu.add(apiItem, 2);
	}
	
	public static void onShowAPI(String url) {
		OpenFileLauncher.openURL(new java.net.URL(url));
	}
	
	public static void onShowManual(String manual) {
		File tempFile = extractTextFile(manual);
		assert tempFile != null;
		showPDF(tempFile);
	}

	private static File extractTextFile(String source) 
		throws IOException {
		final ClassLoader cl = ManualMenuHandler.class.classLoader;
		
		final InputStream is = cl.getResourceAsStream(source);
		assert is != null;
		
		final File retVal = File.createTempFile("manual", ".pdf");
		retVal.mkdirs();
		
		final FileOutputStream fos = new FileOutputStream(retVal);
		final byte[] buffer = new byte[1024];
		int read = -1;
		while((read = is.read(buffer)) >= 0 ) {
			fos.write(buffer, 0, read);
		}
		fos.flush();
		fos.close();
		is.close();
	
		return retVal;
	}
		
	private static void showPDF(File pdfFile) {
		OpenFileLauncher.openURL(pdfFile.toURI().toURL());
	}
}

/**
 * Phon extension point
 *
 */
@PhonPlugin
class ManualMenuHandlerExtPt implements IPluginExtensionPoint<IPluginMenuFilter> {
	
	def factory = new IPluginExtensionFactory<IPluginMenuFilter>() {
		@Override
		public IPluginMenuFilter createObject(Object... args) {
			return new ManualMenuHandler();
		}
	};

	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return factory;
	}
	
}
