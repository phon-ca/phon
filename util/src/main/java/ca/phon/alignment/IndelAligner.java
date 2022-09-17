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
package ca.phon.alignment;

import java.util.Stack;

/**
 * 
 * @param <T>
 */
public abstract class IndelAligner<T> implements Aligner<T> {
	
	private AlignmentMap<T> lastAlignment = null;

	public IndelAligner() {}
	
	/**
	 * Return the similarity between two elements
	 * 
	 * @param ele1
	 * @param ele2
	 * @return
	 */
	protected abstract int costSubstitute(T ele1, T ele2);
	
	protected abstract int costSkip(T ele);

	/**
	 * Return the max value
	 * 
	 * @param values
	 * @return int
	 */
	private int max(int[] values) {
		int retVal = 0;
		
		for(int i = 0; i < values.length; i++)
			if(values[i] > retVal)
				retVal = values[i];
		
		return retVal;
	}
	
	@Override
	public AlignmentMap<T> calculateAlignment(T[] top, T[] bottom) {
		AlignmentMap<T> retVal = new AlignmentMap<>();
		
		int matrix[][];
		int width, height, score;
		Integer[][] alignment = new Integer[2][];
		Stack<Integer> tp, ap;
		
		width = top.length+1;
		height = bottom.length+1;
		tp = new Stack<Integer>();
		ap = new Stack<Integer>();
		
		matrix = new int[width][height];
		
		// set the top row and left column to reflect the PIndel costs
		matrix[0][0] = 0;
		
		for(int i = 1; i < width; i++)
			matrix[i][0] = matrix[i-1][0] + this.costSkip(top[i-1]);
		
		for(int j = 1; j < height; j++)
			matrix[0][j] = matrix[0][j-1] + this.costSkip(bottom[j-1]);
		
		// fill in the matrix
		for(int i = 1; i < width; i++) {
			for(int j = 1; j < height; j++) {
				int values[] = new int[3];
				
				values[0] = matrix[i-1][j] + this.costSkip(top[i-1]);
				values[1] = matrix[i][j-1] + this.costSkip(bottom[j-1]);
				values[2] = matrix[i-1][j-1] + 
					this.costSubstitute(top[i-1], bottom[j-1]);
				
				matrix[i][j] = this.max(values);
			}
		}
		
		score = matrix[width-1][height-1];
		
		alignment = this.retrieveAlignment(top, bottom, width-1, height-1, 0, matrix, tp, ap, score);
		swapIndels(alignment);
		
		retVal.setTopElements(top);
		retVal.setBottomElements(bottom);
		retVal.setTopAlignment(alignment[0]);
		retVal.setBottomAlignment(alignment[1]);
		
		lastAlignment = retVal;
		
		return retVal;
	}
	
	/**
	 * Swaps cases of adjacent indels where the ordering should be reversed
	 * 
	 * @param alignment
	 */
	private void swapIndels(Integer[][] alignment) {
		for(int i = 0; i < alignment[0].length; i++) {
			int top = alignment[0][i];
			int bottom = alignment[1][i];
			if(i > 0 && top == -1) {
				for(int j = i-1; j >= 0; j--) {
					int lastTop = alignment[0][j];
					int lastBottom = alignment[1][j];
					
					if(lastBottom == -1 && lastTop >= bottom) {
						// swap
						alignment[0][j] = top;
						alignment[1][j] = bottom;
						
						alignment[0][j+1] = lastTop;
						alignment[1][j+1] = lastBottom;
					} else {
						break;
					}
				}
			}
		}
	}

