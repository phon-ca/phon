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

import java.util.Comparator;

import ca.phon.extensions.Extension;

/**
 * Implementation of Levenshtein distance (https://en.wikipedia.org/wiki/Levenshtein_distance)
 * for Phon transcripts.
 */
@Extension(IPATranscript.class)
public class LevenshteinDistance {
	
	public static int distance(IPATranscript s, IPATranscript t) {
		return distance(s, t, (a, b) -> { return a.toString().compareTo(b.toString()); } );
	}
	
	/**
	 * Calculate LevenshteinDistance for two IPATranscripts.
	 *  
	 * @param a
	 * @param b
	 * @param comparator
	 * @return
	 */
	public static int distance(IPATranscript a, IPATranscript b, Comparator<IPAElement> comparator) {
		IPATranscript s = a.removePunctuation();
		IPATranscript t = b.removePunctuation();
		
		int m = s.length();
		int n = t.length();
		
		int[][] mat = new int[m+1][n+1];
		
		for(int i = 1; i <= m; i++) mat[i][0] = i;
		for(int j = 1; j <= n; j++) mat[0][j] = j;
		
		for(int j = 1; j <= n; j++) {
			for(int i = 1; i <=m; i++) {
				IPAElement sele = s.elementAt(i-1);
				IPAElement tele = t.elementAt(j-1);
				
				if(comparator.compare(sele, tele) == 0)
					mat[i][j] = mat[i-1][j-1];
				else
					mat[i][j] = Math.min(mat[i-1][j] + 1, 
							Math.min(mat[i][j-1]+1, mat[i-1][j-1]+1));
			}
		}
		
		return mat[m][n];
	}
	
	private final IPATranscript transcript;
	
	public LevenshteinDistance(IPATranscript transcript) {
		super();
		
		this.transcript = transcript;
	}
	
	public int distance(IPATranscript ipa) {
		return LevenshteinDistance.distance(transcript, ipa);
	}
	
}
