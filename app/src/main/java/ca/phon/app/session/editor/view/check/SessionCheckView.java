/*
 * Copyright (C) 2012-2019 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor.view.check;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.event.*;

import au.com.bytecode.opencsv.*;
import ca.phon.app.log.*;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.check.actions.*;
import ca.phon.session.check.*;
import ca.phon.util.icons.*;

public class SessionCheckView extends EditorView {
	
	public final static String VIEW_NAME = "Session Check";
	
	public final static String ICON_NAME = "emblems/flag-red";
	
	private JButton refreshButton;
	
//	final String[] showOptions = { "Warnings for session", "Warnings for current record" };
//	private JComboBox<String> showBox;
	
	private BufferPanel bufferPanel;
	
	public SessionCheckView(SessionEditor editor) {
		super(editor);
		
		init();
		refresh();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		
//		showBox = new JComboBox<>(showOptions);
//		showBox.addItemListener( (e) -> {
//			if(e.getStateChange() == ItemEvent.SELECTED) {
//				refresh();
//			}
//		});
//		
//		toolBar.add(new JLabel("Show:"));
//		toolBar.add(showBox);
//		toolBar.addSeparator();
		
		refreshButton = new JButton(new SessionCheckRefreshAction(this));
		toolBar.add(refreshButton);
		
		bufferPanel = new BufferPanel(VIEW_NAME);
		
		add(toolBar, BorderLayout.NORTH);
		add(bufferPanel, BorderLayout.CENTER);
	}

	public void refresh() {
		bufferPanel.clear();
		
		SessionCheckWorker worker = new SessionCheckWorker();
		worker.execute();
	}
	
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon(ICON_NAME, IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		JMenu retVal = new JMenu();
		
		retVal.add(new SessionCheckRefreshAction(this));
		
		return retVal;
	}
	
	public void setupStatusBar(List<ValidationEvent> events) {
		final SessionEditorStatusBar statusBar = getEditor().getStatusBar();
		
		JLabel lbl = null;
		for(Component comp:statusBar.getExtrasPanel().getComponents()) {
			if(comp.getName() != null && comp.getName().equals(VIEW_NAME)) {
				lbl = (JLabel)comp;
				break;
			}
		}
		
		if(lbl == null) {
			lbl = new JLabel();
			lbl.setName(VIEW_NAME);
			final ImageIcon icn = IconManager.getInstance().getIcon(ICON_NAME, IconSize.XSMALL);
			lbl.setIcon(icn);
			if(events.size() > 0) {
				lbl.setText("<html><u>" +
						events.size() + " warning" + 
						(events.size() == 1 ? "" : "s") + "</u></html>");
				lbl.setToolTipText("Show warnings");
				lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				lbl.addMouseListener(new MouseInputAdapter() {
	
					@Override
					public void mouseClicked(MouseEvent e) {
						getEditor().getViewModel().showView(VIEW_NAME);
					}
	
				});
				statusBar.getExtrasPanel().add(lbl);
			}
		} else {
			if(events.size() == 0)
				statusBar.getExtrasPanel().remove(lbl);
			else {
				lbl.setText("<html><u>" +
						events.size() + " warning" + 
						(events.size() == 1 ? "" : "s") + "</u></html>");
			}
		}
		statusBar.revalidate();
	}
	
	private class SessionCheckWorker extends SwingWorker<List<ValidationEvent>, ValidationEvent> {

		private OutputStreamWriter out;
		private CSVWriter writer;
		
		final String[] cols = new String[] { "Session", "Record #", "Group", "Tier", "Message" };
		
		public SessionCheckWorker() {
			try {
				out = new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream(), "UTF-8");
				
				out.flush();
				out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
				out.flush();

				writer = new CSVWriter(out);
				writer.writeNext(cols);
				writer.flush();
			} catch (IOException e) {
				LogUtil.warning(e);
			}
		}
		
		@Override
		protected List<ValidationEvent> doInBackground() throws Exception {
			List<ValidationEvent> events = new ArrayList<>();
			
			SessionValidator validator = new SessionValidator();
			validator.putExtension(SessionEditor.class, getEditor());
			validator.addValidationListener( (e) -> {
				events.add(e);
				publish(e);
			});
					
			validator.validate(getEditor().getSession());
			
			return events;
		}

		@Override
		protected void process(List<ValidationEvent> chunks) {
			String[] row = new String[cols.length];
			for(var ve:chunks) {
				row[0] = ve.getSession().getCorpus() + "." + ve.getSession().getName();
				row[1] = "" + (ve.getRecord()+1);
				row[2] = "" + (ve.getGroup()+1);
				row[3] = ve.getTierName();
				row[4] = ve.getMessage();
				
				writer.writeNext(row);
				try {
					writer.flush();
				} catch (IOException e) {
					LogUtil.warning(e);
				}
			}
		}

		@Override
		protected void done() {
			try {
				out.flush();
				out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
				out.flush();
				out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
				out.flush();
				out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.PACK_TABLE_COLUMNS);
				out.flush();
			} catch (IOException e) {
				LogUtil.severe( e.getLocalizedMessage(), e);
			} finally {
				try {
					writer.close(); 
					out.close();
				} catch (IOException e) {
					LogUtil.warning( e.getLocalizedMessage(), e);
				}
			}
			
			try {
				setupStatusBar(get());
			} catch (InterruptedException | ExecutionException e) {
				LogUtil.warning(e);
			}
		}
		
	}

}
