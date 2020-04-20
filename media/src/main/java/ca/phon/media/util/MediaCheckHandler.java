package ca.phon.media.util;

public interface MediaCheckHandler {
	
	public static enum MediaCheckStatus {
		OK,
		NEEDS_REENCODE,
		ERROR;
	}
	
	public void mediaCheckComplete(MediaCheckStatus status, String msg);

}
