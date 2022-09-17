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
package ca.phon.ipa.features;

import ca.phon.ipa.parser.IPATokens;
import junit.framework.Assert;
import org.apache.logging.log4j.LogManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

@RunWith(JUnit4.class)
public class TestFeatureMatrix {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(TestFeatureMatrix.class.getName());
	
	/**
	 * Ensures that all phones defined in the {@link FeatureMatrix}
	 * are supported by the IPA parser.
	 */
	@Test
	public void ensureMatrixFullySupported() throws Exception {
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final Set<Character> fmSet = new TreeSet<Character>(fm.getCharacterSet());
		final Set<Character> tokenSet = new TreeSet<Character>(IPATokens.getSharedInstance().getCharacterSet());
		
		// remove all supported characters
		fmSet.removeAll(tokenSet);
		Assert.assertEquals(0, fmSet.size());
	}
	
//	@Test
//	public void ensureIpaFullySupported() {
//		final FeatureMatrix fm = FeatureMatrix.getInstance();
//		final Set<Character> fmSet = new TreeSet<Character>(fm.getCharacterSet());
//		final Set<Character> tokenSet = new TreeSet<Character>(IPATokens.getSharedInstance().getCharacterSet());
//		
//		// remove all supported characters
//		tokenSet.removeAll(fmSet);
//		Assert.assertEquals(0, tokenSet.size());
//	}
}
