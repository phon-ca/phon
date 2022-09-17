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

import javax.swing.*;
import java.awt.*;

/**
 * A painter interface similar to the SwingX painter
 * setup.  Can be used by custom UI components to
 * help with painting.
 */
public interface ComponentPainter<T extends JComponent> {

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
