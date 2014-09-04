package ca.phon.session.impl;

import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.Sex;

/**
 * Basic participant implementation.
 */
public class ParticipantImpl implements Participant {
	
	/*
	 * Attributes
	 */
	private String id;
	
	private DateTime birthDate;
	
	private String name;
	
	private Period age;
	
	private Period ageTo;
	
	private String education;
	
	private String group;
	
	private String SES;
	
	private Sex sex;
	
	private ParticipantRole role;
	
	private String language;
	
	ParticipantImpl() {
		super();
		extSupport.initExtensions();
	}

	@Override
	public Period getAge(DateTime fromDate) {
		// return forced age
		if(age != null) {
			return age;
		} else {
			final DateTime start = getBirthDate();
			final DateTime end = fromDate;
			
			final Period period = new Period(start, end, PeriodType.yearMonthDay());
			return period;
		}
	}

	@Override
	public void setAge(Period age) {
		this.age = age;
	}

	@Override
	public Period getAgeTo() {
		return this.ageTo;
	}

	@Override
	public void setAgeTo(Period ageTo) {
		this.ageTo = ageTo;
	}

	@Override
	public DateTime getBirthDate() {
		return this.birthDate;
	}

	@Override
	public void setBirthDate(DateTime birthDate) {
		this.birthDate = birthDate;
	}

	@Override
	public String getEducation() {
		return this.education;
	}

	@Override
	public void setEducation(String education) {
		this.education = education;
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public void setGroup(String group) {
		this.group = group;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getLanguage() {
		return this.language;
	}

	@Override
	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public ParticipantRole getRole() {
		return this.role;
	}

	@Override
	public void setRole(ParticipantRole role) {
		this.role = role;
	}

	@Override
	public String getSES() {
		return this.SES;
	}

	@Override
	public void setSES(String ses) {
		this.SES = ses;
	}

	@Override
	public Sex getSex() {
		return this.sex;
	}

	@Override
	public void setSex(Sex sex) {
		this.sex = sex;
	}

	/* Class extensions */
	private final ExtensionSupport extSupport = new ExtensionSupport(Participant.class, this);
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
	
	public String toString() {
		return (getName() != null ? getName() : 
				(getId() != null ? getId() : getRole().toString() ));
	}
	
//	@Override
//	public Object clone() {
//		final ParticipantImpl retVal = new ParticipantImpl();
//		
//		// copy fields
//		retVal.setId(getId());
//		retVal.setName(getName());
//		retVal.setBirthDate(getBirthDate());
//		retVal.setEducation(getEducation());
//		retVal.setGroup(getGroup());
//		retVal.setLanguage(getLanguage());
//		retVal.setRole(getRole());
//		retVal.setSES(getSES());
//		retVal.setSex(getSex());
//		retVal.setAgeTo(getAgeTo());
//		
//		return retVal;
//	}
}
