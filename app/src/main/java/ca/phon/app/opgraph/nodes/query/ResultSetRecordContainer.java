package ca.phon.app.opgraph.nodes.query;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ca.phon.app.opgraph.nodes.RecordContainer;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultSet;
import ca.phon.session.Session;

public class ResultSetRecordContainer implements RecordContainer {

	private Session session;
	
	private ResultSet resultSet;

	public ResultSetRecordContainer(Session session, ResultSet resultSet) {
		super();
		
		this.session = session;
		this.resultSet = resultSet;
	}

	@Override
	public Session getSession() {
		return this.session;
	}

	@Override
	public Iterator<Integer> idxIterator() {
		Set<Integer> idxSet = new LinkedHashSet<>();
		for(Result result:this.resultSet) {
			idxSet.add(result.getRecordIndex());
		}
		return idxSet.iterator();
	}

}
