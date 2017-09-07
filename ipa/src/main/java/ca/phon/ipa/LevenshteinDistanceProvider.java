/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ipa;

import ca.phon.extensions.*;

@Extension(IPATranscript.class)
public class LevenshteinDistanceProvider implements ExtensionProvider {

	@Override
	public void installExtension(IExtendable obj) {
		if(!(obj instanceof IPATranscript)) 
			throw new IllegalArgumentException(obj + " is not an instance of IPATranscript");
		
		final IPATranscript transcript = (IPATranscript)obj;
		transcript.putExtension(LevenshteinDistance.class, new LevenshteinDistance(transcript));
	}

}
