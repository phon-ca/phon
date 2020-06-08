package ca.phon.audio;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.logging.log4j.core.time.PreciseClock;

/**
 * An audio file. For a list of supported file types see {@link AudioFileType}.
 * For a list of supported encodings see {@link AudioFileEncoding}
 * 
 * This class is heavily based on code from the Praat open-source
 * software program - melder/melder_audiofiles.cpp.
 */
public final class AudioFile implements Closeable {

	static int ulaw2linear[] = { -32124, -31100, -30076, -29052, -28028, -27004, -25980, -24956, -23932, -22908, -21884,
			-20860, -19836, -18812, -17788, -16764, -15996, -15484, -14972, -14460, -13948, -13436, -12924, -12412,
			-11900, -11388, -10876, -10364, -9852, -9340, -8828, -8316, -7932, -7676, -7420, -7164, -6908, -6652, -6396,
			-6140, -5884, -5628, -5372, -5116, -4860, -4604, -4348, -4092, -3900, -3772, -3644, -3516, -3388, -3260,
			-3132, -3004, -2876, -2748, -2620, -2492, -2364, -2236, -2108, -1980, -1884, -1820, -1756, -1692, -1628,
			-1564, -1500, -1436, -1372, -1308, -1244, -1180, -1116, -1052, -988, -924, -876, -844, -812, -780, -748,
			-716, -684, -652, -620, -588, -556, -524, -492, -460, -428, -396, -372, -356, -340, -324, -308, -292, -276,
			-260, -244, -228, -212, -196, -180, -164, -148, -132, -120, -112, -104, -96, -88, -80, -72, -64, -56, -48,
			-40, -32, -24, -16, -8, 0, 32124, 31100, 30076, 29052, 28028, 27004, 25980, 24956, 23932, 22908, 21884,
			20860, 19836, 18812, 17788, 16764, 15996, 15484, 14972, 14460, 13948, 13436, 12924, 12412, 11900, 11388,
			10876, 10364, 9852, 9340, 8828, 8316, 7932, 7676, 7420, 7164, 6908, 6652, 6396, 6140, 5884, 5628, 5372,
			5116, 4860, 4604, 4348, 4092, 3900, 3772, 3644, 3516, 3388, 3260, 3132, 3004, 2876, 2748, 2620, 2492, 2364,
			2236, 2108, 1980, 1884, 1820, 1756, 1692, 1628, 1564, 1500, 1436, 1372, 1308, 1244, 1180, 1116, 1052, 988,
			924, 876, 844, 812, 780, 748, 716, 684, 652, 620, 588, 556, 524, 492, 460, 428, 396, 372, 356, 340, 324,
			308, 292, 276, 260, 244, 228, 212, 196, 180, 164, 148, 132, 120, 112, 104, 96, 88, 80, 72, 64, 56, 48, 40,
			32, 24, 16, 8, 0 };

	static short alaw2linear[] = { -5504, -5248, -6016, -5760, -4480, -4224, -4992, -4736, -7552, -7296, -8064, -7808,
			-6528, -6272, -7040, -6784, -2752, -2624, -3008, -2880, -2240, -2112, -2496, -2368, -3776, -3648, -4032,
			-3904, -3264, -3136, -3520, -3392, -22016, -20992, -24064, -23040, -17920, -16896, -19968, -18944, -30208,
			-29184, -32256, -31232, -26112, -25088, -28160, -27136, -11008, -10496, -12032, -11520, -8960, -8448, -9984,
			-9472, -15104, -14592, -16128, -15616, -13056, -12544, -14080, -13568, -344, -328, -376, -360, -280, -264,
			-312, -296, -472, -456, -504, -488, -408, -392, -440, -424, -88, -72, -120, -104, -24, -8, -56, -40, -216,
			-200, -248, -232, -152, -136, -184, -168, -1376, -1312, -1504, -1440, -1120, -1056, -1248, -1184, -1888,
			-1824, -2016, -1952, -1632, -1568, -1760, -1696, -688, -656, -752, -720, -560, -528, -624, -592, -944, -912,
			-1008, -976, -816, -784, -880, -848, 5504, 5248, 6016, 5760, 4480, 4224, 4992, 4736, 7552, 7296, 8064, 7808,
			6528, 6272, 7040, 6784, 2752, 2624, 3008, 2880, 2240, 2112, 2496, 2368, 3776, 3648, 4032, 3904, 3264, 3136,
			3520, 3392, 22016, 20992, 24064, 23040, 17920, 16896, 19968, 18944, 30208, 29184, 32256, 31232, 26112,
			25088, 28160, 27136, 11008, 10496, 12032, 11520, 8960, 8448, 9984, 9472, 15104, 14592, 16128, 15616, 13056,
			12544, 14080, 13568, 344, 328, 376, 360, 280, 264, 312, 296, 472, 456, 504, 488, 408, 392, 440, 424, 88, 72,
			120, 104, 24, 8, 56, 40, 216, 200, 248, 232, 152, 136, 184, 168, 1376, 1312, 1504, 1440, 1120, 1056, 1248,
			1184, 1888, 1824, 2016, 1952, 1632, 1568, 1760, 1696, 688, 656, 752, 720, 560, 528, 624, 592, 944, 912,
			1008, 976, 816, 784, 880, 848 };
	
