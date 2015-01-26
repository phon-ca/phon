package ca.phon.media.player;

import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

public class PhonBufferFormatCallback implements BufferFormatCallback {

	@Override
	public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
		return new RV32BufferFormat(sourceWidth, sourceHeight);
	}

}
