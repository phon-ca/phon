/*
 * 
 */
package ca.phon.session;

import java.time.*;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;

/**
 * A participant implementation for 'unidentified' or unknown participants.
 * Property values for instances of this class cannot be modified.
 */
public final class UnidentifiedParticipant implements Participant {
	
	private final ExtensionSupport extSupport =
			new ExtensionSupport(UnidentifiedParticipant.class, this);
	
	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
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
	public void setSex(Sex sex) {
	}
	
	@Override
	public void setSES(String ses) {
	}
	
	@Override
	public void setRole(ParticipantRole role) {
	}
	
	@Override
	public void setName(String name) {
	}
	
	@Override
	public void setLanguage(String language) {
	}
	
	@Override
	public void setId(String id) {
	}
	
	@Override
	public void setGroup(String group) {
	}
	
	@Override
	public void setEducation(String education) {
	}
	
	@Override
	public void setBirthDate(LocalDate birthDate) {
	}
	
	@Override
	public void setAgeTo(Period ageTo) {
	}
	
	@Override
	public void setAge(Period age) {
	}
	
	@Override
	public Sex getSex() {
		return Sex.UNSPECIFIED;
	}
	
	@Override
	public String getSES() {
		return null;
	}
	
	@Override
	public ParticipantRole getRole() {
		return ParticipantRole.UNIDENTIFIED;
	}
	
	@Override
	public String getName() {
		return ParticipantRole.UNIDENTIFIED.getTitle();
	}
	
	@Override
	public String getLanguage() {
		return null;
	}
	
	@Override
	public String getId() {
		return ParticipantRole.UNIDENTIFIED.getId();
	}
	
	@Override
	public String getGroup() {
		return null;
	}
	
	@Override
	public String getEducation() {
		return null;
	}
	
	@Override
	public LocalDate getBirthDate() {
		return null;
	}
	
	@Override
	public Period getAgeTo() {
		return null;
	}
	
	@Override
	public Period getAge(LocalDate fromDate) {
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
