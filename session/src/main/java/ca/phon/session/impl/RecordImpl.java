package ca.phon.session.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import ca.phon.alignment.PhoneMap;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.Comment;
import ca.phon.session.Group;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;

/**
 * Basic record implementation
 *
 */
public class RecordImpl implements Record {
	
	/* Attributes */
	private final AtomicReference<Participant> participantRef = new AtomicReference<Participant>();
	
	private volatile boolean excludeFromSearches = false;
	
	/* default tiers */
	private final Tier<Orthography> orthography;
	
	private final Tier<IPATranscript> ipaTarget;
	
	private final Tier<IPATranscript> ipaActual;
	
	private final Tier<MediaSegment> segment;
	
	private final Tier<String> notes;
	
	private final Tier<PhoneMap> alignment;
	
	/* Additional tiers */
	private final List<Tier<?>> userDefined;

	RecordImpl() {
		super();
		
		final SessionFactory factory = SessionFactory.newFactory();
		orthography = factory.createTier(Orthography.class);
		ipaTarget = factory.createTier(IPATranscript.class);
		ipaActual = factory.createTier(IPATranscript.class);
		segment = factory.createTier(MediaSegment.class);
		notes = factory.createTier();
		alignment = factory.createTier(PhoneMap.class);
		
		userDefined = 
				Collections.synchronizedList(new ArrayList<Tier<?>>());
		
		extSupport.initExtensions();
	}

	@Override
	public Participant getSpeaker() {
		return participantRef.get();
	}

	@Override
	public void setSpeaker(Participant participant) {
		participantRef.getAndSet(participant);
	}

	@Override
	public MediaSegment getSegment() {
		if(segment.numberOfGroups() > 0) {
			return segment.getGroup(0);
		} else {
			return null;
		}
	}

	@Override
	public void setSegment(MediaSegment media) {
		segment.setGroup(0, media);
	}

	@Override
	public boolean isExcludeFromSearches() {
		return this.excludeFromSearches;
	}

	@Override
	public void setExcludeFromSearches(boolean excluded) {
		this.excludeFromSearches = excluded;
	}

	@Override
	public Tier<Orthography> getOrthography() {
		return orthography;
	}

	@Override
	public void setOrthography(Tier<Orthography> ortho) {
		this.orthography.removeAll();
		for(int i = 0; i < ortho.numberOfGroups(); i++) {
			this.orthography.setGroup(i, ortho.getGroup(i));
		}
	}

	@Override
	public int numberOfGroups() {
		return this.orthography.numberOfGroups();
	}

	@Override
	public Group getGroup(int idx) {
		if(idx >= 0 && idx < numberOfGroups()) {
			return new GroupImpl(this, idx);
		} else {
			throw new IndexOutOfBoundsException("Invalid group index " + idx);
		}
	}

	@Override
	public void removeGroup(int idx) {
		if(idx >= 0 && idx < numberOfGroups()) {
			// TODO remove the specified group from all tiers
		} else {
			throw new IndexOutOfBoundsException("Invalid group index " + idx);
		}
	}

	@Override
	public void mergeGroups(int startIdx, int endIdx) {
		// TODO Auto-generated method stub
	}

	@Override
	public void splitGroup(int groupIdx, int wordIdx) {
		// TODO Auto-generated method stub
	}

	@Override
	public Tier<IPATranscript> getIPATarget() {
		return this.ipaTarget;
	}

	@Override
	public void setIPATarget(Tier<IPATranscript> ipa) {
		this.ipaTarget.removeAll();
		for(int i = 0; i < ipa.numberOfGroups(); i++) {
			this.ipaTarget.setGroup(i, ipa.getGroup(i));
		}
	}

	@Override
	public Tier<IPATranscript> getIPAActual() {
		return this.ipaActual;
	}

	@Override
	public void setIPAActual(Tier<IPATranscript> ipa) {
		this.ipaActual.removeAll();
		for(int i = 0; i < ipa.numberOfGroups(); i++) {
			this.ipaActual.setGroup(i, ipa.getGroup(i));
		}
	}
	
	@Override
	public Tier<PhoneMap> getPhoneAlignment() {
		return this.alignment;
	}
	
	@Override
	public void setPhoneAlignment(Tier<PhoneMap> phoneAlignment) {
		this.alignment.removeAll();
		for(int i = 0; i < phoneAlignment.numberOfGroups(); i++) {
			this.alignment.setGroup(i, phoneAlignment.getGroup(i));
		}
	}

	@Override
	public Tier<String> getNotes() {
		return this.notes;
	}

	@Override
	public void setNotes(Tier<String> notes) {
		this.notes.removeAll();
		if(notes.numberOfGroups() > 0) {
			this.notes.addGroup(notes.getGroup(0));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Tier<T> getTier(String name, Class<T> type) {
		Tier<T> retVal = null;
		
		for(Tier<?> userTier:userDefined) {
			if(userTier.getName().equals(name)
					&& userTier.getDeclaredType() == type) {
				retVal = (Tier<T>)userTier;
				break;
			}
		}
		
		return retVal;
	}

	@Override
	public void removeTier(String name) {
		Tier<?> toRemove = null;
		
		for(Tier<?> userTier:userDefined) {
			if(userTier.getName().equals(name)) {
				toRemove = userTier;
			}
		}
		
		if(toRemove != null) {
			userDefined.remove(toRemove);
		}
	}
	
	// COMMENTS
	private final List<Comment> comments = 
			Collections.synchronizedList(new ArrayList<Comment>());
	
	@Override
	public int getNumberOfComments() {
		return comments.size();
	}

	@Override
	public Comment getComment(int idx) {
		return comments.get(idx);
	}

	@Override
	public void addComment(Comment comment) {
		comments.add(comment);
	}

	@Override
	public void removeComment(Comment comment) {
		comments.remove(comment);
	}

	@Override
	public void removeComment(int idx) {
		comments.remove(idx);
	}
	
	/* Extension support */
	private ExtensionSupport extSupport = new ExtensionSupport(Record.class, this);
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
}
