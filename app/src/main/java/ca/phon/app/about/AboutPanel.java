/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.about;

import java.awt.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import ca.phon.app.*;
import ca.phon.app.VersionInfo;
import ca.phon.extensions.*;
import ca.phon.ui.*;
import ca.phon.ui.fonts.*;
import ca.phon.util.*;

public class AboutPanel extends JPanel implements IExtendable {

	private final static String DEFAULT_SPLASH_IMAGE = "data/phonboot.png";

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(AboutPanel.class.getName());

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
			LOGGER.error( e.getMessage(), e);
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
		Font versionFont = FontPreferences.getMonospaceFont();
		g.setFont(versionFont);

		String vString = VersionInfo.getInstance().getVersionNoBuild();
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
					_instance = new CommonModuleFrame("About Phon") {
						@Override
						public void setJMenuBar(JMenuBar menuBar) {
							if(!OSInfo.isMacOs()) {
								super.setJMenuBar(null);
							} else {
								super.setJMenuBar(menuBar);
							}
						}
					};
					_instance.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					_instance.setWindowName("About Phon");
//					_instance.setUndecorated(true);
					AboutPanel panel = new AboutPanel(image);

					_instance.getContentPane().setLayout(new BorderLayout());
					_instance.getContentPane().add(panel, BorderLayout.CENTER);

					_instance.pack();
					
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
