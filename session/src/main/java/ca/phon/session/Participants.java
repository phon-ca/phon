package ca.phon.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;

/**
 * Helper class providing iterator and visitor methods
 * for {@link Session} {@link Participant}s.
 */
public abstract class Participants implements Iterable<Participant>, IExtendable, Visitable<Participant> {

	protected Participants() {
		super();
		extSupport.initExtensions();
	}

	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(Participants.class, this);

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
	public void accept(Visitor<Participant> visitor) {
		for(Participant p:this) {
			visitor.visit(p);
		}
	}
	
	public Map<ParticipantRole, Integer> getRoleCount() {
		final Map<ParticipantRole, Integer> retVal = new HashMap<ParticipantRole, Integer>();
		
		for(Participant p:this) {
			Integer rc = retVal.get(p.getRole());
			if(rc == null) {
				rc = 0;
			}
			rc++;
			retVal.put(p.getRole(), rc);
		}
		
		return retVal;
	}
	
	/**
	 * Returns a list of participants which does not include
	 * the given participant.
	 * 
	 * @param part
	 * @return
	 */
	public List<Participant> otherParticipants(Participant part) {
		List<Participant> retVal = new ArrayList<Participant>();
		for(Participant p:this) {
			if(p == part) continue;
			retVal.add(p);
		}
		return retVal;
	}
	
	public static void copyParticipantInfo(Participant src, Participant dest) {
		dest.setId(src.getId());
		dest.setBirthDate(src.getBirthDate());
		dest.setAge(src.getAge(null));
		dest.setEducation(src.getEducation());
		dest.setGroup(src.getGroup());
		dest.setLanguage(src.getLanguage());
		dest.setName(src.getName());
		dest.setRole(src.getRole());
		dest.setSES(src.getSES());
		dest.setSex(src.getSex());
	}
}
