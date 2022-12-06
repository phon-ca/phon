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
package ca.phon.app.welcome;

import ca.phon.app.log.LogUtil;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.worker.*;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.*;

public class LocalProjectButton extends MultiActionButton {

	/** Project file, should be a directory containing a project.xml file */
	private File projectFile;

	private BgPainter bgPainter = new BgPainter();

	/**
	 * Constructor
	 */
	public LocalProjectButton(File projFile) {
		super();
		this.projectFile = projFile;

		updateLabels();
		setBackgroundPainter(bgPainter);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	@Override
	public void setBackground(Color c) {
		if(bgPainter != null) {
			bgPainter.origColor = c;
			repaint();
		}
	}

	private void updateLabels() {
		String projPath =
			projectFile.getName();
		
		getTopLabel().setText(WorkspaceTextStyler.toHeaderText(projPath));
		getTopLabel().setFont(FontPreferences.getTitleFont());
		getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		Path nioPath = projectFile.toPath();
		ZonedDateTime zonedDate = null;
		try {
			BasicFileAttributes fileAttribs = Files.readAttributes(nioPath, BasicFileAttributes.class);
			FileTime ft = fileAttribs.lastModifiedTime();
			LocalDateTime ldt = ft.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			zonedDate = ldt.atZone(ZoneId.systemDefault());
		} catch (IOException e) {
			LogUtil.warning(e);
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss (zzz)");

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);

		String modStr =	"Modified: " + (zonedDate != null ? formatter.format(zonedDate) : " ??? ");
		getBottomLabel().setText(WorkspaceTextStyler.toDescText(modStr));
	}

	/**
	 * Get the project file
	 */
	public File getProjectFile() {
		return this.projectFile;
	}

	/**
	 * Background painter
	 */
	private class BgPainter extends MouseInputAdapter implements Painter<LocalProjectButton> {

		private boolean useSelected = false;

		private Color origColor = null;

		private Color selectedColor = new Color(0, 100, 200, 100);

		private boolean paintPressed = false;

		public BgPainter() {
			LocalProjectButton.this.addMouseListener(this);
			this.origColor = LocalProjectButton.this.getBackground();
		}

		@Override
		public void paint(Graphics2D g, LocalProjectButton object, int width,
				int height) {
			// create gradient
			g.setColor((origColor != null ? origColor : Color.white));
			g.fillRect(0, 0, width, height);

			if(useSelected) {
				GlowPathEffect effect = new GlowPathEffect();
				effect.setRenderInsideShape(true);
				effect.setBrushColor(selectedColor);

				// get rectangle
				Rectangle2D.Double boundRect =
					new Rectangle2D.Double(0.0f, 0.0f, width, height);

				effect.apply(g, boundRect, 0, 0);
			}

		}

		@Override
		public void mouseEntered(MouseEvent me) {
			useSelected = true;
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent me) {
			useSelected = false;
			repaint();
		}

		@Override
		public void mousePressed(MouseEvent me) {
			if(me.getButton() == MouseEvent.BUTTON1) {
				paintPressed = true;
				repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			paintPressed = false;
			repaint();
		}
	}

	@Override
	public Dimension getMaximumSize() {
		Dimension retVal = super.getMaximumSize();
		Dimension prefVal = super.getPreferredSize();

		retVal.height = prefVal.height;

		return retVal;
	}

}
