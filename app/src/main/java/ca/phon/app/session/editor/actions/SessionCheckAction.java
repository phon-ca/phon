package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferWindow;
import ca.phon.app.log.LogBuffer;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;
import ca.phon.session.check.SessionValidator;
import ca.phon.worker.PhonWorker;

public class SessionCheckAction extends SessionEditorAction {
	
	private static final Logger LOGGER = Logger.getLogger(SessionCheckAction.class.getName());

	private static final long serialVersionUID = -6586378603389699163L;
	
	public static final String TXT = "Check Session";
	
	public static final String DESC = "Perform session checks.";

	public SessionCheckAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final BufferWindow bufferWindow = BufferWindow.getInstance();
		bufferWindow.showWindow();
		
		final BufferPanel bufferPanel = bufferWindow.createBuffer(
				session.getCorpus() + "." + session.getName() + ":" + TXT);
		bufferPanel.getLogBuffer().setText("");
	
		final String[] cols = new String[] { "Session", "Record #", "Group", "Tier", "Message" };
		
		final BufferedWriter out = 
				new BufferedWriter(new OutputStreamWriter(bufferPanel.getLogBuffer().getStdOutStream()));
		final CSVWriter csvWriter = new CSVWriter(out);
		csvWriter.writeNext(cols);
		
		try {
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
		
		final Runnable inBg = () -> {
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
		};
		PhonWorker.getInstance().invokeLater(inBg);
	}

}
