/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.session.impl;

import ca.phon.session.*;
import ca.phon.session.spi.ParticipantSPI;

import java.time.*;

/**
 * Basic participant implementation.
 */
public class ParticipantImpl implements ParticipantSPI {
	
	/*
	 * Attributes
	 */
	private String id;
	
	private LocalDate birthDate;

	private String birthPlace;
	
	private String name;
	
	private Period age;
	
	private Period ageTo;
	
	private String education;
	
	private String group;
	
	private String SES;
	
	private Sex sex;
	
	private ParticipantRole role;
	
	private String language;

	private String firstLanguage;

	private String other;
	
	ParticipantImpl() {
		super();
	}

	@Override
	public Period getAge(LocalDate fromDate) {
		// return forced age
		if(age != null || fromDate == null) {
			return age;
		} else if(getBirthDate() != null && fromDate != null) {
			final LocalDate start = getBirthDate();
			final LocalDate end = fromDate;
			
			final Period period = Period.between(start, end);
			return period;
		} else {
			return age;
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
	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	@Override
	public void setBirthDate(LocalDate birthDate) {
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

	@Override
	public void setBirthplace(String birthplace) {
		this.birthPlace = birthplace;
	}

	@Override
	public String getBirthplace() {
		return this.birthPlace;
	}

	@Override
	public void setFirstLanguage(String firstLanguage) {
		this.firstLanguage = firstLanguage;
	}

	@Override
	public String getFirstLanguage() {
		return this.firstLanguage;
	}

	@Override
	public void setOther(String other) {
		this.other = other;
	}

	@Override
	public String getOther() {
		return this.other;
	}
}
