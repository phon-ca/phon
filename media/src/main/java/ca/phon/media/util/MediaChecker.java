package ca.phon.media.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
		final CheckHandler handler = new CheckHandler(latch);
		MediaCheckTcpServer server = new MediaCheckTcpServer(mediaFile);
		server.setMediaCheckHandler(handler);
		server.startServer();
		
		List<String> fullCmd = new ArrayList<String>();
		String[] cmd = {
				javaBin,
				"-cp", cp,
				"-Djava.library.path=" + libPath
		};
		fullCmd.addAll(Arrays.asList(cmd));
		fullCmd.add(className);
		fullCmd.add(""+server.getPort());
		
		
		// Fail if process exits 
		// with state other than 0 or if process takes
		// more than timeout to complete (considered a hang.)
		ProcessBuilder pb = new ProcessBuilder(fullCmd);
		try {
			Process p = pb.start();
			
			// create process streams or some oses (windows) will deadlock
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			reader.close();
			
			latch.await(timeout, TimeUnit.MILLISECONDS);
			
		} catch (InterruptedException | IOException e) {
			return  false;
		}
		
		return handler.status == MediaCheckStatus.OK;
	}
	
	private static class CheckHandler implements MediaCheckHandler {
		MediaCheckStatus status = MediaCheckStatus.NEEDS_REENCODE;
		String msg = "";
		
		CountDownLatch latch;
		
		public CheckHandler(CountDownLatch latch) {
			this.latch = latch;
		}
		
		@Override
		public void mediaCheckComplete(MediaCheckStatus status, String msg) {
			this.status = status;
			this.msg = msg;
			latch.countDown();
		}
		
	}

	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage 'java ca.phon.media.MediaChecker <port>'");
			System.exit(1);
		}
		
		Integer port = Integer.parseInt(args[0]);
		try (Socket socket = new Socket(InetAddress.getLocalHost(), port)) {
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String filename = in.readLine();
			 
			try {
				LongSound ls = LongSound.fromFile(new File(filename));
				if(ls instanceof SampledLongSound) {
					SampledLongSound sls = (SampledLongSound)ls;
					if(((PCMSampled)sls.getSampled()).getAudioFileFormat().getFormat().getSampleSizeInBits() != 16) {
						throw new IOException(String.format("%s invalid format", args[0]));
					}
				}
			} catch (IOException e) {
				writer.println("ERROR " + e.getLocalizedMessage());
			}
			
			writer.println("OK");
			
			writer.close();
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
}
