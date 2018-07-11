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
import java.util.List;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.session.Session;

public class ValidationEvent implements IExtendable {
	
	private final ExtensionSupport extSupport = new ExtensionSupport(ValidationEvent.class, this);
	
	private Session session;
	
	private int record;
	
	private String tierName;
	
	private int group;
	
	private String message;

	public ValidationEvent(Session session, String message) {
		super();
		this.session = session;
		this.message = message;
	}

	public ValidationEvent(Session session, int record, String message) {
		super();
		this.session = session;
		this.record = record;
		this.message = message;
	}

	public ValidationEvent(Session session, int record, String tierName,
			int group, String message) {
		super();
		this.session = session;
		this.record = record;
		this.tierName = tierName;
		this.group = group;
		this.message = message;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public int getRecord() {
		return record;
	}

	public void setRecord(int record) {
		this.record = record;
	}

	public String getTierName() {
		return tierName;
	}

	public void setTierName(String tierName) {
		this.tierName = tierName;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Can this event be automatically fixed?  Sub-classes
	 * should override this method.
	 * 
	 * @return can the validation event be fixed
	 */
	public boolean canFix() {
		return false;
	}
	
	/**
	 * Options for fixing the problem identified by this
	 * validation event.
	 * 
	 * @return a list of validation options or an empty
	 *  list if this problem does not have a quick fix
	 */
	public List<SessionQuickFix> getQuickFixes() {
		return new ArrayList<>();
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
