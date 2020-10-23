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
package ca.phon.session.impl;

import java.util.concurrent.atomic.*;

import ca.phon.session.spi.*;

public class TranscriberImpl implements TranscriberSPI {
	
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
