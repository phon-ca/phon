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

import java.io.*;
import java.util.*;

/**
 * Service interface for open file handlers.
 */
public interface OpenFileHandler {
	
	/**
	 * 
	 * @return Set of supported file extensions (without '.')
	 */
	public Set<String> supportedExtensions();
	
	/**
	 * Can this handler open this file?
	 * 
	 * @param file
	 * @throws IOException
	 */
	public boolean canOpen(File file) throws IOException;
	
	/**
	 * Open the file in the appropriate editor/viewer.
	 * 
	 * @param file
	 * @param args - arguments pass to the open file handler
	 * @throws IOException
	 */
	public void openFile(File file, Map<String, Object> args) throws IOException;
	
}
