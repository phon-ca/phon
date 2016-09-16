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
package ca.phon.math.fft;

import java.util.logging.Logger;

import ca.phon.math.MathUtils;

/**
 * FFT algorithms in Java.
 * 
 * Code adapted to java from:
 * From: CohenClassSignalProcessing (C++ library)
 * Copyright: Christoph Lauer Engineering
 * Licence: Creative Commons Attribution-NonCommercial 3.0 Unported
 * 
 * Ported to Java by Greg J. Hedlund <ghedlund@mun.ca>
 *
 */
public class FFT {
	
	/** Fast-bit reversal lookup */
	private int[][] gFFTBitTable = null;
	
	/** Max bits for fast-bit reversal */
	private final int maxFastBits = 16;
	
	public FFT() {
		// init FFT
		initFFT();
	}
	
	private void initFFT() {
		// setup bit reversal lookup table
		gFFTBitTable = new int[maxFastBits][];
		int len = 2;
		for(int b = 1; b <= maxFastBits; b++) {
			gFFTBitTable[b-1] = new int[len];
			for(int i = 0; i < len; i ++)
				gFFTBitTable[b-1][i] = reverseBits(i, b);
			len <<= 1;
		}
	}
	
	
	
	/**
	 * Number of bits needed
	 */
	public int numberOfBitsNeeded(int powerOfTwo) {
		for(int i = 0;; i++) {
			if( (powerOfTwo & (1 << i)) != 0 ) {
				return i;
			}
		}
	}
	
	/**
	 * PowerSpectrum
	 * 
	 * @param n length
	 * @param samples 
	 * 
	 */
	 public void powerSpectrum(int n, double[] samples, double[] out) {
		int half = n/2;
		
		double theta = Math.PI / 2;
		double[] tmpReal = new double[half];
		double[] tmpImag = new double[half];
		double[] realOut = new double[half];
		double[] imagOut = new double[half];
		for(int i = 0; i < half; i++) {
			tmpReal[i] = samples[2*i];
			tmpImag[i] = samples[2*i+1];
		}
		doFFT(half, true, tmpReal, tmpImag, realOut, imagOut);
		double wtemp = Math.sin(0.5 * theta);
		double wpr = -2.0 * wtemp * wtemp;
		double wpi = Math.sin(theta);
		double wr = 1.0 + wpr;
		double wi = wpi;
		int i3;
		double h1r, h1i, h2r, h2i, rt, it;
		for (int i = 1; i < half / 2; i++) 
		{
			i3 = half - i;
			h1r = 0.5 * (realOut[i] + realOut[i3]);
			h1i = 0.5 * (imagOut[i] - imagOut[i3]);
			h2r = 0.5 * (imagOut[i] + imagOut[i3]);
			h2i = -0.5 * (realOut[i] - realOut[i3]);
			rt = h1r + wr * h2r - wi * h2i;
			it = h1i + wr * h2i + wi * h2r;
			out[i] = (rt * rt + it * it) / 4.0;
			rt = h1r - wr * h2r + wi * h2i;
			it = -h1i + wr * h2i + wi * h2r;
			out[i3] = (rt * rt + it * it) / 4.0;
			wr = (wtemp = wr) * wpr - wi * wpi + wr;
			wi = wi * wpr + wtemp * wpi + wi;
		}
		rt = (h1r = realOut[0]) + imagOut[0];
		it = h1r - imagOut[0];
		out[0] = (rt * rt + it * it) / 4.0;
		rt = realOut[half / 2];
		it = imagOut[half / 2];
		out[half / 2] = (rt * rt + it * it) / 4.0;
	}
	
