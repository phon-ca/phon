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
package ca.phon.ipadictionary;

import ca.phon.util.resources.ResourceLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Iterator;

@RunWith(JUnit4.class)
public class IPADictionaryManagerTest {

	@Test
	public void testDictionaryList() {
		final IPADictionaryLibrary library = IPADictionaryLibrary.getInstance();
		
		final ResourceLoader<IPADictionary> dictLoader = library.getLoader();
		final Iterator<IPADictionary> dictItr = dictLoader.iterator();
		while(dictItr.hasNext()) {
			final IPADictionary dict = dictItr.next();
			System.out.println(dict.getLanguage());
		}
	}
	
}
