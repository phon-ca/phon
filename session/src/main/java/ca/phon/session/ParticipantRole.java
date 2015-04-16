/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
	TARGET_CHILD("Target Child", "CHI"),
	TARGET_ADULT("Target Adult", "ADU"),
	CHILD("Child", "CHI"),
	MOTHER("Mother", "MOT"),
	FATHER("Father", "FAT"),
	BROTHER("Brother", "BRO"),
	SISTER("Sister", "SIS"),
	SIBLING("Sibling", "SIB"),
	GRANDMOTHER("Grandmother", "GRM"),
	GRANDFATHER("Grandfather", "GRF"),
	AUNT("Aunt", "AUN"),
	UNCLE("Uncle", "UNC"),
	COUSIN("Cousin", "COU"),
	FAMILY_FRIEND("Family Friend", "FAM"),
	STUDENT("Student", "STU"),
	TEACHER("Teacher", "TEA"),
	PLAYMATE("Playmate", "PLA"),
	VISITOR("Visitor", "VIS"),
	BABYSITTER("Babysitter", "SIT"),
	CARETAKER("Caretaker", "CAR"),
	HOUSEKEEPER("Housekeeper", "HOU"),
	INVESTIGATOR("Investigator", "INV"),
	OBSERVER("Observer", "OBS"),
	CLINICIAN("Clinician", "CLI"),
	THERAPIST("Therapist", "THE"),
	INTERVIEWER("Interviewer", "INT"),
	INFORMANT("Informant", "INF"),
	PARTICIPANT("Participant", "PAR"),
	SUBJECT("Subject", "SUB"),
	PARTNER("Partner", "NER"),
	DOCTOR("Doctor", "DOC"),
	NURSE("Nurse", "NUR"),
	PATIENT("Patient", "PAT"),
	UNIDENTIFIED("Unidentified", "UNI"),
	UNCERTAIN("Uncertain", "UNC"),
	CAMERA_OPERATOR("Camera Operator", "CAM"),
	GROUP("Group", "GRO"),
	NARRATOR("Narrator", "NAR"),
	ADULT("Adult", "ADU"),
	TEENAGER("Teenager", "TEE"),
	BOY("Boy", "BOY"),
	GIRL("Girl", "GIR"),
	MALE("Male", "MAL"),
	FEMALE("Female", "FEM"),
	NON_HUMAN("Non Human", "NOH"),
	TOY("Toy", "TOY"),
	MEDIA("Media", "MED"),
	ENVIRONMENT("Environment", "ENV"),
	OFFSCRIPT("Offscript", "OFF"),
	TEXT("Text", "TXT"),
	PLAYROLE("PlayRole", "PLR"),
	JUSTICE("Justice", "JUS"),
	JUDGE("Judge", "JUD"),
	ATTORNEY("Attorney", "ATT"),
	SPEAKER("Speaker", "SPE"),
	SHOWHOST("ShowHost", "SHO"),
	SHOWGUEST("ShowGuest", "SHG"),
	OPERATOR("Operator", "OPR"),
	CALLER("Caller", "CAL")
	;

	private String title;
	
	private String id;
	
	private ParticipantRole(String title, String id) {
		this.title = title;
		this.id = id;
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
	
	public String getId() {
		return this.id;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
}
