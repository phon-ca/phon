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
package ca.phon.app.session.editor;

/**
 * Dock positions for record editor.
 */
public enum DockPosition {
	// byte positions correspond to
	// t, l, b, r
	NORTH(0.33f),
	EAST(0.33f),
	SOUTH(0.33f),
	WEST(0.33f),
	CENTER(0.66f);
	
	final float size;
	
	private DockPosition(float size) {
		this.size = size;
	}
	
	public float getSize() {
		return size;
	}
}
