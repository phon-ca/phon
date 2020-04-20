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

/**
 * Check wav media to see if it can be loaded by
 * the java audio system.
 * 
 * 
 */
public class MediaChecker {
	
	private final static long TIMEOUT = 5000L;
		
	public static boolean checkMediaFile(String mediaFile) {
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
		// more than 2000ms to complete (considered a hang.)
		ProcessBuilder pb = new ProcessBuilder(fullCmd);
		try {
			pb.start();
			latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
			
			return handler.status == MediaCheckStatus.OK;
		} catch (InterruptedException | IOException e) {
			return  false;
		}
		
//		if(ca.phon.util.OSInfo.isWindows()) {
//			// windows requires we read in all buffered
//			// data before it will report the process as complete
//			// this seems to keep the process alive on macOS...
//			pb.redirectErrorStream(true);
//			try {
//				Process p = pb.start();
////				Timer timer = new Timer();
////				timer.schedule(new TimerTask() {
////					@Override
////					public void run() {
////						p.destroyForcibly();
////					}
////				}, TIMEOUT);
//				
//				try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
//					String line;
//					while ((line = reader.readLine()) != null)
//					    System.err.println("mediacheck: " + line);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
////				timer.cancel();
//				
//				return (p.exitValue() == 0);
//			} catch (IOException | IllegalThreadStateException e) {
//				e.printStackTrace();
//				return false;
//			}
//		} else {
////			try {
//				Process p = pb.start();
////				int exitValue = -1;
////				if(!p.waitFor(TIMEOUT, TimeUnit.MILLISECONDS)) {
////					p.destroyForcibly();
////				} else {
////					exitValue = p.exitValue();
////				}
////				return (exitValue == 0);
////			} catch (IOException | InterruptedException e) {
////				return false;
////			}
//		}
		
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
