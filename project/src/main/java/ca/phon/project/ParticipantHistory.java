package ca.phon.project;

import java.io.IOException;
import java.time.Period;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;

/**
 * Runtime extension for Participant objects.  This
 * extension attaches a history of the Participant's
 * age over a number of given sessions.
 *
 */
public class ParticipantHistory {
	
	private final static Logger LOGGER = Logger.getLogger(ParticipantHistory.class.getName());
	
	private Map<SessionPath, Period> history;
	
	public static ParticipantHistory calculateHistoryForParticpant(Project project, Collection<SessionPath> sessions, Participant speaker) {
		final ParticipantHistory history = new ParticipantHistory();
		
		for(SessionPath sessionPath:sessions) {
			try {
				Session session = project.openSession(sessionPath.getCorpus(), sessionPath.getSession());
				// find particpiant from session
				Participant participant = null;
				
				for(Participant p:session.getParticipants()) {
					if(p.getId().equals(speaker.getId())
							&& p.getName().equals(speaker.getName())
							&& p.getRole().equals(speaker.getRole())) {
						participant = p;
						break;
					}
				}
				
				if(participant != null) {
					Period age = participant.getAge(session.getDate());
					history.setAgeForSession(sessionPath, age);
				}
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
		
		return history;
	}
	
	public ParticipantHistory() {
		super();
	
		history = new LinkedHashMap<>();
	}
	
	public Set<SessionPath> getSessions() {
		return history.keySet();
	}

	public Period getAgeForSession(SessionPath sessionPath) {
		return history.get(sessionPath);
	}

	public void setAgeForSession(SessionPath sessionPath, Period age) {
		history.put(sessionPath, age);
	}
	
}
