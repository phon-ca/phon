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
package ca.phon.session.spi;

public interface TranscriberSPI {

	/**
	 * The transcriber username
	 */
	public String getUsername();
	
	public void setUsername(String username);
	
	/**
	 * The real name
	 */
	public String getRealName();
	
	public void setRealName(String name);
	
	/**
	 * Using password?
	 */
	public boolean usePassword();
	
	public void setUsePassword(boolean v);
	
	/**
	 * The hashed-password
	 */
	public String getPassword();

	public void setPassword(String password);
	
}
