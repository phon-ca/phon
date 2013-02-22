/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.session;

/**
 * A transcriber.
 *
 */
public interface Transcriber {
	
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
