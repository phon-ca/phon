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
package ca.phon.session.io;

import ca.phon.extensions.Extension;
import ca.phon.session.Session;

/**
 * Extension added to {@link Session} objects to keep track of the
 * original source format.
 * 
 */
@Extension(Session.class)
public class OriginalFormat {

	private SessionIO sessionIO;
	
	/**
	 * Should the application issue the original format warning
	 * when saving?  This should only be done once.
	 */
	private boolean issueWarning = true;

	public OriginalFormat() {
		super();
	}
	
	public OriginalFormat(SessionIO io) {
		super();
		setSessionIO(io);
	}

	public SessionIO getSessionIO() {
		return sessionIO;
	}

	public void setSessionIO(SessionIO sessionIO) {
		this.sessionIO = sessionIO;
	}
	
	public boolean isIssueWarning() {
		return this.issueWarning;
	}
	
	public void setIssueWarning(boolean issueWarning) {
		this.issueWarning = issueWarning;
	}
	
}
