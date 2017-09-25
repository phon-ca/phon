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
package ca.phon.app.session.editor.actions;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.app.log.*;
import ca.phon.app.session.editor.*;
import ca.phon.session.Session;
import ca.phon.session.check.SessionValidator;
import ca.phon.util.icons.*;
import ca.phon.worker.*;

public class SessionCheckAction extends SessionEditorAction {
	
	private static final Logger LOGGER = Logger.getLogger(SessionCheckAction.class.getName());

	private static final long serialVersionUID = -6586378603389699163L;
	
	public static final String TXT = "Check Session";
	
	public static final String DESC = "Perform session checks.";
	
	// if silent, a message is shown on the status bar showing the number
	// of validation events detected
	private boolean silent = false;

	public SessionCheckAction(SessionEditor editor) {
		this(editor, false);
	}
	
	public SessionCheckAction(SessionEditor editor, boolean silent) {
		super(editor);
		
		this.silent = silent;
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final JComponent checkView = editor.getViewModel().getDynamicView(TXT);
		
		final BufferPanel bufferPanel = (checkView != null ? 
				(BufferPanel)checkView : new BufferPanel(TXT));
		bufferPanel.getLogBuffer().setText("");
	
		final String[] cols = new String[] { "Session", "Record #", "Group", "Tier", "Message" };
		
		final BufferedWriter out = 
				new BufferedWriter(new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream()));
		final CSVWriter csvWriter = new CSVWriter(out);
		csvWriter.writeNext(cols);
		
		try {
			out.flush();
			out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUFFER_CODE);
			out.flush();
			out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
			out.flush();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		
		final String[] row = new String[cols.length];
		final SessionValidator validator = new SessionValidator();
		validator.putExtension(SessionEditor.class, getEditor());
		validator.addValidationListener( (e) -> {
			Arrays.setAll(row, (i) -> { return new String(); } );
			
			row[0] = e.getSession().getCorpus() + "." + e.getSession().getName();
			row[1] = "" + (e.getRecord()+1);
			row[2] = "" + (e.getGroup()+1);
			row[3] = e.getTierName();
			row[4] = e.getMessage();
			
			csvWriter.writeNext(row);
			try {
				csvWriter.flush();
			} catch (Exception e1) {
				LOGGER.log(Level.WARNING, e1.getLocalizedMessage(), e1);
			}
		});
		
		if(!silent) {
			showBuffer(bufferPanel);
		}
		final PhonTask inBg = new PhonTask() {

			@Override
			public void performTask() {
				setStatus(TaskStatus.RUNNING);
				
				validator.validate(session);
				try {
					out.flush();
					out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
					out.flush();
					out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
					out.flush();
					out.write(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.PACK_TABLE_COLUMNS);
					out.flush();
					
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				} finally {
					try {
						csvWriter.close(); 
						out.close();
					} catch (IOException e) {
						LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
					}
				}
				
				// setup status bar message
				setupStatusBar(validator, bufferPanel);
				
				setStatus(TaskStatus.FINISHED);
			}
			
		};
		getEditor().getStatusBar().watchTask(inBg);
		PhonWorker.getInstance().invokeLater(inBg);
	}
	
	public void setupStatusBar(SessionValidator validator, BufferPanel bufferPanel) {
		final SessionEditorStatusBar statusBar = getEditor().getStatusBar();
		
		JLabel lbl = null;
		for(Component comp:statusBar.getExtrasPanel().getComponents()) {
			if(comp.getName() != null && comp.getName().equals(TXT)) {
				lbl = (JLabel)comp;
				break;
			}
		}
		
		if(lbl == null) {
			lbl = new JLabel();
			lbl.setName(TXT);
			final ImageIcon icn = IconManager.getInstance().getIcon("emblems/flag-red", IconSize.XSMALL);
			lbl.setIcon(icn);
			if(validator.getValidationEvents().size() > 0) {
				lbl.setText("<html><u>" +
						validator.getValidationEvents().size() + " warning" + 
						(validator.getValidationEvents().size() == 1 ? "" : "s") + "</u></html>");
				lbl.setToolTipText("Show warnings");
				lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				lbl.addMouseListener(new MouseInputAdapter() {
	
					@Override
					public void mouseClicked(MouseEvent e) {
						showBuffer(bufferPanel);
					}
	
				});
				statusBar.getExtrasPanel().add(lbl);
			}
		} else {
			if(validator.getValidationEvents().size() == 0)
				statusBar.getExtrasPanel().remove(lbl);
			else {
				lbl.setText("<html><u>" +
						validator.getValidationEvents().size() + " warning" + 
						(validator.getValidationEvents().size() == 1 ? "" : "s") + "</u></html>");
			}
		}
	}
	
	public void showBuffer(BufferPanel bufferPanel) {
		if(getEditor().getViewModel().isShowing(TXT)) return;
		Point lblLocation = getEditor().getStatusBar().getExtrasPanel().getLocationOnScreen();
		getEditor().getViewModel().showDynamicFloatingDockable(bufferPanel.getBufferName(), bufferPanel,
				lblLocation.x, lblLocation.y - 400, 500, 400);
	}

}
