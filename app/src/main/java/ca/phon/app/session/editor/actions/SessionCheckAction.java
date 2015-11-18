package ca.phon.app.session.editor.actions;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferWindow;
import ca.phon.app.log.LogBuffer;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionEditorStatusBar;
import ca.phon.session.Session;
import ca.phon.session.check.SessionValidator;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

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
		
		final BufferPanel bufferPanel = new BufferPanel(
				session.getCorpus() + "." + session.getName() + ":" + TXT);
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
		
		final ImageIcon icn = IconManager.getInstance().getIcon("emblems/flag-red", IconSize.XSMALL);
		
		final JLabel lbl = new JLabel();
		lbl.setForeground(Color.orange);
		lbl.setIcon(icn);
		if(validator.getValidationEvents().size() > 0) {
			lbl.setText("<html><u>" +
					validator.getValidationEvents().size() + " warnings</u></html>");
			lbl.setToolTipText("Show warnings");
			lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			lbl.addMouseListener(new MouseInputAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					showBuffer(bufferPanel);
				}

			});
			statusBar.getExtrasPanel().add(lbl);
		} else {
			statusBar.getExtrasPanel().removeAll();
		}
	}
	
	public void showBuffer(BufferPanel bufferPanel) {
		getEditor().getViewModel().showDynamicFloatingDockable(bufferPanel.getBufferName(), bufferPanel, 0, 0, 500, 600);
	}

}
