/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

public class SnapshotFrame extends JFrame {
	
	private SnapshotGlassPane glassPane = new SnapshotGlassPane();
	
	public SnapshotFrame(String title) {
		super(title);
		
		SnapshotMouseListener listener = new SnapshotMouseListener();
		glassPane.addMouseListener(listener);
		glassPane.addMouseMotionListener(listener);
	}
	
	private JComponent cachedGlassPane = null;
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		
		if(System.getProperty("ca.phon.snapshot", "false").equals("true")) {
			JMenuItem toggleSnapshotItem = new JMenuItem("Toggle Snapshot Mode");
			toggleSnapshotItem.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					if(getGlassPane() != glassPane) {
						cachedGlassPane = (JComponent)getGlassPane();
						setGlassPane(glassPane);
						glassPane.setVisible(true);
					} else {
						if(cachedGlassPane != null) {
							setGlassPane(cachedGlassPane);
							cachedGlassPane.setVisible(false);
						}
						glassPane.setVisible(false);
					}
				}
				
			});
			// if the menu has a 'Window' menu
			// add an entry to it for toggling the snapshot mode
			for(int i = 0; i < menuBar.getMenuCount(); i++) {
				JMenu menu = menuBar.getMenu(i);
				
				if(menu.getText().equals("View")) {
					menu.addSeparator();
					menu.add(toggleSnapshotItem);
				}
			}
		}
		super.setJMenuBar(menuBar);
	}
	
	protected String getNextAutoImageName() {
		String userHome = System.getProperty("user.home");
		String desktop = userHome + File.separator + "Desktop";
		
		int fileIndex = 1;
		String filePrefix = desktop + File.separator + "Picture ";
		String fileSuffix = ".png";
		
		for(;;) {
			File f = new File(filePrefix + (fileIndex++) + fileSuffix);
			if(!f.exists())
				return f.getAbsolutePath();
		}
	}
	
	protected void saveComponentSnapshot(Component comp, String file) {
		BufferedImage img = (BufferedImage)comp.createImage(comp.getWidth(), comp.getHeight());
		Graphics2D g2 = img.createGraphics();
		comp.paint(g2);
		
		try {
			ImageIO.write(img, "PNG", new File(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private JComponent getDeepestComponentAt(Point p) {
		JComponent comp = 
			(JComponent)getContentPane().getComponentAt(p);
		
		while(comp != null && comp.getComponentCount() != 0) {
			p = SwingUtilities.convertPoint(comp.getParent(), p, comp);
			
			if(!(comp.getComponentAt(p) instanceof JComponent))
				break;
			
			JComponent subComp = 
				(JComponent)comp.getComponentAt(p);
//			System.out.println(subComp);
			if(subComp != null && subComp != comp) 
				comp = subComp;
			else
				break;
		}
		
		if(comp == null)
			comp = (JComponent)getContentPane();
		
		return comp;
	}
	
	private class SnapshotMouseListener extends MouseInputAdapter {

		@Override
		public void mouseMoved(MouseEvent e) {
			super.mouseMoved(e);
			
			Point mousePoint = e.getPoint();
			
			JComponent comp = null;
			int mask = InputEvent.CTRL_DOWN_MASK;
			if((e.getModifiersEx() & mask) == mask)
				comp =
					(JComponent)getContentPane().getComponentAt(mousePoint);
			else
				comp = 
					getDeepestComponentAt(mousePoint);
			
			if(comp != null) {
				glassPane.setExcludedComp(comp);
				glassPane.repaint();
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			super.mouseClicked(e);
			
			Point mousePoint = e.getPoint();

			JComponent comp = null;
			int mask = InputEvent.CTRL_DOWN_MASK;
			if((e.getModifiersEx() & mask) == mask)
				comp =
					(JComponent)getContentPane().getComponentAt(mousePoint);
			else
				comp = 
					getDeepestComponentAt(mousePoint);
				
			
			if(comp != null) {
				saveComponentSnapshot(comp, getNextAutoImageName());
			}
		}
		
	}

	private class SnapshotGlassPane extends JComponent {
		
		/** The background colour */
		private final Color bgColor = new Color(100, 100, 100, 100);
		
//		public Rectangle excludedRect = null;
		private JComponent excludedComp = null;
		
		public SnapshotGlassPane() {
			super();
		}
		
//		public void addExcludedRect(Rectangle r) {
//			// convert coords
//			if(r != null) 
//				r = SwingUtilities.convertRectangle(getContentPane(), r, this);
//			excludedRect = r;
//		}
		
		public void setExcludedComp(JComponent comp) {
			excludedComp = comp;
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Rectangle bounds = this.getBounds();
			
			Graphics2D g2 = (Graphics2D)g;
			
			Area fillArea = new Area(bounds);
			Rectangle excludedRect = null;
			if(excludedComp != null) {
				excludedRect = excludedComp.getBounds();
				excludedRect = SwingUtilities.convertRectangle(excludedComp.getParent(), excludedRect, this);
			}
			if(excludedRect != null)
				fillArea.subtract(new Area(excludedRect));
			
			g2.setColor(bgColor);
			g2.fill(fillArea);
			
			g2.setColor(Color.DARK_GRAY);
			if(excludedRect != null)
				g2.draw(excludedRect);
		}
	}
}
