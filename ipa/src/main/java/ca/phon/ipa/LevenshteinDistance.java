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
package ca.phon.ipa;

import java.util.*;

import ca.phon.extensions.*;

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
