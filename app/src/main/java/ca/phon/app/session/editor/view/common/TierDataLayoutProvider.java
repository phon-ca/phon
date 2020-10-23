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
