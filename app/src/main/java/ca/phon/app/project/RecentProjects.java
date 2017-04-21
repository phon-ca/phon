/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.phon.util.RecentFiles;

public class RecentProjects extends RecentFiles {
	
	public static final String PROJECT_HISTORY_PROP = RecentProjects.class.getName() + ".stack";
	
	public static final int MAX_PROJECTS = 10;
	
	private List<File> projectHistory = new ArrayList<>();
	
	public RecentProjects() {
		super(PROJECT_HISTORY_PROP, MAX_PROJECTS);
	}
	
}
