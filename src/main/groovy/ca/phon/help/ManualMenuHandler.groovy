package ca.phon.help

import java.awt.Window;

import javax.swing.*

import ca.phon.app.VersionInfo;
import ca.phon.plugin.*
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.OpenFileLauncher;

/**
 * Adds menu entries for opening Phon pdf manuals.
 *
 */
class ManualMenuHandler implements IPluginMenuFilter {

	private final static APP_MANUAL = "META-INF/doc/pdf/phon_application_manual.pdf";
	
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
		action.putValue(PhonUIAction.NAME, "Phon Application Manual");
		JMenuItem manualItem = new JMenuItem(action);
		
		action = new PhonUIAction(ManualMenuHandler.class, "onShowAPI");
		action.setData(API_URL);
		action.putValue(PhonUIAction.NAME, "Phon " + VersionInfo.getInstance().getVersion() + " API");
		action.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open Phon api in a new browser (required online access)");
		JMenuItem apiItem = new JMenuItem(action);
		
		menu.add(manualItem, 0);
		menu.add(apiItem, 1);
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
