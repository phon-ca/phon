/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.media.wavdisplay;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import ca.phon.application.PhonTask;
import ca.phon.application.PhonWorker;
import ca.phon.media.exceptions.PhonMediaException;
import ca.phon.system.logger.PhonLogger;

/**
 * Displays a 16bit wave file.
 *
 */
public class WavDisplay extends JComponent {
	
	// amount of space to leave on the left and right sides of the timebar and channel displays
	public static final int _TIME_INSETS_ = 35;
	
	/* List of properties */
	public static final String _SEGMENT_VALUE_PROP_ = "_Segment_";
	
//	/** Send when the segment selection was manually chagned (i.e., using enter) */
//	public static final String _SEGMENT_MODIFIED_PROP_ = "_Segment_Modified_";
	
	/* File */
	private File _file;
	
	/* Use our own worker thread? */
	private boolean usePrivateWorkerThread = false;

	private PhonWorker _worker;
	
	/* Wav data */
	private WavHelper _audioInfo;
	
	/* Displayed segment */
	private WavHelper _segmentInfo;
	
	/* Is the loader running? */
	private volatile boolean _loading = true;
	
	private JPanel _channelPanel = new JPanel(new GridLayout(0, 1));
	
//	private List<ChannelDisplay> _channelDisplays = 
//		new ArrayList<ChannelDisplay>();
	
	private TimeBar _timeBar;
	
	private double _selectionStart = -1;
	private double _selectionEnd = -1;
	
	/** The amount of time to add to all displayed values */
	private long _displayOffset = 0;
	private long _displayLength = 0;
	
	/** Playback state*/
	private volatile Boolean _playing = false;
//	private DataLine _dataLine;
//	private Integer _dataLineMutex = 0;
	
	/**
	 * Listens for changes to the audio line
	 * when playing back data.
	 *
	 */
	private class AudioLineListener implements LineListener {

		@Override
		public void update(LineEvent event) {
//			System.out.println(event);
			if(event.getType() == LineEvent.Type.START) {
				synchronized(_playing) {
					_playing = true;
//					_dataLine = (SourceDataLine)event.getLine();
				}
//				System.out.println(_segmentInfo.timeForFile());
				_worker.invokeLater(new PlaybackMarkerTask((Clip)event.getLine(), (int)_segmentInfo.timeForFile()+100));
				repaint();
			} else if(event.getType() == LineEvent.Type.STOP) {
				synchronized(_playing) {
					_playing = false;
				}
				
				long endPos = 0;
				if(_selectionStart >= 0 && _selectionEnd >= 0) {
					endPos = (long)Math.max(_selectionStart, _selectionEnd);
				} else {
					endPos = _timeBar.getSegStart()+_timeBar.getSegLength();
				}
				// make sure marker ends where it should
				_timeBar.setCurrentMs(endPos);
				
				repaint();
				
				// close audio line when done
				event.getLine().close();
			}
		}
		
	}
	
	/**
	 * Moves the display marker to current audio position
	 * during playback.
	 *
	 */
	private class PlaybackMarkerTask extends PhonTask {
		
		long timeout = 0;
		
		Clip dataLine;
		
		public PlaybackMarkerTask(Clip line, long timeout) {
			this.dataLine = line;
			this.timeout = timeout;
		}

		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			
//			int numRepaintSent = 0;
			int lastX = -1;
			long startTime = System.currentTimeMillis();
			while(true) {
				synchronized(_playing) {
					if(!_playing)
						break;
				}
				long newTime = System.currentTimeMillis();
				if(newTime - startTime > timeout) {
					break;
				}
				long microSecPos = 0;
				if(dataLine == null) break;
				microSecPos = 
					dataLine.getMicrosecondPosition();
				
				long msPos = Math.round(microSecPos / 1000.0);
				
//				System.out.println(msPos);
				
				if(_selectionStart >= 0 && _selectionEnd >=0) {
					double selStart = 
						Math.min(_selectionStart, _selectionEnd);
					msPos += Math.round(selStart);
				} else {
					msPos += _timeBar.getSegStart() - _timeBar.getStartMs();
				}
				
				_timeBar.setCurrentMs(msPos);
				
				// only send repaint if x position moves
				double msPerPixel = (_timeBar.getEndMs() - _timeBar.getStartMs()) / (double)(_timeBar.getWidth() - 2 * WavDisplay._TIME_INSETS_);
				int xPos = (int)(msPos / msPerPixel) + WavDisplay._TIME_INSETS_;
				if(xPos != lastX) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							repaint();
						}
					});
