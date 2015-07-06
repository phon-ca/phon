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
