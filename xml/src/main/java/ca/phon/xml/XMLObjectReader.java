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
package ca.phon.xml;

import org.w3c.dom.*;

import java.io.IOException;

/**
 * A class that reads objects from a given XML stream.
 * 
 */
public interface XMLObjectReader<T> {

	/**
	 * Read from the given xml input stream.
	 * 
	 * @param eventReader
	 * @param type
	 * 
	 * @return object of given type read from the
	 *  given eventReader
	 *  
	 * @throws IOException if something goes wrong
	 */
	public T read(Document doc, Element ele)
		throws IOException;
	
}
