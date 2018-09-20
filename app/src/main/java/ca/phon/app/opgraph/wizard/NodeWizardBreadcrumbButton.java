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
package ca.phon.app.opgraph.wizard;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.UIManager;

import ca.phon.ui.GUIHelper;
import ca.phon.ui.jbreadcrumb.BreadcrumbButton;
import ca.phon.ui.jbreadcrumb.BreadcrumbStateBorder;

/**
 * @deprecated Use {@link BreadcrumbButton}
 */
@Deprecated
public class NodeWizardBreadcrumbButton extends JButton {

	private static final long serialVersionUID = -8963234070761177007L;
	
	public NodeWizardBreadcrumbButton() {
		super();
		
		setOpaque(false);
		setBorder(new BreadcrumbStateBorder());
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		final Insets insets = getInsets();
		final Rectangle2D rect = new Rectangle2D.Double(insets.left, 0, getWidth()-insets.right-insets.left, getHeight());
		
		if(isEnabled())
			g2.setColor(getBackground());
		else
			g2.setColor(UIManager.getColor("Button.background"));
		g2.fill(rect);
		
		if(isEnabled()) {
			g2.setColor(getForeground());
		} else {
			g2.setColor(UIManager.getColor("Button.disabledForeground"));
		}
		Point p = GUIHelper.centerTextInRectangle(g2, getText(),
				new Rectangle((int)rect.getX(), 0, (int)rect.getWidth(), (int)getHeight()));
		g2.drawString(getText(), p.x, p.y);
	}
	
}
