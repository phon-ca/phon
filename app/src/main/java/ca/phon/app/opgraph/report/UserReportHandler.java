/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.report;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;

import ca.phon.util.PrefHelper;
import ca.phon.util.resources.FolderHandler;

/**
 * Read xml/opgraph files found in ~/Documents/Phon/reports/
 * 
 */
public class UserReportHandler extends FolderHandler<URL> {

	public final static String DEFAULT_USER_REPORT_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "reports";
	
	public UserReportHandler() {
		this(new File(DEFAULT_USER_REPORT_FOLDER));
	}
	
	public UserReportHandler(File file) {
		super(file);
		
		super.setFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return (pathname.getName().endsWith(".xml") ||
						pathname.getName().endsWith(".opgraph"));
			}
		});
	}

	@Override
	public URL loadFromFile(File f) throws IOException {
		return (f != null ? f.toURI().toURL() : null);
	}

}
