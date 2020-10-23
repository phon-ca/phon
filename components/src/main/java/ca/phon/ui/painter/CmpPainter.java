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
package ca.phon.ui.painter;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

public class CmpPainter<T extends JComponent> implements ComponentPainter<T> {

	/**
	 * List of painters
	 */
	private List<ComponentPainter<T>> painters = 
		Collections.synchronizedList(new ArrayList<ComponentPainter<T>>());
	
	/**
	 * Constructor
	 */
	public CmpPainter() {
		super();
	}
	
	public CmpPainter(ComponentPainter<T> ... painters) {
		for(int i = 0; i < painters.length; i++) {
			this.painters.add(painters[i]);
		}
	}
	
	@Override
	public void paint(Graphics2D g2d, T comp, int width, int height) {
		//paint in order
		for(ComponentPainter<T> painter:painters) {
			painter.paint(g2d, comp, width, height);
		}
	}
	
}
