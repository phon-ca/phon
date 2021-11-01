package ca.phon.audio;

public abstract class AbstractSampled implements Sampled {

	@Override
	public double maximumValue(int channel, int firstSample, int lastSample) {
		double[] extrema = getWindowExtrema(channel, firstSample, lastSample);
		return extrema[1];
	}

	@Override
	public double maximumValue(int channel, float startTime, float endTime) {
		double[] extrema = getWindowExtrema(channel, startTime, endTime);
		return extrema[1];
	}

	@Override
	public double minimumValue(int channel, int firstSample, int lastSample) {
		return 0.0;
	}

	@Override
	public double[][] getWindowExtrema(int firstSample, int lastSample) {
		return SampledUtil.getWindowExtrema(this, firstSample, lastSample);
	}

	@Override
	public double[] getWindowExtrema(int channel, int firstSample, int lastSample) {
		double[] retVal = new double[2];
		getWindowExtrema(channel, firstSample, lastSample, retVal);
		return retVal;
	}

	@Override
	public void getWindowExtrema(int channel, int firstSample, int lastSample, double[] extrema) {
		SampledUtil.getWindowExtrema(this, channel, firstSample, lastSample, extrema);
	}

	public double[][] getWindowExtrema(float firstTime, float endTime) {
		return getWindowExtrema(sampleForTime(firstTime), sampleForTime(endTime));
	}

	@Override
	public double[] getWindowExtrema(int channel, float startTime, float endTime) {
		return getWindowExtrema(channel, sampleForTime(startTime), sampleForTime(endTime));
	}

	@Override
	public void getWindowExtrema(int channel, float startTime, float endTime, double[] extrema) {
		getWindowExtrema(channel, sampleForTime(startTime), sampleForTime(endTime), extrema);
	}

}
