/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package ca.phon.session;

import java.time.*;

import ca.phon.extensions.*;
import ca.phon.session.spi.*;

/**
 * Immutable participant object.
 *
 */
public final class Participant extends ExtendableObject {
	
	public static final Participant UNKNOWN = SessionFactory.newFactory().createUnknownParticipant();

	private ParticipantSPI participantImpl;
	
	Participant(ParticipantSPI impl) {
		super();
		this.participantImpl = impl;
	}

	public Period getAge(LocalDate fromDate) {
		return participantImpl.getAge(fromDate);
	}

	public Period getAgeTo() {
		return participantImpl.getAgeTo();
	}

	public LocalDate getBirthDate() {
		return participantImpl.getBirthDate();
	}

	public String getEducation() {
		return participantImpl.getEducation();
	}

	public String getGroup() {
		return participantImpl.getGroup();
	}

	public String getId() {
		return participantImpl.getId();
	}

	public String getLanguage() {
		return participantImpl.getLanguage();
	}

	public String getName() {
		return participantImpl.getName();
	}

	public ParticipantRole getRole() {
		return participantImpl.getRole();
	}

	public String getSES() {
		return participantImpl.getSES();
	}

	public Sex getSex() {
		return participantImpl.getSex();
	}

	public void setAge(Period age) {
		participantImpl.setAge(age);
	}

	public void setAgeTo(Period ageTo) {
		participantImpl.setAgeTo(ageTo);
	}

	public void setBirthDate(LocalDate birthDate) {
		participantImpl.setBirthDate(birthDate);
	}

	public void setEducation(String education) {
		participantImpl.setEducation(education);
	}

	public void setGroup(String group) {
		participantImpl.setGroup(group);
	}

	public void setId(String id) {
		participantImpl.setId(id);
	}

	public void setLanguage(String language) {
		participantImpl.setLanguage(language);
	}

	public void setName(String name) {
		participantImpl.setName(name);
	}

	public void setRole(ParticipantRole role) {
		participantImpl.setRole(role);
	}

	public void setSES(String ses) {
		participantImpl.setSES(ses);
	}

	public void setSex(Sex sex) {
		participantImpl.setSex(sex);
	}
	
	@Override
	public String toString() {
		return (getName() != null ? getName() : 
				(getId() != null ? getId() : getRole().toString() ));
	}
	
}
