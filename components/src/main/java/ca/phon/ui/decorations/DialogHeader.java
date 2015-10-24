/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui.decorations;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.GlossPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.PinstripePainter;

import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * A header component meant to be placed at the top of a dialog.
 * 
 */
public class DialogHeader extends JXHeader {
	private ImageIcon icon;

	private final int VERTICAL_PADDING = 10;
	
	/**
	 * Constructs the header with the specified text. 
	 * @param text  the text to use in the header
	 */
	public DialogHeader(String header, String description) {
		super();
		
		super.setTitle(header);
		super.setTitleFont(FontPreferences.getTitleFont());
		super.setDescription(description);
		
		super.setTitleFont(getTitleFont().deriveFont(Font.BOLD, 16.0f));
		
		icon = IconManager.getInstance().getIcon("apps/database-phon", IconSize.XXLARGE);
		
		GlossPainter gloss = new GlossPainter();
		IconPainter icon = new IconPainter();
		
		GradientPaint gp = new GradientPaint(new Point(0, 0), Color.white, new Point(200, 100), UIManager.getColor("control"));
		MattePainter mp = new MattePainter(gp);
		
		PinstripePainter pinstripe = new PinstripePainter(new Color(240, 240, 240), 45.0, 0.5, 5.0);
		
		CompoundPainter<DialogHeader> cmpPainter = new CompoundPainter<DialogHeader>(mp, pinstripe, icon, gloss);
		setBackgroundPainter(cmpPainter);
	}
	
	public void setHeaderText(String txt) {
		super.setTitle(txt);
	}
	
	public void setDescText(String txt) {
		super.setDescription(txt);
	}
	
	public String getDescText() {
		return super.getDescription();
	}
	
	/**
	 * Background painter
	 */
	private class IconPainter implements Painter<JXHeader> {

		@Override
		public void paint(Graphics2D g2d, JXHeader header, int width, int height) {
			Composite originalComposite = g2d.getComposite();
			
			// Calculate icon positions and draw the icon
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			
			int x2 = VERTICAL_PADDING * 2;
			int y2 = (getHeight() - h * 2) / 2;
			
			g2d.setComposite(
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
			g2d.drawImage(
				icon.getImage(),
				x2, y2, x2 + w * 2 - 1, y2 + h * 2 - 1,
				0, 0, w - 1, h - 1,
				null);
			
			g2d.setComposite(originalComposite);
		}
		
	}
	
}