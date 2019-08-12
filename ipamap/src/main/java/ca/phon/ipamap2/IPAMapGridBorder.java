package ca.phon.ipamap2;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

public class IPAMapGridBorder extends AbstractBorder {

	private IPAMapGrid mapGrid;
	
	private JLabel titleRenderer;
	
	private final Icon collapsedIcon = UIManager.getIcon("Tree.collapsedIcon");
	
	private final Icon expandedIcon = UIManager.getIcon("Tree.expandedIcon");
	
	public IPAMapGridBorder(IPAMapGrid mapGrid) {
		super();
		
		this.mapGrid = mapGrid;
		
		titleRenderer = new JLabel();
		titleRenderer.setDoubleBuffered(false);
		
		updateLabel();
		installListeners();
	}
	
	private void updateLabel() {
		titleRenderer.setText(mapGrid.getGrid().getName());
//		titleRenderer.setFont(mapGrid.getFont().deriveFont(Font.BOLD));
		titleRenderer.setIcon(
				mapGrid.isCollapsed() ? collapsedIcon : expandedIcon );
	}
	
	private void installListeners() {
		mapGrid.addPropertyChangeListener("collapsed", (e) -> {
			updateLabel();
			mapGrid.repaint();
		});
		
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		super.paintBorder(c, g, x, y, width, height);
		
		updateLabel();
		Dimension labelPrefSize = titleRenderer.getPreferredSize();
		
		SwingUtilities.paintComponent(g, titleRenderer, c.getParent(), new Rectangle(0, 0, labelPrefSize.width, labelPrefSize.height));
	}

	@Override
	public Insets getBorderInsets(Component c) {
		Insets retVal = super.getBorderInsets(c);
		
		updateLabel();
		Dimension labelPref = titleRenderer.getPreferredSize();
		
		retVal.top = labelPref.height + 1;
		retVal.bottom = 1;
		retVal.left = 1;
		retVal.right = 1;
		
		return retVal;
	}
	
}
