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
package ca.phon.session.impl;

import ca.phon.session.TierViewItem;

/**
 * Information about tier visibility, font and locking.  This
 * item is also used in a list for ordering.
 */
public class TierViewItemImpl implements TierViewItem {
	
	private final String name;
	
	private final boolean visible;
	
	private final String font;
	
	private final boolean locked;
	
	TierViewItemImpl(String name) {
		this(name, true, "default", false);
	}
	
	TierViewItemImpl(String name, boolean visible) {
		this(name, visible, "default", false);
	}
	
	TierViewItemImpl(String name, boolean visible, String font) {
		this(name, visible, font, false);
	}
	
	TierViewItemImpl(String name, boolean visible, boolean locked) {
		this(name, visible, "default", locked);
	}
	
	TierViewItemImpl(String name, boolean visible, String font, boolean locked) {
		super();
		this.name = name;
		this.visible = visible;
		this.font = font;
		this.locked = locked;
	}

	@Override
	public String getTierName() {
		return name;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public String getTierFont() {
		return font;
	}

	@Override
	public boolean isTierLocked() {
		return locked;
	}

	
	
}
