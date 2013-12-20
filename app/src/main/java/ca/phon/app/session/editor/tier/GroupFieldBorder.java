package ca.phon.app.session.editor.tier;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

/**
 * Border for group text fields
 *
 */
public class GroupFieldBorder implements Border {
	
	private final static char GROUP_START = '[';
	
	private final static char GROUP_END = ']';
	
	private final static int TOP_INSET = 2;
	
	private final static int BOTTOM_INSET = 2;
	
	private final static int SIDE_INSET = 2;
	
	private final static Color COLOR = Color.LIGHT_GRAY;

	@Override
	public Insets getBorderInsets(Component c) {
		final Font font = c.getFont();
		final FontMetrics metrics = c.getFontMetrics(font);
		
		final int startWidth = metrics.getWidths()[(int)GROUP_START];
		final int endWidth = metrics.getWidths()[(int)GROUP_END];
		
		return new Insets(TOP_INSET, startWidth + SIDE_INSET * 2, BOTTOM_INSET, endWidth + SIDE_INSET);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		final int baseline = c.getBaseline(width, height);
		
		final Font font = c.getFont();
		final FontMetrics metrics = c.getFontMetrics(font);
		
		final int endWidth = metrics.getWidths()[(int)GROUP_END];
		
		g.setColor(COLOR);
		g.drawString(GROUP_START+"", 1, baseline);
		g.drawString(GROUP_END+"", width-endWidth-1, baseline);
	}
	
}
