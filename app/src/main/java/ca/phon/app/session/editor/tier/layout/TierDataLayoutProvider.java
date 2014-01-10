package ca.phon.app.session.editor.tier.layout;

import java.awt.Container;
import java.awt.Dimension;

/**
 * Interface implemented by layout provider implementations.
 */
public interface TierDataLayoutProvider {
	
	/**
	 * Layout container.
	 * 
	 * @param container
	 * @poram layout
	 * 
	 */
	public void layoutContainer(Container parent, TierDataLayout layout);
	
	/**
	 * Calculate preferred size of the container.
	 * 
	 * @param container
	 * @param layout
	 */
	public Dimension preferredSize(Container parent, TierDataLayout layout);
	
	/**
	 * Calculate minimum size of the container.
	 * 
	 * @param container
	 * @param layout
	 */
	public Dimension minimumSize(Container parent, TierDataLayout layout);
	
	/**
	 * Calculate maximum size of the container.
	 * 
	 * @param container
	 * @param layout
	 */
	public Dimension maximumSize(Container parent, TierDataLayout layout);
	
	/**
	 * Invalidate cached layout information.
	 * 
	 * @param container
	 * @param layout
	 */
	public void invalidate(Container parent, TierDataLayout layout);
	
}
