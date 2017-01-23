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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * Default {@link BreadCrumb} state renderer.
 *
 * @param <S>
 * @param <V>
 */
public class DefaultBreadCrumbStateRenderer<S, V> extends JLabel implements BreadCrumbStateRenderer<S, V> {
	
	private static final long serialVersionUID = 2519454994582489644L;
	
	public DefaultBreadCrumbStateRenderer() {
		super();
		
		setOpaque(false);
	}

	@Override
	public JComponent createStateComponent(BreadCrumbViewer<S, V> viewer, int stateIdx, S state, V value,
			boolean hasFocus) {
		
		setFont(viewer.getFont());
		setText(value.toString());
		
		if(stateIdx == viewer.getBreadcrumb().size()-1) {
			setBorder(new BreadCrumbStateBorder(false));
		} else {
			setBorder(new BreadCrumbStateBorder());
		}
		
		if(stateIdx == 0) {
			setBackground(viewer.getCurrentStateBackground());
			setForeground(viewer.getCurrentStateForeground());
		} else {
			setBackground(viewer.getStateBackground());
			setForeground(viewer.getStateForeground());
		}
		
		return this;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		final Insets insets = getInsets();
		final Rectangle2D rect = new Rectangle2D.Double(insets.left, 0, getWidth()-insets.right-insets.left, getHeight());
		
		g2.setColor(getBackground());
		g2.fill(rect);
		
		super.paintComponent(g2);
	}

}
