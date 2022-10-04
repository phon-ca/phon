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
package ca.phon.app.actions;

import javax.xml.stream.events.StartElement;
import java.io.*;
import java.util.*;

public interface XMLOpenHandler {

	/**
	 * Get supported extensions (without '.')
	 */
	public Set<String> supportedExtensions();
	
	/**
	 * @param startEle
	 * @return <code>true</code> if this reader can handle the given
	 *  root start element.
	 */
	public boolean canRead(StartElement startEle);
	
	/**
	 * Open xml document
	 *
	 * @param file
	 * @param args
	 */
	public void openXMLFile(File file, Map<String, Object> args) throws IOException;
	
}
