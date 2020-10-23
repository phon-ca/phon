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

import java.io.*;

/**
 * XML Serializer.
 * 
 * 
 */
public interface XMLSerializer {
	
	/**
	 * Read an object of type {@link #declaredType()} from
	 * the given {@link InputStream}.
	 * 
	 * @param input
	 * @return the read object
	 * 
	 * @throws IOException if an error occurs during
	 *  read
	 */
	public <T> T read(Class<T> type, InputStream input) throws IOException;
	
	/**
	 * Write an object of type {@link #declaredType()} to
	 * the given {@link OutputStream} as xml data.
	 * 
	 * @param output
	 * 
	 * @throws IOException if an error occurs during
	 *  write
	 */
	public <T> void write(Class<T> type, T obj, OutputStream output) throws IOException;
	
	/**
	 * The type handled by this serializer.
	 * 
	 * @return
	 */
	public Class<?> declaredType();

}
