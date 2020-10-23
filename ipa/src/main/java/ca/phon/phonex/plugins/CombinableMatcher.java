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
package ca.phon.phonex.plugins;

import ca.phon.phonex.*;

/**
 * Interface for plug-in matchers which are able to be combined.
 * This means that if the same plug-in matcher is specified multiple times,
 * they are 'combined' into a single matcher.  This is useful, for example,
 * with the SyllableConstituentMatcher.
 */
public interface CombinableMatcher {

	public void combineMatcher(PhoneMatcher matcher);
	
}
