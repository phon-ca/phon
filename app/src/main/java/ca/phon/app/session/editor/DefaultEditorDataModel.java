package ca.phon.app.session.editor;

import ca.phon.session.Record;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.velocity.runtime.parser.node.GetExecutor;

/**
 * Default data model for the {@link SessionEditor}
 * 
 */
public class DefaultEditorDataModel implements EditorDataModel {

	/**
	 * Reference to session
	 */
	private final WeakReference<Session> sessionRef;
	
	/**
	 * Transcriber
	 */
	private AtomicReference<Transcriber> transcriberRef = new AtomicReference<>();
	
	/**
	 * Constructor
	 */
	public DefaultEditorDataModel(Session session) {
		super();
		this.sessionRef = new WeakReference<>(session);
	}

	@Override
	public Session getSession() {
		return sessionRef.get();
	}

	@Override
	public int getRecordCount() {
		int retVal = 0;
		
		final Session session = getSession();
		if(session != null) retVal = session.getRecordCount();
		
		return retVal;
	}

	@Override
	public Record getRecord(int idx) {
		Record retVal = null;
		
		final Session session = getSession();
		if(session != null) retVal = session.getRecord(idx);
		
		return retVal;
	}

	@Override
	public int getNextRecordIndex(int idx) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPrevRecordIndex(int idx) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RecordFilter getRecordFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRecordFilter(RecordFilter filter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Transcriber getTranscriber() {
		return transcriberRef.get();
	}

	@Override
	public void setTranscriber(Transcriber transcriber) {
		transcriberRef.getAndSet(transcriber);
	}
	
}
