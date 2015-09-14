package ca.phon.util;

public class ByteSize {
	
	/**
	 * Return a human-readable version of the given byte size
	 * 
	 * @param bytes
	 * @param si use si units?
	 * 
	 * @return human readable byte size
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
