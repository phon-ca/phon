package ca.phon.app.session.editor.view.common;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

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
