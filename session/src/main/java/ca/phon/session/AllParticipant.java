package ca.phon.session;

import ca.phon.session.spi.ParticipantSPI;

import java.time.*;

/**
 * Used as a placeholder in lists for all participants
 */
public final class AllParticipant implements ParticipantSPI {

	@Override
	public void setSex(Sex sex) {
	}

	@Override
	public void setBirthplace(String birthplace) {

	}

	@Override
	public String getBirthplace() {
		return null;
	}

	@Override
	public void setFirstLanguage(String firstLanguage) {

	}

	@Override
	public String getFirstLanguage() {
		return null;
	}

	@Override
	public void setOther(String other) {

	}

	@Override
	public String getOther() {
		return null;
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
		return "All Participants";
	}

	@Override
	public String getLanguage() {
		return null;
	}

	@Override
	public String getId() {
		return "ALL";
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

}

