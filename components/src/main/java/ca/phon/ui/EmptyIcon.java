package ca.phon.ui;

import javax.swing.*;
import java.awt.*;

public class EmptyIcon implements Icon {

	int width;
	int height;

	public EmptyIcon() {
		this(0, 0);
	}

	public EmptyIcon(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

}