//					numRepaintSent++;
				} else {
					lastX = xPos;
				}
//				repaint();
				
				try {
					Thread.sleep(1000/30);
				} catch (InterruptedException e) {
					super.setStatus(TaskStatus.ERROR);
					super.err = e;
					break;
				}
			}
//			System.out.println("Number of paints: " + numRepaintSent);
			
			super.setStatus(TaskStatus.FINISHED);
		}
		
	}
	
//	/* Task to export the audio sample needed */
//	private class AudioExporter extends PhonTask {
//
//		private String inFile;
//		private long segStart;
//		private long segDuration;
//		
//		public AudioExporter(String file, long segStart, long segDuration) {
//			super();
//			
//			this.inFile = file;
//			this.segStart = segStart;
//			this.segDuration = segDuration;
//		}
//		
//		@Override
//		public void performTask() {
//			super.setStatus(TaskStatus.RUNNING);
//			
//			try {
//				File tmpFile = File.createTempFile("phon", ".wav", 
//						new File(PhonUtilities.getTempDirectory()));
//				
//				tmpFile.deleteOnExit();
//				
//				if(!PhonMediaUtilities.create16bitAudioSample(
//						inFile, segStart, (int)segDuration, tmpFile.getAbsolutePath())) {
//					super.setStatus(TaskStatus.ERROR);
//					super.err = new PhonMediaException("Could not create 16bit audio sample");
//					return;
//				}
//				
//				_file = tmpFile;
//			} catch (IOException e) {
//				super.setStatus(TaskStatus.ERROR);
//				super.err = e;
//				return;
//			}
//			
//			
//			super.setStatus(TaskStatus.FINISHED);
//		}
//		
//	}
	
	/*
	 * Task to load sample data.
	 */
	private class SampleLoader extends PhonTask {
		
		private WavHelper segmentInfo;
		
		public SampleLoader(WavHelper segInfo) {
			this.segmentInfo = segInfo;
		}

		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			_loading = true;
//			repaint();
			
			try {
//				if(_audioInfo != null) {
//					_audioInfo = null;
//					_channelDisplays.clear();
////					System.gc();
//				}
//				AudioInputStream audioInputStream = 
//					AudioSystem.getAudioInputStream(_file);
//				_audioInfo = new AudioInfo(audioInputStream);
				
				// create a task for the EVT thread
				Runnable run = new Runnable() {
					@Override
					public void run() {
						if(_audioInfo == null) return;
						
//						WavHelper _segInfo = 
//							_audioInfo.getSegment(_dipslayOffset, _displayLength);
						
						MouseTimeListener mListener = new MouseTimeListener();
//						_timeBar.setSegStart(500);
//						_timeBar.setSegLength(3000);
						_timeBar.setStartMs(_displayOffset);
						_timeBar.setEndMs(_displayOffset+_displayLength);
//						_timeBar.setEndMs((long)segmentInfo.timeForFile());
						_timeBar.addMouseMotionListener(mListener);
						_timeBar.addMouseListener(mListener);
						
						
						_channelPanel.removeAll();
						
						for(int channel = 0; channel < segmentInfo.getNumberOfChannels(); channel++) {
							
							
							ChannelDisplay cd = new ChannelDisplay(segmentInfo, channel, WavDisplay.this);
							cd.addMouseMotionListener(mListener);
							cd.addMouseListener(mListener);
//							_channelDisplays.add(cd);
							_channelPanel.add(cd);
						}
						_timeBar.repaint();
						revalidate();
//						JFrame parentFrame = 
//							(JFrame)SwingUtilities.getAncestorOfClass(JFrame.class, WavDisplay.this);
//						if(parentFrame != null)
//							parentFrame.pack();
					}
				};
				SwingUtilities.invokeLater(run);

				super.setStatus(TaskStatus.FINISHED);
			} catch (Exception e) {
				PhonLogger.severe(e.toString());
				super.setStatus(TaskStatus.ERROR);
				super.err = e;
			}
			
			
			_loading = false;
			repaint();
		}
		
	}
	
	private class DisplayKeyListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
				synchronized(_playing) {
					if(_playing) {
//						// try to stop playback
//						if(_dataLine != null) {
//							_dataLine.stop();
//							_dataLine.close();
//						}
					} else {
						// clear selection
						_selectionStart = -1;
						_selectionEnd = -1;
						repaint();
					}
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			
		}

		@Override
		public void keyTyped(KeyEvent e) {
			if(e.getKeyChar() == KeyEvent.VK_ENTER) {
				onEnter(e);
			}
//			else if (e.getKeyChar() == KeyEvent.VK_SPACE) {
//				if(!_playing) {
//					if(_selectionStart >= 0 && _selectionEnd >=0) {
//						playSelection();
//					} else {
//						playSegment();
//					}
//				}
//			}
		}
		
	}
	
	private class MouseTimeListener extends MouseInputAdapter {

		private boolean _selecting = false;
		private int _xPos = 0;
		
		@Override
		public void mouseMoved(MouseEvent e) {
			
			synchronized(_playing) {
				if(_playing) return;
			}
			Rectangle2D segStartRect = _timeBar.getSegStartRect();
			Rectangle2D segEndRect = _timeBar.getSegEndRect();
			
			int segStartX = (int)Math.round(segStartRect.getX() + segStartRect.getWidth());
			int segEndX = (int)Math.round(segEndRect.getX());
			
			if(e.getX() == segStartX ||
					e.getX() == segEndX) {
				setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			} else {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}
			
			int x = e.getX() - WavDisplay._TIME_INSETS_;
			
			if(x < 0 || x > getWidth() - 2* WavDisplay._TIME_INSETS_) {
				_timeBar.setCurrentMs(-1);
			} else {
				double msPerPixel = 
					_segmentInfo.timeForFile() / (getWidth() - 2 * _TIME_INSETS_);
//				System.out.println(_segmentInfo.timeForFile() + " / " + (double)(getWidth()-2*_TIME_INSETS_) + " = " + msPerPixel);
				double time = x * msPerPixel;
				_timeBar.setCurrentMs(Math.round(time));
			}
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			synchronized(_playing) {
				if(_playing) return;
			}
			
			if(_selecting) {
				double msPerPixel = _segmentInfo.timeForFile() / (getWidth() - 2 * _TIME_INSETS_);
				_selectionEnd = (e.getX() - WavDisplay._TIME_INSETS_) * msPerPixel;
				if(_selectionEnd < 0.0)
					_selectionEnd = 0.0;
				if(_selectionEnd > _segmentInfo.timeForFile()) 
					_selectionEnd = _segmentInfo.timeForFile();
				_timeBar.setCurrentMs(Math.round(_selectionEnd));
				repaint();
			} else {
				mouseMoved(e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			synchronized(_playing) {
				if(_playing) return;
			}
			requestFocusInWindow();
			_selectionStart = -1;
			_selectionEnd = -1;
			if(!_selecting) {
				if(e.getX() >= WavDisplay._TIME_INSETS_ && e.getX() < getWidth() - WavDisplay._TIME_INSETS_) {
					_xPos = e.getX() - WavDisplay._TIME_INSETS_;
					_selecting = true;
					double msPerPixel = _segmentInfo.timeForFile() / (getWidth() - 2 * _TIME_INSETS_);
					_selectionStart = _xPos * msPerPixel;
				}
			}
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			synchronized(_playing) {
				if(_playing) return;
			}
			if(_selecting) {
				_selecting = false;
			}
		}
		
		
		
	}
	
	public WavDisplay() {
		super();
		
		setLayout(new BorderLayout());
		add(_channelPanel, BorderLayout.CENTER);
		
		this._timeBar = new TimeBar(this);
		add(_timeBar, BorderLayout.NORTH);
		
		super.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		if(usePrivateWorkerThread) {
			this._worker = PhonWorker.createWorker();
			this._worker.setName("Audio Sample Loader");
			this._worker.start();
		} else {
			this._worker = PhonWorker.getInstance();
		}
		
		super.setFocusable(true);
		this.addKeyListener(new DisplayKeyListener());
	}
	
	public WavDisplay(String file) {
		this(new File(file));
	}
	
	public WavDisplay(File file) {
		super();
		
		setLayout(new BorderLayout());
		add(_channelPanel, BorderLayout.CENTER);
		
		this._timeBar = new TimeBar(this);
		add(_timeBar, BorderLayout.NORTH);
		
		super.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		this._file = file;

		if(usePrivateWorkerThread) {
			this._worker = PhonWorker.createWorker();
			this._worker.setName("Audio Sample Loader");
	//		this._worker.invokeLater(new SampleLoader());
			this._worker.start();
		} else {
			this._worker = PhonWorker.getInstance();
		}
		
		this._audioInfo = new WavHelper(file);
		
		super.setFocusable(true);
		this.addKeyListener(new DisplayKeyListener());
	}
	
	public void shutdown() {
		_worker.shutdown();
	}
	
//	public WavDisplay(String bigMovie, long segStart, long segDuration) {
//		super();
//		
//		setLayout(new BorderLayout());
//		add(_channelPanel, BorderLayout.CENTER);
//		
//		this._timeBar = new TimeBar(this);
//		add(_timeBar, BorderLayout.NORTH);
//		
//		super.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
//		
//		this._worker = PhonWorker.createWorker();
//		this._worker.setName("Audio Sample Loader");
//		this._worker.invokeLater(new AudioExporter(bigMovie, segStart, segDuration));
//		this._worker.invokeLater(new SampleLoader());
//		this._worker.start();
//		
//		this._dipslayOffset = segStart;
//		
//		super.setFocusable(true);
//		this.addKeyListener(new DisplayKeyListener());
//	}
	
	public void loadFile(File file) {
		this._file = file;
		
		if(file == null)
			_audioInfo = null;
		else
			_audioInfo = new WavHelper(file);
//		this._audioInfo = new WavHelper(file);
//		this._worker.invokeLater(new SampleLoader());
	}
	
	public File getFile() {
		return this._file;
	}
	
	/**
	 * Clears channel displays.
	 */
	public void clear() {
//		for(ChannelDisplay cd:_channelDisplays){
//			remove(cd);
//		}
//		_channelDisplays.clear();
		_channelPanel.removeAll();
		revalidate();
	}
	
	public void load(long start, long dur) {
		this._displayOffset = start;
		this._displayLength = dur;
		
		_segmentInfo = _audioInfo.getSegment(_displayOffset, _displayLength);
//		this._worker.invokeLater(new AudioExporter(bigFile, start, dur));
		this._worker.invokeLater(new SampleLoader(_segmentInfo));
	}
	
	public void reload() {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				if(_audioInfo == null) return;
				
				WavHelper _segInfo = 
					_audioInfo.getSegment(_displayOffset, _displayLength);
//				_segInfo.playStream();
				System.out.println(_segInfo);
				
				MouseTimeListener mListener = new MouseTimeListener();
//				_timeBar.setSegStart(500);
//				_timeBar.setSegLength(3000);
				_timeBar.setEndMs((long)_segInfo.timeForFile());
				_timeBar.addMouseMotionListener(mListener);
				_timeBar.addMouseListener(mListener);
				
				_channelPanel.removeAll();
				
				for(int channel = 0; channel < _segInfo.getNumberOfChannels(); channel++) {
					
					
					ChannelDisplay cd = new ChannelDisplay(_segInfo, channel, WavDisplay.this);
					cd.addMouseMotionListener(mListener);
					cd.addMouseListener(mListener);
//					_channelDisplays.add(cd);
					_channelPanel.add(cd);
				}
				
				revalidate();
//				JFrame parentFrame = 
//					(JFrame)SwingUtilities.getAncestorOfClass(JFrame.class, WavDisplay.this);
//				if(parentFrame != null)
//					parentFrame.pack();
			}
		};
		SwingUtilities.invokeLater(run);
	}
	
	public boolean is_loading() {
		return _loading;
	}

	public TimeBar get_timeBar() {
		return _timeBar;
	}

	public double get_selectionStart() {
		return _selectionStart;
	}

	public void set_selectionStart(int start) {
		_selectionStart = start;
	}

	public double get_selectionEnd() {
		return _selectionEnd;
	}

	public void set_selectionEnd(int end) {
		_selectionEnd = end;
	}
	
	/** Playback */
	private void playSelection() 
		throws PhonMediaException {
//		PhonTask task = new PhonTask() {
//			@Override
//			public void performTask() {
				double segStart = 
					Math.min(_selectionStart, _selectionEnd);
				double segLen = 
					Math.abs(_selectionEnd - + _selectionStart);
				
				WavHelper selection = 
					_segmentInfo.getSegment(segStart, segLen);
				
				selection.playStream(lineListener);
//			}
//		};
//		_worker.invokeLater(task);
	}
	
	private void playSegment() 
		throws PhonMediaException {
//		PhonTask task = new PhonTask() {
//
//			@Override
//			public void performTask() {
				double segStart = (double)_timeBar.getSegStart()-_displayOffset;
				double segLen = (_timeBar.getSegLength());
//				
				WavHelper segment = 
					_segmentInfo.getSegment(segStart, segLen);
				segment.playStream(lineListener);
				
//			}
//			
//		};
//		_worker.invokeLater(task);
	}
	
	private AudioLineListener lineListener = new AudioLineListener();
	/**
	 * Play the current selection/segment.
	 */
	public void play() 
		throws PhonMediaException {
		synchronized(_playing) {
			if(_playing) return;
		}
		if(lineListener == null) {
			lineListener = new AudioLineListener();
		}
//		Runnable r = new Runnable() {
//			public void run() {
//				if(!_playing) {
					if(_selectionStart >= 0 && _selectionEnd >=0) {
						playSelection();
					} else {
						playSegment();
					}
//				}
//			}
//		};
//		_worker.invokeLater(r);
	}
	
