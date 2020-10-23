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
package ca.phon.ui.layout;

/**
 * (x,y) and w+h of a component using
 * the GridCellLayout layout manager.
 *
 */
public class GridCellConstraint {
	
	/**
	 * x
	 */
	public int x = 0;
	
	/**
	 * y
	 */
	public int y = 0;

	/**
	 * w
	 */
	public int w = 1;

	/**
	 * h 
	 */
	public int h = 1;
	
	public static GridCellConstraint xy(int x, int y) {
		return new GridCellConstraint(x, y);
	}
	
	public static GridCellConstraint xyw(int x, int y, int w) {
		return new GridCellConstraint(x, y, w);
	}
	
	public static GridCellConstraint xywh(int x, int y, int w, int h) {
		return new GridCellConstraint(x, y, w, h);
	}
	
	public GridCellConstraint(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public GridCellConstraint(int x, int y, int w) {
		super();
		this.x = x;
		this.y = y;
		this.w = w;
	}

	public GridCellConstraint(int x, int y, int w, int h) {
		super();
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}
	
	
}
