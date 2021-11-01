package ca.phon.audio;

import java.io.IOException;

/**
 * Helper sampled methods
 */
public final class SampledUtil {

	public static double[][] getWindowExtrema(Sampled sampled, int firstSample, int lastSample) {
		int numSamples = (int)(lastSample - firstSample);
		double[][] retVal = new double[sampled.getNumberOfChannels()][];
		for(int i = 0; i < sampled.getNumberOfChannels(); i++) {
			retVal[i] = new double[2];
		}

		double[][] data = new double[sampled.getNumberOfChannels()][];
		for(int i = 0; i < sampled.getNumberOfChannels(); i++) {
			data[i] = new double[numSamples];
		}

		sampled.loadSampleData(data, 0, firstSample, numSamples);
		for(int isamp = 0; isamp < numSamples; isamp++) {
			for(int ichan = 0; ichan < sampled.getNumberOfChannels(); ichan++) {
				retVal[ichan][0] = Math.min(retVal[ichan][0], data[ichan][isamp]);
				retVal[ichan][1] = Math.max(retVal[ichan][1], data[ichan][isamp]);
			}
		}

		return retVal;
	}

	public static void getWindowExtrema(Sampled sampled, int channel, int firstSample, int lastSample, double[] extrema) {
		int numSamples = (int)(lastSample - firstSample);

		double[][] data = new double[sampled.getNumberOfChannels()][];
		for(int i = 0; i < sampled.getNumberOfChannels(); i++) {
			data[i] = new double[numSamples];
		}

		sampled.loadSampleData(data, 0, firstSample, numSamples);
		for(int isamp = 0; isamp < numSamples; isamp++) {
			for(int ichan = 0; ichan < sampled.getNumberOfChannels(); ichan++) {
				extrema[0] = Math.min(extrema[0], data[ichan][isamp]);
				extrema[1] = Math.max(extrema[1], data[ichan][isamp]);
			}
		}
	}

}