	/**
	 * Get the alignment from the completed matrix
	 * 
	 * @param top top elements
	 * @param bottom bottom elements
	 * @param i the width of the matrix
	 * @param j the height of the matrix
	 * @param tally the total score accumulated in at each setp of the recursion
	 * @param Matrix the completed dynamic algorithm
	 * @param tp the stack used to store the target alignment
	 * @param ap the stack used to store the actual alignment
	 * @param score
	 */
	protected Integer[][] retrieveAlignment(T[] top, T[] bottom,
											int i, int j, int tally, int[][] matrix, Stack<Integer> tp, Stack<Integer> ap, int score) {
		// the base case for our recursion, we are looking at a 0x0 matrix
		if(i == 0 && j == 0) {
			Integer[][] toReturn = new Integer[2][];
			
			Integer[] t = new Integer[tp.size()];
			Integer[] a = new Integer[ap.size()];
			
			for(int k = 0; !tp.empty(); k++) {
				t[k] = tp.pop();
			}
			
			for(int l = 0; !ap.empty(); l++) {
				a[l] = ap.pop();
			}
			
			toReturn[0] = t;
			toReturn[1] = a;
			
			return toReturn;
		}
		
		// check if the maximum value is for a match
		if(i > 0 && j > 0) {
			int subVal = this.costSubstitute(top[i-1], bottom[j-1]);
			
			int chkVal = matrix[i-1][j-1] + subVal + tally;
			
			if(chkVal >= score) {
				tp.push(i-1);
				ap.push(j-1);
				
				int newTally = tally + subVal;
				
				return this.retrieveAlignment(top, bottom,
						i-1, j-1, newTally,
						matrix, 
						tp, ap, score);
			}
		}
		
		// check if max value came from skipping target[i]
		if(j > 0) {
			int chkVal = matrix[i][j-1] + this.costSkip(bottom[j-1]) + tally;
			
			if(chkVal >= score) {
				tp.push(AlignmentMap.INDEL_VALUE);
				ap.push(j-1); // with actual
				
				int newTally = tally + this.costSkip(bottom[j-1]);
				
				return this.retrieveAlignment(top, bottom,
						i, j-1, newTally,
						matrix, 
						tp, ap,
						score);
			}
		}
		
		// check if max value came from skipping actual[j]
		if(i > 0) {
			ap.push(AlignmentMap.INDEL_VALUE);
			
			tp.push(i-1);
			
			int newTally = tally + this.costSkip(top[i-1]);
			
			return this.retrieveAlignment(top, bottom,
					i-1, j, newTally,
					matrix, 
					tp, ap,
					score);
		} else {
			// special case - i = 0, j = 1
			// (_)XXXX
			// (X)__XX
			// We need to add an indel on the target side to align with the first
			// actual phone
			if(i == 0 && j > 0) {
				tp.push(AlignmentMap.INDEL_VALUE);
				ap.push(j-1);
				
				int newTally = tally + this.costSkip(top[i]);
				
				return this.retrieveAlignment(top, bottom, i, j-1, newTally,
						matrix, 
						tp, ap, score);
			} else {
				// return DEFAULT alignment
				// NOTE: should never get here
				//PhonLogger.warning("[Aligner] Could not determine an appropriate alignment, returning default.");
				
				
				int maxLen = Math.max(top.length, bottom.length);
				Integer[][] toReturn = new Integer[2][];
				toReturn[0] = new Integer[maxLen];
				int tIndex = 0;
				for( ; tIndex < top.length; tIndex++)
					toReturn[0][tIndex] = tIndex;
				for(int pIndex = tIndex; pIndex < maxLen; pIndex++)
					toReturn[0][pIndex] = AlignmentMap.INDEL_VALUE;
				
				toReturn[1] = new Integer[maxLen];
				int aIndex = 0;
				for( ; aIndex < bottom.length; aIndex++)
					toReturn[1][aIndex] = aIndex;
				for(int pIndex = aIndex; pIndex < maxLen; pIndex++)
					toReturn[1][pIndex] = AlignmentMap.INDEL_VALUE;
				
				return toReturn;
			}
		}
	}
	
	@Override
	public AlignmentMap<T> getAlignmentMap() {
		return lastAlignment;
	}

}
