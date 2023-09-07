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
import ca.phon.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Helper class for holding location for sessions
 * in a project.
 */
public class SessionPath implements IExtendable, Comparable<SessionPath> {

	public final static String PATH_SEP = OSInfo.isWindows() ? "\\" : "/";

	private String corpus;

	private String session;

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
			setCorpus("");
			setSession(path);
		} else {
			setCorpus(path.substring(0, sepIdx));
			setSession(path.substring(sepIdx + 1));
		}
		
		extSupport.initExtensions();
	}

	public SessionPath(String corpus, String session) {
		super();
		this.corpus = corpus;
		this.session = session;
		extSupport.initExtensions();
	}

	/**
	 * Get corpus
	 */
	public String getCorpus() {
		return this.corpus;
	}

	/**
	 * Set corpus
	 */
	public void setCorpus(String corpus) {
		this.corpus = corpus;
	}

	/**
	 * Get session
	 */
	public String getSession() {
		return this.session;
	}

	/**
	 * Set session
	 */
	public void setSession(String session) {this.session = session;
	}

	@Override
	public int hashCode() {
		long hash = getCorpus().hashCode();
		hash = hash * 31 + getSession().hashCode();
		return (int)hash;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if(getCorpus() != null && !getCorpus().isEmpty()) {
			builder.append(getCorpus()).append(PATH_SEP);
		}
		builder.append(getSession());
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
