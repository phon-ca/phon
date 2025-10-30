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

    /**
     * RMS result record
     *
     * @param rmsValues array of RMS values
     * @param maxRmsValue maximum RMS value
     * @param rmsFramesPerSecond number of RMS frames per second
     */
    public record RmsFrames(float[] rmsValues, float maxRmsValue, float rmsFramesPerSecond) {

        public int numFrames() {
            return rmsValues.length;
        }

        public float frameTime(int frameIndex) {
            return frameIndex / rmsFramesPerSecond;
        }

        public float duration() {
            return rmsValues.length / rmsFramesPerSecond;
        }

    }

    /**
     * Calculate RMS values for the entire audio for all channels
     *
     * @param rmsFramesPerSecond number of RMS frames per second
     * @return array of RmsFrames for each channel
     */
    public RmsFrames[] rmsValues(float rmsFramesPerSecond) {
        return rmsValues(0, getNumberOfSamples(), rmsFramesPerSecond);
    }

    /**
     * Calculate RMS values for the specified sample range for all channels
     *
     * @param firstSample first sample
     * @param lastSample last sample
     * @param rmsFramesPerSecond number of RMS frames per second
     * @return array of RmsFrames for each channel
     */
    public RmsFrames[] rmsValues(int firstSample, int lastSample, float rmsFramesPerSecond) {
        double[][] buffer = new double[getNumberOfChannels()][];
        for (int i = 0; i < getNumberOfChannels(); i++) {
            buffer[i] = new double[lastSample - firstSample];
        }
        loadSampleData(buffer, 0, firstSample, lastSample - firstSample);
        RmsFrames[] rmsValues = new RmsFrames[getNumberOfChannels()];
        for (int i = 0; i < getNumberOfChannels(); i++) {
            rmsValues[i] = rmsValues(buffer[i], getSampleRate(), rmsFramesPerSecond);
        }
        return rmsValues;
    }

    /**
     * Calculate RMS values for the given samples
     *
     * @param samples array of samples
     * @param sampleRate sample rate of the audio
     * @param rmsFramesPerSecond number of RMS frames per second
     * @return RmsFrames containing RMS values and max RMS value
     */
    public RmsFrames rmsValues(double[] samples, float sampleRate, float rmsFramesPerSecond) {
        if (samples == null || samples.length == 0) {
            return new RmsFrames(new float[0], 0.0f, rmsFramesPerSecond);
        }

        int frameSize = Math.round(sampleRate / rmsFramesPerSecond);
        if (frameSize <= 0) {
            frameSize = 1;
        }

        final int numFrames = (int) Math.ceil((double) samples.length / frameSize);
        final float[] rmsValues = new float[numFrames];
        float maxRmsValue = 0.0f;

        for (int frame = 0; frame < numFrames; frame++) {
            final int startIdx = frame * frameSize;
            final int endIdx = Math.min(startIdx + frameSize, samples.length);

            double sumSquares = 0.0;
            final int count = endIdx - startIdx;

            for (int i = startIdx; i < endIdx; i++) {
                sumSquares += samples[i] * samples[i];
            }

            rmsValues[frame] = (float) Math.sqrt(sumSquares / count);
            if (rmsValues[frame] > maxRmsValue) {
                maxRmsValue = rmsValues[frame];
            }
        }

        return new RmsFrames(rmsValues, maxRmsValue, rmsFramesPerSecond);
    }

}
