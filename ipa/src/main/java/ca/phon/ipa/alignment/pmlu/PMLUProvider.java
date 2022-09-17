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
package ca.phon.ipa.alignment.pmlu;

import ca.phon.extensions.*;
import ca.phon.ipa.alignment.PhoneMap;

/**
 * Attaches a new PMLU object to every PhoneMap when initialized.
 * 
 */
@Extension(PhoneMap.class)
public class PMLUProvider implements ExtensionProvider {

	@Override
	public void installExtension(IExtendable obj) {
		// we are ensured that obj will be of the correct type
		final PhoneMap pm = (PhoneMap)obj;
		pm.putExtension(PMLU.class, new PMLU(pm));
	}

}
