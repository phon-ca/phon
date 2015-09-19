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
