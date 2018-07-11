/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.session.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.session.Session;

/**
 * Session validator with plug-in support.  This class maintains the
 * list of available validator plug-ins as well as a set of
 * validation listeners.
 * 
 * @author Greg
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
	
	public void fireValidationEvent(Session session, String message) {
		fireValidationEvent(new ValidationEvent(session, message));
	}
	
	public void fireValidationEvent(Session session, int record, String message) {
		fireValidationEvent(new ValidationEvent(session, record, message));
	}

	public void fireValidationEvent(Session session, int record, String tierName, int group, String message) {
		fireValidationEvent(new ValidationEvent(session, record, tierName, group, message));
	}
	
	public void fireValidationEvent(final ValidationEvent evt) {
		events.add(evt);
		listeners.forEach( (l) -> { l.validationInfo(evt); } );
	}
	
	public void validate(Session session) {
		sessionChecks.forEach( (check) -> check.checkSession(this, session, new HashMap<>()) );
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
