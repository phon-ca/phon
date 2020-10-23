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
package ca.phon.app.session.editor.view.common;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import ca.phon.ui.*;
import ca.phon.util.icons.*;

/**
 * Border for group text fields
 *
 */
public class GroupFieldBorder implements Border {
	
	private final static int TOP_INSET = 2;
	
	private final static int BOTTOM_INSET = 2;
	
	private final static int LEFT_INSET = 2;
	
	private final static int RIGHT_INSET = 2;
	
	private final static Color COLOR = Color.LIGHT_GRAY;
	
	private final static double LINE_WIDTH = 1.5;
	
	private final static int NOTCH_WIDTH = 4;
	
	private final ImageIcon lockIcon = 
			IconManager.getInstance().getIcon("emblems/emblem-readonly", IconSize.XSMALL);

	private final ImageIcon warningIcon =
			IconManager.getInstance().getIcon("emblems/flag-red", IconSize.XSMALL);
	
	private boolean showWarningIcon = false;
	
	@Override
	public Insets getBorderInsets(Component c) {
		final int startWidth = NOTCH_WIDTH + 1;
		final int endWidth = NOTCH_WIDTH + 1;
		
		final int top = TOP_INSET + (int)LINE_WIDTH;
		final int left = startWidth + LEFT_INSET * 2 + (int)LINE_WIDTH;
		final int right = endWidth + RIGHT_INSET * 2 + (int)LINE_WIDTH  + 
				(isShowWarningIcon() ? NOTCH_WIDTH + (int)LINE_WIDTH : 0);
		final int bottom = BOTTOM_INSET + (int)LINE_WIDTH;
		
		return new Insets(top, left, bottom, right);
	}
	
	public boolean isShowWarningIcon() {
		return this.showWarningIcon;
	}
	
	public void setShowWarningIcon(boolean show) {
		this.showWarningIcon = show;
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}
	
	private boolean isShowLock(Component c) {
		return (c instanceof JTextComponent ? !((JTextComponent)c).isEditable() || !c.isEnabled() : !c.isEnabled());
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		final Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		
		if(c.hasFocus())
			g.setColor(PhonGuiConstants.PHON_FOCUS);
		else
			g.setColor(COLOR);
		
		
		final Rectangle2D r1 = new Rectangle2D.Double(
				x+LEFT_INSET, y+TOP_INSET,
				LINE_WIDTH, height-BOTTOM_INSET);
		
		final Rectangle2D r2 = new Rectangle2D.Double(
				x+LEFT_INSET, y+TOP_INSET,
				LINE_WIDTH + NOTCH_WIDTH, LINE_WIDTH);
		
		final Rectangle2D r3 = new Rectangle2D.Double(
				x+LEFT_INSET, r1.getY() + r1.getHeight() - LINE_WIDTH,
				LINE_WIDTH + NOTCH_WIDTH, LINE_WIDTH);
		
		g2.fill(r1);
		g2.fill(r2);
		g2.fill(r3);
		
		final double rightX = x + (width - RIGHT_INSET - 1 - LINE_WIDTH);
		
		final Rectangle2D r4 = new Rectangle2D.Double(
				rightX - LINE_WIDTH, y+TOP_INSET,
				LINE_WIDTH, height-BOTTOM_INSET);
				
		final Rectangle2D r5 = new Rectangle2D.Double(
				rightX - LINE_WIDTH - NOTCH_WIDTH, y+TOP_INSET,
				LINE_WIDTH + NOTCH_WIDTH, LINE_WIDTH);
		
		final Rectangle2D r6 = new Rectangle2D.Double(
				rightX - LINE_WIDTH - NOTCH_WIDTH, r4.getY() + r4.getHeight() - LINE_WIDTH,
				LINE_WIDTH + NOTCH_WIDTH, LINE_WIDTH);
		
		g2.fill(r4);
		g2.fill(r5);
		g2.fill(r6);
		
		if(isShowLock(c)) {
			g.drawImage(lockIcon.getImage(), width-IconSize.XSMALL.getWidth(), height-IconSize.XSMALL.getHeight(), c);
		}
		
		if(isShowWarningIcon()) {
			g.drawImage(warningIcon.getImage(), width-(int)(IconSize.XSMALL.getWidth()+1+LINE_WIDTH+NOTCH_WIDTH+LINE_WIDTH), 0, c);
		}
		
	}
	
}
