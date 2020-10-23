/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.util.*;

import ca.phon.plugin.*;

/**
 * Factory for creating {@link SessionWriter}s
 *
 */
public class SessionOutputFactory {

	private List<IPluginExtensionPoint<SessionWriter>> writerExtPts;
	
	/**
	 * Constructor
	 */
	public SessionOutputFactory() {
		super();
		writerExtPts = PluginManager.getInstance().getExtensionPoints(SessionWriter.class);
	}
		
	public List<SessionIO> availableSessionIOs() {
		final List<SessionIO> retVal = new ArrayList<>();
		
		for(IPluginExtensionPoint<SessionWriter> extPt:writerExtPts) {
			final SessionWriter writer = extPt.getFactory().createObject(new Object[0]);
			final SessionIO sessionIO = writer.getClass().getAnnotation(SessionIO.class);
			if(sessionIO != null) {
				retVal.add(sessionIO);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Create a new session writer
	 * 
	 * @return a new {@link SessionWriter} or <code>null</code>
	 *  if a compatible writer could not be found
	 */
	public SessionWriter createWriter() {
		SessionWriter retVal = null;
		
		if(writerExtPts.size() > 0) {
			retVal = writerExtPts.get(0).getFactory().createObject(new Object[0]);
		}
		
		return retVal;
	}
	
	/**
	 * Create a new session writer for the given version name.
	 * 
	 * @param version
	 * @return a new {@link SessionWriter} or <code>null</code>
	 *  if a writer for the version is not found
	 */
	public SessionWriter createWriter(String id, String version) {
		SessionWriter retVal = null;
		
		for(IPluginExtensionPoint<SessionWriter> extPt:writerExtPts) {
			final SessionWriter writer = extPt.getFactory().createObject(new Object[0]);
			final SessionIO sessionIO = writer.getClass().getAnnotation(SessionIO.class);
			if(sessionIO != null && sessionIO.id().equals(id) && sessionIO.version().equals(version)) {
				retVal = writer;
				break;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Create a new session writer given the SessionIO annotation.
	 * 
	 * @param sessionIO
	 * @return a new {@link SessionWriter} or <code>null</code> if not found
	 */
	public SessionWriter createWriter(SessionIO sessionIO) {
		SessionWriter retVal = null;
		for(IPluginExtensionPoint<SessionWriter> extPt:writerExtPts) {
			final SessionWriter writer = extPt.getFactory().createObject(new Object[0]);
			final SessionIO sIO = writer.getClass().getAnnotation(SessionIO.class);
			if(sessionIO != null && sessionIO.id().equals(sIO.id()) && sessionIO.version().equals(sIO.version())) {
				retVal = writer;
				break;
			}
		}
		return retVal;
	}
}
