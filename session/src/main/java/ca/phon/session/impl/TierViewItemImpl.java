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