	private final static int WAVE_FORMAT_PCM = 0x0001;
	private final static int WAVE_FORMAT_IEEE_FLOAT = 0x0003;
	private final static int WAVE_FORMAT_ALAW = 0x0006;
	private final static int WAVE_FORMAT_MULAW = 0x0007;
	private final static int WAVE_FORMAT_DVI_ADPCM = 0x0011;
	private final static int WAVE_FORMAT_EXTENSIBLE = 0xFFFE;

	private File file;
	
	private RandomAccessFile raf;
	
	// file as memory mapped buffer
	private MappedByteBuffer mappedBuffer;

	private long dataOffset = -1;
	
	private int numberOfChannels = -1;
	
	private double sampleRate = 0.0;
	
	private long numberOfSamples = 0;

	private AudioFileType audioFileType;

	private AudioFileEncoding audioFileEncoding;

	/**
	 * Constructor
	 */
	AudioFile(File file) throws IOException, InvalidHeaderException, UnsupportedFormatException {
		super();

		this.file = file;
		raf = new RandomAccessFile(file, "r");
		audioFileType = checkFile();
		mappedBuffer = raf.getChannel().map(MapMode.READ_ONLY, dataOffset, raf.getChannel().size() - dataOffset);
	}
	
	@Override
	public void close() throws IOException {
		raf.close();
	}
	
	private AudioFileType checkFile() throws IOException, InvalidHeaderException, UnsupportedFormatException {
		byte[] data = new byte[16];
		if(raf.read(data) < 16) throw new UnsupportedFormatException("File too short");
		raf.seek(0L);
		
		String ft = new String(data, 0, 4);
		String ft2 = new String(data, 8, 4);
		if(ft.equals("FORM") && ft2.equals("AIFF")) {
			checkAiffFile();
			return AudioFileType.AIFF;
		} else if(ft.equals("FORM") && ft2.equals("AIFC")) {
			checkAiffFile();
			return AudioFileType.AIFC;
		} else if(ft.equals("RIFF") && (ft2.equals("WAVE") || ft2.equals("CDDA"))) {
			checkWavFile();
			return AudioFileType.WAV;
		}
		
		throw new UnsupportedFormatException("Unsupported file type");
	}

