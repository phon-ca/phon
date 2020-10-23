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
package ca.phon.util.icons;

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
	
	public static IconSize getHiDPISize(IconSize size) {
		if(size == XXSMALL) {
			return IconSize.SMALL;
		} else if(size == XSMALL) {
			return IconSize.MEDIUM;
		} else if(size == SMALL) {
			return IconSize.LARGE;
		} else if(size == MEDIUM) {
			return IconSize.LARGE;
		} else if(size == LARGE) {
			return IconSize.XLARGE;
		} else if(size == XLARGE) {
			return IconSize.XXLARGE;
		} else {
			// no size available
			return size;
		}
	}
	
	public IconSize getHiDPISize() {
		return IconSize.getHiDPISize(this);
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
