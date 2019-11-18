package ca.phon.session.spi;

import java.time.LocalDate;
import java.time.Period;

import ca.phon.session.ParticipantRole;
import ca.phon.session.Sex;

public interface ParticipantSPI {

	/**
	 * @return Returns the age.
	 */
	public abstract Period getAge(LocalDate fromDate);

	/**
	 * @return Returns the ageTo.
	 */
	public abstract Period getAgeTo();

	/**
	 * @return Returns the birthDate.
	 */
	public abstract LocalDate getBirthDate();

	/**
	 * @return Returns the education.
	 */
	public abstract String getEducation();
	
	/**
	 * @return Returns the group.
	 */
	public abstract String getGroup();

	/**
	 * @return Returns the id.
	 */
	public abstract String getId();

	/**
	 * @return Returns the language.
	 */
	public abstract String getLanguage();

	/**
	 * @return Returns the name.
	 */
	public abstract String getName();

	/**
	 * @return Returns the role.
	 */
	public abstract ParticipantRole getRole();
	
	/**
	 * @return Returns the sES.
	 */
	public abstract String getSES();

	/**
	 * @return Returns the sex.
	 */
	public abstract Sex getSex();
	
	/**
	 * @param age The age to set.
	 */
	public abstract void setAge(Period age);
	
	/**
	 * @param ageTo The ageTo to set.
	 */
	public abstract void setAgeTo(Period ageTo);
	
	/**
	 * @param birthDate The birthDate to set.
	 */
	public abstract void setBirthDate(LocalDate birthDate);
	
	/**
	 * @param education The education to set.
	 */
	public abstract void setEducation(String education);
	
	/**
	 * @param group The group to set.
	 */
	public abstract void setGroup(String group);
	
	/**
	 * @param id The id to set.
	 */
	public abstract void setId(String id);
	
	/**
	 * @param language The language to set.
	 */
	public abstract void setLanguage(String language);
	
	/**
	 * @param name The name to set.
	 */
	public abstract void setName(String name);
	
	/**
	 * @param role The role to set.
	 */
	public abstract void setRole(ParticipantRole role);
	
	/**
	 * @param ses The sES to set.
	 */
	public abstract void setSES(String ses);
	
	/**
	 * @param sex The sex to set.
	 */
	public abstract void setSex(Sex sex);
	
}
