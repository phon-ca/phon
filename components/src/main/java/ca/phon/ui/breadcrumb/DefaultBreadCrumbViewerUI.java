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
package ca.phon.ui.breadcrumb;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

/**
 * Default UI implementation for {@link BreadCrumbViewer}
 *
 */
public class DefaultBreadCrumbViewerUI extends BreadCrumbViewerUI {
	
	private final static Color BREADCRUMB_BACKGROUND = UIManager.getColor("Panel.background");
	
	private final static Color CURRENT_STATE_BACKGROUND = UIManager.getColor("List.selectionBackground");
	private final static Color CURRENT_STATE_FOREGROUND = UIManager.getColor("List.selectionForeground");
	
	private final static Color STATE_BACKGROUND = UIManager.getColor("Button.background");
	private final static Color STATE_FOREGROUND = UIManager.getColor("Button.foreground");
	
	private BreadCrumbViewer<? super Object, ? super Object> breadCrumbViewer;
	
	private final List<Rectangle> stateRects = new ArrayList<>();

	@SuppressWarnings("unchecked")
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		
		if(!(c instanceof BreadCrumbViewer))
			throw new IllegalArgumentException("Component must be of type BreadCrumbViewer");
		
		breadCrumbViewer = (BreadCrumbViewer<? super Object, ? super Object>)c;
		if(breadCrumbViewer.getStateRenderer() == null)
			breadCrumbViewer.setStateRenderer(new DefaultBreadCrumbStateRenderer<>());
		
		breadCrumbViewer.setBackground(BREADCRUMB_BACKGROUND);
		
		breadCrumbViewer.setStateBackground(STATE_BACKGROUND);
		breadCrumbViewer.setStateForeground(STATE_FOREGROUND);
		
		breadCrumbViewer.setCurrentStateForeground(CURRENT_STATE_FOREGROUND);
		breadCrumbViewer.setCurrentStateBackground(CURRENT_STATE_BACKGROUND);
		
		breadCrumbViewer.addMouseListener(mouseInputAdapter);
		breadCrumbViewer.addPropertyChangeListener("model", (e) -> 
			breadCrumbViewer.getBreadcrumb().addBreadcrumbListener(breadCrumbListener) );
		breadCrumbViewer.getBreadcrumb().addBreadcrumbListener(breadCrumbListener);
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		
		breadCrumbViewer.removeMouseListener(mouseInputAdapter);
		breadCrumbViewer.getBreadcrumb().removeBreadcrumbListener(breadCrumbListener);
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		g.fillRect(0, 0, breadCrumbViewer.getWidth(), breadCrumbViewer.getHeight());
		
		final BreadCrumb<Object, Object> breadcrumb = breadCrumbViewer.getBreadcrumb();
		synchronized(breadCrumbViewer.getBreadcrumb()) {
			final BreadCrumbStateRenderer<Object, Object> stateRenderer = breadCrumbViewer.getStateRenderer();
			
			int x = 0;
			stateRects.clear();
			for(int stateIndex = breadcrumb.size()-1; stateIndex >= 0; stateIndex--) {
				final Object state = breadcrumb.getStates().get(stateIndex);
				final Object value = breadcrumb.getValues().get(stateIndex);
				
				final JComponent comp = stateRenderer.createStateComponent(breadCrumbViewer, stateIndex, state, value, false);
				final Rectangle compRect = new Rectangle(x, 0, comp.getPreferredSize().width, comp.getPreferredSize().height);
				SwingUtilities.paintComponent(g, comp, breadCrumbViewer, compRect);
				x += compRect.width;
				
				stateRects.add(compRect);
			}
		}
	}
	
	@Override
	public Dimension getPreferredSize() {
		int width = 0;
		int height = 0;
		
		final BreadCrumb<Object, Object> breadcrumb = breadCrumbViewer.getBreadcrumb();
		for(int i = breadcrumb.size()-1; i >= 0; i--) {
			final Object state = breadCrumbViewer.getBreadcrumb().getStates().get(i);
			final JComponent comp = breadCrumbViewer.getStateRenderer().createStateComponent(breadCrumbViewer, i, state,
					breadCrumbViewer.getBreadcrumb().getValues().get(i), false);
			width += comp.getPreferredSize().width;
			height = Math.max(height, comp.getPreferredSize().height);
		}
		
		return new Dimension(width, height);
	}

	@Override
	public int locationToStateIndex(Point p) {
		int retVal = -1;
		
		for(int stateIndex = 0; stateIndex < stateRects.size(); stateIndex++) {
			final Rectangle rect = stateRects.get(stateIndex);
			if(rect.contains(p)) {
				retVal = stateIndex;
				break;
			}
		}
		
		return retVal;
	}
	
	private BreadCrumbListener<? super Object, ? super Object> breadCrumbListener = (evt) -> {
		breadCrumbViewer.revalidate();
	};
	
	private MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {

		@Override
		public void mouseClicked(MouseEvent e) {
			final Point p = e.getPoint();
			final BreadCrumb<Object, Object> breadcrumb = breadCrumbViewer.getBreadcrumb();
			for(int i = 0; i < stateRects.size(); i++) {
				if(stateRects.get(i).contains(p)) {
					breadcrumb.gotoState(breadcrumb.getStates().get(breadcrumb.size()-(i+1)));
					break;
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mouseDragged(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mouseMoved(e);
		}
		
	};
	
}
