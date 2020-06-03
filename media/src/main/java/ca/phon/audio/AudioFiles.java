package ca.phon.audio;

import java.io.File;
import java.io.IOException;

public class AudioFiles {

	public static AudioFile openAudioFile(File file) throws IOException, UnsupportedFormatException, InvalidHeaderException {
		return new AudioFile(file);
	}
	
}