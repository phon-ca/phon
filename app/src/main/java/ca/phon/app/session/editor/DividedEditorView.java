/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.session.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

/**
 * A component with a divided background.  Background division
 * is set using the divLocaion variable.
 * 
 *
 */
public abstract class DividedEditorView extends EditorView {

	/** Divider location */
	public static final int divLocation = 150;  // 150px
	
	private Color sideColor = new Color(207, 213, 224);
	
	public DividedEditorView(SessionEditor editor) {
		super(editor);
		
		setBackground(Color.white);
	}
	
	public DividedEditorView(SessionEditor editor, LayoutManager layoutManager) {
		super(editor);
		setLayout(layoutManager);
		setBackground(Color.white);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Dimension size = getSize();
		// fill background with background colour
		g.setColor(getBackground());
		g.fillRect(0, 0, size.width, size.height);
		
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setColor(sideColor);
		g2.fillRect(0, 0, divLocation, size.height);
		
		GradientPaint gp = new GradientPaint(
				divLocation, 0.0f, sideColor, (divLocation + 5.0f), 0.0f, new Color(237,243, 254), true);
		g2.setPaint(gp);
		g2.fillRect(divLocation, 0, 5, size.height);
	}
	
}
