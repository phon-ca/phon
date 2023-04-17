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

import ca.phon.extensions.ExtendableObject;
import ca.phon.session.spi.SessionMetadataSPI;

/**
 * Transcript Metadata
 * 
 *
 */
public final class SessionMetadata extends ExtendableObject {
	
	private SessionMetadataSPI sessionMetadataImpl;
	
	SessionMetadata(SessionMetadataSPI impl) {
		super();
		this.sessionMetadataImpl = impl;
	}

	public String getTitle() {
		return sessionMetadataImpl.getTitle();
	}

	public void setTitle(String title) {
		sessionMetadataImpl.setTitle(title);
	}

	public String getCreator() {
		return sessionMetadataImpl.getCreator();
	}

	public void setCreator(String creator) {
		sessionMetadataImpl.setCreator(creator);
	}

	public String getAppID() {
		return sessionMetadataImpl.getAppID();
	}

	public void setAppID(String appID) {
		sessionMetadataImpl.setAppID(appID);
	}

	public String getSubject() {
		return sessionMetadataImpl.getSubject();
	}

	public void setSubject(String subject) {
		sessionMetadataImpl.setSubject(subject);
	}

	public String getDescription() {
		return sessionMetadataImpl.getDescription();
	}

	public void setDescription(String description) {
		sessionMetadataImpl.setDescription(description);
	}

	public String getPublisher() {
		return sessionMetadataImpl.getPublisher();
	}

	public void setPublisher(String publisher) {
		sessionMetadataImpl.setPublisher(publisher);
	}

	public String getContributor() {
		return sessionMetadataImpl.getContributor();
	}

	public void setContributor(String contributer) {
		sessionMetadataImpl.setContributor(contributer);
	}

	public String getDate() {
		return sessionMetadataImpl.getDate();
	}

	public void setDate(String date) {
		sessionMetadataImpl.setDate(date);
	}

	public String getType() {
		return sessionMetadataImpl.getType();
	}

	public void setType(String type) {
		sessionMetadataImpl.setType(type);
	}

	public String getFormat() {
		return sessionMetadataImpl.getFormat();
	}

	public void setFormat(String format) {
		sessionMetadataImpl.setFormat(format);
	}

	public String getIdentifier() {
		return sessionMetadataImpl.getIdentifier();
	}

	public void setIdentifier(String identifier) {
		sessionMetadataImpl.setIdentifier(identifier);
	}

	public String getSource() {
		return sessionMetadataImpl.getSource();
	}

	public void setSource(String source) {
		sessionMetadataImpl.setSource(source);
	}

	public String getLanguage() {
		return sessionMetadataImpl.getLanguage();
	}

	public void setLanguage(String language) {
		sessionMetadataImpl.setLanguage(language);
	}

	public String getRelation() {
		return sessionMetadataImpl.getRelation();
	}

	public void setRelation(String relation) {
		sessionMetadataImpl.setRelation(relation);
	}

	public String getCoverage() {
		return sessionMetadataImpl.getCoverage();
	}

	public void setCoverage(String coverage) {
		sessionMetadataImpl.setCoverage(coverage);
	}

	public String getRights() {
		return sessionMetadataImpl.getRights();
	}

	public void setRights(String rights) {
		sessionMetadataImpl.setRights(rights);
	}

	/**
	 * Return number of comments at beginning of session.
	 *
	 * @return number of comments
	 */
	public int getNumberOfComments() {
		return sessionMetadataImpl.getNumberOfComments();
	}

	/**
	 * Return comment at beginning of session at given index
	 *
	 * @param idx
	 * @return
	 */
	public Comment getComment(int idx) {
		return sessionMetadataImpl.getComment(idx);
	}

	/**
	 * Add comment to beginning of session
	 * @param comment
	 */
	public void addComment(Comment comment) {
		sessionMetadataImpl.addComment(comment);
	}

	/**
	 * Remove comment from beginning of session
	 *
	 * @param comment
	 */
	public void removeComment(Comment comment) {
		sessionMetadataImpl.removeComment(comment);
	}

	/**
	 * Remove comment from beginning of session at given index
	 * @param idx
	 */
	public void removeComment(int idx) {
		sessionMetadataImpl.removeComment(idx);
	}

	/**
	 * Return number of comments at end of session.
	 *
	 * @return number of comments
	 */
	public int getNumberOfTrailingComments() {
		return sessionMetadataImpl.getNumberOfTrailingComments();
	}

	/**
	 * Return comment at end of session at given index
	 *
	 * @param idx
	 * @return
	 */
	public Comment getTrailingComment(int idx) {
		return sessionMetadataImpl.getTrailingComment(idx);
	}

	/**
	 * Add comment to end of session
	 * @param comment
	 */
	public void addTrailingComment(Comment comment) {
		sessionMetadataImpl.addTrailingComment(comment);
	}

	/**
	 * Remove comment from end of session
	 *
	 * @param comment
	 */
	public void removeTrailingComment(Comment comment) {
		sessionMetadataImpl.removeTrailingComment(comment);
	}

	/**
	 * Remove comment from beginning of end at given index
	 * @param idx
	 */
	public void removeTrailingComment(int idx) {
		sessionMetadataImpl.removeTrailingComment(idx);
	}

}