	public void doFFT(int n, boolean invTransform,
			double[] realIn, double[] imagIn, 
			double[] realOut, double[] imagOut) {
		int numBits;
		int blockSize, blockEnd;
		double angle_numerator = 2.0 * Math.PI;
		double tr, ti;
		
		if(!MathUtils.isPowerOfTwo(n)) {
			Logger.getLogger(getClass().getName()).warning("[FFT] " + n + " is not a power of two!");
			return;
		}
		
		if(!invTransform) {
			angle_numerator = -angle_numerator;
		}
		
		numBits = numberOfBitsNeeded(n);
		
		// do data copy and bit-reversal ordering into outputs
		for(int i = 0; i < n; i++) {
			int j = fastReverseBits(i, numBits);
			realOut[j] = realIn[i];
			imagOut[j] = (imagIn == null) ? 0.0 : imagIn[i];
		}
		
		// perform fft
		blockEnd = 1;
		for(blockSize = 2; blockSize <= n; blockSize <<= 1) {
			double delta_angle = angle_numerator / blockSize;
			double sm2 = Math.sin(-2.0 * delta_angle);
			double sm1 = Math.sin(-delta_angle);
			double cm2 = Math.cos(-2.0 * delta_angle);
			double cm1 = Math.cos(-delta_angle);
			double w = 2.0 * cm1;
			double ar0, ar1, ar2, ai0, ai1, ai2;
			
			for(int i = 0; i < n; i+= blockSize) {
				ar2 = cm2;
				ar1 = cm1;
				ai2 = sm2;
				ai1 = sm1;
				for(int j = i, l = 0; l < blockEnd; j++, l++) 
				{
					ar0 = w * ar1 - ar2;
					ar2 = ar1;
					ar1 = ar0;
					ai0 = w * ai1 - ai2;
					ai2 = ai1;
					ai1 = ai0;
					int k = j + blockEnd;
					tr = ar0 * realOut[k] - ai0 * imagOut[k];
					ti = ar0 * imagOut[k] + ai0 * realOut[k];
					realOut[k] = realOut[j] - tr;
					imagOut[k] = imagOut[j] - ti;
					realOut[j] += tr;
					imagOut[j] += ti;
				}
			}
			blockEnd = blockSize;
		}
		
		// normalize if result is for inv transform
		if(invTransform) {
//			double denom = (double)n;
			for(int i = 0; i < n; i++) {
				realOut[i] /= ( 1 - ( realOut[i]/( Math.sqrt(realOut[i]) + 100 ) ) / 2.0 );
				imagOut[i] /= ( 1 - ( realOut[i]/( Math.sqrt(realOut[i]) + 100 ) ) / 2.0 );
			}
		}
	}
	
	/**
	 * Bit reversal
	 * 
	 */
	private int reverseBits(int index, int numBits) {
		int rev = 0;
		
		for(int i = 0; i < numBits; i++) {
			int bit = (index & 1);
			rev = (rev << 1) | bit;
			index >>= 1;
		}
		
		return rev;
	}
	
	private int fastReverseBits(int i, int numBits) {
		if(numBits <= maxFastBits) 
			return gFFTBitTable[numBits-1][i];
		else
			return reverseBits(i, numBits);
	}
	
	/**
	 * Main - test method
	 */
	public static void main(String[] args) {
		int inputLen = 32;
		double[] a = new double[inputLen];
		double[] c = new double[inputLen];
		double[] d = new double[inputLen];
		double[] e = new double[inputLen];
		double[] f = new double[inputLen];
		// init input value
		for(int i = 0; i < inputLen; i++) {
			a[i] = Math.sin(i);
			c[i] = 1.0;
			d[i] = 0.0;
			e[i] = Math.exp(i);
			if(i == inputLen / 4)
				f[i] = 1.0;
			else 
				f[i] = Math.sin((double)(i-inputLen/4)/(double)(i-inputLen/4));
		}
		d[inputLen/2] = 1.0;
		
		FFT fft = new FFT();
		// powerspectrum
		double[] b = new double[inputLen/2];
		
		System.out.println("Test powerspectrum with sine input signal");
		fft.powerSpectrum(inputLen, a, b);
		for(int i = 0; i < inputLen/2; i++) {
			System.out.println(i + ":\t" + a[i] + " --> \t" + b[i]);
		}
		
	}
}