	private void checkWavFile() throws IOException, InvalidHeaderException, UnsupportedFormatException {
		byte[] data = new byte[14];
		byte[] chunkId = new byte[4];
		
		boolean formatChunkPresent = false;
		boolean dataChunkPresent = false;
		
		int numberOfBitsPerSamplePoint = -1;
		int dataChunkSize = 0xffffffff;
		
		int bytesRead = raf.read(data, 0, 4);
		if(bytesRead < 4) throw new InvalidHeaderException("File too small: no RIFF statement");
		String riff = new String(data, 0, 4);
		if(!riff.equals("RIFF")) throw new InvalidHeaderException("Not a WAV file (RIFF statement expected)");
		
		bytesRead = raf.read(data, 0, 4);
		if(bytesRead < 4) throw new InvalidHeaderException("File too small: no size of RIFF chunk.");
		bytesRead = raf.read(data, 0, 4);
		if(bytesRead < 4) throw new InvalidHeaderException("File too small: no file type info (expected WAVE statement).");
		
		String fileTypeInfo = new String(data, 0, 4);
		if(!fileTypeInfo.equals("WAVE") && !fileTypeInfo.equals("CDDA")) {
			throw new InvalidHeaderException("Not a WAVE or CD audio file (wrong file type info).");
		}
		
		/* Search for format and data chunks */
		while(raf.read(chunkId) == 4) {
			int chunkSize = readIntLE(raf);
			String chunk = new String(chunkId);
			if(chunk.equals("fmt ")) {
				short winEncoding = readShortLE(raf);
				formatChunkPresent = true;
				numberOfChannels = readShortLE(raf);
				if(numberOfChannels < 0) throw new InvalidHeaderException("Too few sound channels (" + numberOfChannels + ")");
				sampleRate = (double) readIntLE(raf);
				if(sampleRate < 0.0) throw new InvalidHeaderException("Wrong sampling freq (" + sampleRate + ")");
				// read unused data
				readIntLE(raf); // avgBytesPerSec
				readShortLE(raf); // blockAlign
				numberOfBitsPerSamplePoint = readShortLE(raf);
				
				if(numberOfBitsPerSamplePoint == 0) {
					numberOfBitsPerSamplePoint = 16; // default
				} else if(numberOfBitsPerSamplePoint < 4) {
					throw new InvalidHeaderException("Too few bits per sample (" + numberOfBitsPerSamplePoint + ")");
				} else if(numberOfBitsPerSamplePoint > 64) {
					throw new InvalidHeaderException("Too many bits per sample (" + numberOfBitsPerSamplePoint + "); max is 32");
				}
				
				switch(Short.toUnsignedInt(winEncoding)) {
				case WAVE_FORMAT_PCM:
					audioFileEncoding =
						numberOfBitsPerSamplePoint > 24 ? AudioFileEncoding.LINEAR_32_LITTLE_ENDIAN :
						numberOfBitsPerSamplePoint > 16 ? AudioFileEncoding.LINEAR_24_LITTLE_ENDIAN :
						numberOfBitsPerSamplePoint > 8 ? AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN :
						AudioFileEncoding.LINEAR_8_UNSIGNED;
					break;
					
				case WAVE_FORMAT_IEEE_FLOAT:
					audioFileEncoding = 
						numberOfBitsPerSamplePoint == 64 ? AudioFileEncoding.IEEE_FLOAT_64_LITTLE_ENDIAN :
							AudioFileEncoding.IEEE_FLOAT_32_LITTLE_ENDIAN;
					break;
				
				case WAVE_FORMAT_ALAW:
					audioFileEncoding = AudioFileEncoding.ALAW;
					break;
					
				case WAVE_FORMAT_MULAW:
					audioFileEncoding = AudioFileEncoding.MULAW;
					break;
					
				case WAVE_FORMAT_DVI_ADPCM:
					throw new UnsupportedFormatException("Unsupported encoding: DVI ADPCM");

				case WAVE_FORMAT_EXTENSIBLE: {
					if(chunkSize < 40) throw new InvalidHeaderException("Not enough format data in extensible WAV format");
					readShortLE(raf); // extensionSize
					readShortLE(raf); // validBitsPreSample
					readIntLE(raf); // channelMask
					short winEncoding2 = readShortLE(raf);
					switch(Short.toUnsignedInt(winEncoding2)) {
					case WAVE_FORMAT_PCM:
						audioFileEncoding =
							numberOfBitsPerSamplePoint > 24 ? AudioFileEncoding.LINEAR_32_LITTLE_ENDIAN :
							numberOfBitsPerSamplePoint > 16 ? AudioFileEncoding.LINEAR_24_LITTLE_ENDIAN :
							numberOfBitsPerSamplePoint > 8 ? AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN :
							AudioFileEncoding.LINEAR_8_UNSIGNED;
						break;
						
					case WAVE_FORMAT_IEEE_FLOAT:
						audioFileEncoding = 
							numberOfBitsPerSamplePoint == 64 ? AudioFileEncoding.IEEE_FLOAT_64_LITTLE_ENDIAN :
								AudioFileEncoding.IEEE_FLOAT_32_LITTLE_ENDIAN;
						break;
					
					case WAVE_FORMAT_ALAW:
						audioFileEncoding = AudioFileEncoding.ALAW;
						break;
						
					case WAVE_FORMAT_MULAW:
						audioFileEncoding = AudioFileEncoding.MULAW;
						break;
						
					case WAVE_FORMAT_DVI_ADPCM:
						throw new UnsupportedFormatException("Unsupported encoding: DVI ADPCM");
					
					default:
						throw new InvalidHeaderException("Unsupported windows audio encoding " + Integer.toString(winEncoding, 8));
					}
					bytesRead = raf.read(data, 0, 14);
					if(bytesRead < 14) throw new InvalidHeaderException("File too small: no SubFormat data");
					continue;
				}
					
				default:
					throw new InvalidHeaderException("Unsupported windows audio encoding " + Integer.toString(winEncoding, 8));
				}
				if(chunkSize % 2 == 1) ++chunkSize;
				for(int i = 17; i <= chunkSize; i++) {
					bytesRead = raf.read(data, 0, 1);
					if(bytesRead < 1) throw new InvalidHeaderException("File too small: expected " + chunkSize + " bytes in fmt chunk, but found " + i);
				}
			} else if(chunk.equals("data")) {
				dataChunkPresent = true;
				dataChunkSize = chunkSize;
				dataOffset = raf.getFilePointer();
				if(chunkSize % 2 == 1) ++chunkSize;
				if(chunkSize > Integer.MAX_VALUE - 100) { // incorrect data chunk (sometimes -44); assume that the data run till the end of the file
					long fileLength = raf.length();
					chunkSize = (int)(fileLength - dataOffset);
					dataChunkSize = chunkSize;
				}
				if(formatChunkPresent) break;
			} else {
				if(chunkSize % 2 == 1) ++chunkSize;
				for(int i = 1; i <= chunkSize; i++) {
					bytesRead = raf.read(data, 0, 1);
					if(bytesRead < 1) 
						throw new InvalidHeaderException("File too small: expected " + chunkSize + " bytes, but found " + i);
				}
			}
		} // end while
		
		if(!formatChunkPresent) throw new InvalidHeaderException("Found no format chunk");
		if(!dataChunkPresent) throw new InvalidHeaderException("Found no data chunk");
		numberOfSamples = dataChunkSize / numberOfChannels / ((numberOfBitsPerSamplePoint + 7) / 8);
	}
	
