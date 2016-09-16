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
package ca.phon.session.impl;

import java.util.concurrent.atomic.AtomicReference;

import ca.phon.session.Transcriber;

public class TranscriberImpl implements Transcriber {
	
	private final AtomicReference<String> usernameRef = 
			new AtomicReference<String>();
	
	private final AtomicReference<String> nameRef =
			new AtomicReference<String>();
	
	private final AtomicReference<String> passwordRef =
			new AtomicReference<String>();
	
	private volatile boolean usePassword = false;

	@Override
	public String getUsername() {
		return usernameRef.get();
	}

	@Override
	public void setUsername(String username) {
		usernameRef.getAndSet(username);
	}

	@Override
	public String getRealName() {
		return nameRef.get();
	}

	@Override
	public void setRealName(String name) {
		nameRef.getAndSet(name);
	}

	@Override
	public boolean usePassword() {
		return usePassword;
	}

	@Override
	public void setUsePassword(boolean v) {
		usePassword = v;
	}

	@Override
	public String getPassword() {
		return passwordRef.get();
	}

	@Override
	public void setPassword(String password) {
		passwordRef.getAndSet(password);
	}

}
