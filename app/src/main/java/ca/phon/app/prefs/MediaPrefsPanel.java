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
package ca.phon.app.prefs;

import ca.phon.media.MediaLocator;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Edit prefs for media such as media search paths.
 *
 */
public class MediaPrefsPanel extends PrefsPanel {

	private static final long serialVersionUID = -4239108936828092749L;

	private PathListPanel pathListPanel;

	public MediaPrefsPanel() {
		super("Media");

		init();
	}

	private void init() {
		List<String> mediaPaths = MediaLocator.getMediaIncludePaths();
		pathListPanel = new PathListPanel(mediaPaths);
		pathListPanel.addPropertyChangeListener(PathListPanel.PATH_LIST_CHANGED_PROP, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				MediaLocator.setMediaIncludePaths(pathListPanel.getPaths());
			}

		});


		String mediaLblTxt = "<html>" +
				"<p>The default location for media is the project's __res/media folder.<br>" +
				"Phon will search the default media folder followed by the paths listed below if the<br> full path to media is not specified in Session Information.</p>" +
				"" +
				"</html>";

		JPanel mediaPathPanel = new JPanel(new BorderLayout());
		mediaPathPanel.add(new JLabel(mediaLblTxt), BorderLayout.NORTH);
		mediaPathPanel.add(pathListPanel);

		mediaPathPanel.setBorder(BorderFactory.createTitledBorder("Media Folders"));

		FormLayout layout = new FormLayout(
				"fill:pref:grow", "pref");
		CellConstraints cc = new CellConstraints();
		setLayout(layout);

		add(mediaPathPanel, cc.xy(1,1));
	}
}
