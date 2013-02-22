/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.session;

/**
 * An entry for tier ordering, vibility and locking.
 *
 */
public interface TierOrderItem {

	/**
	 * Tier name
	 * 
	 */
	public String getTierName();
	
	public void setTierName(String tierName);
	
	/**
	 * Tier visibility
	 */
	public boolean isVisible();
	
	public void setVisible(boolean v);
	
	/**
	 * Get the font.  The string should be parsable
	 * by the standard awt.Font class.
 	 */
	public String getTierFont();
	
	/**
	 * @param font
	 */
	public void setTierFont(String font);
	
	/**
	 * Set locked
	 */
	public void setTierLocked(boolean locked);
	
	/**
	 * Get is locked
	 */
	public boolean isTierLocked();
}
