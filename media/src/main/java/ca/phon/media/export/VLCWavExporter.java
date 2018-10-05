/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

package ca.phon.media.export;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import ca.phon.media.exceptions.PhonMediaException;


/**
 * Uses vlc4j to export a media file into a wav file.  This class performs the extra
 * step of fixing the headers in the wav file after export.  VLC does not write
 * the correct values for fields <code>SubChunk2Size</code> and <code>SubChunk2Size</code>.
 * 
 */
public class VLCWavExporter extends VLCMediaExporter {
	
	public VLCWavExporter(File mediaFile, File outputFile) {
		super(mediaFile, outputFile, Preset.WAV);
		setIncludeVideo(false);
	}
	
	/**
	 * Constructor
	 */
	public VLCWavExporter(String mediaFile, String outputFile) {
		this(new File(mediaFile), new File(outputFile));
	}

	@Override
	protected void doExport() throws PhonMediaException {
		super.doExport();
		try {
			fixHeader();
		} catch (IOException e) {
			throw new PhonMediaException(e);
		}
	}
	
	private void fixHeader() throws IOException {
		File f = getOutputFile();

		final RandomAccessFile raf = new RandomAccessFile(f, "rw");
		FileChannel rwChannel = raf.getChannel();
		ByteBuffer rwBuffer =
				rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, rwChannel.size());

		// we need to fix SubChunk2Size in the wave header
		// SubChunk2Size = SubChunkSize - 36
		// SubChunkSize is contained in bytes 4-8 and bytes
		// need to be reversed
		byte buf[] = new byte[4];

		// write chunksize
		rwBuffer.position(4);
		int chunkSize =
				(int)rwChannel.size() - 8;
		buf[0] = (byte)((chunkSize & 0x000000FF) >> 0);
		buf[1] = (byte)((chunkSize & 0x0000FF00) >> 8);
		buf[2] = (byte)((chunkSize & 0x00FF0000) >> 16);
		buf[3] = (byte)((chunkSize & 0xFF000000) >> 24);
		rwBuffer.put(buf);

		rwBuffer.position(40);
		int dataChunkSize = chunkSize - 36;
		buf[0] = (byte)((dataChunkSize & 0x000000FF) >> 0);
		buf[1] = (byte)((dataChunkSize & 0x0000FF00) >> 8);
		buf[2] = (byte)((dataChunkSize & 0x00FF0000) >> 16);
		buf[3] = (byte)((dataChunkSize & 0xFF000000) >> 24);
		rwBuffer.put(buf);

		rwChannel.close();
		raf.close();
	}
	
}
