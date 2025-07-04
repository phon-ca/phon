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

import ca.phon.ui.MultiActionButton;
import ca.phon.worker.*;
import com.jgoodies.forms.layout.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.effects.GlowPathEffect;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.regex.*;

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
	}
	
	private void init() {
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
		projDetailsLabel.setText(WorkspaceTextStyler.toDescText(modStr));
	}
	
	private static final long  MEGABYTE = 1024L * 1024L;
	public static double bytesToMeg(long bytes) {
		return (double)bytes / (double)MEGABYTE ;
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
