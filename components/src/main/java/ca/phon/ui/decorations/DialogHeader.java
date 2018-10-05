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
package ca.phon.ui.decorations;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

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
public class DialogHeader extends JPanel {
	
	private ImageIcon icon;
	
	private JLabel topLabel;
	
	private JLabel bottomLabel;
	
	private CompoundPainter<DialogHeader> cmpPainter;

	private final int PADDING = 10;
	
	/**
	 * Constructs the header with the specified text. 
	 * @param text  the text to use in the header
	 */
	public DialogHeader(String header, String description) {
		super();
		
		setOpaque(false);
		
		icon = IconManager.getInstance().getIcon("apps/database-phon", IconSize.XXLARGE);
		init();
		
		setHeaderText(header);
		setDescText(description);
	}
	
	private void init() {
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(PADDING, PADDING, 5, PADDING);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		topLabel = new JLabel();
		topLabel.setFont(FontPreferences.getTitleFont().deriveFont(Font.BOLD, 16.0f));
		add(topLabel, gbc);
		
		++gbc.gridy;
		gbc.insets = new Insets(0, PADDING * 2, PADDING, PADDING);
		bottomLabel = new JLabel();
		bottomLabel.setFont(FontPreferences.getTitleFont());
		add(bottomLabel, gbc);
		
		GlossPainter gloss = new GlossPainter();
		IconPainter icon = new IconPainter();
		
		GradientPaint gp = new GradientPaint(new Point(0, 0), Color.white, new Point(200, 100), UIManager.getColor("control"));
		MattePainter mp = new MattePainter(gp);
		
		PinstripePainter pinstripe = new PinstripePainter(new Color(240, 240, 240), 45.0, 0.5, 5.0);
		
		cmpPainter = new CompoundPainter<DialogHeader>(mp, pinstripe, icon, gloss);
	}
	
	public JLabel getTopLabel() {
		return this.topLabel;
	}
	
	public JLabel getBottomLabel() {
		return this.bottomLabel;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		cmpPainter.paint((Graphics2D)g, this, getWidth(), getHeight());
		
		super.paintComponent(g);
	}
	
	public void setHeaderText(String txt) {
		getTopLabel().setText(txt);
	}
	
	public void setDescText(String txt) {
		getBottomLabel().setText(txt);
	}
	
	public String getDescText() {
		return getBottomLabel().getText();
	}
	
	/**
	 * Background painter
	 */
	private class IconPainter implements Painter<DialogHeader> {

		@Override
		public void paint(Graphics2D g2d, DialogHeader header, int width, int height) {
			Composite originalComposite = g2d.getComposite();
			
			// Calculate icon positions and draw the icon
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			
			int x2 = PADDING * 2;
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