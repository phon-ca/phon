/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.math;

/**
 * Math helper methods.
 *
 *
 */
public class MathUtils {

//	public static final double  PI = (3.14159265358979323846);
//	public static final double TWO_PI = (2.0*PI);
//	public static final double HALF_PI = (PI/2.0);
//	public static final double SQRT2 = (1.414213562373095);
	/**
	 * Is the given number a power of two.
	 */
	static public boolean isPowerOfTwo(int x) {
		boolean retVal = true;
		
		if(x < 2)
			retVal = false;
		if((x & (x - 1)) != 0) // tricky method
			retVal = false;
		
		return retVal;
	}
	
	/**
	 * Return next power of two.
	 */
	static public int nextPowerOfTwo(int v) {
		int n = 1;
		while(n < v) {
			n = n << 1;
		}
		return n;
	}
	
	/* Window functions */
	/**
	  * This inplace function applyes an Hamming window function to the gives array.
	  * The Hamming window function is per definition delared as:
	  * w(n) = 25/46 - 21/46*cos(2*Pi*n/(N-1))
	  *
	  * @param  data     A pointer to the input samples. Note that the window function is applyed
	  *                     in place which means that no further vector is allocated or given back.
	  * @param  len    The number of input samples.
	  */
	public static void applyHammingWindow(double[] data, int len) {
		for(int n = 0; n < len; n++) {
			data[n] *= ( 25.0/46.0 
						 - ( 21.0/46.0 
								 * Math.cos( 2.0 * Math.PI * (n+0.5) / len )
							)
						);	
		}
	}
	
	/**
	  * This inplace function applyes an Hann window function to the gives array.
	  * The Hann window function is per definition delared as:
	  * w(n) = 0.5*(1-cos(2*Pi*n/(N-1)))
	  *
	  * @param  sampels     A pointer to the input samples. Note that the window function is applyed
	  *                     in place which means that no further vector is allocated or given back.
	  * @param  nSamples    The number of input samples.
	  */
	  public static void applyHannWindow(double[] samples, int N)
	  {
	    for (int n=0; n<N; n++) 
	      samples[n] *= (0.5 - 0.5 * Math.cos( 2.0*Math.PI*(n+0.5) / N ));
	  } 

	 /**
	  * This inplace function applyes a Blackman window function to the gives array.
	  *
	  * @param  Sampels     A pointer to the input samples. Note that the window function is applyed
	  *                     in place which means that no further vector is allocated or given back.
	  * @param  nSamples    The number of input samples.
	  */
	  public static void applyBlackmanWindow(double[] samples, int N)
	  {
	    for (int n=0; n<N; n++) 
	      samples[n] *= (0.42 - ( 0.5 * Math.cos( 2.0*Math.PI*(n) / N ) + 0.08 * Math.cos( 2.0*Math.PI*(n+1) / N) ));
	  }
	  
	 /**
	  * This inplace function applyes a Tringle window function to the gives array.
	  *
	  * @param  Sampels     A pointer to the input samples. Note that the window function is applyed
	  *                     in place which means that no further vector is allocated or given back.
	  * @param  nSamples    The number of input samples.
	  */
	  public static void applyTriangleWindow(double[] samples, int N)
	  {
	    for (int n=0; n<N; n++) 
	    {
	      if ( n < N/2)
	        samples[n] *= ((double)n / (double)N * 2.0);
	      else 
	        samples[n] *= (2.0 - (double)(n+1) / (double)N * 2.0);
	    }
	  }

	  /**
	  * This inplace function applyes a Welch window function to the gives array.
	  *
	  * @param  Sampels     A pointer to the input samples. Note that the window function is applyed
	  *                     in place which means that no further vector is allocated or given back.
	  * @param  nSamples    The number of input samples.
	  */
	  public static void applyWelchWindow(double[] samples, int N)
	  {
	    for (int n=0; n<N; n++) 
	      samples[n] *= (1.0 - Math.pow( ((n+0.5) - N / 2.0) / (N / 2.0), 2.0));
	  }

	 /**
	  * This inplace function applyes an Gauss window function to the gives array.
	  *
	  * @param  sampels     A pointer to the input samples. Note that the window function is applyed
	  *                     in place which means that no further vector is allocated or given back.
	  * @param  nSamples    The number of input samples.
	  */
	  public static void applyGaussWindow(double[] samples, int N)
	  {
	    double rho = 1.0 / 3.0;
	    for (int n=0; n<N; n++) 
	      samples[n] *= (Math.exp( -0.5 * Math.pow( ((n+0.5)-N/2.0) / (rho*N/2.0), 2.0)));
	  }
	  
	 /**
	  * This inplace function applyes an Cosine window function to the gives array.
	  *
	  * @param  sampels     A pointer to the input samples. Note that the window function is applyed
	  *                     in place which means that no further vector is allocated or given back.
	  * @param  nSamples    The number of input samples.
	  */
	  public static void applyCosineWindow(double[] samples, int N)
	  {
	    for (int n=0; n<N; n++) 
	      samples[n] *= (Math.cos( Math.PI*(n) / (N-1.0) - Math.PI/2.0));
	  }
	  
	 /**
	  * This inplace function apllyes an Asynchron Expoantion Window function to the given array.
	  *  
	  * @param  samples     A Pointer to the input samples. Note that the window function is applyed 
	  *                     in place which means that no further vector is allocated or given back.
	  * @param  nSamples    The number of input sampels.
	  * @param  asymFact    This factor influences the unsymetrical side relation from one side to another.
	  *                     The value value should be more/less in the intervall [0.8...1.5]. A typical value is 1.0
	  */  
	  public static void applyAsymetricalExponentialWindow(double[] samples, int nSamples, double asymFact)
	  {
	    // first prepare som values
	    double cosineFrequency = (Math.PI * 2.0) / nSamples;
	    double alpha = Math.log(asymFact);
	    double max = 0.0;
	    double[] asymExpoWin = new double[nSamples];
	    
	    // generate the window function here
	    for(int i=0; i<nSamples; i++)
	      {
	        double d_i = (i);
	      	asymExpoWin[i] = 0.5 * (1.0 - Math.cos(cosineFrequency * (d_i))) * (d_i)*  Math.exp(alpha *(d_i)) ;
	      	if(asymExpoWin[i] > max)
	          max = asymExpoWin[i];
	      }
	      
	    // normalize and applay the window
	    for(int i=0; i<nSamples; i++) 
	      samples[i] *= asymExpoWin[i]/=max;
	  }
	
	  public static double lin2log(double lin) {
		  double fac = 10.0;
		  double ref = 1.0;
		  return fac * Math.log10(lin/ref);
	  }
	
}
