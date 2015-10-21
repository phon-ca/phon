package ca.phon.app.opgraph.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.opgraph.nodes.query.ResultSetRecordContainer;
import ca.phon.project.Project;
import ca.phon.query.db.ResultSet;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;

public interface RecordContainer {
	
	static Logger LOGGER = Logger.getLogger(RecordContainer.class.getName());
	
	public static List<RecordContainer> toRecordContainers(Project project, Object obj) {
		List<RecordContainer> retVal = new ArrayList<>();
		if(obj instanceof SessionPath) {
			SessionPath sessionLoc = (SessionPath)obj;
			try {
				Session session = project.openSession(sessionLoc.getCorpus(), sessionLoc.getSession());
				retVal.add(new SessionRecordContainer(session));
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		} else if(obj instanceof SessionPath[]) {
			SessionPath[] paths = (SessionPath[])obj;
			for(SessionPath path:paths) retVal.addAll(toRecordContainers(project, path));
		} else if(obj instanceof Session) {
			retVal.add(new SessionRecordContainer((Session)obj));
		} else if(obj instanceof Session[]) {
			Session[] sessions = (Session[])obj;
			for(Session session:sessions) retVal.add(new SessionRecordContainer(session));
		} else if(obj instanceof ResultSet) {
			ResultSet rs = (ResultSet)obj;
			try {
				Session session = project.openSession(rs.getCorpus(), rs.getSession());
				retVal.add(new ResultSetRecordContainer(session, rs));
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		} else if(obj instanceof ResultSet[]) {
			ResultSet[] resultSets = (ResultSet[])obj;
			for(ResultSet resultSet:resultSets) retVal.addAll(toRecordContainers(project, resultSet));
		} else if(obj instanceof Collection) {
			Collection<?> collection = (Collection<?>)obj;
			for(Object o:collection) retVal.addAll(toRecordContainers(project, o));
		}
		return retVal;
	}

	public Session getSession();
	
	public Iterator<Integer> idxIterator();
	
}
