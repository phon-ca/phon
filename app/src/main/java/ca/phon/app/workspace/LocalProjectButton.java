/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.workspace;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import ca.phon.ui.MultiActionButton;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

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
		
		PhonWorker.getInstance().invokeLater(new ProjectSizeCalcTask());
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
//		super.setBackground(c);

//		if(getBackgroundPainter() != null) {
			//((BgPainter)getBackgroundPainter()).origColor = c;
		if(bgPainter != null) {
			bgPainter.origColor = c;
			repaint();
		}
	}
	
	private void updateLabels() {
		String projPath = 
			projectFile.getName();
//		if(filterPattern != null) {
//			Pattern p = Pattern.compile(filterPattern);
//			Matcher m = p.matcher(projPath);
//			if(m.find()) {
//				int s = m.start();
//				int e = m.end();
//				
//				String p1 = projPath.substring(0, s);
//				String p2 = projPath.substring(s, e);
//				String p3 = projPath.substring(e);
//				
//				projPath = p1 + "<font style='background-color: blue;'>" + p2 + "</font>" + p3;
//			}
//		}
		getTopLabel().setText(WorkspaceTextStyler.toHeaderText(projPath));
		getTopLabel().setFont(FontPreferences.getTitleFont());
		getTopLabel().setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		
		SimpleDateFormat sdf =
			new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date modDate = new Date(projectFile.lastModified());
		
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		
		String modStr = 
			"Modified: " + sdf.format(modDate);
		
		String sizeStr = "Size: ";
		if(!projSizeCalculated) {
			sizeStr += "Calculating...";
		} else {
			projSizeLock.lock();
			sizeStr += getSizeString(projSize);
			projSizeLock.unlock();
		}
		
		String detailsStr =modStr + " &#8226; " + sizeStr;
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
			for(File lf:f.listFiles()) {
				retVal += getSize(lf);
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
