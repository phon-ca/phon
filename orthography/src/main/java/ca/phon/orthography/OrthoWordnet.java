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
package ca.phon.orthography;

/**
 * Two words joined by a marker.
 */
public class OrthoWordnet extends AbstractOrthoElement {
	
	private final OrthoWord word1;
	
	private final OrthoWordnetMarker marker;
	
	private final OrthoWord word2;
	
	public OrthoWordnet(OrthoWord word1, OrthoWord word2) {
		this(word1, word2, OrthoWordnetMarker.COMPOUND);
	}
	
	public OrthoWordnet(OrthoWord word1, OrthoWord word2, OrthoWordnetMarker marker) {
		super();
		this.word1 = word1;
		this.word2 = word2;
		this.marker = marker;
	}
	
	public OrthoWord getWord1() {
		return word1;
	}

	public OrthoWordnetMarker getMarker() {
		return marker;
	}

	public OrthoWord getWord2() {
		return word2;
	}

	@Override
	public String text() {
		return word1.toString() + marker.getMarker() + word2.toString();
	}
	
	@Override
	public String toString() {
		return text();
	}

}
