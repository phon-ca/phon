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

public class ValidationEvent implements IExtendable {

	public static enum Severity {
		INFO,
		WARNING,
		ERROR
	};

	private final ExtensionSupport extSupport = new ExtensionSupport(ValidationEvent.class, this);

	private final Severity severity;

	private final Session session;
	
	private final int elementIndex;
	
	private final String tierName;
	
	private final String message;

    private final Transcriber transcriber;
	
	private final List<SessionQuickFix> quickFixes;

	public ValidationEvent(Severity severity, Session session, Transcriber transcriber, String message) {
		this(severity, session, transcriber, message, new SessionQuickFix[0]);
	}

	public ValidationEvent(Session session, Transcriber transcriber, String message) {
		this(Severity.WARNING, session, transcriber, message);
	}

	public ValidationEvent(Severity severity, Session session, Transcriber transcriber, String message, SessionQuickFix ... quickFixes) {
		super();
		this.severity = severity;
		this.session = session;
		this.message = message;
        this.transcriber = transcriber;
        this.elementIndex = -1;
        this.tierName = null;
		this.quickFixes = List.of(quickFixes);

	}

	public ValidationEvent(Session session, Transcriber transcriber, String message, SessionQuickFix ... quickFixes) {
		this(Severity.WARNING, session, transcriber, message, quickFixes);
	}

	public ValidationEvent(Severity severity, Session session, int elementIndex, Transcriber transcriber, String message) {
		this(severity, session, elementIndex, transcriber, message, new SessionQuickFix[0]);
	}

	public ValidationEvent(Session session, int elementIndex, Transcriber transcriber, String message) {
		this(Severity.WARNING, session, elementIndex, transcriber, message);
	}

	public ValidationEvent(Severity severity, Session session, int elementIndex, Transcriber transcriber, String message, SessionQuickFix ... quickFixes) {
		super();
		this.severity = severity;
		this.session = session;
		this.elementIndex = elementIndex;
		this.message = message;
        this.transcriber = transcriber;
        this.tierName = null;
		this.quickFixes = List.of(quickFixes);
	}

	public ValidationEvent(Session session, int elementIndex, Transcriber transcriber, String message, SessionQuickFix ... quickFixes) {
		this(Severity.WARNING, session, elementIndex, transcriber, message, quickFixes);
	}

	public ValidationEvent(Session session, int elementIndex, String tierName, Transcriber transcriber, String message) {
		this(session, elementIndex, tierName, transcriber, message, new SessionQuickFix[0]);
	}

	public ValidationEvent(Session session, int elementIndex, String tierName, Transcriber transcriber,
						   String message, SessionQuickFix ... quickFixes) {
		this(Severity.WARNING, session, elementIndex, tierName, transcriber, message, quickFixes);
	}

	public ValidationEvent(Severity severity, Session session, int elementIndex, String tierName, Transcriber transcriber,
						   String message, SessionQuickFix ... quickFixes) {
		super();
		this.severity = severity;
		this.session = session;
		this.elementIndex = elementIndex;
		this.tierName = tierName;
		this.message = message;
        this.transcriber = transcriber;
		this.quickFixes = List.of(quickFixes);
	}

    public Transcriber getTranscriber() {
        return this.transcriber;
    }

	public Severity getSeverity() {
		return this.severity;
	}

	public Session getSession() {
		return session;
	}

	public int getElementIndex() {
		return elementIndex;
	}

	public String getTierName() {
		return tierName;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * Can this event be automatically fixed?  Sub-classes
	 * should override this method.
	 * 
	 * @return can the validation event be fixed
	 */
	public boolean canFix() {
		return quickFixes.size() > 0;
	}
	
	/**
	 * Options for fixing the problem identified by this
	 * validation event.
	 * 
	 * @return a list of validation options or an empty
	 *  list if this problem does not have a quick fix
	 */
	public List<SessionQuickFix> getQuickFixes() {
		return quickFixes;
	}
	
	@Override
	public String toString() {
		return getMessage();
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
