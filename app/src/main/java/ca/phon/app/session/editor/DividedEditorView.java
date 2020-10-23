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
package ca.phon.app.session.editor;

import java.awt.*;

/**
 * A component with a divided background.  Background division
 * is set using the divLocaion variable.
 * 
 *
 */
public abstract class DividedEditorView extends EditorView {

	private static final long serialVersionUID = 7652953184027508708L;

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
