package ca.phon.media.sampled;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.swing.SwingWorker;

import ca.phon.media.PlaySegment;

public class SampledPlaySegment extends PlaySegment {
	
	private final Sampled sampled;
	
	private Info mixerInfo = null;
	
	private PlaybackMarkerTask playbackTask = null;
	
	public SampledPlaySegment(Sampled sampled) {
		super();
		
		this.sampled = sampled;
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
		final AudioFormat format = new AudioFormat(getSampled().getSampleRate(), 
				getSampled().getSampleSize(), getSampled().getNumberOfChannels(), 
				getSampled().isSigned(), false);
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
	public void playSegment(float startTime, float endTime) throws IOException {
		if(isPlaying()) return;
		
		AudioFormat format = getAudioFormat();
		final byte[] audioData = getSampled().getBytes(startTime, endTime);
		AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length);
		try {
			final boolean canPlayFormat = canPlayAudioFormat(getMixerInfo(), format);
			if(!canPlayFormat) {
				// playback 16-bit audio
				final AudioFormat playbackFormat = new AudioFormat(
						format.getEncoding(),
						format.getSampleRate(),
						16,
						format.getChannels(),
						format.getChannels() * 2,
						format.getFrameRate(),
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
				final long clipPos = clip.getMicrosecondPosition() % clip.getMicrosecondLength();
				final float lineMs = clipPos / 1000.0f / 1000.0f;
				
				final float currentTime = startTime + lineMs;
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
