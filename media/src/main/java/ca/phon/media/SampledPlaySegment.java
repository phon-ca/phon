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
package ca.phon.media;

import ca.phon.audio.*;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.Mixer.Info;
import javax.swing.*;
import java.beans.*;
import java.io.*;
import java.util.*;

public class SampledPlaySegment extends PlaySegment {
	
	private final Sampled sampled;
	
	private Info mixerInfo = null;
	
	private PlaybackMarkerTask playbackTask = null;

	private VolumeModel volumeModel;

	public SampledPlaySegment(Sampled sampled) {
		this(sampled, new VolumeModel());
	}

	public SampledPlaySegment(Sampled sampled, VolumeModel volumeModel) {
		super(volumeModel);

		this.sampled = sampled;
		this.volumeModel = volumeModel;
	}
	
	public Sampled getSampled() {
		return this.sampled;
	}
	
	private PlaybackMarkerTask getPlaybackTask() {
		return this.playbackTask;
	}
	
	private void setPlaybackTask(PlaybackMarkerTask task) {
		this.playbackTask = task;
	}

	public AudioFormat getAudioFormat() {
		return this.getAudioFormat(1.0f);
	}

	public AudioFormat getAudioFormat(float playbackRate) {
		final AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, sampled.getSampleRate() * playbackRate,
				AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN.getBitsPerSample(), sampled.getNumberOfChannels(), sampled.getNumberOfChannels() * AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN.getBytesPerSample(), sampled.getSampleRate() * playbackRate, false);
		return format;
	}
	
	private Mixer.Info defaultMixerInfo() {
		// return first mixer with target lines
		final Optional<Mixer.Info> mixerInfo = 
				Arrays.stream(AudioSystem.getMixerInfo())
					.filter( (info) -> {
						final Mixer mixer = AudioSystem.getMixer(info);
						boolean include = false;
						for(Line.Info lineInfo:mixer.getSourceLineInfo()) {
							if(lineInfo instanceof DataLine.Info) {
								final DataLine.Info dataLineInfo = (DataLine.Info)lineInfo;
								include = dataLineInfo.getFormats().length > 0;
							}
						}
						return include;
					} )
					.findFirst();
		return mixerInfo.get();
	}
	
	private boolean canPlayAudioFormat(Mixer.Info mixerInfo, AudioFormat audioFormat) {
		final Mixer mixer = AudioSystem.getMixer(mixerInfo);
		boolean canPlay = false;
		if(mixer != null) {
			for(Line.Info lineInfo:mixer.getSourceLineInfo()) {
				if(lineInfo instanceof DataLine.Info) {
					final DataLine.Info dataLineInfo = (DataLine.Info)lineInfo;
					canPlay |= dataLineInfo.isFormatSupported(audioFormat);
				}
			}
			
		}
		return canPlay;
	}
	
	private Info getMixerInfo() {
		if(this.mixerInfo == null) {
			this.mixerInfo = defaultMixerInfo();
		}
		return this.mixerInfo;
	}
	
	@Override
	public void stop() {
		if(!isPlaying()) return;
		PlaybackMarkerTask task = getPlaybackTask();
		if(task != null)
			task.clip.stop();
	}
	
	@Override
	public void playSegment(float startTime, float endTime, float playbackRate) throws IOException {
		if(isPlaying()) return;
		
		AudioFormat format = getAudioFormat(playbackRate);
		
		int firstSample = sampled.sampleForTime(startTime);
		int lastSample = sampled.sampleForTime(endTime);
		int numSamples = lastSample - firstSample;
		
		int numBytesRequired = numSamples * format.getFrameSize();
		byte[] audioData = new byte[numBytesRequired];
		try {
			AudioIO.writeSamples(sampled, firstSample, numSamples, AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN, audioData, 0);
		} catch (AudioIOException e1) {
			throw new IOException(e1);
		}
		
		AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length);
		try {
			final boolean canPlayFormat = canPlayAudioFormat(getMixerInfo(), format);
			if(!canPlayFormat) {
				// playback 16-bit audio
				final AudioFormat playbackFormat = new AudioFormat(
						format.getEncoding(),
						sampled.getSampleRate() * playbackRate,
						16,
						format.getChannels(),
						format.getChannels() * 2,
						format.getFrameRate() * playbackRate,
						format.isBigEndian());
				
				if(AudioSystem.isConversionSupported(format, playbackFormat)) {
					AudioInputStream convertedAis = AudioSystem.getAudioInputStream(playbackFormat, ais);
					ais = convertedAis;
				}
			}
			
			// playback audio using Clip
			@SuppressWarnings("resource")
			final Clip audioClip = (getMixerInfo() == null ? AudioSystem.getClip() : AudioSystem.getClip(getMixerInfo()));
			audioClip.open(ais);

			updateVolume(audioClip);

			final VolumeModelListener volumeModelListener = new VolumeModelListener(audioClip);
			volumeModel.addPropertyChangeListener(volumeModelListener);

			final LineListener lineListener = new LineListener() {
				
				@Override
				public void update(LineEvent event) {
					if(event.getType() == LineEvent.Type.START) {
						setPosition(startTime);
						setPlaying(true);
						final PlaybackMarkerTask task = new PlaybackMarkerTask(audioClip, startTime);
						setPlaybackTask(task);
						task.execute();
					} else if(event.getType() == LineEvent.Type.STOP) {
						setPlaying(false);
						setPlaybackTask(null);
						event.getLine().close();
						volumeModel.removePropertyChangeListener(volumeModelListener);
					}
				}
				
			};
			audioClip.addLineListener(lineListener);
			
			if(isLoop())
				audioClip.loop(Clip.LOOP_CONTINUOUSLY);
			else
				audioClip.start();
		} catch ( LineUnavailableException e) {
			throw new IOException(e);
		} finally {
			ais.close();
		}
	}

	private void updateVolume(Clip audioClip) {
		float vol = volumeModel.isMuted() ? 0.0f : volumeModel.getVolumeLevel();
		FloatControl gainCtrl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
		if(gainCtrl != null) {
			gainCtrl.setValue(20.0f * (float) Math.log10(vol));
		}
	}

	private class VolumeModelListener implements PropertyChangeListener {

		private Clip audioClip;

		public VolumeModelListener(Clip audioClip) {
			this.audioClip = audioClip;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			switch(evt.getPropertyName()) {
				case "muted":
				case "volumeLevel":
					updateVolume(audioClip);
					break;

				default:
					break;
			}
		}

	}

	private class PlaybackMarkerTask extends SwingWorker<Float, Float> {
		
		private final Clip clip;
		
		private final float startTime;
		
		public PlaybackMarkerTask(Clip clip, float startTime) {
			this.clip = clip;
			this.startTime = startTime;
		}

		@Override
		protected Float doInBackground() throws Exception {
			while(isPlaying() && clip.isOpen()) {
				final float lineTime = (clip.getFramePosition() / sampled.getSampleRate());
				final float currentTime = startTime + lineTime;
				publish(currentTime);
				
				try { Thread.sleep(10); } catch(Exception e) {}
			}
			return 0.0f;
		}

		@Override
		protected void process(List<Float> chunks) {
			// only use the last value
			setPosition(chunks.get(chunks.size()-1));
		}
		
	}
	
}
