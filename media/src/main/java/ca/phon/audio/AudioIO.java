package ca.phon.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

/**
 * Methods for reading and writing audio sample data.
 * 
 * 
 */
public class AudioIO {
	
	private final static int WAVE_FORMAT_PCM = 0x0001;
	private final static int WAVE_FORMAT_IEEE_FLOAT = 0x0003;
	private final static int WAVE_FORMAT_ALAW = 0x0006;
	private final static int WAVE_FORMAT_MULAW = 0x0007;
	private final static int WAVE_FORMAT_DVI_ADPCM = 0x0011;
	private final static int WAVE_FORMAT_EXTENSIBLE = 0xFFFE;
	
	private final static byte[] WAVE_SUBFORMAT_DATA = { 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, (byte)(0x80), 0x00, 0x00, (byte)0xAA, 0x00, 0x38, (byte)0x9b, 0x71 };
	
	private static int ulaw2linear[] = { -32124, -31100, -30076, -29052, -28028, -27004, -25980, -24956, -23932, -22908, -21884,
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

	private static short alaw2linear[] = { -5504, -5248, -6016, -5760, -4480, -4224, -4992, -4736, -7552, -7296, -8064, -7808,
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

	public static AudioFile openAudioFile(File file) throws IOException, UnsupportedFormatException, InvalidHeaderException {
		AudioFileInfo audioFileInfo = checkHeaders(file);
		return new AudioFile(file, audioFileInfo);
	}
	
	/**
	 * Check headers of given file and return an filled AudioFileInfo object.
	 * 
	 * @param file
	 * @return audio file information
	 * 
	 * @throws IOException
	 * @throws InvalidHeaderException
	 * @throws UnsupportedFormatException
	 */
	public static AudioFileInfo checkHeaders(File file) throws IOException, InvalidHeaderException, UnsupportedFormatException {
		AudioFileInfo info = new AudioFileInfo();
		
		InputStream is = new FileInputStream(file);
		byte[] data = new byte[16];
		if(is.read(data) < 16) {
			is.close();
			throw new UnsupportedFormatException("File too short");
		}
		is.close();
		
		is = new FileInputStream(file);
		String ft = new String(data, 0, 4);
		String ft2 = new String(data, 8, 4);
		if(ft.equals("FORM") && ft2.equals("AIFF")) {
			info.setFileType(AudioFileType.AIFF);
			checkAiffHeaders(is, info);
		} else if(ft.equals("FORM") && ft2.equals("AIFC")) {
			info.setFileType(AudioFileType.AIFC);
			checkAiffHeaders(is, info);
		} else if((ft.equals("RIFF") || ft.equals("RIFX")) && (ft2.equals("WAVE") || ft2.equals("CDDA"))) {
			info.setFileType(AudioFileType.WAV);
			
			if(ft.equals("RIFF"))
				checkRiffHeaders(is, info);
			else
				checkRifxHeaders(is, info);
			
			// fix data chunk size if necessary
			if(info.getDataChunkSize() <= 0) { // incorrect data chunk (sometimes -44); assume that the data run till the end of the file
				long fileLength = file.length();
				long chunkSize = (fileLength - info.getDataOffset());
				info.setDataChunkSize(chunkSize);
			}
			
			long numberOfSamples = info.getDataChunkSize() / info.getNumberOfChannels() / ((info.getEncoding().getBitsPerSample() + 7) / 8);
			info.setNumberOfSamples(numberOfSamples);
		} else {
			is.close();
			throw new UnsupportedFormatException("Unsupported file type");
		}
		
		is.close();
		
		return info;
	}

	private static void checkRiffHeaders(InputStream is, AudioFileInfo /*out*/ info) throws IOException, InvalidHeaderException, UnsupportedFormatException {
		byte[] data = new byte[14];
		byte[] chunkId = new byte[4];
		
		AudioFileEncoding audioFileEncoding = AudioFileEncoding.EXTENDED;
		boolean formatChunkPresent = false;
		boolean dataChunkPresent = false;
		
		int totalBytesRead = 0;
		int numberOfBitsPerSamplePoint = -1;
		
		int bytesRead = is.read(data, 0, 4);
		totalBytesRead += bytesRead;
		if(bytesRead < 4) throw new InvalidHeaderException("File too small: no RIFF statement");
		String riff = new String(data, 0, 4);
		if(!riff.equals("RIFF")) throw new InvalidHeaderException("Not a WAV file (RIFF statement expected)");
		
		bytesRead = is.read(data, 0, 4);
		totalBytesRead += bytesRead;
		if(bytesRead < 4) throw new InvalidHeaderException("File too small: no size of RIFF chunk.");
		bytesRead = is.read(data, 0, 4);
		totalBytesRead += bytesRead;
		if(bytesRead < 4) throw new InvalidHeaderException("File too small: no file type info (expected WAVE statement).");
		
		String fileTypeInfo = new String(data, 0, 4);
		if(!fileTypeInfo.equals("WAVE") && !fileTypeInfo.equals("CDDA")) {
			throw new InvalidHeaderException("Not a WAVE or CD audio file (wrong file type info).");
		}
		
		/* Search for format and data chunks */
		while(is.read(chunkId) == 4) {
			totalBytesRead += 4;
			int chunkSize = AudioIO.readIntLE(is);
			totalBytesRead += 4;
			String chunk = new String(chunkId);
			if(chunk.equals("fmt ")) {
				short winEncoding = readShortLE(is);
				totalBytesRead += 2;
				formatChunkPresent = true;
				int numberOfChannels = readShortLE(is);		
				totalBytesRead += 2;
				if(numberOfChannels < 0) throw new InvalidHeaderException("Too few sound channels (" + numberOfChannels + ")");
				info.setNumberOfChannels(numberOfChannels);
				double sampleRate = (double) readIntLE(is);
				totalBytesRead += 4;
				if(sampleRate < 0.0) throw new InvalidHeaderException("Wrong sampling freq (" + sampleRate + ")");
				info.setSampleRate((float)sampleRate);
				// read unused data
				readIntLE(is); // avgBytesPerSec
				totalBytesRead += 4;
				readShortLE(is); // blockAlign
				totalBytesRead += 2;
				numberOfBitsPerSamplePoint = readShortLE(is);
				totalBytesRead += 2;
				
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
					readShortLE(is); // extensionSize
					totalBytesRead += 2;
					readShortLE(is); // validBitsPreSample
					totalBytesRead += 2;
					readIntLE(is); // channelMask
					totalBytesRead += 4;
					short winEncoding2 = readShortLE(is);
					totalBytesRead += 2;
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
					
					bytesRead = is.read(data, 0, 14);
					totalBytesRead += bytesRead;
					if(bytesRead < 14) throw new InvalidHeaderException("File too small: no SubFormat data");
					continue;
				}
					
				default:
					throw new InvalidHeaderException("Unsupported windows audio encoding " + Integer.toString(winEncoding, 8));
				}
				
				if(chunkSize % 2 == 1) ++chunkSize;
				for(int i = 17; i <= chunkSize; i++) {
					bytesRead = is.read(data, 0, 1);
					totalBytesRead += bytesRead;
					if(bytesRead < 1) throw new InvalidHeaderException("File too small: expected " + chunkSize + " bytes in fmt chunk, but found " + i);
				}
			} else if(chunk.equals("data")) {
				dataChunkPresent = true;
				int dataOffset = totalBytesRead;
				info.setDataOffset(dataOffset);

				if(chunkSize % 2 == 1) ++chunkSize;
				info.setDataChunkSize(chunkSize);
				
				if(formatChunkPresent) break;
			} else {
				if(chunkSize % 2 == 1) ++chunkSize;
				for(int i = 1; i <= chunkSize; i++) {
					bytesRead = is.read(data, 0, 1);
					totalBytesRead += bytesRead;
					if(bytesRead < 1) 
						throw new InvalidHeaderException("File too small: expected " + chunkSize + " bytes, but found " + i);
				}
			}
		} // end while
		
		info.setEncoding(audioFileEncoding);
		if(!formatChunkPresent) throw new InvalidHeaderException("Found no format chunk");
		if(!dataChunkPresent) throw new InvalidHeaderException("Found no data chunk");
	}
	
