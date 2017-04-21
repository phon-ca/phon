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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.phon.project.ProjectPath;

public class ProjectPathTransferable implements Transferable {
	/**
	* This is a data flavor used for transferring lists of {@link ProjectPath}s.  The
	* representation type is a {@link java.util.List}, with each 
	* element of the list being a {@link ProjectPath}.</code>.
	*/
	public static final DataFlavor projectPathListFlavor = new DataFlavor("application/x-project-path-list; class=java.util.List",
                    "Project Path List");
	
	private final DataFlavor[] flavors = new DataFlavor[] { projectPathListFlavor, DataFlavor.javaFileListFlavor,
			DataFlavor.stringFlavor };
	
	private List<ProjectPath> projectPaths;
	
	public ProjectPathTransferable(List<ProjectPath> paths) {
		super();
		this.projectPaths = Collections.unmodifiableList(paths);
	}
	
	public List<ProjectPath> getProjectPaths() {
		return this.projectPaths;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		Object retVal = null;
		if(flavor == projectPathListFlavor) {
			retVal = getProjectPaths();
		} else if(flavor == DataFlavor.javaFileListFlavor) {
			final List<File> fileList = new ArrayList<>();
			for(ProjectPath path:getProjectPaths()) {
				fileList.add(new File(path.getAbsolutePath()));
			}
			retVal = fileList;
		} else if(flavor == DataFlavor.stringFlavor) {
			final StringBuilder sb = new StringBuilder();
			for(ProjectPath path:getProjectPaths()) {
				if(sb.length() > 0) sb.append(";");
				sb.append(path.getAbsolutePath());
			}
			retVal = sb.toString();
		} else {
			throw new IllegalArgumentException("Invalid data flavor " + flavor.toString());
		}
		return retVal;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return Arrays.asList(flavors).contains(flavor);
	}
	
}