	private void checkAiffFile() throws IOException, InvalidHeaderException, UnsupportedFormatException {
		byte[] data  = new byte[4];
		byte[] chunkID = new byte[4];
		
		boolean commonChunkPresent = false;
		boolean dataChunkPresent = false;
		boolean isAifc = true;
		int numberOfBitsPerSamplePoint;
		
		/* Read header of AIFF(-C) file: 12 bytes. */

		int bytesRead = raf.read(data, 0, 4);
		if(bytesRead != 4) throw new InvalidHeaderException("File too small: no FORM statement.");
		if(!(new String(data, 0, 4)).contentEquals("FORM")) throw new InvalidHeaderException("Not an AIFF or AIFC file (FORM statement expected.)");
				
		bytesRead = raf.read(data, 0, 4);
		if(bytesRead != 4) throw new InvalidHeaderException("File too small: no size of FORM chunk.");
		
		bytesRead = raf.read(data, 0, 4);
		if(bytesRead != 4) throw new InvalidHeaderException("File too small: no file type info (expected AIFF or AIFC).");
		
		String type = new String(data, 0, 4);
		if(!type.equalsIgnoreCase("AIFF") && !type.equalsIgnoreCase("AIFC"))
			throw new InvalidHeaderException("Not an AIFF or AIFC file (wrong file type info).");
		if (type.equalsIgnoreCase("AIFF")) isAifc = false;
		
		/* Search for Common Chunk and Data Chunk. */
		
		while(raf.read(chunkID) == 4) {
			int chunkSize = raf.readInt();
			if(chunkSize % 2 > 0) ++chunkSize; // keep chunksize even
			
			String chunk = new String(chunkID);
			/* IN SOUND FILES PRODUCED BY THE SGI'S soundeditor PROGRAM, */
			/* THE COMMON CHUNK HAS A chunkSize OF 18 INSTEAD OF 38, */
			/* ALTHOUGH THE COMMON CHUNK CONTAINS */
			/* THE 20-BYTE SEQUENCE "\016not compressed\0". */
			/* START FIX OF FOREIGN BUG */
			if(chunk.equals("NONE")
					&& (chunkSize==(14<<24)+('n'<<16)+('o'<<8)+'t'||chunkSize==('t'<<24)+('o'<<16)+('n'<<8)+14)) {
				Logger.getLogger(AudioFile.class.getName()).fine("Fixing SGI soundeditor bug");
				for(int i=1;i<=20/*diff*/-8/*header*/;i++)raf.read(data, 0, 1);continue;
			}
			/* FINISH FIX OF FOREIGN BUG */
			
			if(chunk.equals("COMM")) {
				commonChunkPresent = true;
				numberOfChannels = raf.readShort();
				if(numberOfChannels < 1) throw new InvalidHeaderException("Too few sound channels (" + numberOfChannels + ")");
				
				numberOfSamples = raf.readInt();
				if(numberOfSamples <= 0) throw new InvalidHeaderException("Too few samples (" + numberOfSamples + ")");
				
				numberOfBitsPerSamplePoint = raf.readShort();
				if(numberOfBitsPerSamplePoint > 64) throw new InvalidHeaderException("Too many bits per sample (" + numberOfBitsPerSamplePoint + ")");
				
				audioFileEncoding =
					numberOfBitsPerSamplePoint > 24 ? AudioFileEncoding.LINEAR_32_BIG_ENDIAN :
					numberOfBitsPerSamplePoint > 16 ? AudioFileEncoding.LINEAR_24_BIG_ENDIAN :
					numberOfBitsPerSamplePoint > 8 ? AudioFileEncoding.LINEAR_16_BIG_ENDIAN :
					AudioFileEncoding.LINEAR_8_SIGNED;
					
				sampleRate = readLongDouble(raf);
				if(sampleRate <= 0.0) throw new InvalidHeaderException("Wrong sample rate");
				if(isAifc) {
					/*
					 * Read compression info
					 */
					bytesRead = raf.read(data, 0, 4);
					if(bytesRead < 4) throw new InvalidHeaderException("File too small: no compression info.");
					
					String ctype = new String(data, 0, 4);
					if (! ctype.equalsIgnoreCase("NONE") && ! ctype.equalsIgnoreCase("sowt") 
							&& !ctype.equalsIgnoreCase("ALAW") && !ctype.equalsIgnoreCase("ULAW")
							&& !ctype.equalsIgnoreCase("fl32") && !ctype.equalsIgnoreCase("fl64") ) {
						throw new UnsupportedFormatException("Cannot compressed AIFC file with compression type " + ctype);
					}
					if (ctype.equalsIgnoreCase("sowt")) {
						audioFileEncoding =
							numberOfBitsPerSamplePoint > 24 ? AudioFileEncoding.LINEAR_32_LITTLE_ENDIAN :
							numberOfBitsPerSamplePoint > 16 ? AudioFileEncoding.LINEAR_24_LITTLE_ENDIAN :
							numberOfBitsPerSamplePoint > 8 ? AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN :
							AudioFileEncoding.LINEAR_8_SIGNED;
					} else if(ctype.equalsIgnoreCase("ALAW")) {
						audioFileEncoding = AudioFileEncoding.ALAW;
					} else if(ctype.equalsIgnoreCase("ULAW")) {
						audioFileEncoding = AudioFileEncoding.MULAW;
					} else if(ctype.equalsIgnoreCase("fl32")) {
						audioFileEncoding = AudioFileEncoding.IEEE_FLOAT_32_BIG_ENDIAN;
					} else if(ctype.equalsIgnoreCase("fl64")) {
						audioFileEncoding = AudioFileEncoding.IEEE_FLOAT_64_BIG_ENDIAN;
					}
					
					/*
					 * Read rest of compression info.
					 */
					for (int i = 23; i <= chunkSize; i ++)
						if (raf.read(data, 0, 1) < 1)
							throw new InvalidHeaderException("File too small: expected chunk of " + chunkSize + " bytes, but found " + (i + 22) + ".");
				}
			} else if(chunk.equals("SSND")) {
				dataChunkPresent = true;
				dataOffset = raf.getFilePointer() + 8;
				if(commonChunkPresent) break;
			} else {
				for(int i = 1; i <= chunkSize; i++) {
					if(raf.read(data, 0, 1) < 1) 
						throw new InvalidHeaderException("File too small: expected chunk of " + chunkSize + " bytes, but found " + i + ".");
				}
			}
		}
		
		if(!commonChunkPresent) throw new InvalidHeaderException("Found no common chunk.");
		if(!dataChunkPresent) throw new InvalidHeaderException("Found no data chunk.");
	}

