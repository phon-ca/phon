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
import ca.phon.util.Tuple;

import java.util.Set;

/**
 * Helper class for holding location for sessions
 * in a project.
 */
public class SessionPath extends Tuple<String, String> implements IExtendable {
	
	private final ExtensionSupport extSupport = new ExtensionSupport(SessionPath.class, this);

	/**
	 * Constructor
	 */
	public SessionPath() {
		this("", "");
	}

	public SessionPath(String path) {
		super();
		int dotIdx = path.indexOf('.');
		if(dotIdx < 0) throw new IllegalArgumentException(path);
		setObj1(path.substring(0, dotIdx));
		setObj2(path.substring(dotIdx+1));
		
		extSupport.initExtensions();
	}

	public SessionPath(String corpus, String session) {
		super(corpus, session);
		
		extSupport.initExtensions();
	}

	/**
	 * Get corpus
	 */
	public String getCorpus() {
		return getObj1();
	}

	/**
	 * Set corpus
	 */
	public void setCorpus(String corpus) {
		setObj1(corpus);
	}

	/**
	 * Get session
	 */
	public String getSession() {
		return getObj2();
	}

	/**
	 * Set session
	 */
	public void setSession(String session) {
		setObj2(session);
	}

	@Override
	public int hashCode() {
		long hash = getObj1().hashCode();
		hash = hash * 31 + getObj2().hashCode();
		return (int)hash;
	}

	@Override
	public String toString() {
		return getCorpus() + "." + getSession();
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

	
	
}
