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
package ca.phon.app.welcome;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import com.jgoodies.forms.layout.*;

import ca.phon.ui.MultiActionButton;
import ca.phon.worker.*;

/**
 * Displays a project button for the start dialog.
 * 
 * Each button will display the project location,
 * modified date, creation date, and size by default.
 * 
 * A section for actions will be located at the right-hand 
 * side of the component.  Actions can be customized by sub-classes
 * or classes using ProjectButton.
 * 
 */
public class ProjectButton extends MultiActionButton {
	
	/** Project file, should be a directory containing a project.xml file */
	private File projectFile;
	
	/** Default action - performed on mouse click */
	private Action defaultAction = null;
	
	/** Actions for the button */
	private List<Action> buttonActions = new ArrayList<Action>();
	
	private Lock projSizeLock = new ReentrantLock();
	private long projSize = 0L;
	private boolean projSizeCalculated = false;
	
	private JXLabel projPathLabel = new JXLabel();
	private JXLabel projDetailsLabel = new JXLabel();
	
	private String filterPattern = null;
	
	private ProjectButtonBgPainter bgPainter;
	
	public ProjectButton(String projectPath) {
		this(new File(projectPath));
	}
	
	public ProjectButton(File projectFile) {
		this.projectFile = projectFile;

		bgPainter = new ProjectButtonBgPainter();
		super.setBackgroundPainter(bgPainter);
		
		init();
	}
	
	public File getProjectFile() {
		return this.projectFile;
	}
	
	@Override
	public Insets getInsets() {
		Insets retVal = super.getInsets();
		
		retVal.top += 5;
		retVal.bottom += 5;
		retVal.left += 5;
		retVal.right += 5;
		
		return retVal;
	}
	
	@Override
	public void setBackground(Color c) {
		if(bgPainter != null)
			bgPainter.setOrigColor(c);
	//	repaint();
	}
	
	private void init() {
		PhonWorker worker = PhonWorker.getInstance();
		worker.invokeLater(new ProjectSizeCalcTask());
		
		setToolTipText(projectFile.getAbsolutePath());
		
		refreshComponents();
		
		addMouseListener(new ProjectButtonMouseHandler());
	}
	
	private void refreshComponents() {
		String colLayout = "fill:pref:grow";
		for(int i = 0; i < buttonActions.size(); i++) {
			colLayout += ", pref" + 
				(i == buttonActions.size()-1 ? "" : ", 2dlu");
		}
		String rowLayout = "top:pref, pref";
		
		setLayout(new FormLayout(colLayout, rowLayout));
		CellConstraints cc = new CellConstraints();
		
		updateLabels();
		
		add(projPathLabel, cc.xy(1,1));
		add(projDetailsLabel, cc.xy(1,2));
		
		int colIdx = 2;
		for(Action act:buttonActions) {
			add(getActionButton(act), cc.xywh(colIdx++, 1, 1, 1));
			colIdx++;
		}
	}
	
	private void updateLabels() {
		String projPath = 
			projectFile.getName();
		if(filterPattern != null) {
			Pattern p = Pattern.compile(filterPattern);
			Matcher m = p.matcher(projPath);
			if(m.find()) {
				int s = m.start();
				int e = m.end();
				
				String p1 = projPath.substring(0, s);
				String p2 = projPath.substring(s, e);
				String p3 = projPath.substring(e);
				
				projPath = p1 + "<font style='background-color: blue;'>" + p2 + "</font>" + p3;
			}
		}
		projPathLabel.setText(WorkspaceTextStyler.toHeaderText(projPath));
		
		SimpleDateFormat sdf =
			new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date modDate = new Date(projectFile.lastModified());
		
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		
		String modStr = 
			"<i>Modified: </i>" + sdf.format(modDate);
		
		String sizeStr = "<i>Size: </i>";
		if(!projSizeCalculated) {
			sizeStr += "Calculating...";
		} else {
			projSizeLock.lock();
			sizeStr += getSizeString(projSize);
			projSizeLock.unlock();
		}
		
		String detailsStr = modStr + " &#8226; " + sizeStr;
		projDetailsLabel.setText(WorkspaceTextStyler.toDescText(detailsStr));
	}
	
	private static final long  MEGABYTE = 1024L * 1024L;
	public static double bytesToMeg(long bytes) {
		return (double)bytes / (double)MEGABYTE ;
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
	
	private class ProjectButtonMouseHandler extends MouseInputAdapter {
		@Override
		public void mouseEntered(MouseEvent me) {
			bgPainter.useSelected = true;
			ProjectButton.this.repaint();
			setActionButtonsVisible(true);
		}
		
		@Override
		public void mouseExited(MouseEvent me) {
			// still selected if inside bounds...
			Rectangle bounds = new Rectangle(0, 0, 
					ProjectButton.this.getWidth(), ProjectButton.this.getHeight());
			
//			System.out.println(bounds + " " + me);
			if(!bounds.contains(me.getPoint())) {
				bgPainter.useSelected = false;
				ProjectButton.this.repaint();
				setActionButtonsVisible(false);
			}
			
		}
		
		private void setActionButtonsVisible(boolean v) {
			for(int compIdx = 0; compIdx < ProjectButton.this.getComponentCount(); compIdx++) {
				Component c = ProjectButton.this.getComponent(compIdx);
				if(c instanceof JXButton) {
					c.setVisible(v);
				}
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent me) {
			if(me.getButton() == 1) {
				if(defaultAction != null) {
					ActionEvent ae = new ActionEvent(me.getSource(), me.getID(), 
							(String)defaultAction.getValue(Action.ACTION_COMMAND_KEY));
					defaultAction.actionPerformed(ae);
				}
			}
		}
	}
	
	private class ProjectButtonBgPainter implements Painter<JXPanel> {
		
		private Color origColor = null;
		
		private Color selectedColor = new Color(0, 100, 200, 100);
		
		private boolean useSelected = false;
		
		public ProjectButtonBgPainter() {
			setOrigColor(ProjectButton.this.getBackground());
		}
		
		public void setOrigColor(Color c) {
			this.origColor = c;
		}

		@Override
		public void paint(Graphics2D g, JXPanel object, int width, int height) {
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
		
		
	}

}