	public File getFile() {
		return file;
	}

	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	public double getSampleRate() {
		return sampleRate;
	}

	public long getNumberOfSamples() {
		return numberOfSamples;
	}

	public AudioFileType getAudioFileType() {
		return audioFileType;
	}

	public AudioFileEncoding getAudioFileEncoding() {
		return audioFileEncoding;
	}
	
	public long sampleIndexForTime(float time) {
		final double frameRate = getSampleRate();
		final long sampleIdx = Math.round(frameRate * time);
		return sampleIdx;
	}
	
	public float timeForSampleIndex(long sample) {
		final double frameRate = getSampleRate();
		final float time = 
				BigDecimal.valueOf(sample / frameRate).setScale(3, RoundingMode.HALF_UP).floatValue();
		return time;
	}
	
	public float getLength() {
		return (float)(getNumberOfSamples() / getSampleRate());
	}
	
	public synchronized void seekToTime(float time) {
		long sample = sampleIndexForTime(time);
		seekToSample(sample);
	}
	
	public synchronized void seekToSample(long sample) {
		long byteIdx = sample * getFrameSize();
		mappedBuffer.position((int)byteIdx);
	}
	
	/**
	 * Read buffer[0].length samples
	 * from buffer.length channels starting
	 * from the current position
	 * 
	 * @param buffer
	 */
	public synchronized void readSamples(double[][] buffer) throws IOException {
		if(buffer.length == 0) // no samples to read
			return;
		if(buffer[0] == null)
			throw new IOException(new NullPointerException("buffer[0]"));
		readSamples(buffer, 0, buffer[0].length);
	}
	
