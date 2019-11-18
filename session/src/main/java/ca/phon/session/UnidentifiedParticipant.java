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

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.session.spi.ParticipantSPI;

/**
 * A participant implementation for 'unidentified' or unknown participants.
 * Property values for instances of this class cannot be modified.
 */
public final class UnidentifiedParticipant implements ParticipantSPI {

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
	
}
