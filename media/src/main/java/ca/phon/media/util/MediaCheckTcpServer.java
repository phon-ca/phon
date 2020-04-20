package ca.phon.media.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.media.util.MediaCheckHandler.MediaCheckStatus;
import ca.phon.util.PrefHelper;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

public class MediaCheckTcpServer {
	
	public final static String TIMEOUT = MediaCheckTcpServer.class.getName() + ".timeout";
	private final static Integer DEFAULT_TIMEOUT = 0;
	
	private final Logger LOGGER = Logger.getLogger(MediaCheckTcpServer.class.getName());
	
	private ServerSocket serverSock;
	
	private MediaCheckHandler mediaCheckHandler;
	
	private final String mediaFile;
	
	public MediaCheckTcpServer(String mediaFile) {
		super();
		
		this.mediaFile = mediaFile;
		try {
			serverSock = new ServerSocket(0);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	public void startServer() {
		PhonWorker worker = PhonWorker.createWorker();
		worker.setName("MediaCheck");
		worker.setFinishWhenQueueEmpty(true);
		worker.invokeLater(server);
		worker.start();
	}
	
	public String getMediaFile() {
		return this.mediaFile;
	}
	
	public MediaCheckHandler getMediaCheckHandler() {
		return mediaCheckHandler;
	}

	public void setMediaCheckHandler(MediaCheckHandler mediaCheckHandler) {
		this.mediaCheckHandler = mediaCheckHandler;
	}

	public Integer getTimeout() {
		return PrefHelper.getInt(TIMEOUT, DEFAULT_TIMEOUT);
	}
	
	public Integer getPort() {
		return serverSock.getLocalPort();
	}
	
	private final PhonTask server = new PhonTask() {
		
		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			// start listening on socket, with timeout
			try {
				LOGGER.log(Level.INFO, "Setting up MediaCheck listener on port:" + serverSock.getLocalPort());
				final Socket sock = serverSock.accept();
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(sock.getInputStream(), "UTF-8"));
				
				final PrintWriter writer = new PrintWriter(sock.getOutputStream(), true);
				writer.println(getMediaFile());
				
				String line = null;
				final StringBuilder sb = new StringBuilder();
				while((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				reader.close();
				sock.close();
				
				MediaCheckStatus status = sb.toString().startsWith("OK") ? MediaCheckStatus.OK : MediaCheckStatus.NEEDS_REENCODE;
				if(getMediaCheckHandler() != null) {
					getMediaCheckHandler().mediaCheckComplete(status, sb.toString());
				}
				
				LOGGER.log(Level.INFO, "Media check completed: " + sb.toString());
				
				setStatus(TaskStatus.FINISHED);
			} catch (IOException e) {
				super.err = e;
				setStatus(TaskStatus.ERROR);
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				
				if(getMediaCheckHandler() != null) {
					getMediaCheckHandler().mediaCheckComplete(MediaCheckStatus.ERROR, e.getLocalizedMessage());
				}
			} finally {
				if(!serverSock.isClosed()) {
					try {
						serverSock.close();
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(),
								e);
					}
				}
			}
		}
		
	};
	
}
