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
package ca.phon.syllabifier.phonex;

import java.util.*;

import ca.phon.phonex.*;

/**
 * Provides the phonex 'mdc' (minimum distance constraint)
 * plug-in for syllabification.  Format of input should be
 * 
 *  INT,[true|false]
 *  
 * Where INT is the minimum distance from the previous phone
 * and the boolean indicates if flat sonority is
 * allowed.
 */
@PhonexPlugin(name = "mdc", description="Minimum distance constraint", arguments= {"distance", "allowFlat"} )
public class SonorityMdcPlugin
implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		if(args.size() != 2)
			throw new IllegalArgumentException("Invalid number of arguments, should be 2, is " + args.size());
		int dist = 0;
		boolean allowFlat = false;
		try {
			dist = Integer.parseInt(args.get(0));
			allowFlat = Boolean.parseBoolean(args.get(1));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
		
		return new SonorityDistancePhoneMatcher(dist, allowFlat);
	}

}
