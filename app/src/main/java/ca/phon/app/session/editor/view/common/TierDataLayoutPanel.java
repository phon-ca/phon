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
package ca.phon.app.session.editor.view.common;

import javax.swing.*;
import java.awt.*;

/**
 * {@link JPanel} which uses {@link TierDataLayout} and {@link TierDataLayoutBgPainter}
 * by default.
 */
public class TierDataLayoutPanel extends JPanel {

	private static final long serialVersionUID = -121740230880584662L;

	/**
	 * Background painter
	 */
	private TierDataLayoutBgPainter bgPainter;
	
	/**
	 * Layout
	 */
	private TierDataLayout layout;
	
	public TierDataLayoutPanel() {
		super();
		
		layout = new TierDataLayout();
		setLayout(layout);
		
		// turn off swing background painting
		setBackground(Color.white);
		setOpaque(false);
		bgPainter = new TierDataLayoutBgPainter();
	}
	
	public TierDataLayout getTierLayout() {
		return (TierDataLayout)super.getLayout();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		final Graphics2D g2 = (Graphics2D)g;
		bgPainter.paintComponent(this, g2, layout);
		super.paintComponent(g2);
	}
	
}
