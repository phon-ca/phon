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

/**
 * List of possible participant roles.
 *
 */
public enum ParticipantRole {
	ADULT("Adult", "ADU"),
	ATTORNEY("Attorney", "ATT"),
	AUNT("Aunt", "AUN"),
	BABYSITTER("Babysitter", "SIT"),
	BOY("Boy", "BOY"),
	BROTHER("Brother", "BRO"),
	CALLER("Caller", "CAL"),
	CAMERA_OPERATOR("Camera Operator", "CAM"),
	CARETAKER("Caretaker", "CAR"),
	CHILD("Child", "CHI"),
	CLINICIAN("Clinician", "CLI"),
	COUSIN("Cousin", "COU"),
	DOCTOR("Doctor", "DOC"),
	ENVIRONMENT("Environment", "ENV"),
	FAMILY_FRIEND("Family Friend", "FAM"),
	FATHER("Father", "FAT"),
	FEMALE("Female", "FEM"),
	GIRL("Girl", "GIR"),
	GRANDFATHER("Grandfather", "GRF"),
	GRANDMOTHER("Grandmother", "GRM"),
	GROUP("Group", "GRO"),
	HOUSEKEEPER("Housekeeper", "HOU"),
	INFORMANT("Informant", "INF"),
	INTERVIEWER("Interviewer", "INT"),
	INVESTIGATOR("Investigator", "INV"),
	JUDGE("Judge", "JUD"),
	JUSTICE("Justice", "JUS"),
	MALE("Male", "MAL"),
	MEDIA("Media", "MED"),
	MOTHER("Mother", "MOT"),
	NARRATOR("Narrator", "NAR"),
	NON_HUMAN("Non Human", "NOH"),
	NURSE("Nurse", "NUR"),
	OBSERVER("Observer", "OBS"),
	OFFSCRIPT("Offscript", "OFF"),
	OPERATOR("Operator", "OPR"),
	PARTICIPANT("Participant", "PAR"),
	PARTNER("Partner", "NER"),
	PATIENT("Patient", "PAT"),
	PLAYMATE("Playmate", "PLA"),
	PLAYROLE("PlayRole", "PLR"),
	SHOWGUEST("ShowGuest", "SHG"),
	SHOWHOST("ShowHost", "SHO"),
	SIBLING("Sibling", "SIB"),
	SISTER("Sister", "SIS"),
	SPEAKER("Speaker", "SPE"),
	STUDENT("Student", "STU"),
	SUBJECT("Subject", "SUB"),
	TARGET_ADULT("Target Adult", "ADU"),
	TARGET_CHILD("Target Child", "CHI"),
	TEACHER("Teacher", "TEA"),
	TEENAGER("Teenager", "TEE"),
	TEXT("Text", "TXT"),
	THERAPIST("Therapist", "THE"),
	TOY("Toy", "TOY"),
	UNCERTAIN("Uncertain", "UNC"),
	UNCLE("Uncle", "UNC"),
	UNIDENTIFIED("Unidentified", "UNI"),
	VISITOR("Visitor", "VIS")
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