	/**
	 * Read length samples from buffer.length channels
	 * Data will be inserted into buffer[channel] starting at offset
	 * 
	 * @param buffer
	 * @param offset
	 * @param length
	 * 
	 * @returns number of samples read
	 */
	public synchronized int readSamples(double[][] buffer, int offset, int length) throws IOException {
		int numChannels = buffer.length;
		int numSamples = length;
		
		int samplesRead = 0;
				
		switch(getAudioFileEncoding()) {
		case LINEAR_8_SIGNED: {
			try {
				for(int isamp = 0; isamp < numSamples; isamp++) {
					for(int ichan = 0; ichan < numChannels; ichan++) {
						byte value = mappedBuffer.get();
						buffer[ichan][isamp] = (float)(value * (1.0f/128.0f));
					}
					++samplesRead;
				}
			} catch (ArrayIndexOutOfBoundsException | BufferUnderflowException e) {
				throw new IOException(e);
			}
		} break; // LINEAR_8_SIGNED
			
		case LINEAR_8_UNSIGNED: {
			try {
				for(int isamp = 0; isamp < numSamples; isamp++) {
					for(int ichan = 0; ichan < numChannels; ichan++) {
						byte value = mappedBuffer.get();
						buffer[ichan][isamp] = (float)(Byte.toUnsignedInt(value) * (1.0f / 128.0f) - 1.0f);
					}
					++samplesRead;
				}
			} catch (ArrayIndexOutOfBoundsException | BufferUnderflowException e) {
				throw new IOException(e);
			}
		} break; // LINEAR_8_UNSIGNED
		
		case LINEAR_16_LITTLE_ENDIAN:
		case LINEAR_16_BIG_ENDIAN:
		case LINEAR_24_LITTLE_ENDIAN:
		case LINEAR_24_BIG_ENDIAN:
		case LINEAR_32_LITTLE_ENDIAN:
		case LINEAR_32_BIG_ENDIAN: {
			int numberOfBytes = (int)(
					getNumberOfChannels() * numSamples * getAudioFileEncoding().getBytesPerSample());
			int frameSize = getFrameSize();
			
			byte[] buf = new byte[numberOfBytes];
			mappedBuffer.get(buf);
			
			switch(getAudioFileEncoding()) {
			case LINEAR_16_BIG_ENDIAN: 
				for(int isamp = 0; isamp < numSamples; isamp++) {
					for(int ichan = 0; ichan < numChannels; ichan++) {
						int byteOffset = (isamp * frameSize) + (ichan * getAudioFileEncoding().getBytesPerSample());
						byte b1 = buf[byteOffset];
						byte b2 = buf[byteOffset+1];						
						short value = 
							(short)(
								(((int)b1 & 0xff) << 8) |
								((int)b2 & 0xff));
						buffer[ichan][isamp + offset] = value * (1.0f / 32768.0f);
					}
					++samplesRead;
				} break;
			
			
			case LINEAR_16_LITTLE_ENDIAN:
				for(int isamp = 0; isamp < numSamples; isamp++) {
					for(int ichan = 0; ichan < numChannels; ichan++) {
						int byteOffset = (isamp * frameSize) + (ichan * getAudioFileEncoding().getBytesPerSample());
						byte b1 = buf[byteOffset];
						byte b2 = buf[byteOffset+1];						
						short value = 
							(short)(
								(((int)b2 & 0xff) << 8) |
								((int)b1 & 0xff));
						buffer[ichan][isamp + offset] = value * (1.0f / 32768.0f);
					}
					++samplesRead;
				} break;
				
			case LINEAR_24_BIG_ENDIAN:
				for(int isamp = 0; isamp < numSamples; isamp++) {
					for(int ichan = 0; ichan < numChannels; ichan++) {
						int byteOffset = (isamp * frameSize) + (ichan * getAudioFileEncoding().getBytesPerSample());
						byte b1 = buf[byteOffset];
						byte b2 = buf[byteOffset+1];
						byte b3 = buf[byteOffset+2];
						int unsignedValue = 
								(int)(
									(((int)b1 & 0xff) << 16)
								|	(((int)b2 & 0xff) << 8)
								|	((int)b3 & 0xff)
								);
						if( ((int)b1 & 0x80) != 0) unsignedValue |= 0xff000000; // extend sign
						buffer[ichan][isamp + offset] =  unsignedValue * (1.0f / 8388608.0f);
					}
					++samplesRead;
				} break;
				
			case LINEAR_24_LITTLE_ENDIAN:
				for(int isamp = 0; isamp < numSamples; isamp++) {
					for(int ichan = 0; ichan < numChannels; ichan++) {
						int byteOffset = (isamp * frameSize) + (ichan * getAudioFileEncoding().getBytesPerSample());
						byte b1 = buf[byteOffset];
						byte b2 = buf[byteOffset+1];
						byte b3 = buf[byteOffset+2];
						int unsignedValue = 
								(int)(
									(((int)b3 & 0xff) << 16)
								|	(((int)b2 & 0xff) << 8)
								|	((int)b1 & 0xff)
								);
						if(((int)b3 & 0x80) != 0) unsignedValue |= 0xff000000; // extend sign
						buffer[ichan][isamp + offset] =  unsignedValue * (1.0f / 8388608.0f);
					}
					++samplesRead;
				}
				break;
				
			case LINEAR_32_BIG_ENDIAN:
				for(int isamp = 0; isamp < numSamples; isamp++) {
					for(int ichan = 0; ichan < numChannels; ichan++) {
						int byteOffset = (isamp * frameSize) + (ichan * getAudioFileEncoding().getBytesPerSample());
						byte b1 = buf[byteOffset];
						byte b2 = buf[byteOffset+1];
						byte b3 = buf[byteOffset+2];
						byte b4 = buf[byteOffset+3];
						int unsignedValue = 
								(int)(
								    (((int)b1 & 0xff) << 24)
								|	(((int)b2 & 0xff) << 16)
								|	(((int)b3 & 0xff) << 8)
								|	((int)b4 & 0xff)
								);
						buffer[ichan][isamp + offset] =  unsignedValue * (1.0f / 32768.0f / 65536.0f);
					}
					++samplesRead;
				}
				break;
				
			case LINEAR_32_LITTLE_ENDIAN:
				for(int isamp = 0; isamp < numSamples; isamp++) {
					for(int ichan = 0; ichan < numChannels; ichan++) {
						int byteOffset = (isamp * frameSize) + (ichan * getAudioFileEncoding().getBytesPerSample());
						byte b1 = buf[byteOffset];
						byte b2 = buf[byteOffset+1];
						byte b3 = buf[byteOffset+2];
						byte b4 = buf[byteOffset+3];
						int unsignedValue = 
								(int)(
								    (((int)b4 & 0xff) << 24)
								|	(((int)b3 & 0xff) << 16)
								|	(((int)b2 & 0xff) << 8)
								|	((int)b1 & 0xff)
								);
						buffer[ichan][isamp + offset] =  unsignedValue * (1.0f / 32768.0f / 65536.0f);
					}
					++samplesRead;
				}
				break;
				
			default:
				break;
			}
			
		} break; // 16, 24, 32-bit
		
		case IEEE_FLOAT_32_BIG_ENDIAN:
			for(int isamp = 0; isamp < numSamples; isamp++) {
				for(int ichan = 0; ichan < numChannels; ichan++) {
					buffer[ichan][isamp] = mappedBuffer.getFloat();
				}
				++samplesRead;
			}
			break;
		
		case IEEE_FLOAT_32_LITTLE_ENDIAN:
			for(int isamp = 0; isamp < numSamples; isamp++) {
				byte[] buf = new byte[4];
				for(int ichan = 0; ichan < numChannels; ichan++) {
					mappedBuffer.get(buf);
					buffer[ichan][isamp] = readFloatLE(buf, 0);
				}
				++samplesRead;
			}
			break;
			
		case IEEE_FLOAT_64_BIG_ENDIAN:
			for(int isamp = 0; isamp < numSamples; isamp++) {
				for(int ichan = 0; ichan < numChannels; ichan++) {
					buffer[ichan][isamp] = mappedBuffer.getDouble();
				}
				++ samplesRead;
			}
			break;
			
		case IEEE_FLOAT_64_LITTLE_ENDIAN:
			for(int isamp = 0; isamp < numSamples; isamp++) {
				byte[] buf = new byte[8];
				for(int ichan = 0; ichan < numChannels; ichan++) {
					mappedBuffer.get(buf);
					buffer[ichan][isamp] = readDoubleLE(buf, 0);
				}
				++samplesRead;
			}
			break;
			
		case MULAW:
			for(int isamp = 0; isamp < numSamples; isamp++) {
				for(int ichan = 0; ichan < numChannels; ichan++) {
					int idx = Byte.toUnsignedInt(mappedBuffer.get());
					buffer[ichan][isamp] = ulaw2linear[idx] * (1.0 / 32768.0);
				}
				++samplesRead;
			}
			break;
			
		case ALAW:
			for(int isamp = 0; isamp < numSamples; isamp++) {
				for(int ichan = 0; ichan < numChannels; ichan++) {
					int idx = Byte.toUnsignedInt(mappedBuffer.get());
					buffer[ichan][isamp] = alaw2linear[idx] * (1.0 / 32768.0);
				}
				++samplesRead;
			}
			break;
			
		default:
			throw new IOException(new UnsupportedFormatException());
		} // switch
		
		return samplesRead;
	}
	
