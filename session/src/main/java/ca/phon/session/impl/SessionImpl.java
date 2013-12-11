package ca.phon.session.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.DateTime;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.session.Participant;
import ca.phon.session.Participants;
import ca.phon.session.Record;
import ca.phon.session.Records;
import ca.phon.session.Session;
import ca.phon.session.SessionMetadata;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierDescription;
import ca.phon.session.TierDescriptions;
import ca.phon.session.TierViewItem;
import ca.phon.session.Transcriber;
import ca.phon.session.Transcribers;

public class SessionImpl implements Session {
	
	/*
	 * Properties
	 */
	private final AtomicReference<String> corpusRef = new AtomicReference<String>();
	
	private final AtomicReference<String> nameRef = new AtomicReference<String>();
	
	private final AtomicReference<DateTime> dateRef = new AtomicReference<DateTime>();
	
	private final AtomicReference<String> langRef = new AtomicReference<String>();
	
	private final AtomicReference<String> mediaRef = new AtomicReference<String>();
	
	private final SessionMetadata metadata;
	
	private final List<Participant> participants =
			Collections.synchronizedList(new ArrayList<Participant>());
	
	private final List<Transcriber> transcribers =
			Collections.synchronizedList(new ArrayList<Transcriber>());
	
	private final List<TierViewItem> tierOrder =
			Collections.synchronizedList(new ArrayList<TierViewItem>());
	
	private final List<TierDescription> userTiers =
			Collections.synchronizedList(new ArrayList<TierDescription>());
	
	private final List<Record> records =
			Collections.synchronizedList(new ArrayList<Record>());
	
	/*
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(Session.class, this);
	
	SessionImpl() {
		super();
		metadata = new SessionMetadataImpl();
		extSupport.initExtensions();
	}

	@Override
	public String getCorpus() {
		return corpusRef.get();
	}

	@Override
	public String getName() {
		return nameRef.get();
	}

	@Override
	public DateTime getDate() {
		return dateRef.get();
	}

	@Override
	public String getLanguage() {
		return langRef.get();
	}

	@Override
	public String getMediaLocation() {
		return (mediaRef.get() != null ? mediaRef.get() : "");
	}

	@Override
	public List<TierViewItem> getTierView() {
		return Collections.unmodifiableList(this.tierOrder);
	}

	@Override
	public Transcriber getTranscriber(String username) {
		Transcriber retVal = null;
		synchronized(transcribers) {
			for(Transcriber transcriber:this.transcribers) {
				final String tName = transcriber.getUsername();
				if(tName.equals(username)) {
					retVal = transcriber;
					break;
				}
			}
		}
		return retVal;
	}
	
	@Override
	public Transcriber getTranscriber(int i) {
		return transcribers.get(i);
	}
	
	@Override
	public Transcribers getTranscribers() {
		return new TranscribersImpl(this);
	}
	
	@Override
	public void removeTranscriber(int i) {
		transcribers.remove(i);
	}
	
	@Override
	public SessionMetadata getMetadata() {
		return metadata;
	}

	@Override
	public Record getRecord(int pos) {
		return records.get(pos);
	}

	@Override
	public int getRecordCount() {
		return records.size();
	}

	@Override
	public Records getRecords() {
		return new RecordsImpl(this);
	}
	
	@Override
	public int getRecordPosition(Record record) {
		return records.indexOf(record);
	}

	@Override
	public int getParticipantCount() {
		return participants.size();
	}

	@Override
	public void addParticipant(Participant participant) {
		participants.add(participant);
	}

	@Override
	public Participant getParticipant(int idx) {
		return participants.get(idx);
	}
	
	@Override
	public Participants getParticipants() {
		return new ParticipantsImpl(this);
	}

	@Override
	public void setCorpus(String corpus) {
		corpusRef.getAndSet(corpus);
	}

	@Override
	public void setName(String name) {
		nameRef.getAndSet(name);
	}

	@Override
	public void setDate(DateTime date) {
		dateRef.getAndSet(date);
	}

	@Override
	public void setLanguage(String language) {
		langRef.getAndSet(language);
	}

	@Override
	public void setMediaLocation(String mediaLocation) {
		mediaRef.getAndSet(mediaLocation);
	}

	@Override
	public void setTierView(List<TierViewItem> view) {
		tierOrder.clear();
		tierOrder.addAll(view);
//		for(TierViewItem item:view) {
//			// Segment tier has been deprecated, make sure it never gets into our view ordering
//			if(!SystemTierType.Segment.getName().equals(item.getTierName())) {
//				tierOrder.add(item);
//			}
//		}
	}

	@Override
	public void addTranscriber(Transcriber t) {
		transcribers.add(t);
	}

	@Override
	public void removeTranscriber(Transcriber t) {
		transcribers.remove(t);
	}

	@Override
	public void removeTranscriber(String username) {
		final Transcriber t = getTranscriber(username);
		if(t != null) {
			transcribers.remove(t);
		}
	}

	@Override
	public void addRecord(Record record) {
		records.add(record);
	}

	@Override
	public void addRecord(int pos, Record record) {
		records.add(pos, record);
	}

	@Override
	public void removeRecord(Record record) {
		records.remove(record);
	}

	@Override
	public void removeRecord(int pos) {
		records.remove(pos);
	}

	@Override
	public void removeParticipant(Participant participant) {
		participants.remove(participant);
	}

	@Override
	public void removeParticipant(int idx) {
		participants.remove(idx);
	}
	
	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	@Override
	public int getUserTierCount() {
		return userTiers.size();
	}

	@Override
	public TierDescription getUserTier(int idx) {
		return userTiers.get(idx);
	}

	@Override
	public TierDescription removeUserTier(int idx) {
		return userTiers.remove(idx);
	}

	@Override
	public TierDescription removeUserTier(TierDescription tierDescription) {
		userTiers.remove(tierDescription);
		return tierDescription;
	}

	@Override
	public void addUserTier(TierDescription tierDescription) {
		userTiers.add(tierDescription);
	}

	@Override
	public void addUserTier(int idx, TierDescription tierDescription) {
		userTiers.add(idx, tierDescription);
	}
	
	@Override
	public TierDescriptions getUserTiers() {
		return new TierDescriptionsImpl(this);
	}

	@Override
	public int getTranscriberCount() {
		return transcribers.size();
	}
}
