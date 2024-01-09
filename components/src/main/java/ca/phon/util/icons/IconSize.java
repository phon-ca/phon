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

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Icons size used by IconManager
 *
 * @param width
 * @param height
 */
public record IconSize(int width, int height) implements Comparable<IconSize> {

	public static IconSize XXSMALL = new IconSize(8, 8);
	public static IconSize XSMALL = new IconSize(12, 12);
	public static IconSize SMALL = new IconSize(16, 16);
	public static IconSize MEDIUM = new IconSize(22, 22);
	public static IconSize MEDIUM_LARGE = new IconSize(28, 28);
	public static IconSize LARGE = new IconSize(32, 32);
	public static IconSize XLARGE = new IconSize(64, 64);
	public static IconSize XXLARGE = new IconSize(128, 128);

	private final static IconSize[] values = new IconSize[] {
		XXSMALL,
		XSMALL,
		SMALL,
		MEDIUM,
		MEDIUM_LARGE,
		LARGE,
		XLARGE,
		XXLARGE
	};

	public int getWidth() { return width(); }

	public int getHeight() { return height(); }

	public IconSize getHiDPISize() {
		return new IconSize(width * 2, height * 2);
	}

	public static IconSize[] values() {
		return values;
	}

	public int ordinal() {
		int idx = Arrays.binarySearch(values(), this);
		if(idx < 0)
			idx = values().length;
		return idx;
	}

	@Override
	public int compareTo(@NotNull IconSize o) {
		return Integer.compare(getWidth() + getHeight(), o.getWidth() + o.getHeight());
	}

}
