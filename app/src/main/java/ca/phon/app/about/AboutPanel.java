/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.about;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.phon.app.PhonSplasher;
import ca.phon.app.VersionInfo;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;

public class AboutPanel extends JPanel implements IExtendable {

	private final static String DEFAULT_SPLASH_IMAGE = "data/phonboot.png";

	private final static Logger LOGGER = Logger.getLogger(AboutPanel.class.getName());

	private final ExtensionSupport extSupport = new ExtensionSupport(AboutPanel.class, this);

	private Image bootImage;

	public AboutPanel(Image img) {
		super();

		bootImage = img;

		setBackground(new Color(0,0,0,0));

		MediaTracker mt = new MediaTracker(this);
		mt.addImage(bootImage, 0);

		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

		setPreferredSize(new Dimension(bootImage.getWidth(this), bootImage.getHeight(this)));
		init();

		setFocusable(true);

		extSupport.initExtensions();
	}

	private void init() {
		setLayout(null);
	}

	private static volatile boolean paintCalled = false;
//	public static boolean paintCalled = false;
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		g.drawImage(bootImage, 0, 0, this);

		// paint the version string
		Font versionFont = FontPreferences.getControlFont();
		g.setFont(versionFont);

		String vString = VersionInfo.getInstance().getVersion();
		FontMetrics fm = g.getFontMetrics();
		int vWidth = (int)fm.getStringBounds(vString, g).getWidth();
		g.setColor(Color.black);
		g.drawString(vString, 400-vWidth-20, 250-20);

		if(!paintCalled)
			paintCalled = true;
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	private static CommonModuleFrame _instance;

	public static void splash() {
		final String splashImage = PrefHelper.get(PhonSplasher.SPLASH_IMAGE_PROPERTY, DEFAULT_SPLASH_IMAGE);
		splash(AboutPanel.class.getClassLoader().getResource(splashImage));
	}

	public static void splash(URL imageURL) {
		if(imageURL != null) {
			splash(Toolkit.getDefaultToolkit().createImage(imageURL));
		}
	}

	public static void splash(final Image image) {
		if(_instance == null) {
			final Runnable onEDT = new Runnable() {

				@Override
				public void run() {
					_instance = new CommonModuleFrame("About Phon");
					_instance.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					_instance.setWindowName("About Phon");
//					_instance.setUndecorated(true);
					AboutPanel panel = new AboutPanel(image);

					_instance.getContentPane().setLayout(new BorderLayout());
					_instance.getContentPane().add(panel, BorderLayout.CENTER);

					_instance.pack();
					if(!OSInfo.isMacOs()) {
						_instance.setSize(_instance.getWidth(), _instance.getHeight()+_instance.getJMenuBar().getHeight());
					}
//					_instance.setLocationByPlatform(true);
					_instance.centerWindow();

					_instance.setVisible(true);
					panel.requestFocus();
				}
			};
			SwingUtilities.invokeLater(onEDT);
		} else {
			SwingUtilities.invokeLater( () -> _instance.setVisible(true) );
		}
	}

	public static void disposeSplash() {
		if(_instance != null) {
			_instance.setVisible(false);
		}
	}

}
