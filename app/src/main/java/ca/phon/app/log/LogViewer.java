/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.log;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.PrefHelper;
import org.cef.browser.*;
import org.cef.handler.CefLoadHandlerAdapter;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.zip.GZIPInputStream;

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
				final PhonUIAction<File> loadCurrentLogAct = PhonUIAction.consumer(LogViewer.this::loadLog, new File(LogManager.LOG_FILE));
				loadCurrentLogAct.putValue(PhonUIAction.NAME, "Current log");
				loadCurrentLogAct.putValue(PhonUIAction.SHORT_DESCRIPTION, LogManager.LOG_FILE);
				loadCurrentLogAct.putValue(PhonUIAction.SELECTED_KEY,
						bufferPanel.getCurrentBuffer() != null && (bufferPanel.getCurrentBuffer().getName() + ".html").equals(currentLogFile.getName()));
				final JCheckBoxMenuItem currentLogItem = new JCheckBoxMenuItem(loadCurrentLogAct);
				logMenu.add(currentLogItem);
				
				List<File> sortedFiles = new ArrayList<>(List.of(LogManager.getInstance().getPreviousLogs()));
				sortedFiles.sort( (f1, f2) -> Long.valueOf(f2.lastModified()).compareTo(f1.lastModified()) );
				
				for(File previousLogFile:sortedFiles) {
					final PhonUIAction<File> loadLogAct = PhonUIAction.consumer(LogViewer.this::loadLog, previousLogFile);
					var logName = previousLogFile.getName();
					loadLogAct.putValue(PhonUIAction.NAME, logName);
					loadLogAct.putValue(PhonUIAction.SHORT_DESCRIPTION, previousLogFile.getAbsolutePath());
					loadLogAct.putValue(PhonUIAction.SELECTED_KEY, 
							bufferPanel.getCurrentBuffer() != null && (bufferPanel.getCurrentBuffer().getName()+".html.gz").equals(logName));
					final JCheckBoxMenuItem prevLogItem = new JCheckBoxMenuItem(loadLogAct);
					logMenu.add(prevLogItem);
				}
				
				logMenu.addSeparator();
				
				final File logFolder = new File(LogManager.LOG_FOLDER);
				final PhonUIAction<Void> showLogFolderAct = PhonUIAction.runnable(() -> {
					try {
						Desktop.getDesktop().open(logFolder);
					} catch (IOException ex) {
						LogUtil.warning(ex);
					}
				});
				showLogFolderAct.putValue(PhonUIAction.NAME, "Show log folder");
				showLogFolderAct.putValue(PhonUIAction.SHORT_DESCRIPTION, logFolder.getAbsolutePath());
				final JMenuItem showLogFolderItem = new JMenuItem(showLogFolderAct);
				logMenu.add(showLogFolderItem);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		
		if(PrefHelper.getBoolean("phon.debug", false)) {
			final JMenu bufferMenu = builder.addMenu(".@Logs", "Buffer");
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
		
	}

	public void loadLog(File logFile) {
		String bufferName = logFile.getName();
		
		if(bufferName.endsWith(".html")) {
			bufferName = bufferName.substring(0, bufferName.length()-5);
		} else if(bufferName.endsWith(".html.gz")) {
			bufferName = bufferName.substring(0, bufferName.length()-8);
		}
		if(bufferPanel.getBuffer(bufferName) != null) {
			bufferPanel.selectBuffer(bufferName);
			return;
		}

		if(!logFile.exists()) return;
		final BufferPanel buffer = bufferPanel.createBuffer(bufferName, true);
		if(logFile.getName().endsWith(".html")) {
			buffer.showHtml(false);
			// wait for load of about:blank before attempting to load another url
			buffer.addBrowserLoadHandler(new CefLoadHandlerAdapter() {
				@Override
				public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
					if(!isLoading) {
						SwingUtilities.invokeLater(() -> {
							buffer.removeBrowserLoadHandler(this);
							buffer.getBrowser().loadURL(logFile.toURI().toString());
						});
					}
				}
			});
		} else if(logFile.getName().endsWith(".html.gz")) {
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
