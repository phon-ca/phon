/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.session;

/**
 * List of possible participant roles.
 *
 */
public enum ParticipantRole {
	TARGET_CHILD("Target Child"),
	TARGET_ADULT("Target Adult"),
	CHILD("Child"),
	MOTHER("Mother"),
	FATHER("Father"),
	BROTHER("Brother"),
	SISTER("Sister"),
	SIBLING("Sibling"),
	GRANDMOTHER("Grandmother"),
	GRANDFATHER("Grandfather"),
	AUNT("Aunt"),
	UNCLE("Uncle"),
	COUSIN("Cousin"),
	FAMILY_FRIEND("Family Friend"),
	STUDENT("Student"),
	TEACHER("Teacher"),
	PLAYMATE("Playmate"),
	VISITOR("Visitor"),
	BABYSITTER("Babysitter"),
	CARETAKER("Caretaker"),
	HOUSEKEEPER("Housekeeper"),
	INVESTIGATOR("Investigator"),
	OBSERVER("Observer"),
	CLINICIAN("Clinician"),
	THERAPIST("Therapist"),
	INTERVIEWER("Interviewer"),
	INFORMANT("Informant"),
	PARTICIPANT("Participant"),
	SUBJECT("Subject"),
	PARTNER("Partner"),
	DOCTOR("Doctor"),
	NURSE("Nurse"),
	PATIENT("Patient"),
	UNIDENTIFIED("Unidentified"),
	UNCERTAIN("Uncertain"),
	CAMERA_OPERATOR("Camera Operator"),
	GROUP("Group"),
	NARRATOR("Narrator"),
	ADULT("Adult"),
	TEENAGER("Teenager"),
	BOY("Boy"),
	GIRL("Girl"),
	MALE("Male"),
	FEMALE("Female"),
	NON_HUMAN("Non Human"),
	TOY("Toy"),
	MEDIA("Media"),
	ENVIRONMENT("Environment"),
	OFFSCRIPT("Offscript"),
	TEXT("Text"),
	PLAYROLE("PlayRole"),
	JUSTICE("Justice"),
	JUDGE("Judge"),
	ATTORNEY("Attorney"),
	SPEAKER("Speaker"),
	SHOWHOST("ShowHost"),
	SHOWGUEST("ShowGuest"),
	OPERATOR("Operator"),
	CALLER("Caller")
	;

	private String title;
	
	private ParticipantRole(String title) {
		this.title = title;
	}
	
	public static ParticipantRole fromString(String title) {
		ParticipantRole retVal = null;
		if(title == null) return retVal;
		
		for(ParticipantRole role:ParticipantRole.values()) {
			if(title.equalsIgnoreCase(role.getTitle())
					|| title.replaceAll("_", " ").equalsIgnoreCase(role.getTitle())) {
				retVal = role;
				break;
			}
		}
		
		return retVal;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
}
