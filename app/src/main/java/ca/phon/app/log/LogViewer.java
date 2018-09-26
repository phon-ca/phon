/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.log;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.OpenFileLauncher;
import javafx.application.Platform;

/**
 * Application log viewer.
 */
public class LogViewer extends CommonModuleFrame {
	
	private MultiBufferPanel bufferPanel;
	
	public LogViewer() {
		super("Logs");
		setWindowName("Phon : Application Logs");
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
				
		bufferPanel = new MultiBufferPanel();
		SwingUtilities.invokeLater( () -> loadLog(new File(LogManager.LOG_FILE)) );
		
		add(bufferPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		
		final MenuBuilder builder = new MenuBuilder(menuBar);
		final JMenu logMenu = builder.addMenu(".@Query", "Logs");
		
		logMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				logMenu.removeAll();
				
				final File currentLogFile = new File(LogManager.LOG_FILE);
				final PhonUIAction loadCurrentLogAct = new PhonUIAction(LogViewer.this, "loadLog", new File(LogManager.LOG_FILE));
				loadCurrentLogAct.putValue(PhonUIAction.NAME, "Current log");
				loadCurrentLogAct.putValue(PhonUIAction.SHORT_DESCRIPTION, LogManager.LOG_FILE);
				loadCurrentLogAct.putValue(PhonUIAction.SELECTED_KEY,
						bufferPanel.getCurrentBuffer() != null && bufferPanel.getCurrentBuffer().getName().equals(currentLogFile.getName()));
				final JCheckBoxMenuItem currentLogItem = new JCheckBoxMenuItem(loadCurrentLogAct);
				logMenu.add(currentLogItem);
				
				List<File> sortedFiles = new ArrayList<>(List.of(LogManager.getInstance().getPreviousLogs()));
				sortedFiles.sort( (f1, f2) -> Long.valueOf(f2.lastModified()).compareTo(f1.lastModified()) );
				
				for(File previousLogFile:sortedFiles) {
					final PhonUIAction loadLogAct = new PhonUIAction(LogViewer.this, "loadLog", previousLogFile);
					var logName = previousLogFile.getName();
					loadLogAct.putValue(PhonUIAction.NAME, logName);
					loadLogAct.putValue(PhonUIAction.SHORT_DESCRIPTION, previousLogFile.getAbsolutePath());
					loadLogAct.putValue(PhonUIAction.SELECTED_KEY, 
							bufferPanel.getCurrentBuffer() != null && bufferPanel.getCurrentBuffer().getName().equals(logName));
					final JCheckBoxMenuItem prevLogItem = new JCheckBoxMenuItem(loadLogAct);
					logMenu.add(prevLogItem);
				}
				
				logMenu.addSeparator();
				
				final File logFolder = new File(LogManager.LOG_FOLDER);
				try {
					final PhonUIAction showLogFolderAct = new PhonUIAction(OpenFileLauncher.class, "openURL",
							logFolder.toURI().toURL() );
					showLogFolderAct.putValue(PhonUIAction.NAME, "Show log folder");
					showLogFolderAct.putValue(PhonUIAction.SHORT_DESCRIPTION, logFolder.getAbsolutePath());
					final JMenuItem showLogFolderItem = new JMenuItem(showLogFolderAct);
					logMenu.add(showLogFolderItem);
				} catch (MalformedURLException e1) {
					LogUtil.severe(e1.getLocalizedMessage(), e1);
				}
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		
		final JMenu bufferMenu = builder.addMenu(".@Logs", "Buffers");
		bufferMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				bufferPanel.setupMenu(bufferMenu);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		
		
	}

	public void loadLog(File logFile) {
		final String bufferName = logFile.getName();
		if(bufferPanel.getBuffer(bufferName) != null) {
			bufferPanel.selectBuffer(bufferName);
			return;
		}

		if(!logFile.exists()) return;
		final BufferPanel buffer = bufferPanel.createBuffer(bufferName, true);
		if(bufferName.endsWith(".html")) {
			final var webView = buffer.getWebView();
			buffer.showHtml(false);
			Platform.runLater( () -> {
				webView.getEngine().load(logFile.toURI().toString());
			});
		} else if(bufferName.endsWith(".html.gz")) {
			try (
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(new GZIPInputStream(new FileInputStream(logFile)), "UTF-8")) ) {
				
				final PrintWriter writer = new PrintWriter(buffer.getLogBuffer().getStdOutStream());
				
				String line = "";
				while((line = reader.readLine()) != null) {
					writer.write(line);
					writer.write("\n");
					writer.flush();
				}
				writer.close();
				
				buffer.showHtml();
			} catch (IOException e) {
				LogUtil.severe(e.getLocalizedMessage(), e);
			}
		}
	}
	
}
