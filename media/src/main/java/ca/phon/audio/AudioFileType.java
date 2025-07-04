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
package ca.phon.audio;

import ca.phon.plugin.*;

import java.util.*;

/**
 * Audio file types
 *
 */
public class AudioFileType {
	final public static AudioFileType AIFF = new AudioFileType("AIFF file", "aiff", "aif");
	final public static AudioFileType AIFC = new AudioFileType("AIFC file", "aifc", "aic");
	final public static AudioFileType WAV = new AudioFileType("WAV file", "wav");
	
	private String name;
	
	private String[] extensions;
	
	protected AudioFileType(String name, String ... exts) {
		this.name = name;
		this.extensions = exts;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String[] getExtensions() {
		return this.extensions;
	}
		
	/**
	 * Return a list of supported file types.
	 * These include AIFF, AIFC, WAV and any other
	 * registered AudioFileType plug-ins
	 */
	public static List<AudioFileType> getSupportedFileTypes() {
		List<AudioFileType> retVal = new ArrayList<>();
		retVal.add(AIFF);
		retVal.add(AIFC);
		retVal.add(WAV);
		
		for(IPluginExtensionPoint<AudioFileType> audioFileTypeExtPt:PluginManager.getInstance().getExtensionPoints(AudioFileType.class)) {
			AudioFileType extendedType = audioFileTypeExtPt.getFactory().createObject();
			retVal.add(extendedType);
		}
		
		return retVal;
	}
	
}
