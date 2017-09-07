/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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

package ca.phon.media.export;

import java.io.*;
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