	public int getFrameSize() {
		return (getNumberOfChannels() * getAudioFileEncoding().getBytesPerSample());
	}
		
	/* Read functions */
	private short readShortLE(RandomAccessFile raf) throws IOException {
		byte[] buffer = new byte[2];
		if(raf.read(buffer) < 2) throw new BufferUnderflowException();
		return readShortLE(buffer, 0);
	}
	
	private short readShortLE(byte[] data, int offset) throws BufferUnderflowException {
		if(data.length < offset + 2) throw new BufferUnderflowException();
		
		return (short)(
				(((long)data[offset + 1] & 0xff) << 8)
				| ((long)data[offset] & 0xff));
	}
	
	private int readIntLE(RandomAccessFile raf) throws IOException {
		byte[] buffer = new byte[4];
		if(raf.read(buffer) < 4) throw new BufferUnderflowException();
		return readIntLE(buffer, 0);
	}
	
	private int readIntLE(byte[] data, int offset) throws BufferUnderflowException {
		if(data.length < offset + 4) throw new BufferUnderflowException();
		
		return	(int)(
				(((long)data[3] & 0xff) << 24)
				| (((long)data[2] & 0xff) << 16) 
				| (((long)data[1] & 0xff) << 8)
				| ((long)data[0] & 0xff));
	}
		
