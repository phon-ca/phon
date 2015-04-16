/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.project;

/**
 * Listen for changes to project structure and
 * data.
 * 
 * @author Greg J. Hedlund <ghedlund@cs.mun.ca>
 *
 */
public interface ProjectListener {
	
	/**
	 * Informs the listener of changes to
	 * project structure.
	 * @param ProjectEvent
	 */
	public void projectStructureChanged(ProjectEvent pe);
	
	/**
	 * Informs the listener of changes to
	 * project data.
	 * @param ProjectEvent
	 */
	public void projectDataChanged(ProjectEvent pe);
	
	/**
	 * Informs the listener of changes to
	 * transcript write locks.
	 * @param ProjectEvent
	 */
	public void projectWriteLocksChanged(ProjectEvent pe);
	
}
