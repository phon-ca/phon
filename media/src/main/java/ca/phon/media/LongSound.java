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
package ca.phon.media;

import java.io.*;
import java.util.*;

import ca.phon.extensions.*;
import ca.phon.plugin.*;
import ca.phon.util.*;

/**
 * LongSound objects allow for access to audio stream information
 * without loading the entire audio file into memory.  Segments may
 * be extracted from the LongSound for analysis.
 * 
 */
public abstract class LongSound implements IExtendable {

	public final static String PREFERRED_LONGSOUND_LOADER_PROP = 
			LongSound.class.getName() + ".preferredLongSoundLoader";
	
	public static LongSound fromFile(File file) throws IOException {
		String preferredLoader = PrefHelper.get(PREFERRED_LONGSOUND_LOADER_PROP, null);
		
		final List<IPluginExtensionPoint<LongSound>> soundLoaders =
				PluginManager.getInstance().getExtensionPoints(LongSound.class);
		if(soundLoaders.size() > 0) {
			IPluginExtensionPoint<LongSound> defaultLoader = (preferredLoader != null 
					? soundLoaders.stream().filter( (l)-> l.getClass().getName().contentEquals(preferredLoader)).findFirst().get()
					: soundLoaders.get(0));
			if(defaultLoader == null) defaultLoader = soundLoaders.get(0);
			try {
				return defaultLoader.getFactory().createObject(file);
			} catch (Exception e) {
				throw new IOException(e);
			}
		} else {
			return new SampledLongSound(file);
		}
	}
	
	private final File file;	
	
	private ExtensionSupport extSupport = new ExtensionSupport(LongSound.class, this);
	
	protected LongSound(File file) {
		super();
		
		this.file = file;
		
		extSupport.initExtensions();
	}
	
	public File getFile() {
		return this.file;
	}

	public abstract void close() throws IOException;
	
	/**
	 * Number of channels in audio file.
	 * @return number of channels
	 */
	public abstract int numberOfChannels();
	
	/**
	 * Length of audio in seconds
	 * 
	 * @return
	 */
	public abstract float length();
	
	/**
	 * Extract portion of sound for analysis.
	 * 
	 * @param startTime
	 * @param endTime
	 * 
	 * @return sound segment
	 */
	public abstract Sound extractPart(float startTime, float endTime);

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