	private float readFloatLE(RandomAccessFile raf) throws IOException {
		byte[] buffer = new byte[4];
		if(raf.read(buffer) < 4) throw new BufferUnderflowException();
		return readFloatLE(buffer, 0);
	}
		
	private float readFloatLE(byte[] data, int offset) throws BufferUnderflowException {
		int exp = (int)(
				(((long)data[offset+3] & 0x0000007F) << 1)
				| (((long)data[offset+2] & 0x00000080) >> 7));
		int mantissa = (int)(
				(((long)data[offset+2] & 0x0000007F) << 16)
				| (((long)data[offset+1] & 0xFF) << 8)
				| (((long)data[offset] & 0xFF)));
		float x = 0.0f;
		if(exp == 0) {
			if(mantissa == 0) 
				x = 0.0f;
			else
				x = Math.scalb(mantissa, exp - 149);
		} else if(exp == 0x000000FF) {
			return Float.NaN;
		} else {
			x = Math.scalb(mantissa, exp - 149);
		}
		return (data[offset + 3] & 0x80) != 0 ? - x : x;		
	}
	
	private double readDoubleLE(RandomAccessFile raf) throws IOException {
		byte[] buffer = new byte[8];
		if(raf.read(buffer) < 8) throw new BufferUnderflowException();
		return readDoubleLE(buffer, 0);
	}
	
	private double readDoubleLE(byte[] data, int offset) throws BufferUnderflowException {
		if(data.length < offset + 8) throw new BufferUnderflowException();
		
		int exp = (int)(
				(((long)data[offset+7] & 0x0000007F) << 4)
				| (((long)data[offset+6] & 0x000000F0) >> 4));
		long highMantissa = 
				(((long)data[offset+6] & 0x0F) << 16)
				| (((long)data[offset+5] & 0xFF) << 8)
				| (((long)data[offset+4] & 0xFF));
		long lowMantissa = 
				(((long)data[offset+3] & 0xFF) << 24)
				| (((long)data[offset+2] & 0xFF) << 16)
				| (((long)data[offset+1] & 0xFF) << 8)
				| (((long)data[offset] & 0xFF));
		
		double x = Double.NaN;
		if(exp == 0) {
			if(highMantissa == 0 && lowMantissa == 0) x = 0.0;
			else {
				x = Math.scalb(highMantissa, exp - 1042);
				x += Math.scalb(lowMantissa, exp - 1074);
			}
		} else if(exp == 0x000007FF) {
			x = Double.NaN;
		} else {
			x = Math.scalb(highMantissa | 0x00100000, exp - 1043);
			x += Math.scalb(lowMantissa, exp - 1075);
		}
		return (data[offset+7] & 0x80) != 0 ? - x : x;
	}
	
	private double readLongDouble(RandomAccessFile raf) throws IOException {
		byte[] buffer = new byte[10];
		if(raf.read(buffer) < 10) throw new BufferUnderflowException();
		return readLongDouble(buffer, 0);
	}
	
	private double readLongDouble(byte[] data, int offset) throws BufferUnderflowException {
		if(data.length < offset + 10) throw new BufferUnderflowException();
		
		int exp = (((int)data[offset] & 0x7F) << 8) | data[offset + 1];
		// using longs to avoid issues with signing
		long highMantissa = (long)(
			((long)data[offset + 2] & 0xFF) << 24 |
			((long)data[offset + 3] & 0xFF) << 16 | 
			((long)data[offset + 4] & 0xFF) << 8 |
			((long)data[offset + 5] & 0xFF));
		highMantissa = (highMantissa & 0xFFFFFFFF);
		long lowMantissa = (long)(
			((long)data[offset + 6] & 0xFF) << 24 |
			((long)data[offset + 7] & 0xFF) << 16 | 
			((long)data[offset + 8] & 0xFF) << 8 |
			((long)data[offset + 9] & 0xFF));
		
		double x = Double.NaN;
		if(exp == 0 && highMantissa == 0 && lowMantissa == 0) x = 0.0;
		else if(exp == 0x00007FFF) return Double.NaN;
		else {
			exp -= 16383;
			x = Math.scalb(highMantissa, exp - 31);
			x += Math.scalb(lowMantissa, exp - 63);
		}
		return (data[offset + 0] & 0x80) != 0 ? - x : x;
	}

	public static void main(String[] args) {
		byte[] buf = { -127, 0x00, 0x01, -1 };
		
		long unsignedValue = 0L;
		unsignedValue |=
				(long)(Byte.toUnsignedInt(buf[0])) << 24;
		unsignedValue |=
				 Byte.toUnsignedInt(buf[1]) << 16
				| Byte.toUnsignedInt(buf[2]) << 8
				| Byte.toUnsignedInt(buf[3]);
		System.out.println(Long.toString(unsignedValue));
		System.out.println((((long)buf[0]) & 0xff)
				+ "");
		
		System.out.println(Integer.toString((int)unsignedValue));
	}
}