//	private void playStream(AudioInputStream stream) {
//		AudioFormat format = stream.getFormat();
//		SourceDataLine line = null;
//		DataLine.Info	info = new DataLine.Info(SourceDataLine.class,
//				 format);
//		try
//		{
//			line = (SourceDataLine) AudioSystem.getLine(info);
//			
//			/*
//			The line is there, but it is not yet ready to
//			receive audio data. We have to open the line.
//			*/
//			line.open(format);
//		}
//		catch (LineUnavailableException e)
//		{
//			e.printStackTrace();
//			System.exit(1);
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			System.exit(1);
//		}
//		
//		line.addLineListener(new AudioLineListener());
//		
//		/*
//		Still not enough. The line now can receive data,
//		but will not pass them on to the audio output device
//		(which means to your sound card). This has to be
//		activated.
//		*/
//		line.start();
//
//		int	nBytesRead = 0;
//		byte[]	abData = new byte[128];
//		while (nBytesRead != -1)
//		{
//			try
//			{
//				nBytesRead = stream.read(abData, 0, abData.length);
//			}
//			catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//			if (nBytesRead >= 0)
//			{
//				line.write(abData, 0, nBytesRead);
//			}
//		}
//		
//		if(line.available() > 0)
//			line.drain();
//		
//		line.stop();
//		line.close();
//	}

	/* Key event handlers */
	private void onEnter(KeyEvent e) {
		if(get_selectionStart() >= 0 && get_selectionEnd() >= 0) {
			double segStart = 
				Math.min(get_selectionStart(), get_selectionEnd());
			double segLen = 
				Math.abs(get_selectionStart() - get_selectionEnd());
			_timeBar.setSegStart(Math.round(segStart)+_displayOffset);
			_timeBar.setSegLength(Math.round(segLen));
			
			// clear selection
			set_selectionStart(-1);
			set_selectionEnd(-1);
			
			super.firePropertyChange(_SEGMENT_VALUE_PROP_, false, true);
//			super.firePropertyChange(SEGMENT_, oldValue, newValue)
			repaint();
		}
	}
	
	public WavHelper getSegmentInfo() {
		double segStart = (double)_timeBar.getSegStart()-_displayOffset;
		double segLen = (_timeBar.getSegLength());
		WavHelper segment = 
			_segmentInfo.getSegment(segStart, segLen);
		return segment;
	}

	public long get_dipslayOffset() {
		return _displayOffset;
	}

	public void set_dipslayOffset(long offset) {
		_displayOffset = offset;
	}
	
}
