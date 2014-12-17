package ca.phon.ui.painter;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Interface for painter classes.
 *
 * @param <T>
 */
public interface Painter<T> {

	/**
	 * Paint to the given graphics context inside
	 * the given bounds.
	 * 
	 * @param obj
	 * @param g2
	 * @param bounds
	 */
	public void paint(T obj, Graphics2D g2, Rectangle2D bounds);
	
}
