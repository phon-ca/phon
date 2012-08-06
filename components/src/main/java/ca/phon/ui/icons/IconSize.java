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
package ca.phon.ui.icons;

public enum IconSize {
	XXSMALL,
	XSMALL,
	SMALL,
	MEDIUM,
	LARGE,
	XLARGE,
	XXLARGE;
	
	public static int getHeightForSize(IconSize size) {
		if(size == XXSMALL)
			return 8;
		else if(size == XSMALL)
			return 12;
		else if(size == SMALL)
			return 16;
		else if(size == MEDIUM)
			return 22;
		else if(size == LARGE)
			return 32;
		else if(size == XLARGE)
			return 64;
		else if(size == XXLARGE)
			return 128;
		else
			return 0;
	}
	public int getHeight() {
		return IconSize.getHeightForSize(this);
	}
	
	public static int getWidthForSize(IconSize size) {
		if(size == XXSMALL)
			return 8;
		else if(size == XSMALL)
			return 12;
		else if(size == SMALL)
			return 16;
		else if(size == MEDIUM)
			return 22;
		else if(size == LARGE)
			return 32;
		else if(size == XLARGE)
			return 64;
		else if(size == XXLARGE)
			return 128;
		else
			return 0;
	}
	public int getWidth() {
		return IconSize.getWidthForSize(this);
	}
}
