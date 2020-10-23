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
	GRANDFATHER("Grandfather", "GRF"),
	GRANDMOTHER("Grandmother", "GRM"),
	RELATIVE("Relative", "REL"),
	PARTICIPANT("Participant", "PAR"),
	THERAPIST("Therapist", "THE"),
	INFORMANT("Informant", "INF"),
	SUBJECT("Subject", "SUB"),
	INVESTIGATOR("Investigator", "INV"),
	PARTNER("Partner", "NER"),
	BOY("Boy", "BOY"),
	GIRL("Girl", "GIR"),
	ADULT("Adult", "ADU"),
	TEENAGER("Teenager", "TEE"),
	MALE("Male", "MAL"),
	FEMALE("Female", "FEM"),
	VISITOR("Visitor", "VIS"),
	FRIEND("Friend", "FRI"),
	PLAYMATE("Playmate", "PLA"),
	CARETAKER("Caretaker", "CAR"),
	ENVIRONMENT("Environment", "ENV"),
	GROUP("Group", "GRO"),
	UNIDENTIFIED("Unidentified", "UNI"),
	UNCERTAIN("Uncertain", "UNC"),
	OTHER("Other", "OTH"),
	TEXT("Text", "TXT"),
	MEDIA("Media", "MED"),
	PLAYROLE("PlayRole", "PLR"),
	LENA("LENA", "LNA"),
	JUSTICE("Justice", "JUS"),
	ATTORNEY("Attorney", "ATT"),
	DOCTOR("Doctor", "DOC"),
	NURSE("Nurse", "NUR"),
	STUDENT("Student", "STU"),
	TEACHER("Teacher", "TEA"),
	HOST("Host", "HOS"),
	GUEST("Guest", "GST"),
	LEADER("Leader", "LDR"),
	MEMBER("Member", "MBR"),
	NARRATOR("Narrator", "NAR"),
	SPEAKER("Speaker", "SPE"),
	AUDIENCE("Audience", "AUD"),
	
	/*
	 * Removed as of TalkBank 2.10.0
	 */
	@Deprecated
	AUNT("Aunt", "AUN"),
	@Deprecated
	BABYSITTER("Babysitter", "SIT"),
	@Deprecated
	CALLER("Caller", "CAL"),
	@Deprecated
	CAMERA_OPERATOR("Camera Operator", "CAM"),
	@Deprecated
	CLINICIAN("Clinician", "CLI"),
	@Deprecated
	COUSIN("Cousin", "COU"),
	@Deprecated
	FAMILY_FRIEND("Family Friend", "FAM"),
	@Deprecated
	HOUSEKEEPER("Housekeeper", "HOU"),
	@Deprecated
	INTERVIEWER("Interviewer", "INT"),
	@Deprecated
	JUDGE("Judge", "JUD"),
	@Deprecated
	NON_HUMAN("Non Human", "NOH"),
	@Deprecated
	OBSERVER("Observer", "OBS"),
	@Deprecated
	OFFSCRIPT("Offscript", "OFF"),
	@Deprecated
	OPERATOR("Operator", "OPR"),
	@Deprecated
	PATIENT("Patient", "PAT"),
	@Deprecated
	SHOWGUEST("ShowGuest", "SHG"),
	@Deprecated
	SHOWHOST("ShowHost", "SHO"),
	@Deprecated
	TOY("Toy", "TOY"),
	@Deprecated
	UNCLE("Uncle", "UNC")
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
