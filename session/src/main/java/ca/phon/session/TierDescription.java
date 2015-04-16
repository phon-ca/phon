/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
 * Name and grouped information for a tier.
 *
 */
public interface TierDescription {
	
	/**
	 * Is the tier grouped, if tier is not grouped,
	 * {@link Tier#numberOfGroups()} will always return 1.
	 * 
	 * @return <code>true</code> if the tier is grouped, <code>false</code>
	 *  otherwise
	 */
	public boolean isGrouped();
	
	/**
	 * Get the name of the tier.
	 * 
	 * @return name of the tier
	 */
	public String getName();
	
	/**
	 * Get declared type of the tier
	 * 
	 * 
	 */
	public Class<?> getDeclaredType();

}
