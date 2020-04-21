package ca.phon.media.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.core.util.FileWatcher;

import ca.phon.media.LongSound;
import ca.phon.media.sampled.PCMSampled;
import ca.phon.media.sampled.SampledLongSound;
import ca.phon.media.util.MediaCheckHandler.MediaCheckStatus;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;

/**
 * Check wav media to see if it can be loaded by
 * the java audio system.
 * 
 * 
 */
public class MediaChecker {
	
	public final static String TIMEOUT = MediaChecker.class.getName() + ".timeout";
	private final static int DEFAULT_TIMEOUT = 5000;
		
	public static boolean checkMediaFile(String mediaFile) {
		int timeout = PrefHelper.getInt(TIMEOUT, DEFAULT_TIMEOUT);
		
		String className = MediaChecker.class.getName();
		
		final String javaHome = System.getProperty("java.home");
		final String javaBin = javaHome + File.separator + "bin" + File.separator + "java" + 
				(OSInfo.isWindows() ? ".exe" : "");
		final String cp = System.getProperty("java.class.path");
		final String libPath = System.getProperty("java.library.path");
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		final Logger LOGGER = Logger.getLogger(className);
		try {
			File tmpFile = File.createTempFile("phon", "MediaCheck");
			final CheckHandler handler = new CheckHandler(tmpFile, latch);
			File directory = tmpFile.getParentFile();
			FileAlterationObserver observer = new FileAlterationObserver(directory);
			observer.addListener(handler);
			FileAlterationMonitor monitor = new FileAlterationMonitor(100, observer);
			monitor.start();
			
			List<String> fullCmd = new ArrayList<String>();
			String[] cmd = {
					javaBin,
					"-cp", cp,
					"-Djava.library.path=" + libPath
			};
			fullCmd.addAll(Arrays.asList(cmd));
			fullCmd.add(className);
			fullCmd.add(mediaFile);
			fullCmd.add(tmpFile.getAbsolutePath());
						
			// Fail if process exits 
			// with state other than 0 or if process takes
			// more than timeout to complete (considered a hang.)
			ProcessBuilder pb = new ProcessBuilder(fullCmd);
			try {
				LOGGER.log(Level.INFO, "(MediaCheck) checking " + mediaFile);
				Process p = pb.start();
				
				// create process streams or some oses (windows) will deadlock
				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				reader.close();
				
				latch.await(timeout, TimeUnit.MILLISECONDS);
				LOGGER.log((handler.status == MediaCheckStatus.OK ? Level.INFO : Level.SEVERE), "(MediaCheck) " + handler.msg);
			} catch (InterruptedException | IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				return  false;
			}
			monitor.stop();
			
			return handler.status == MediaCheckStatus.OK;
		} catch (Exception e1) {
			Logger.getLogger(className).severe(e1.getLocalizedMessage());
		}
		
		return false;
	}
	
	private static class CheckHandler extends FileAlterationListenerAdaptor {
		MediaCheckStatus status = MediaCheckStatus.NEEDS_REENCODE;
		String msg = "";
		
		File tmpFile;
		CountDownLatch latch;
		
		public CheckHandler(File tmpFile, CountDownLatch latch) {
			this.tmpFile = tmpFile;
			this.latch = latch;
		}
		
		@Override
		public void onFileChange(File file) {
			if(file.equals(tmpFile)) {
				try {
					String content = Files.readString(tmpFile.toPath());
					// the first message will not have content
					if(content.trim().length() > 0) {
						status = (content.startsWith("OK") ? MediaCheckStatus.OK : MediaCheckStatus.NEEDS_REENCODE);
						msg = content;
						latch.countDown();
					}
				} catch (IOException e) {
					status = MediaCheckStatus.ERROR;
					msg = e.toString();
					latch.countDown();
				}
			}
		}
		
	}

	public static void main(String[] args) {
		if(args.length != 2) {
			System.err.println("Usage 'java ca.phon.media.MediaChecker <mediafile> <outputfile>'");
			System.exit(1);
		}
		
		String mediafile = args[0];
		String outputfile = args[1];
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(outputfile), true), true)) {
			try {
				LongSound ls = LongSound.fromFile(new File(mediafile));
				if(ls instanceof SampledLongSound) {
					SampledLongSound sls = (SampledLongSound)ls;
					if(((PCMSampled)sls.getSampled()).getAudioFileFormat().getFormat().getSampleSizeInBits() != 16) {
						throw new IOException(String.format("%s invalid format", args[0]));
					}
				}
				writer.println("OK");
			} catch (IOException e) {
				writer.println("ERROR " + e.getLocalizedMessage());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
}