	private static void checkRifxHeaders(InputStream is, AudioFileInfo /*out*/ info) throws IOException, InvalidHeaderException, UnsupportedFormatException {
		byte[] data = new byte[14];
		byte[] chunkId = new byte[4];
		
		AudioFileEncoding audioFileEncoding = AudioFileEncoding.EXTENDED;
		boolean formatChunkPresent = false;
		boolean dataChunkPresent = false;
		
		int totalBytesRead = 0;
		int numberOfBitsPerSamplePoint = -1;
		
		int bytesRead = is.read(data, 0, 4);
		totalBytesRead += bytesRead;
		if(bytesRead < 4) throw new InvalidHeaderException("File too small: no RIFX statement");
		String riff = new String(data, 0, 4);
		if(!riff.equals("RIFX")) throw new InvalidHeaderException("Not a WAV file (RIFX statement expected)");
		
		bytesRead = is.read(data, 0, 4);
		totalBytesRead += bytesRead;
		if(bytesRead < 4) throw new InvalidHeaderException("File too small: no size of RIFX chunk.");
		bytesRead = is.read(data, 0, 4);
		totalBytesRead += bytesRead;
		if(bytesRead < 4) throw new InvalidHeaderException("File too small: no file type info (expected WAVE statement).");
		
		String fileTypeInfo = new String(data, 0, 4);
		if(!fileTypeInfo.equals("WAVE") && !fileTypeInfo.equals("CDDA")) {
			throw new InvalidHeaderException("Not a WAVE or CD audio file (wrong file type info).");
		}
		
		/* Search for format and data chunks */
		while(is.read(chunkId) == 4) {
			totalBytesRead += 4;
			int chunkSize = AudioIO.readInt(is);
			totalBytesRead += 4;
			String chunk = new String(chunkId);
			if(chunk.equals("fmt ")) {
				short winEncoding = readShort(is);
				totalBytesRead += 2;
				formatChunkPresent = true;
				int numberOfChannels = readShort(is);		
				totalBytesRead += 2;
				if(numberOfChannels < 0) throw new InvalidHeaderException("Too few sound channels (" + numberOfChannels + ")");
				info.setNumberOfChannels(numberOfChannels);
				double sampleRate = (double) readInt(is);
				totalBytesRead += 4;
				if(sampleRate < 0.0) throw new InvalidHeaderException("Wrong sampling freq (" + sampleRate + ")");
				info.setSampleRate((float)sampleRate);
				// read unused data
				readInt(is); // avgBytesPerSec
				totalBytesRead += 4;
				readShort(is); // blockAlign
				totalBytesRead += 2;
				numberOfBitsPerSamplePoint = readShort(is);
				totalBytesRead += 2;
				
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
						numberOfBitsPerSamplePoint > 24 ? AudioFileEncoding.LINEAR_32_BIG_ENDIAN :
						numberOfBitsPerSamplePoint > 16 ? AudioFileEncoding.LINEAR_24_BIG_ENDIAN :
						numberOfBitsPerSamplePoint > 8 ? AudioFileEncoding.LINEAR_16_BIG_ENDIAN :
						AudioFileEncoding.LINEAR_8_UNSIGNED;
					break;
					
				case WAVE_FORMAT_IEEE_FLOAT:
					audioFileEncoding = 
						numberOfBitsPerSamplePoint == 64 ? AudioFileEncoding.IEEE_FLOAT_64_BIG_ENDIAN :
							AudioFileEncoding.IEEE_FLOAT_32_BIG_ENDIAN;
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
					readShort(is); // extensionSize
					totalBytesRead += 2;
					readShort(is); // validBitsPreSample
					totalBytesRead += 2;
					readInt(is); // channelMask
					totalBytesRead += 4;
					short winEncoding2 = readShort(is);
					totalBytesRead += 2;
					switch(Short.toUnsignedInt(winEncoding2)) {
					case WAVE_FORMAT_PCM:
						audioFileEncoding =
							numberOfBitsPerSamplePoint > 24 ? AudioFileEncoding.LINEAR_32_BIG_ENDIAN :
							numberOfBitsPerSamplePoint > 16 ? AudioFileEncoding.LINEAR_24_BIG_ENDIAN :
							numberOfBitsPerSamplePoint > 8 ? AudioFileEncoding.LINEAR_16_BIG_ENDIAN :
							AudioFileEncoding.LINEAR_8_UNSIGNED;
						break;
						
					case WAVE_FORMAT_IEEE_FLOAT:
						audioFileEncoding = 
							numberOfBitsPerSamplePoint == 64 ? AudioFileEncoding.IEEE_FLOAT_64_BIG_ENDIAN :
								AudioFileEncoding.IEEE_FLOAT_32_BIG_ENDIAN;
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
					
					bytesRead = is.read(data, 0, 14);
					totalBytesRead += bytesRead;
					if(bytesRead < 14) throw new InvalidHeaderException("File too small: no SubFormat data");
					continue;
				}
					
				default:
					throw new InvalidHeaderException("Unsupported windows audio encoding " + Integer.toString(winEncoding, 8));
				}
				
				if(chunkSize % 2 == 1) ++chunkSize;
				for(int i = 17; i <= chunkSize; i++) {
					bytesRead = is.read(data, 0, 1);
					totalBytesRead += bytesRead;
					if(bytesRead < 1) throw new InvalidHeaderException("File too small: expected " + chunkSize + " bytes in fmt chunk, but found " + i);
				}
			} else if(chunk.equals("data")) {
				dataChunkPresent = true;
				int dataOffset = totalBytesRead;
				info.setDataOffset(dataOffset);

				if(chunkSize % 2 == 1) ++chunkSize;
				info.setDataChunkSize(chunkSize);
				
				if(formatChunkPresent) break;
			} else {
				if(chunkSize % 2 == 1) ++chunkSize;
				for(int i = 1; i <= chunkSize; i++) {
					bytesRead = is.read(data, 0, 1);
					totalBytesRead += bytesRead;
					if(bytesRead < 1) 
						throw new InvalidHeaderException("File too small: expected " + chunkSize + " bytes, but found " + i);
				}
			}
		} // end while
		
