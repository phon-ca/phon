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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.concurrent.locks.*;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.app.log.LogUtil;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.*;

import ca.phon.ui.*;
import ca.phon.ui.fonts.*;
import ca.phon.worker.*;

public class LocalProjectButton extends MultiActionButton {

	/** Project file, should be a directory containing a project.xml file */
	private File projectFile;

	private Lock projSizeLock = new ReentrantLock();
	private long projSize = 0L;
	private boolean projSizeCalculated = false;

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

	public void updateProjectSize(PhonWorker worker) {
		worker.invokeLater(new ProjectSizeCalcTask());
	}

	/**
	 * Get the calculated size of the project.
	 * May be 0 if size has not been calculated.
	 */
	public long getProjectSize() {
		return this.projSize;
	}

	/**
	 * Has the sie been calculated
	 */
	public boolean isProjectSizeCalculated() {
		return this.projSizeCalculated;
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
		String sizeStr = "Size: ";
		if(!projSizeCalculated) {
			sizeStr += "Calculating...";
		} else {
			projSizeLock.lock();
			sizeStr += getSizeString(projSize);
			projSizeLock.unlock();
		}

		String detailsStr = modStr + " &#8226; " + sizeStr;
		getBottomLabel().setText(WorkspaceTextStyler.toDescText(detailsStr));
	}

	private class ProjectSizeCalcTask extends PhonTask {

		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);

			long ps = getSize(projectFile);
			projSizeLock.lock();
			projSize = ps;
			projSizeCalculated = true;
			projSizeLock.unlock();

			Runnable r = new Runnable() {
				@Override
				public void run() {
					updateLabels();
				}
			};
			SwingUtilities.invokeLater(r);

			super.setStatus(TaskStatus.FINISHED);
		}

	}

	/**
	 * Returns size of the given file/directory.
	 * If a directory, this method will recusively
	 * traverse and calculate the size of all files.
	 *
	 * @param f
	 * @return the size of the file/directory
	 */
	private long getSize(File f) {
		long retVal = 0;
		if(f.isFile()) {
			// return size of file
			retVal = f.length();
		} else if(f.isDirectory()) {
			File[] files = f.listFiles();
			if(files != null) {
				for (File lf : files) {
					retVal += getSize(lf);
				}
			}
		}
		return retVal;
	}

	/**
	 * Get string from byte size
	 * @param bytes
	 * @return
	 */
	private String getSizeString(long bytes) {
		int kb = 1024;
		int mb = kb * 1024;
		int gb = mb * 1024;

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);

		String retVal = bytes + " B";
		if(bytes > gb) {
			double numgbs = (double)bytes/(double)gb;
			retVal = nf.format(numgbs) + " GB";
		} else if(bytes > mb) {
			double nummbs = (double)bytes/(double)mb;
			retVal = nf.format(nummbs) + " MB";
		} else if(bytes > kb) {
			double numkbs = (double)bytes/(double)kb;
			retVal = nf.format(numkbs) + " KB";
		}

		return retVal;
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
//				GradientPaint gp = new GradientPaint(
//						(float)0, 0.0f, new Color(237,243, 254), (float)0.0f, (float)height, new Color(207, 213, 224), true);
//				MattePainter bgPainter = new MattePainter(gp);
//				bgPainter.paint(g, object, width, height);
//
//				NeonBorderEffect effect  = new NeonBorderEffect();
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
