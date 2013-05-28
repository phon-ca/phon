package ca.phon.gui.ipamap;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.UIManager;

import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Setup IPA Map as a system tray application.
 * 
 */
public class IpaMapApp {
	
	private static final Logger LOGGER = Logger.getLogger(IpaMapApp.class
			.getName());
	
	/**
	 * IPA Map frame
	 */
	private IpaMapFrame mapFrame;

	/**
	 * Get the map frame
	 */
	public IpaMapFrame getMapFrame() {
		if(mapFrame == null) {
			mapFrame = new IpaMapFrame();
			mapFrame.pack();
			mapFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			mapFrame.getMapContents().addListener(new IpaMapRobot());
		}
		return mapFrame;
	}
	
	/**
	 * Toggle frame
	 */
	public void toggleFrame() {
		if(getMapFrame().isVisible())
			getMapFrame().setVisible(false);
		else
			getMapFrame().showWindow();
	}
	
	/**
	 * Exit application
	 */
	public void quit() {
		final IpaMapFrame mapFrame = getMapFrame();
		mapFrame.setVisible(true);
		mapFrame.dispose();
		
		System.exit(0);
	}
	
	/**
	 * Get the context menu for the frame.
	 */
	public JPopupMenu getContextMenu() {
		final JPopupMenu menu = new JPopupMenu();
		
		final IpaMap map = getMapFrame().getMapContents();
		map.setupContextMenu(menu, map);
		menu.addSeparator();
		
		// add exit and toggle window menu items
		final PhonUIAction toggleWindowAction = 
				new PhonUIAction("Toggle map frame", this, "toggleFrame");
		menu.add(toggleWindowAction);
		
		final PhonUIAction quitAppAction =
				new PhonUIAction("Exit", this, "quit");
		menu.add(quitAppAction);
		
		return menu;
	}
	
	/**
	 * Main
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		// load icon
		final ImageIcon appIcn =
				IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL);
		
		// init app
		final IpaMapApp app = new IpaMapApp();
		
		// setup system tray
		if(SystemTray.isSupported()) {
			final SystemTray systemTray = SystemTray.getSystemTray();
			
			final TrayIcon trayIcon = 
					new TrayIcon(appIcn.getImage(), "IPA Map");
			trayIcon.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					if(e.isPopupTrigger()) {
						showContextMenu(e);
					} else {
						if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
							app.toggleFrame();
						}
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if(e.isPopupTrigger()) {
						showContextMenu(e);
					}
				}
				
				private void showContextMenu(MouseEvent e) {
					final JPopupMenu menu = app.getContextMenu();
					menu.setInvoker(menu);
					menu.setLocation(e.getX(), e.getY());
					menu.setVisible(true);
				}
			});
			try {
				systemTray.add(trayIcon);
			} catch (AWTException e) {
				LOGGER.severe(e.getMessage());
				app.quit();
			}
			
		} else {
			LOGGER.severe("System tray not supported.");
			System.exit(1);
		}
		
	}
	
}
