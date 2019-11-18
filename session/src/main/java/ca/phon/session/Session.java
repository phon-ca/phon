package ca.phon.session;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import ca.phon.extensions.ExtendableObject;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.session.spi.SessionSPI;

/**
 * A session in a project.
 *
 */
public final class Session extends ExtendableObject {

	private SessionSPI sessionImpl;
	
	Session(SessionSPI sessionImpl) {
		super();
		this.sessionImpl = sessionImpl;
	}
	
	/* Delegates */
	/**
	 * @return corpus name
	 */
	public String getCorpus() {
		return sessionImpl.getCorpus();
	}

	/**
	 * @return session name
	 */
	public String getName() {
		return sessionImpl.getName();
	}

	/**
	 * @return session recording date
	 */
	public LocalDate getDate() {
		return sessionImpl.getDate();
	}

	public String getLanguage() {
		return sessionImpl.getLanguage();
	}

	public String getMediaLocation() {
		return sessionImpl.getMediaLocation();
	}

	public List<TierViewItem> getTierView() {
		return sessionImpl.getTierView();
	}


	public int getUserTierCount() {
		return sessionImpl.getUserTierCount();
	}

	public TierDescription getUserTier(int idx) {
		return sessionImpl.getUserTier(idx);
	}

	public TierDescription removeUserTier(int idx) {
		return sessionImpl.removeUserTier(idx);
	}

	public TierDescription removeUserTier(TierDescription tierDescription) {
		return sessionImpl.removeUserTier(tierDescription);
	}

	public void addUserTier(TierDescription tierDescription) {
		sessionImpl.addUserTier(tierDescription);
	}

	public void addUserTier(int idx, TierDescription tierDescription) {
		sessionImpl.addUserTier(idx, tierDescription);
	}

	public TierDescriptions getUserTiers() {
		return new TierDescriptions(this);
	}

	public int getTranscriberCount() {
		return sessionImpl.getTranscriberCount();
	}

	public Transcriber getTranscriber(String username) {
		return sessionImpl.getTranscriber(username);
	}

	public Transcriber getTranscriber(int i) {
		return sessionImpl.getTranscriber(i);
	}

	public void removeTranscriber(int i) {
		sessionImpl.removeTranscriber(i);
	}

	public Transcribers getTranscribers() {
		return new Transcribers(this);
	}

	public SessionMetadata getMetadata() {
		return sessionImpl.getMetadata();
	}

	public Record getRecord(int pos) {
		return sessionImpl.getRecord(pos);
	}

	public int getRecordCount() {
		return sessionImpl.getRecordCount();
	}

	public Records getRecords() {
		return new Records(this);
	}

	public int getRecordPosition(Record record) {
		return sessionImpl.getRecordPosition(record);
	}

	public void setRecordPosition(Record record, int position) {
		sessionImpl.setRecordPosition(record, position);
	}

	public int getParticipantCount() {
		return sessionImpl.getParticipantCount();
	}

	public void addParticipant(Participant participant) {
		sessionImpl.addParticipant(participant);
	}

	public Participant getParticipant(int idx) {
		return sessionImpl.getParticipant(idx);
	}

	public Participants getParticipants() {
		return new Participants(this);
	}

	public void setCorpus(String corpus) {
		sessionImpl.setCorpus(corpus);
	}

	public void setName(String name) {
		sessionImpl.setName(name);
	}

	public void setDate(LocalDate date) {
		sessionImpl.setDate(date);
	}

	public void setLanguage(String language) {
		sessionImpl.setLanguage(language);
	}

	public void setMediaLocation(String mediaLocation) {
		sessionImpl.setMediaLocation(mediaLocation);
	}

	public void setTierView(List<TierViewItem> view) {
		sessionImpl.setTierView(view);
	}

	public void addTranscriber(Transcriber t) {
		sessionImpl.addTranscriber(t);
	}

	public void removeTranscriber(Transcriber t) {
		sessionImpl.removeTranscriber(t);
	}

	public void removeTranscriber(String username) {
		sessionImpl.removeTranscriber(username);
	}

	public void addRecord(Record record) {
		sessionImpl.addRecord(record);
	}

	public void addRecord(int pos, Record record) {
		sessionImpl.addRecord(pos, record);
	}

	public void removeRecord(Record record) {
		sessionImpl.removeRecord(record);
	}

	public void removeRecord(int pos) {
		sessionImpl.removeRecord(pos);
	}

	public void removeParticipant(Participant participant) {
		sessionImpl.removeParticipant(participant);
	}

	public void removeParticipant(int idx) {
		sessionImpl.removeParticipant(idx);
	}
	
}
