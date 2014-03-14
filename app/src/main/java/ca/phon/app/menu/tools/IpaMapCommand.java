package ca.phon.app.menu.tools;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ca.phon.ipamap.IPAMapEP;
import ca.phon.ipamap.IpaMap;
import ca.phon.plugin.PluginAction;

/**
 * Toggle visiblity of the {@link IpaMap}
 *
 */
public class IpaMapCommand extends PluginAction {
	
	private static final long serialVersionUID = -4547303616567952540L;

	public IpaMapCommand() {
		super(IPAMapEP.EP_NAME);
		putValue(Action.NAME, "IPA Map");
		putValue(Action.SHORT_DESCRIPTION, "IPA Map");
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
	
}
