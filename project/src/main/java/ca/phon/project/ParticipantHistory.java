/*
 * 
 */
package ca.phon.project;

import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import ca.phon.session.SessionPath;

/**
 * Runtime extension for Participant objects.  This
 * extension attaches a ageHistory of the Participant's
 * age over a number of given sessions.
 *
 */
public class ParticipantHistory {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ParticipantHistory.class.getName());
	
	// age ageHistory for participant
	private Map<SessionPath, Period> ageHistory;
	
	// number of records for participant
	private Map<SessionPath, Integer> recordHistory; 
	
//	public static ParticipantHistory calculateHistoryForParticpant(Project project, Collection<SessionPath> sessions, Participant speaker) {
//		final ParticipantHistory history = new ParticipantHistory();
//		
//		for(SessionPath sessionPath:sessions) {
//			try {
//				Session session = project.openSession(sessionPath.getCorpus(), sessionPath.getSession());
//				// find particpiant from session
//				Participant participant = null;
//				
//				for(Participant p:session.getParticipants()) {
//					if(p.getId().equals(speaker.getId())
//							&& p.getName().equals(speaker.getName())
//							&& p.getRole().equals(speaker.getRole())) {
//						participant = p;
//						break;
//					}
//				}
//				
//				// get record count
//				int count = 0;
//				for(Record r:session.getRecords()) {
//					if(r.getSpeaker() == speaker) ++count;
//				}
//				
//				if(participant != null || count > 0) {
//					Period age = 
//							(participant != null ? participant.getAge(session.getDate()) : null);
//					history.setAgeForSession(sessionPath, age);
//					history.setNumberOfRecordsForSession(sessionPath, count);
//				}
//			} catch (IOException e) {
//				LOGGER.warn( e.getLocalizedMessage(), e);
//			}
//		}
//		
//		return history;
//	}
	
	public ParticipantHistory() {
		super();
	
		ageHistory = new LinkedHashMap<>();
		recordHistory = new LinkedHashMap<>();
	}
	
	public Set<SessionPath> getSessions() {
		return ageHistory.keySet();
	}

	public Period getAgeForSession(SessionPath sessionPath) {
		return ageHistory.get(sessionPath);
	}

	public void setAgeForSession(SessionPath sessionPath, Period age) {
		ageHistory.put(sessionPath, age);
	}
	
	public Integer getNumberOfRecordsForSession(SessionPath sessionPath) {
		return recordHistory.get(sessionPath);
	}
	
	public void setNumberOfRecordsForSession(SessionPath sessionPath, int count) {
		recordHistory.put(sessionPath, count);
	}
	
}
