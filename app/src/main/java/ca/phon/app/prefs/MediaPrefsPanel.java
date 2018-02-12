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
package ca.phon.app.prefs;

import java.awt.BorderLayout;
import java.beans.*;
import java.util.List;

import javax.swing.*;

import com.jgoodies.forms.layout.*;

import ca.phon.media.util.MediaLocator;

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
