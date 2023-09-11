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

import ca.phon.extensions.*;
import ca.phon.util.OSInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Helper class for holding location for sessions
 * in a project.
 */
public class SessionPath implements IExtendable, Comparable<SessionPath> {

	public final static String PATH_SEP = OSInfo.isWindows() ? "\\" : "/";

	private String folder;

	private String sessionFile;

	private final ExtensionSupport extSupport = new ExtensionSupport(SessionPath.class, this);

	/**
	 * Constructor
	 */
	SessionPath() {
		this("", "");
	}

	SessionPath(String path) {
		super();
		int sepIdx = path.lastIndexOf(PATH_SEP);
		if(sepIdx < 0) {
			setFolder("");
			setSessionFile(path);
		} else {
			setFolder(path.substring(0, sepIdx));
			setSessionFile(path.substring(sepIdx + 1));
		}
		
		extSupport.initExtensions();
	}

	public SessionPath(String folder, String sessionFile) {
		super();
		this.folder = folder;
		this.sessionFile = sessionFile;
		extSupport.initExtensions();
	}

	/**
	 * Get corpus
	 */
	public String getFolder() {
		return this.folder;
	}

	/**
	 * Set corpus
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * Get session
	 */
	public String getSessionFile() {
		return this.sessionFile;
	}

	/**
	 * Set session
	 */
	public void setSessionFile(String sessionFile) {this.sessionFile = sessionFile;
	}

	@Override
	public int hashCode() {
		long hash = getFolder().hashCode();
		hash = hash * 31 + getSessionFile().hashCode();
		return (int)hash;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if(getFolder() != null && !getFolder().isEmpty()) {
			builder.append(getFolder()).append(PATH_SEP);
		}
		builder.append(getSessionFile());
		return builder.toString();
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}


	@Override
	public int compareTo(@NotNull SessionPath o) {
		return toString().compareTo(o.toString());
	}

}
