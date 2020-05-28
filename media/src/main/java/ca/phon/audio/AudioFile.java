package ca.phon.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.logging.Logger;

public final class AudioFile {

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

	private File file;
	
	private RandomAccessFile raf;

	private long dataOffset = -1;
	
	private int numberOfChannels = -1;
	
	private double sampleRate = 0.0;
	
	private int numberOfSamples = 0;

	private AudioFileType audioFileType;

	private AudioFileEncoding audioFileEncoding;

	/**
	 * Constructor
	 */
	AudioFile(File file) throws IOException, UnsupportedFormatException {
		super();

		this.file = file;
		raf = new RandomAccessFile(file, "r");
		audioFileType = checkFile();
	}

	private AudioFileType checkFile() throws IOException, UnsupportedFormatException {
		byte[] data = new byte[16];
		if(raf.read(data) < 16) throw new UnsupportedFormatException("File too short");
		raf.seek(0L);
		
		String ft = new String(data, 0, 4);
		if(ft.equals("FORM")) {
			checkAiffFile();
			return AudioFileType.AIFF;
		}
		
		throw new UnsupportedFormatException("Unsupported file type");
	}
	
	private void checkAiffFile() throws IOException, UnsupportedFormatException {
		byte[] data  = new byte[4];
		byte[] chunkID = new byte[4];
		
		boolean commonChunkPresent = false;
		boolean dataChunkPresent = false;
		boolean isAifc = true;
		int numberOfBitsPerSamplePoint;
		
		/* Read header of AIFF(-C) file: 12 bytes. */

		int bytesRead = raf.read(data, 0, 4);
		if(bytesRead != 4) throw new UnsupportedFormatException("File too small: no FORM statement.");
		if(!(new String(data, 0, 4)).contentEquals("FORM")) throw new UnsupportedFormatException("Not an AIFF or AIFC file (FORM statement expected.)");
				
		bytesRead = raf.read(data, 0, 4);
		if(bytesRead != 4) throw new UnsupportedFormatException("File too small: no size of FORM chunk.");
		
		bytesRead = raf.read(data, 0, 4);
		if(bytesRead != 4) throw new UnsupportedFormatException("File too small: no file type info (expected AIFF or AIFC).");
		
		String type = new String(data, 0, 4);
		if(!type.equalsIgnoreCase("AIFF") && !type.equalsIgnoreCase("AIFC"))
			throw new UnsupportedFormatException("Not an AIFF or AIFC file (wrong file type info).");
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
				if(numberOfChannels < 1) throw new UnsupportedFormatException("Too few sound channels (" + numberOfChannels + ")");
				
				numberOfSamples = raf.readInt();
				if(numberOfSamples <= 0) throw new UnsupportedFormatException("Too few samples (" + numberOfSamples + ")");
				
				numberOfBitsPerSamplePoint = raf.readShort();
				if(numberOfBitsPerSamplePoint > 32) throw new UnsupportedFormatException("Too many bits per sample (" + numberOfBitsPerSamplePoint + ")");
				
				audioFileEncoding =
					numberOfBitsPerSamplePoint > 24 ? AudioFileEncoding.LINEAR_32_BIG_ENDIAN :
					numberOfBitsPerSamplePoint > 16 ? AudioFileEncoding.LINEAR_24_BIG_ENDIAN :
					numberOfBitsPerSamplePoint > 8 ? AudioFileEncoding.LINEAR_16_BIG_ENDIAN :
					AudioFileEncoding.LINEAR_8_SIGNED;
					
				sampleRate = readLongDouble(raf);
				if(sampleRate <= 0.0) throw new UnsupportedFormatException("Wrong sample rate");
				if(isAifc) {
					/*
					 * Read compression info
					 */
					bytesRead = raf.read(data, 0, 4);
					if(bytesRead < 4) throw new UnsupportedFormatException("File too small: no compression info.");
					
					String ctype = new String(data, 0, 4);
					if (! ctype.equals("NONE") && ! ctype.equals("sowt") ) {
						throw new UnsupportedFormatException("Cannot read compressed AIFC files (compression type " + data + ").");
					}
					if (ctype.equals("sowt"))
						audioFileEncoding =
							numberOfBitsPerSamplePoint > 24 ? AudioFileEncoding.LINEAR_32_LITTLE_ENDIAN :
							numberOfBitsPerSamplePoint > 16 ? AudioFileEncoding.LINEAR_24_LITTLE_ENDIAN :
							numberOfBitsPerSamplePoint > 8 ? AudioFileEncoding.LINEAR_16_LITTLE_ENDIAN :
							AudioFileEncoding.LINEAR_8_SIGNED;
					/*
					 * Read rest of compression info.
					 */
					for (int i = 23; i <= chunkSize; i ++)
						if (raf.read(data, 0, 1) < 1)
							throw new UnsupportedFormatException("File too small: expected chunk of " + chunkSize + " bytes, but found " + (i + 22) + ".");
				}
			} else if(chunk.equals("SSND")) {
				dataChunkPresent = true;
				dataOffset = raf.getFilePointer() + 8;
				if(commonChunkPresent) break;
			} else {
				for(int i = 1; i <= chunkSize; i++) {
					if(raf.read(data, 0, 1) < 1) 
						throw new UnsupportedFormatException("File too small: expected chunk of " + chunkSize + " bytes, but found " + i + ".");
				}
			}
		}
		
		if(!commonChunkPresent) throw new UnsupportedFormatException("Found no common chunk.");
		if(!dataChunkPresent) throw new UnsupportedFormatException("Found no data chunk.");
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

	public int getNumberOfSamples() {
		return numberOfSamples;
	}

	public AudioFileType getAudioFileType() {
		return audioFileType;
	}

	public AudioFileEncoding getAudioFileEncoding() {
		return audioFileEncoding;
	}

	/* Read functions */
	private double readLongDouble(RandomAccessFile raf) throws IOException {
		byte[] bytes = new byte[10];
		if(raf.read(bytes) != 10) throw new IOException("Could not read 10 bytes");
		
		int exp = (((int)bytes[0] & 0x7F) << 8) | bytes[1];
		// using longs to avoid issues with signing
		long highMantissa = 
			((long)bytes[2] & 0xFF) << 24 |
			((long)bytes[3] & 0xFF) << 16 | 
			((long)bytes[4] & 0xFF) << 8 |
			((long)bytes[5] & 0xFF);
		highMantissa = (highMantissa & 0xFFFFFFFF);
		long lowMantissa = 
			((long)bytes[6] & 0xFF) << 24 |
			((long)bytes[7] & 0xFF) << 16 | 
			((long)bytes[8] & 0xFF) << 8 |
			((long)bytes[9] & 0xFF);
		
		double x = Double.NaN;
		if(exp == 0 && highMantissa == 0 && lowMantissa == 0) x = 0.0;
		else if(exp == 0x00007FFF) return Double.NaN;
		else {
			exp -= 16383;
			x = Math.scalb(highMantissa, exp - 31);
			x += Math.scalb(lowMantissa, exp - 63);
		}
		return (bytes[0] & 0x80) == 0x80 ? - x : x;
	}
}
