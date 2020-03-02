package ca.phon.media.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ca.phon.media.LongSound;
import ca.phon.media.sampled.PCMSampled;
import ca.phon.ui.nativedialogs.OSInfo;

/**
 * Check wav media to see if it can be loaded by
 * the java audio system.
 * 
 * 
 */
public class MediaChecker {
	
	public static boolean checkMediaFile(String mediaFile) {
		String className = MediaChecker.class.getName();
		
		final String javaHome = System.getProperty("java.home");
		final String javaBin = javaHome + File.separator + "bin" + File.separator + "java" + 
				(OSInfo.isWindows() ? ".exe" : "");
		final String cp = System.getProperty("java.class.path");
		final String libPath = System.getProperty("java.library.path");
		
		List<String> fullCmd = new ArrayList<String>();
		String[] cmd = {
				javaBin,
				"-cp", cp,
				"-Djava.library.path=" + libPath
		};
		fullCmd.addAll(Arrays.asList(cmd));
		fullCmd.add(className);
		fullCmd.add(mediaFile);
		
		// Fail if process exits 
		// with state other than 0 or if process takes
		// more than 1000ms to complete (considered a hang.)
		ProcessBuilder pb = new ProcessBuilder(fullCmd);
		pb.redirectError(new File("/Users/ghedlund/Desktop/err.txt"));
		try {
			Process p = pb.start();
			int exitValue = -1;
			if(!p.waitFor(1000L, TimeUnit.MILLISECONDS)) {
				p.destroyForcibly();
			} else {
				exitValue = p.exitValue();
			}
			return (exitValue == 0);
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}

	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage 'java ca.phon.media.MediaChecker <file>'");
			System.exit(1);
		}
		
		try {
			LongSound ls = LongSound.fromFile(new File(args[0]));
			System.out.println(String.format("%s %fs OK", args[0], ls.length()));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
		
		System.exit(0);
	}
	
}
