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
package ca.phon.app.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SessionPath;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.worker.PhonTask;

/**
 * Handle merging of sessions
 *
 */
public class SessionMerger extends PhonTask {

	private final static Logger LOGGER = Logger.getLogger(SessionMerger.class.getName());

	private final Project project;

	private Set<SessionPath> sessions;

	private Set<Participant> participants;

	private Map<SessionPath, RecordFilter> filterMap;

	private Session mergedSession;

	final Comparator<Participant> participantComparator = (p1, p2) -> {
		int retVal = p1.getRole().compareTo(p2.getRole());
		if(retVal == 0) {
			final String p1Name = (p1.getName() == null ? "" : p1.getName());
			final String p2Name = (p2.getName() == null ? "" : p2.getName());
			retVal = p1Name.compareTo(p2Name);
		}
		return retVal;
	};

	public SessionMerger(Project project) {
		super();

		this.project = project;

		this.sessions = new LinkedHashSet<>();
		this.participants = new LinkedHashSet<>();

		mergedSession = SessionFactory.newFactory().createSession();

		this.filterMap = new TreeMap<>( (p1, p2) -> p1.toString().compareTo(p2.toString()) );
	}

	public Session getMergedSession() {
		return this.mergedSession;
	}

	public void setMergedSession(Session session) {
		this.mergedSession = session;
	}

	public Set<SessionPath> getSessionPaths() {
		return this.sessions;
	}

	public void addSessionPath(SessionPath sp) {
		this.sessions.add(sp);
	}

	public Set<Participant> getParticipants() {
		return this.participants;
	}

	public void addParticipant(Participant participant) {
		// assign ID by role
		String id = participant.getRole().getId();
		int idx = 0;
		// look at other participants, see if we need to modify id
		for(Participant otherP:this.participants) {
			if(otherP == participant) continue;
			if(otherP.getId().equals(id)) {
				id = participant.getRole().getId().substring(0, 2) + (++idx);
			}
		}
		participant.setId(id);

		this.participants.add(participant);
	}

	public void setRecordFilter(SessionPath path, RecordFilter filter) {
		filterMap.put(path, filter);
	}

	public RecordFilter getRecordFilter(SessionPath path) {
		return filterMap.get(path);
	}

	public Session mergeSessions() throws IOException {
		for(SessionPath sessionPath:getSessionPaths()) {
			final Session session = project.openSession(sessionPath.getCorpus(), sessionPath.getSession());
			mergeSession(getMergedSession(), session, getRecordFilter(sessionPath));
		}

		return getMergedSession();
	}

	/**
	 * Merge the given sessions using the given
	 * utterance filters.
	 *
	 * @param dest
	 * @param src
	 * @param filter
	 */
	public void mergeSession(Session dest, Session src,
			RecordFilter filter) {
		mergeDependentTiers(dest, src);

		// add selected records
		addRecordsFromSession(dest, src, filter);
	}

	/**
	 * Merge dependent tiers.
	 *
	 */
	public void mergeDependentTiers(Session dest, Session src) {
		final SessionFactory factory = SessionFactory.newFactory();
		for(int i = 0; i < src.getUserTierCount(); i++) {
			final TierDescription tierDesc = src.getUserTier(i);
			// try to find first
			TierDescription newDesc = null;
			for(int j = 0; j < dest.getUserTierCount(); j++) {
				final TierDescription td = dest.getUserTier(j);
				if(td.getName().equals(tierDesc.getName())) {
					newDesc = td;
					break;
				}
			}

			if(newDesc == null) {
				// add new dep tier
				newDesc = factory.createTierDescription(tierDesc.getName(), tierDesc.isGrouped());
				dest.addUserTier(newDesc);
				
				final TierViewItem tvi = factory.createTierViewItem(tierDesc.getName(), true);
				final List<TierViewItem> tierView = new ArrayList<>(dest.getTierView());
				tierView.add(tvi);
				dest.setTierView(tierView);
			}
		}
	}

	/**
	 * Add filtered utterances to new transcript
	 *
	 * @param dest
	 * @param src
	 * @param filter
	 */
	private void addRecordsFromSession(Session dest,
			Session src, RecordFilter filter) {
		for(int i = 0; i < src.getRecordCount(); i++) {
			final Record r = src.getRecord(i);
			if(!filter.checkRecord(r)) continue;
			final Participant speaker = r.getSpeaker();
			Participant sessionParticipant = null;
			for(Participant p:participants) {
				if(participantComparator.compare(speaker, p) == 0) {
					sessionParticipant = p;
					break;
				}
			}
			if(sessionParticipant != null) {
				r.setSpeaker(sessionParticipant);
				dest.addRecord(r);
			}
		}
	}

	@Override
	public void performTask() {
		setStatus(TaskStatus.RUNNING);

		try {
			mergeSessions();
			setStatus(TaskStatus.FINISHED);
		} catch (IOException e) {
			super.err = e;
			setStatus(TaskStatus.ERROR);
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}
