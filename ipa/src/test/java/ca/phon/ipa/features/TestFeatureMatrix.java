/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.ipa.features;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.parser.IPATokens;

@RunWith(JUnit4.class)
public class TestFeatureMatrix {

	private static final Logger LOGGER = Logger
			.getLogger(TestFeatureMatrix.class.getName());
	
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
