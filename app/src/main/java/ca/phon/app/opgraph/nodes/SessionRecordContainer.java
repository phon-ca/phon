package ca.phon.app.opgraph.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ca.phon.session.Session;

public class SessionRecordContainer implements RecordContainer {
	
	private Session session;
	
	public SessionRecordContainer(Session session) {
		super();
		this.session = session;
	}

	@Override
	public Session getSession() {
		return this.session;
	}

	@Override
	public Iterator<Integer> idxIterator() {
		List<Integer> idxList = new ArrayList<>();
		for(int i = 0; i < session.getRecordCount(); i++) {
			idxList.add(i);
		}
		return idxList.iterator();
	}

}
