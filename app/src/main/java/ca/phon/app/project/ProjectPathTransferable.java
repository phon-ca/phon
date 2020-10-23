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
package ca.phon.app.project;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

import ca.phon.project.*;

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
