package ca.phon.media.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ca.phon.media.LongSound;
import ca.phon.media.sampled.PCMSampled;
import ca.phon.ui.nativedialogs.OSInfo;
import ca.phon.worker.PhonWorker;

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
		// more than 2000ms to complete (considered a hang.)
		ProcessBuilder pb = new ProcessBuilder(fullCmd);
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					p.destroyForcibly();
				}
			}, 2000L);
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null)
				    System.err.println("mediacheck: " + line);
			} catch (IOException e) {
				e.printStackTrace();
			}
			timer.cancel();
			
			return (p.exitValue() == 0);
		} catch (IOException | IllegalThreadStateException e) {
			return false;
		}
	}

	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage 'java ca.phon.media.MediaChecker <file>'");
			System.exit(1);
		}
		
		try {
			System.err.println(String.format("Opening file %s", args[0]));
			LongSound ls = LongSound.fromFile(new File(args[0]));
			System.err.println(String.format("%s %fs OK", args[0], ls.length()));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
		
		System.exit(0);
	}
	
}
