/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
