package ca.phon.app.about;

import java.awt.Toolkit;
import java.awt.Window;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ca.phon.app.log.LogUtil;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.plugin.PhonPlugin;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.PrefHelper;

@PhonPlugin(author="Greg J. Hedlund", comments="Add manual items to Help menu", minPhonVersion="2.2.0", name="HelpMenuHandler", version="1")
public class HelpMenuHandler implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {

	// use github pages mirror
	

	private final static String ONLINE_MANUAL = "phon-manual/misc/Welcome.html";

	public HelpMenuHandler() {
	}

	public void filterWindowMenu(Window owner, JMenuBar menuBar) {
		JMenu menu = null;
		for(int i = 0; i < menuBar.getMenuCount(); i++) {
			if(menuBar.getMenu(i).getText().equals("Help")) {
				menu = menuBar.getMenu(i);
				break;
			}
		}
		assert menu != null;
		
		final String path = PrefHelper.get(HelpLink.WEBSITE_ROOT_PROP, HelpLink.DEFAULT_WEBSITE_ROOT) + ONLINE_MANUAL;
		final PhonUIAction showOnlineManualAct = new PhonUIAction(HelpMenuHandler.class, "showOnlineManual", path);
		showOnlineManualAct.putValue(PhonUIAction.NAME, "Show manual (online)...");
		showOnlineManualAct.putValue(PhonUIAction.SHORT_DESCRIPTION, path);

		menu.add(new JMenuItem(showOnlineManualAct), 0);
	}

	public static void showOnlineManual(String path) {
		try {
			OpenFileLauncher.openURL(new URL(path));
		} catch (MalformedURLException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	}

	public Class<?> getExtensionType() {
		return IPluginMenuFilter.class;
	}

	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
		return (args) -> this;
	}

}
