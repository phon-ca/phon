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
package ca.phon.session.check;

import ca.phon.extensions.*;
import ca.phon.session.Session;
import ca.phon.session.Transcriber;

import java.util.*;

/**
 * Session validator with plug-in support.  This class maintains the
 * list of available validator plug-ins as well as a set of
 * validation listeners.
 *
 *
 */
public class SessionValidator implements IExtendable {
	
	private final ExtensionSupport extSupport = new ExtensionSupport(SessionValidator.class, this);
	
	private final List<SessionCheck> sessionChecks = new ArrayList<>();
	
	private final List<ValidationListener> listeners = new ArrayList<>();
	
	private final List<ValidationEvent> events = new ArrayList<>();
	
	public SessionValidator() {
		this(SessionCheck.availableChecks());
	}
	
	public SessionValidator(SessionCheck[] sessionChecks) {
		this(Arrays.asList(sessionChecks));
	}
	
	public SessionValidator(List<SessionCheck> sessionChecks) {
		super();
		
		this.sessionChecks.addAll(sessionChecks);
	}

	public void fireValidationEvent(ValidationEvent.Severity severity, Session session, Transcriber transcriber, String message) {
		fireValidationEvent(new ValidationEvent(severity, session, transcriber, message));
	}

	public void fireValidationEvent(Session session, Transcriber transcriber, String message) {
		fireValidationEvent(new ValidationEvent(session, transcriber, message));
	}

	public void fireValidationEvent(ValidationEvent.Severity severity, Session session, int record, Transcriber transcriber, String message) {
		fireValidationEvent(new ValidationEvent(severity, session, record, transcriber, message));
	}

	public void fireValidationEvent(Session session, int record, Transcriber transcriber, String message) {
		fireValidationEvent(new ValidationEvent(session, record, transcriber, message));
	}

	public void fireValidationEvent(ValidationEvent.Severity severity, Session session, int record, String tierName, Transcriber transcriber, String message) {
		fireValidationEvent(new ValidationEvent(severity, session, record, tierName, transcriber, message));
	}

	public void fireValidationEvent(Session session, int record, String tierName, Transcriber transcriber, String message) {
		fireValidationEvent(new ValidationEvent(session, record, tierName, transcriber, message));
	}

	public void fireValidationEvent(final ValidationEvent evt) {
		events.add(evt);
		listeners.forEach( (l) -> { l.validationInfo(evt); } );
	}

    /**
     * Validate the given session using all available session checks.
     *
     * @param session the session to validate
     * @param transcriber check transcripts for given transcriber, or validated transcripts if validator
     * @return true if session was modified, false otherwise
     */
	public boolean validate(Session session, Transcriber transcriber) {
		boolean modified = false;
		for(SessionCheck check:sessionChecks) {
			modified |= check.checkSession(this, session, transcriber);
		}
		return modified;
	}

    /**
     * Validate the given transcript element index using all available session checks.
     *
     * @param session the session to validate
     * @param elementIndex the transcript element index to validate
     * @param transcriber check transcripts for given transcriber, or validated transcripts if validator
     * @return true if session was modified, false otherwise
     */
	public boolean validate(Session session, int elementIndex, Transcriber transcriber) {
		boolean modified = false;
		for(SessionCheck check:sessionChecks) {
			modified |= check.checkTranscriptElement(this, session, elementIndex, transcriber);
		}
		return modified;
	}
	
	public void addValidationListener(ValidationListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeValidationListener(ValidationListener listener) {
		this.listeners.remove(listener);
	}
	
	public List<ValidationListener> getValidationListeners() {
		return this.listeners;
	}
	
	public List<ValidationEvent> getValidationEvents() {
		return this.events;
	}
	
	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

}
