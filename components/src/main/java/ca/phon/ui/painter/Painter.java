/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.ui.painter;

import java.awt.*;
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
