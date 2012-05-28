package ca.phon.math.fft;

import java.io.File;
import java.util.logging.Logger;

import ca.phon.math.MathUtils;

/**
 * Short-Time Fourier Transform (STFT)
 * 
 * Code adapted to java from:
 * From: CohenClassSignalProcessing (C++ library)
 * Copyright: Christoph Lauer Engineering
 * Licence: Creative Commons Attribution-NonCommercial 3.0 Unported
 * 
 * Ported to Java by Greg J. Hedlund <ghedlund@mun.ca>
 *
 */
public class STFT {
	
	// window function enum
	public static enum WindowFunction {
		HAMMING,
		HANN,
		BLACKMAN,
		TRIANGLE,
		WELCH,
		GAUSS,
		COSINE,
		ASYMEXPO;
	}
	
	// stft data
	private double[] data;
	private int length;
	private int sampleRate;
	private int timePoints;
	private int freqPoints;
	private int fftLength;
	private double shift = 1.0/6.0;
	private WindowFunction windowFunction = WindowFunction.HAMMING;
	
	// the generated spectrogram
	private double[][] spectrogram = null;
	
	/**
	 * Constructor.
	 * 
	 * Creates a new STFT object for the given samples.
	 * 
	 * @param samples
	 * @param length
	 * @param sampleRate
	 * @param fftLength
	 */
	public STFT(double[] samples, int length, int sampleRate, int fftLength) {
		setData(samples);
		setLength(length);
		setSampleRate(sampleRate);
		setFFTLength(fftLength);
	}
	
	public double[] getData() {
		return data;
	}
	
	public void setData(double[] samples) {
		data = samples;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public int getSampleRate() {
		return sampleRate;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}
	
	public void setFFTLength(int fftLen) {
		this.fftLength = fftLen;
		
		// check if a values were given for the window sizes
		if(fftLength == 0) {
			// find the window size using the assumption that we should
			// have approx 2 * freqPoint length.  The 16/9 factor is for the
			// adaption of the screen and image output
			fftLength = MathUtils.nextPowerOfTwo((int)Math.sqrt(length/shift));
		} else {
			// ensure it's a power of two
			if(!MathUtils.isPowerOfTwo(fftLength)) {
				Logger.getLogger(getClass().getName()).warning("The given fftLength is not a power of two.");
				fftLength = MathUtils.nextPowerOfTwo(fftLength);
				Logger.getLogger(getClass().getName()).warning("Using fftLength=" + fftLength);
			}
		}
	}
	
	public int getFFTLength() {
		return fftLength;
	}
	
	public int getTimePoints() {
		return this.timePoints;
	}
	
	public int getFreqPoints() {
		return this.freqPoints;
	}
	
	public double getShift() {
		return shift;
	}
	
	public void setShift(double s) {
		this.shift = s;
	}
	
	public WindowFunction getWindowFunction() {
		return this.windowFunction;
	}
	
	public void setWindowFunction(WindowFunction f) {
		this.windowFunction = f;
	}
	
	public double[][] getSpectrogram()
	{
		return spectrogram;
	}

	/**
	 * Generate STFT.
	 * 
	 * Get generated value using getSpectrum() method.
	 */
	public void generateShortTimeFourierTransformation()
	{
		FFT fft = new FFT();
		
		// calculate dimensions of return matrix
		freqPoints = getFFTLength()/2;
		int winShift = 
			(int)(getFFTLength() * shift);
		timePoints = (int)Math.ceil((double)getLength() / (double)winShift);
		
		// create return matrix
		spectrogram = new double[timePoints][];
		for(int i = 0; i < timePoints; i++) {
			spectrogram[i] = new double[freqPoints];
		}
		
		// temp time window
		double[] tmpTimeDomain = new double[fftLength];
		
		// loop over time windows
		for(int i = 0, j = 0; i < timePoints; i++, j+=winShift) {
			// copy the time domain window
			for(int k = 0; k < fftLength; k++) {
				int index = j+k;
				if(index < getLength()) 
					tmpTimeDomain[k] = data[index];
				else
					tmpTimeDomain[k] = 0.0;
			}
			// apply correct window function
			switch(this.windowFunction)
			{
			case HAMMING:
				MathUtils.applyHammingWindow(tmpTimeDomain, fftLength);
				break;
				
			case HANN:
				MathUtils.applyHannWindow(tmpTimeDomain, fftLength);
				break;
				
			case BLACKMAN:
				MathUtils.applyBlackmanWindow(tmpTimeDomain, fftLength);
				break;
				
			case TRIANGLE:
				MathUtils.applyTriangleWindow(tmpTimeDomain, fftLength);
				break;
				
			case WELCH:
				MathUtils.applyWelchWindow(tmpTimeDomain, fftLength);
				break;
				
			case GAUSS:
				MathUtils.applyGaussWindow(tmpTimeDomain, fftLength);
				break;
				
			case COSINE:
				MathUtils.applyCosineWindow(tmpTimeDomain, fftLength);
				break;
				
			case ASYMEXPO:
				MathUtils.applyAsymetricalExponentialWindow(tmpTimeDomain, fftLength, 1.0);
				break;
				
			default:
				MathUtils.applyHammingWindow(tmpTimeDomain, fftLength);
				break;
			}
			// calculate fft
			fft.powerSpectrum(fftLength, tmpTimeDomain, spectrogram[i]);
		}
	}
	
}
