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
	
	static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(RecordContainer.class.getName());
	
	public static List<RecordContainer> toRecordContainers(Project project, Object obj) {
		List<RecordContainer> retVal = new ArrayList<>();
		if(obj instanceof SessionPath) {
			SessionPath sessionLoc = (SessionPath)obj;
			try {
				Session session = project.openSession(sessionLoc.getCorpus(), sessionLoc.getSession());
				retVal.add(new SessionRecordContainer(session));
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
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
				LOGGER.error( e.getLocalizedMessage(), e);
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