		info.setEncoding(audioFileEncoding);
		if(!formatChunkPresent) throw new InvalidHeaderException("Found no format chunk");
		if(!dataChunkPresent) throw new InvalidHeaderException("Found no data chunk");
	}
	
	private static void checkAiffHeaders(InputStream raf, AudioFileInfo info) throws IOException, InvalidHeaderException, UnsupportedFormatException {
		byte[] data  = new byte[4];
		byte[] chunkID = new byte[4];
		
		boolean commonChunkPresent = false;
		boolean dataChunkPresent = false;
		boolean isAifc = true;
		
		int totalBytesRead = 0;
		
		/* Read header of AIFF(-C) file: 12 bytes. */

		int bytesRead = raf.read(data, 0, 4);
		totalBytesRead += bytesRead;
		if(bytesRead != 4) throw new InvalidHeaderException("File too small: no FORM statement.");
		if(!(new String(data, 0, 4)).contentEquals("FORM")) throw new InvalidHeaderException("Not an AIFF or AIFC file (FORM statement expected.)");
				
		bytesRead = raf.read(data, 0, 4);
		totalBytesRead += bytesRead;
		if(bytesRead != 4) throw new InvalidHeaderException("File too small: no size of FORM chunk.");
		
		bytesRead = raf.read(data, 0, 4);
		totalBytesRead += bytesRead;
		if(bytesRead != 4) throw new InvalidHeaderException("File too small: no file type info (expected AIFF or AIFC).");
		
		String type = new String(data, 0, 4);
		if(!type.equalsIgnoreCase("AIFF") && !type.equalsIgnoreCase("AIFC"))
			throw new InvalidHeaderException("Not an AIFF or AIFC file (wrong file type info).");
		if (type.equalsIgnoreCase("AIFF")) isAifc = false;
		
		/* Search for Common Chunk and Data Chunk. */
		
		while(raf.read(chunkID) == 4) {
			totalBytesRead += 4;
			int chunkSize = readInt(raf);
			totalBytesRead += 4;
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
				for(int i=1;i<=20/*diff*/-8/*header*/;i++) {
					raf.read(data, 0, 1);
					totalBytesRead++;
				}
				continue;
			}
			/* FINISH FIX OF FOREIGN BUG */
			
			if(chunk.equals("COMM")) {
				commonChunkPresent = true;
				short numberOfChannels = readShort(raf);
				totalBytesRead += 2;
				info.setNumberOfChannels(numberOfChannels);
				if(numberOfChannels < 1) throw new InvalidHeaderException("Too few sound channels (" + numberOfChannels + ")");
				
				int numberOfSamples = readInt(raf);
				totalBytesRead += 4;
				info.setNumberOfSamples(numberOfSamples);
				if(numberOfSamples <= 0) throw new InvalidHeaderException("Too few samples (" + numberOfSamples + ")");
				
				short numberOfBitsPerSamplePoint = readShort(raf);
				totalBytesRead += 2;
				if(numberOfBitsPerSamplePoint > 64) throw new InvalidHeaderException("Too many bits per sample (" + numberOfBitsPerSamplePoint + ")");
				
				AudioFileEncoding audioFileEncoding =
					numberOfBitsPerSamplePoint > 24 ? AudioFileEncoding.LINEAR_32_BIG_ENDIAN :
					numberOfBitsPerSamplePoint > 16 ? AudioFileEncoding.LINEAR_24_BIG_ENDIAN :
					numberOfBitsPerSamplePoint > 8 ? AudioFileEncoding.LINEAR_16_BIG_ENDIAN :
					AudioFileEncoding.LINEAR_8_SIGNED;
				info.setEncoding(audioFileEncoding);
				
				double sampleRate = readLongDouble(raf);
				totalBytesRead += 10;
				info.setSampleRate((float)sampleRate);
				if(sampleRate <= 0.0) throw new InvalidHeaderException("Wrong sample rate");
				if(isAifc) {
					/*
					 * Read compression info
					 */
					bytesRead = raf.read(data, 0, 4);
					totalBytesRead += bytesRead;
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
					
					info.setEncoding(audioFileEncoding);
					
					/*
					 * Read rest of compression info.
					 */
					for (int i = 23; i <= chunkSize; i ++) {
						if (raf.read(data, 0, 1) < 1)
							throw new InvalidHeaderException("File too small: expected chunk of " + chunkSize + " bytes, but found " + (i + 22) + ".");
						totalBytesRead++;
					}
				}
			} else if(chunk.equals("SSND")) {
				dataChunkPresent = true;
				long dataOffset = totalBytesRead + 8;
				info.setDataOffset(dataOffset);
				if(commonChunkPresent) break;
			} else {
				for(int i = 1; i <= chunkSize; i++) {
					if(raf.read(data, 0, 1) < 1) 
						throw new InvalidHeaderException("File too small: expected chunk of " + chunkSize + " bytes, but found " + i + ".");
					++totalBytesRead;
				}
			}
		}
		
		if(!commonChunkPresent) throw new InvalidHeaderException("Found no common chunk.");
		if(!dataChunkPresent) throw new InvalidHeaderException("Found no data chunk.");
	}
	
	private static AudioFileType typeFromFilename(File file) throws UnsupportedFormatException {
		String ext = FilenameUtils.getExtension(file.getName());
		if(ext == null || ext.length() == 0) throw new UnsupportedFormatException("Unable to determine file type - no extension given");
		
		for(AudioFileType type:AudioFileType.values()) {
			int idx = Arrays.binarySearch(type.getExtensions(), ext);
			if(idx >= 0)
				return type;
		}
		throw new UnsupportedFormatException("Unable to determine file type from extension: " + ext);
	}
	public static void writeSamplesToFile(Sampled samples, int firstSample, int numSamples, AudioFileEncoding encoding, File file) throws IOException, AudioIOException {
		writeSamplesToFile(samples, firstSample, numSamples, typeFromFilename(file), encoding, file);
	}
	
	public static void writeSamplesToFile(Sampled samples, int firstSample, int numSamples, AudioFileType fileType, AudioFileEncoding encoding, File file) throws IOException, AudioIOException {
		FileOutputStream fout = new FileOutputStream(file);
		writeHeaders(samples, numSamples, fileType, encoding, fout);
		writeSamples(samples, firstSample, numSamples, encoding, fout);
		
		fout.flush();
		fout.close();
	}
	
	private static void writeHeaders(Sampled samples, int numSamples, AudioFileType fileType, AudioFileEncoding encoding, OutputStream os) throws IOException, UnsupportedFormatException {
		switch(fileType) {
		case WAV:
			if(encoding.getBytesPerSample() > 1 && encoding.isBigEndian())
				writeRifxHeaders(samples, numSamples, encoding, os);
			else
				writeRiffHeaders(samples, numSamples, encoding, os);
			break;
			
		case AIFF:
		case AIFC:
			if(encoding.getBytesPerSample() > 1 && encoding.isBigEndian()) {
				writeAiffHeaders(samples, numSamples, encoding, os);
			} else {
				writeAifcHeaders(samples, numSamples, encoding, os);
			}
			break;
			
		default:
			throw new UnsupportedFormatException();
		}
	}
	
	private static void writeRiffHeaders(Sampled samples, int numSamples, AudioFileEncoding encoding, OutputStream os) throws IOException, UnsupportedFormatException {
		if(encoding.getBytesPerSample() > 1 && encoding.isBigEndian()) throw new UnsupportedFormatException("RIFF requires little endian encoding");
		
		boolean needsExtensibleFormat = 
				encoding.getBitsPerSample() > 16 ||
				samples.getNumberOfChannels() > 2 ||
				encoding.getBitsPerSample() != encoding.getBytesPerSample() * 8;
		int formatSize = needsExtensibleFormat ? 40 : 16;
		long dataSize = numSamples * samples.getNumberOfChannels() * encoding.getBytesPerSample();
		
		byte[] bytes = "RIFF".getBytes();
		os.write(bytes);
		long sizeOfRiffChunk = 4 + (12 + formatSize) + (4 + dataSize);
		writeIntLE((int)sizeOfRiffChunk, os);
		
		bytes = "WAVE".getBytes();
		os.write(bytes);
		bytes = "fmt ".getBytes();
		os.write(bytes);
		writeIntLE(formatSize, os);
		writeShortLE(needsExtensibleFormat ? (short)WAVE_FORMAT_EXTENSIBLE : (short)WAVE_FORMAT_PCM , os);
		writeShortLE((short)samples.getNumberOfChannels(), os);
		writeIntLE((int)samples.getSampleRate(), os);
		writeIntLE((int)(samples.getSampleRate() * encoding.getBytesPerSample() * samples.getNumberOfChannels()), os);  // avg bytes per sec
		writeShortLE((short)(encoding.getBytesPerSample() * samples.getNumberOfChannels()), os); // block alignment
		writeShortLE((short)(encoding.getBytesPerSample() * 8), os); // padded bits per sample
		if(needsExtensibleFormat) {
			writeShortLE((short)(22), os); // ext size
			writeShortLE((short)encoding.getBitsPerSample(), os);
			writeIntLE(0, os); // speaker position mark
			switch(encoding) {
			case LINEAR_12_LITTLE_ENDIAN:
			case LINEAR_16_LITTLE_ENDIAN:
			case LINEAR_24_LITTLE_ENDIAN:
			case LINEAR_32_LITTLE_ENDIAN:
				writeShortLE((short)WAVE_FORMAT_PCM, os);
				break;
				
			case IEEE_FLOAT_32_LITTLE_ENDIAN:
			case IEEE_FLOAT_64_LITTLE_ENDIAN:
				writeShortLE((short)WAVE_FORMAT_IEEE_FLOAT, os);
				break;
				
			default:
				throw new UnsupportedFormatException("Cannot write RIFF files with encoding " + encoding);
			}
			os.write(WAVE_SUBFORMAT_DATA);
		}
		os.write("data".getBytes());
		writeIntLE((int)dataSize, os);
	}
	
	private static void writeRifxHeaders(Sampled samples, int numSamples, AudioFileEncoding encoding, OutputStream os) throws IOException, UnsupportedFormatException {
		if(encoding.getBytesPerSample() > 1 && !encoding.isBigEndian()) throw new UnsupportedFormatException("RIFX requires big endian encoding");
		
		boolean needsExtensibleFormat = 
				encoding.getBitsPerSample() > 16 ||
				samples.getNumberOfChannels() > 2 ||
				encoding.getBitsPerSample() != encoding.getBytesPerSample() * 8;
		int formatSize = needsExtensibleFormat ? 40 : 16;
		long dataSize = numSamples * samples.getNumberOfChannels() * encoding.getBytesPerSample();
		
		byte[] bytes = "RIFX".getBytes();
		os.write(bytes);
		long sizeOfRiffChunk = 4 + (12 + formatSize) + (4 + dataSize);
		writeInt((int)sizeOfRiffChunk, os);
		
		bytes = "WAVE".getBytes();
		os.write(bytes);
		bytes = "fmt ".getBytes();
		os.write(bytes);
		writeInt(formatSize, os);
		writeShort(needsExtensibleFormat ? (short)WAVE_FORMAT_EXTENSIBLE : (short)WAVE_FORMAT_PCM , os);
		writeShort((short)samples.getNumberOfChannels(), os);
		writeInt((int)samples.getSampleRate(), os);
		writeInt((int)(samples.getSampleRate() * encoding.getBytesPerSample() * samples.getNumberOfChannels()), os);  // avg bytes per sec
		writeShort((short)(encoding.getBytesPerSample() * samples.getNumberOfChannels()), os); // block alignment
		writeShort((short)(encoding.getBytesPerSample() * 8), os); // padded bits per sample
		if(needsExtensibleFormat) {
			writeShort((short)(22), os); // ext size
			writeShort((short)encoding.getBitsPerSample(), os);
			writeInt(0, os); // speaker position mark
			switch(encoding) {
			case LINEAR_12_BIG_ENDIAN:
			case LINEAR_16_BIG_ENDIAN:
			case LINEAR_24_BIG_ENDIAN:
			case LINEAR_32_BIG_ENDIAN:
				writeShort((short)WAVE_FORMAT_PCM, os);
				break;
				
			case IEEE_FLOAT_32_BIG_ENDIAN:
			case IEEE_FLOAT_64_BIG_ENDIAN:
				writeShort((short)WAVE_FORMAT_IEEE_FLOAT, os);
				break;
				
			default:
				throw new UnsupportedFormatException("Cannot write RIFX files with encoding " + encoding);
			}
			os.write(WAVE_SUBFORMAT_DATA);
		}
		os.write("data".getBytes());
		writeInt((int)dataSize, os);
	}

	private static void writeAiffHeaders(Sampled samples, int numSamples, AudioFileEncoding encoding, OutputStream os) throws IOException, UnsupportedFormatException {
		if(!encoding.isBigEndian()) {
			writeAifcHeaders(samples, numSamples, encoding, os);
		} else {
			int dataSize = numSamples * encoding.getBytesPerSample() * samples.getNumberOfChannels();
			
			os.write("FORM".getBytes());
			writeInt(4 + (8 + 4) + (8 + 18) + (8 + 8 + dataSize), os); // size of FORM
			os.write("AIFF".getBytes());
			
			/* format version (8+4) */
			os.write("FVER".getBytes());
			writeInt(4, os);
			writeInt(0xA2805140, os); // time of version
			
			/* common chunk (8+18) */
			os.write("COMM".getBytes());
			writeInt(18, os);
			writeShort((short)samples.getNumberOfChannels(), os);
			writeInt(numSamples, os);
			writeShort((short)encoding.getBitsPerSample(), os);
			writeLongDouble(samples.getSampleRate(), os);
			
			/* data chunk */
			os.write("SSND".getBytes());
			writeInt(dataSize + 8, os);
			writeInt(0, os); // offset
			writeInt(0, os); // block size
		}
	}
	
	private static void writeAifcHeaders(Sampled samples, int numSamples, AudioFileEncoding encoding, OutputStream os) throws IOException, UnsupportedFormatException {
		int dataSize = numSamples * encoding.getBytesPerSample() * samples.getNumberOfChannels();
		
		String ctype = "NONE";
		switch(encoding) {
		case LINEAR_12_BIG_ENDIAN:
		case LINEAR_16_BIG_ENDIAN:
		case LINEAR_24_BIG_ENDIAN:
		case LINEAR_32_BIG_ENDIAN:
			break;
		
		case LINEAR_12_LITTLE_ENDIAN:
		case LINEAR_16_LITTLE_ENDIAN:
		case LINEAR_24_LITTLE_ENDIAN:
		case LINEAR_32_LITTLE_ENDIAN:
			ctype = "sowt";
			break;
			
		case IEEE_FLOAT_32_BIG_ENDIAN:
			os.write("fl32".getBytes());
			break;
			
		case IEEE_FLOAT_64_BIG_ENDIAN:
			os.write("fl64".getBytes());
			break;
			
		default:
			throw new UnsupportedFormatException("Cannot write AIFC files with encoding " + encoding);
		}
		
		int sizeOfComm = 24;
		
		os.write("FORM".getBytes());
		writeInt(4 + (8 + 4) + (8 + sizeOfComm) + (8 + 8 + dataSize), os);
		os.write("AIFC".getBytes());
		
		os.write("FVER".getBytes());
		writeInt(4, os);
		writeInt(0xA2805140, os);
		
		os.write("COMM".getBytes());
		writeInt(sizeOfComm, os);
		writeShort((short)samples.getNumberOfChannels(), os);
		writeInt(numSamples, os);
		writeShort((short)encoding.getBitsPerSample(), os);
		writeLongDouble(samples.getSampleRate(), os);
		os.write(ctype.getBytes());
		writeShort((short)0, os);
		
		os.write("SSND".getBytes());
		writeInt(8 + dataSize, os);
		writeInt(0, os);
		writeInt(0, os);
	}
	
	public static int writeSamples(Sampled samples, int firstSample, int numSamples, AudioFileEncoding encoding, OutputStream os) throws IOException, AudioIOException {
		int numBytesRequired = samples.getNumberOfChannels() * numSamples * encoding.getBytesPerSample();
		int frameSize = samples.getNumberOfChannels() * encoding.getBytesPerSample();
		byte[] buffer = new byte[numBytesRequired];
		int samplesWritten = writeSamples(samples, firstSample, numSamples, encoding, buffer, 0);
		os.write(buffer, 0, samplesWritten * frameSize);
		return samplesWritten;
	}
	
	/**
	 * Write numSamples from Sampled starting at firstSample
	 * into the buffer starting at offset
	 * 
	 * @param samples
	 * @param buffer
	 * @param offset
	 * @param numSamples
	 * @param encoding
	 * 
	 * @return number of samples written
	 */
	public static int writeSamples(Sampled samples, int firstSample, int numSamples, AudioFileEncoding encoding, byte[] buffer, int offset) throws AudioIOException {
		int numBytesRequired = samples.getNumberOfChannels() * numSamples * encoding.getBytesPerSample();
		if(buffer.length < offset + numBytesRequired) throw new AudioIOException("Buffer too small");
		
		int frameSize = samples.getNumberOfChannels() * encoding.getBytesPerSample();
		
		if(samples.getNumberOfSamples() < firstSample + numSamples) throw new AudioIOException("Not enough sample data");
		double[][] sampleData = new double[samples.getNumberOfChannels()][];
		for(int i = 0; i < samples.getNumberOfChannels(); i++) {
			sampleData[i] = new double[numSamples];
		}
		samples.loadSampleData(sampleData, 0, firstSample, numSamples);
		
		int retVal = 0;
		double sampleFrame[] = new double[samples.getNumberOfChannels()];
		int idx = offset;
		for(int isamp = 0; isamp < numSamples; isamp++) {
			for(int ichan = 0; ichan < samples.getNumberOfChannels(); ichan++) {
				sampleFrame[ichan] = sampleData[ichan][isamp];
			}
			encodeFrame(sampleFrame, encoding, buffer, idx);
			idx += frameSize;
			++retVal;
		}
		
		return retVal;
	}
	
	public static double[] decodeFrame(byte[] buffer, int offset, AudioFileEncoding encoding, int numberOfChannels) 
		throws BufferUnderflowException, UnsupportedFormatException {
		double[] retVal = new double[numberOfChannels];
		decodeFrame(buffer, offset, encoding, numberOfChannels, retVal, 0);
		return retVal;
	}
	
	/**
	 * Decode a single frame of channel interleaved audio data from <code>buffer</code> at <code>offset</code> using <code>encoding</code>.
	 * Samples will be written into <code>samples</code> starting at <code>sampleOffset</code>.
	 * 
	 * @param buffer
	 * @param offset
	 * @param encoding
	 * @param numberOfChannels
	 * @param samples
	 * @param sampleOffset
	 * 
	 * @throws BufferUnderflowException if <code>buffer</code> or <code>samples</code> is not large enough
	 * @throws UnsupportedFormatException if unable to decode samples which using <code>encoding</code>
	 */
	public static void decodeFrame(byte[] buffer, int offset, AudioFileEncoding encoding, int numberOfChannels, double[] samples, int sampleOffset) 
			throws BufferUnderflowException, UnsupportedFormatException {
		int frameSize = encoding.getBytesPerSample() * numberOfChannels;
		if(buffer.length < offset + frameSize) throw new BufferUnderflowException();
		
		if(samples.length < sampleOffset + numberOfChannels) throw new BufferUnderflowException();
		
		switch(encoding) {
		case LINEAR_8_UNSIGNED:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				samples[sampleOffset + ichan] = (Byte.toUnsignedInt(buffer[offset]) * (1.0/128.0) - 1.0);
				offset += encoding.getBytesPerSample();
			}
			break;
		
		case LINEAR_8_SIGNED:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				samples[sampleOffset + ichan] = (buffer[offset] * (1.0/128.0));
				offset += encoding.getBytesPerSample();
			}
			break;
		
		case LINEAR_12_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				byte b1 = buffer[offset];
				byte b2 = buffer[offset+1];
				short value = (short)(
					(((int)b2 & 0xff) << 8) |
					((int)b1 & 0xff));
				samples[sampleOffset + ichan] = value * (1.0 / 2048.0);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_12_BIG_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				byte b1 = buffer[offset];
				byte b2 = buffer[offset+1];
				short value = (short)(
					(((int)b1 & 0xff) << 8) |
					((int)b2 & 0xff));
				samples[sampleOffset + ichan] = value * (1.0 / 2048.0);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_16_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				byte b1 = buffer[offset];
				byte b2 = buffer[offset+1];
				short value = (short)(
					(((int)b2 & 0xff) << 8) |
					((int)b1 & 0xff));
				samples[sampleOffset + ichan] = value * (1.0 / 32768.0);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_16_BIG_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				byte b1 = buffer[offset];
				byte b2 = buffer[offset+1];
				short value = (short)(
					(((int)b1 & 0xff) << 8) |
					((int)b2 & 0xff));
				samples[sampleOffset + ichan] = value * (1.0 / 32768.0);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_24_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				byte b1 = buffer[offset];
				byte b2 = buffer[offset+1];
				byte b3 = buffer[offset+2];
				int unsignedValue = 
						(int)(
							(((long)b3 & 0xff) << 16)
						|	(((long)b2 & 0xff) << 8)
						|	((long)b1 & 0xff)
						);
				if( ((int)b3 & 0x80) != 0) unsignedValue |= 0xff000000; // extend sign
				samples[sampleOffset + ichan] = unsignedValue * (1.0 / 8388608.0);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_24_BIG_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				byte b1 = buffer[offset];
				byte b2 = buffer[offset+1];
				byte b3 = buffer[offset+2];
				int unsignedValue = 
						(int)(
							(((long)b1 & 0xff) << 16)
						|	(((long)b2 & 0xff) << 8)
						|	((long)b3 & 0xff)
						);
				if( ((int)b1 & 0x80) != 0) unsignedValue |= 0xff000000; // extend sign
				samples[sampleOffset + ichan] = unsignedValue * (1.0 / 8388608.0);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_32_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				byte b1 = buffer[offset];
				byte b2 = buffer[offset+1];
				byte b3 = buffer[offset+2];
				byte b4 = buffer[offset+3];
				int value = 
						(int)(
							(((long)b4 & 0xff) << 24)
						|	(((long)b3 & 0xff) << 16)
						|	(((long)b2 & 0xff) << 8)
						|	((long)b1 & 0xff)
						);
				samples[sampleOffset + ichan] = value * (1.0 / 32768.0 / 65536.0);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_32_BIG_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				byte b1 = buffer[offset];
				byte b2 = buffer[offset+1];
				byte b3 = buffer[offset+2];
				byte b4 = buffer[offset+3];
				int value = 
						(int)(
							(((long)b1 & 0xff) << 24)
						|	(((long)b2 & 0xff) << 16)
						|	(((long)b3 & 0xff) << 8)
						|	((long)b4 & 0xff)
						);
				samples[sampleOffset + ichan] = value * (1.0 / 32768.0 / 65536.0);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case IEEE_FLOAT_32_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				samples[sampleOffset + ichan] = getFloatLE(buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case IEEE_FLOAT_32_BIG_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				samples[sampleOffset + ichan] = getFloat(buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case IEEE_FLOAT_64_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				samples[sampleOffset + ichan] = getDoubleLE(buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case IEEE_FLOAT_64_BIG_ENDIAN:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				samples[sampleOffset + ichan] = getDouble(buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case ALAW:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				samples[sampleOffset + ichan] = alaw2linear[Byte.toUnsignedInt(buffer[offset])] * (1.0 / 32768.0);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case MULAW:
			for(int ichan = 0; ichan < numberOfChannels; ichan++) {
				samples[sampleOffset + ichan] = ulaw2linear[Byte.toUnsignedInt(buffer[offset])] * (1.0 / 32768.0);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		default:
			throw new UnsupportedFormatException();
		}
	}
	
	public static byte[] encodeFrame(double[] samples, AudioFileEncoding encoding) throws UnsupportedFormatException, BufferUnderflowException {
		int frameSize = samples.length * encoding.getBytesPerSample();
		byte[] retVal = new byte[frameSize];
		encodeFrame(samples, encoding, retVal, 0);
		return retVal;
	}
	
	/**
	 * Encode frame using encoding into buffer at offset
	 * 
	 * @param samples array of samples, one per channel
	 * @param encoding
	 * @param buffer
	 * @param offset
	 * 
	 * @return <code>true</code> if the sample was clipped, <code>false</code> otherwise
	 * 
	 * @throws UnsupportedFormatException if encoding is not supported
	 * @throws BufferUnderflowException if the buffer does not have enough space
	 */
	public static boolean encodeFrame(double[] samples, AudioFileEncoding encoding, byte[] buffer, int offset) throws UnsupportedFormatException, BufferUnderflowException {
		int numBytesRequired = samples.length * encoding.getBytesPerSample();
		if(buffer.length < offset + numBytesRequired) throw new BufferUnderflowException();
		
		boolean clipped = false;
		
		switch(encoding) {
		case LINEAR_8_UNSIGNED:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				double value = Math.floor((samples[ichan] + 1.0) * 128.0);
				if(value < 0.0) { value = 0.0; clipped = true; }
				if(value > 255.0) { value = 255.0; clipped = true; }
				buffer[offset] = (byte)((long)value & 0xFF);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_8_SIGNED:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				double value = Math.floor(samples[ichan] * 128.0);
				if(value < -128.0) { value = -128.0; clipped = true; }
				if(value > 127.0) { value = 127.0; clipped = true; }
				buffer[offset] = (byte)((long)value & 0xFF);
				offset += encoding.getBytesPerSample();
			}
			break;
		
		case LINEAR_12_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				double value = Math.round(samples[ichan] * 2048.0);
				// clipping
				if(value < -2048.0) { value = -2048.0; clipped = true; }
				if(value > 2047.0) { value = 2047.0; clipped = true; }
				
				short bits = (short)((long)value & 0xFFFF);
				putShortLE(bits, buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_12_BIG_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				double value = Math.round(samples[ichan] * 2048.0);
				// clipping
				if(value < -2048.0) { value = -2048.0; clipped = true; }
				if(value > 2047.0) { value = 2047.0; clipped = true; }
				
				short bits = (short)((long)value & 0xFFFF);
				putShort(bits, buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_16_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				double value = Math.round(samples[ichan] * 32768.0);
				// clipping
				if(value < -32768.0) { value = -32768.0; clipped = true; }
				if(value > 32767.0) { value = 32767.0; clipped = true; }
				
				short bits = (short)((long)value & 0x0000FFFF);
				putShortLE(bits, buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_16_BIG_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				double value = Math.round(samples[ichan] * 32768.0);
				// clipping
				if(value < -32768.0) { value = -32768.0; clipped = true; }
				if(value > 32767.0) { value = 32767.0; clipped = true; }
				
				short bits = (short)((long)value & 0x0000FFFF);
				putShort(bits, buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_24_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				double value = Math.round(samples[ichan] * 8388608.0);
				if(value < -8388608.0) { value = -8388608.0; clipped = true; }
				if(value > 8388607.0) { value = -8388607.0; clipped = true; }
				
				int bits = (int)value;
				put24bitIntLE(bits, buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_24_BIG_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				double value = Math.round(samples[ichan] * 8388608.0);
				if(value < -8388608.0) { value = -8388608.0; clipped = true; }
				if(value > 8388607.0) { value = -8388607.0; clipped = true; }
				
				int bits = (int)value;
				put24bitInt(bits, buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_32_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				double value = Math.round(samples[ichan] * 2147483648.0);
				if(value < -2147483648.0) { value = -2147483648.0; clipped = true; }
				if(value > 2147483647.0) { value = 2147483647.0; clipped = true; }
				
				int bits = (int)value;
				putIntLE(bits, buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case LINEAR_32_BIG_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				double value = Math.round(samples[ichan] * 2147483648.0);
				if(value < -2147483648.0) { value = -2147483648.0; clipped = true; }
				if(value > 2147483647.0) { value = 2147483647.0; clipped = true; }
				
				int bits = (int)value;
				putInt(bits, buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
		
		case IEEE_FLOAT_32_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				putFloatLE((float)samples[ichan], buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case IEEE_FLOAT_32_BIG_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				putFloat((float)samples[ichan], buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case IEEE_FLOAT_64_LITTLE_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				putDoubleLE(samples[ichan], buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
			
		case IEEE_FLOAT_64_BIG_ENDIAN:
			for(int ichan = 0; ichan < samples.length; ichan++) {
				putDouble(samples[ichan], buffer, offset);
				offset += encoding.getBytesPerSample();
			}
			break;
		
		default:
			throw new UnsupportedFormatException(encoding.toString());
		}
		
		return clipped;
	}
	
	/*
	 * Useful functions visible to package
	 */
	static short readShort(InputStream is) throws IOException {
		byte[] buffer = new byte[2];
		if(is.read(buffer) < 2) throw new BufferUnderflowException();
		return getShort(buffer, 0);
	}
	
	static short getShort(byte[] data, int offset) throws BufferUnderflowException {
		if(data.length < offset + 2) throw new BufferUnderflowException();
		
		return (short)(
				(((long)data[offset] & 0xff) << 8)
				| ((long)data[offset+1] & 0xff));
	}
	
	static short readShortLE(InputStream is) throws IOException {
		byte[] buffer = new byte[2];
		if(is.read(buffer) < 2) throw new BufferUnderflowException();
		return getShortLE(buffer, 0);
	}
	
	static short getShortLE(byte[] data, int offset) throws BufferUnderflowException {
		if(data.length < offset + 2) throw new BufferUnderflowException();
		
		return (short)(
				(((long)data[offset + 1] & 0xff) << 8)
				| ((long)data[offset] & 0xff));
	}
	
	static int readInt(InputStream is) throws IOException {
		byte[] buffer = new byte[4];
		if(is.read(buffer) < 4) throw new BufferUnderflowException();
		return getInt(buffer, 0);
	}
	
	static int getInt(byte[] data, int offset) throws BufferUnderflowException {
		if(data.length < offset + 4) throw new BufferUnderflowException();
		
		return	(int)(
				(((long)data[offset] & 0xff) << 24)
				| (((long)data[offset+1] & 0xff) << 16) 
				| (((long)data[offset+2] & 0xff) << 8)
				| ((long)data[offset+3] & 0xff));
	}
	
	static int readIntLE(InputStream is) throws IOException {
		byte[] buffer = new byte[4];
		if(is.read(buffer) < 4) throw new BufferUnderflowException();
		return getIntLE(buffer, 0);
	}
	
	static int getIntLE(byte[] data, int offset) throws BufferUnderflowException {
		if(data.length < offset + 4) throw new BufferUnderflowException();
		
		return	(int)(
				(((long)data[offset+3] & 0xff) << 24)
				| (((long)data[offset+2] & 0xff) << 16) 
				| (((long)data[offset+1] & 0xff) << 8)
				| ((long)data[offset] & 0xff));
	}
	
	static float readFloat(InputStream is) throws IOException {
		byte[] buffer = new byte[4];
		if(is.read(buffer) < 4) throw new BufferUnderflowException();
		return getFloat(buffer, 0);
	}
		
	static float getFloat(byte[] data, int offset) throws BufferUnderflowException {
		int exp = (int)(
				(((long)data[offset] & 0x0000007F) << 1)
				| (((long)data[offset+1] & 0x00000080) >> 7));
		int mantissa = (int)(
				(((long)data[offset+1] & 0x0000007F) << 16)
				| (((long)data[offset+2] & 0xFF) << 8)
				| (((long)data[offset+3] & 0xFF)));
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
		return (data[offset] & 0x80) != 0 ? - x : x;		
	}
	
	static float readFloatLE(InputStream is) throws IOException {
		byte[] buffer = new byte[4];
		if(is.read(buffer) < 4) throw new BufferUnderflowException();
		return getFloatLE(buffer, 0);
	}
		
	static float getFloatLE(byte[] data, int offset) throws BufferUnderflowException {
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
	
	static double readDouble(InputStream is) throws IOException {
		byte[] buffer = new byte[8];
		if(is.read(buffer) < 8) throw new BufferUnderflowException();
		return getDouble(buffer, 0);
	}
	
	static double getDouble(byte[] data, int offset) throws BufferUnderflowException {
		if(data.length < offset + 8) throw new BufferUnderflowException();
		
		int exp = (int)(
				(((long)data[offset] & 0x0000007F) << 4)
				| (((long)data[offset+1] & 0x000000F0) >> 4));
		long highMantissa = 
				(((long)data[offset+1] & 0x0F) << 16)
				| (((long)data[offset+2] & 0xFF) << 8)
				| (((long)data[offset+3] & 0xFF));
		long lowMantissa = 
				(((long)data[offset+4] & 0xFF) << 24)
				| (((long)data[offset+5] & 0xFF) << 16)
				| (((long)data[offset+6] & 0xFF) << 8)
				| (((long)data[offset+7] & 0xFF));
		
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
		return (data[offset] & 0x80) != 0 ? - x : x;
	}
	
	static double readDoubleLE(InputStream is) throws IOException {
		byte[] buffer = new byte[8];
		if(is.read(buffer) < 8) throw new BufferUnderflowException();
		return getDoubleLE(buffer, 0);
	}
	
	static double getDoubleLE(byte[] data, int offset) throws BufferUnderflowException {
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
	
	static double readLongDouble(InputStream is) throws IOException {
		byte[] buffer = new byte[10];
		if(is.read(buffer) < 10) throw new BufferUnderflowException();
		return getLongDouble(buffer, 0);
	}
	
	static double getLongDouble(byte[] data, int offset) throws BufferUnderflowException {
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
	
	/*
	 * Package visible write methods
	 */
	static void writeShort(short value, OutputStream os) throws IOException {
		byte[] bytes = new byte[2];
		putShort(value, bytes, 0);
		os.write(bytes);
	}
	
	static void putShort(short value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 2) throw new BufferUnderflowException();
		
		buffer[offset] = (byte)((value >> 8) & 0xff);
		buffer[offset+1] = (byte)(value & 0xff);
	}
	
	static void writeShortLE(short value, OutputStream os) throws IOException {
		byte[] bytes = new byte[2];
		putShortLE(value, bytes, 0);
		os.write(bytes);
	}
	
	static void putShortLE(short value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 2) throw new BufferUnderflowException();
		
		buffer[offset+1] = (byte)((value >> 8) & 0xff);
		buffer[offset] = (byte)(value & 0xff);
	}
	
	static void write24bitInt(int value, OutputStream os) throws IOException {
		byte[] bytes = new byte[3];
		put24bitInt(value, bytes, 0);
		os.write(bytes);
	}
	
	static void put24bitInt(int value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 3) throw new BufferUnderflowException();
		
		buffer[offset] = (byte)((value >> 16) & 0xff);
		buffer[offset+1] = (byte)((value >> 8) & 0xff);
		buffer[offset+2] = (byte)(value & 0xff);
	}
	
	static void write24bitIntLE(int value, OutputStream os) throws IOException {
		byte[] bytes = new byte[3];
		put24bitIntLE(value, bytes, 0);
		os.write(bytes);
	}
	
	static void put24bitIntLE(int value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 3) throw new BufferUnderflowException();
		
		buffer[offset+2] = (byte)((value >> 16) & 0xff);
		buffer[offset+1] = (byte)((value >> 8) & 0xff);
		buffer[offset] = (byte)(value & 0xff);
	}

	static void writeInt(int value, OutputStream os) throws IOException {
		byte[] bytes = new byte[4];
		putInt(value, bytes, 0);
		os.write(bytes);
	}
	
	static void putInt(int value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 4) throw new BufferUnderflowException();
		
		buffer[offset] = (byte)((value >> 24) & 0xff);
		buffer[offset+1] = (byte)((value >> 16) & 0xff);
		buffer[offset+2] = (byte)((value >> 8) & 0xff);
		buffer[offset+3] = (byte)(value & 0xff);
	}
	
	static void writeIntLE(int value, OutputStream os) throws IOException {
		byte[] bytes = new byte[4];
		putIntLE(value, bytes, 0);
		os.write(bytes);
	}
	
	static void putIntLE(int value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 4) throw new BufferUnderflowException();
		
		buffer[offset+3] = (byte)((value >> 24) & 0xff);
		buffer[offset+2] = (byte)((value >> 16) & 0xff);
		buffer[offset+1] = (byte)((value >> 8) & 0xff);
		buffer[offset] = (byte)(value & 0xff);
	}
	
	static void writeFloat(float value, OutputStream os) throws IOException {
		byte[] bytes = new byte[4];
		putFloat(value, bytes, 0);
		os.write(bytes);
	}
	
	static void putFloat(float value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 4) throw new BufferUnderflowException();
		
		int bits = Float.floatToRawIntBits(value);
		buffer[offset] = (byte)((bits >> 24) & 0xff);
		buffer[offset+1] = (byte)((bits >> 16) & 0xff);
		buffer[offset+2] = (byte)((bits >> 8) & 0xff);
		buffer[offset+3] = (byte)(bits & 0xff);
	}
	
	static void writeFloatLE(float value, OutputStream os) throws IOException {
		byte[] bytes = new byte[4];
		putFloatLE(value, bytes, 0);
		os.write(bytes);
	}
	
	static void putFloatLE(float value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 4) throw new BufferUnderflowException();
		
		int bits = Float.floatToRawIntBits(value);
		buffer[offset+3] = (byte)((bits >> 24) & 0xff);
		buffer[offset+2] = (byte)((bits >> 16) & 0xff);
		buffer[offset+1] = (byte)((bits >> 8) & 0xff);
		buffer[offset] = (byte)(bits & 0xff);
	}
	
	static void writeDouble(double value, OutputStream os) throws IOException {
		byte[] bytes = new byte[8];
		putDouble(value, bytes, 0);
		os.write(bytes);
	}
	
	static void putDouble(double value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 8) throw new BufferUnderflowException();
		
		long bits = Double.doubleToRawLongBits(value);
		buffer[offset] = (byte)((bits >> 56) & 0xff);
		buffer[offset+1] = (byte)((bits >> 48) & 0xff);
		buffer[offset+2] = (byte)((bits >> 40) & 0xff);
		buffer[offset+3] = (byte)((bits >> 32) & 0xff);
		buffer[offset+4] = (byte)((bits >> 24) & 0xff);
		buffer[offset+5] = (byte)((bits >> 16) & 0xff);
		buffer[offset+6] = (byte)((bits >> 8) & 0xff);
		buffer[offset+7] = (byte)(bits & 0xff);
	}
	
	static void writeDoubleLE(double value, OutputStream os) throws IOException {
		byte[] bytes = new byte[8];
		putDoubleLE(value, bytes, 0);
		os.write(bytes);
	}
	
	static void putDoubleLE(double value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 8) throw new BufferUnderflowException();
		
		long bits = Double.doubleToRawLongBits(value);
		buffer[offset+7] = (byte)((bits >> 56) & 0xff);
		buffer[offset+6] = (byte)((bits >> 48) & 0xff);
		buffer[offset+5] = (byte)((bits >> 40) & 0xff);
		buffer[offset+4] = (byte)((bits >> 32) & 0xff);
		buffer[offset+3] = (byte)((bits >> 24) & 0xff);
		buffer[offset+2] = (byte)((bits >> 16) & 0xff);
		buffer[offset+1] = (byte)((bits >> 8) & 0xff);
		buffer[offset] = (byte)(bits & 0xff);
	}
	
	static void writeLongDouble(double value, OutputStream os) throws IOException {
		byte[] bytes = new byte[10];
		putLongDouble(value, bytes, 0);
		os.write(bytes);
	}
	
	static void putLongDouble(double value, byte[] buffer, int offset) throws BufferUnderflowException {
		if(buffer.length < offset + 10) throw new BufferUnderflowException();
		
		/*
		 * This methods will attempt to convert a 64-bit floating point
		 * number to an 80-bit number using some bit magic.
		 */
		long bits = Double.doubleToLongBits(value);
		
		// convert exponent
		int exp = (int)(((long)bits >> 52) & 0x07ff) - 1023;
		exp += 16383;
		
		// grab sign and add to exp
		byte sign = (byte)(((long)bits >> 63) & 0x01);
		exp |= (sign << 15);
				
		// update fraction portion and set integer bit
		long fraction = (long)(bits & 0x000FFFFFFFFFFFFFL) << 11; 
		fraction |= 0x8000000000000000L;
		
		// big endian
		buffer[offset] = (byte)((exp >> 8) & 0xff);
		buffer[offset+1] = (byte)(exp & 0xff);
		
		buffer[offset+2] = (byte)((fraction >> 56) & 0xff);
		buffer[offset+3] = (byte)((fraction >> 48) & 0xff);
		buffer[offset+4] = (byte)((fraction >> 40) & 0xff);
		buffer[offset+5] = (byte)((fraction >> 32) & 0xff);
		buffer[offset+6] = (byte)((fraction >> 24) & 0xff);
		buffer[offset+7] = (byte)((fraction >> 16) & 0xff);
		buffer[offset+8] = (byte)((fraction >> 8) & 0xff);
		buffer[offset+9] = (byte)(fraction & 0xff);
	}
	
}
