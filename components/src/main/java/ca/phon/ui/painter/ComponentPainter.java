package ca.phon.ui.painter;

import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * A painter interface similar to the SwingX painter
 * setup.  Can be used by custom UI components to
 * help with painting.
 */
public interface Painter<T extends JComponent> {

	/**
	 * Paint the component
	 * 
	 * @param g2d graphics context as a Graphics2D object
	 * @param comp component
	 * @param width width to paint
	 * @param height height to paint
	 */
	public void paint(Graphics2D g2d, T comp,
			int width, int height);
	
}
