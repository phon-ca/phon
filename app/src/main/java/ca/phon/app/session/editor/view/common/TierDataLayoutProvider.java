/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.session.editor.view.common;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;

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
	
	/**
	 * Get rectangle for given row
	 * 
	 * @param row
	 */
	public Rectangle rowRect(Container parent, TierDataLayout layout, int row);
	
}
