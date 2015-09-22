package ca.phon.app.project;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JList;

import org.apache.commons.io.FileUtils;

import ca.phon.project.Project;
import ca.phon.project.ProjectPath;
import ca.phon.ui.dnd.FileTransferHandler;
import ca.phon.ui.nativedialogs.FileFilter;

public class ProjectDataTransferHandler extends FileTransferHandler {
	
	private final static Logger LOGGER = Logger.getLogger(ProjectDataTransferHandler.class.getName());

	private static final long serialVersionUID = -4261706466908550605L;
	
	private ProjectWindow window;
	
	public ProjectDataTransferHandler(ProjectWindow window) {
		super();
		this.window = window;
	}
	
	public ProjectWindow getWindow() {
		return this.window;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		boolean canImport = super.canImport(comp, transferFlavors);
		canImport |= Arrays.asList(transferFlavors).contains(ProjectPathTransferable.projectPathListFlavor);
		
		return canImport;
	}

	@Override
	public boolean importData(TransferSupport support) {
		boolean imported = false;
		final Transferable transferable = support.getTransferable();
		if(transferable.isDataFlavorSupported(ProjectPathTransferable.projectPathListFlavor)) {
			try {
				@SuppressWarnings("unchecked")
				List<ProjectPath> projectPathList = 
						(List<ProjectPath>)transferable.getTransferData(ProjectPathTransferable.projectPathListFlavor);
				imported = importProjectPathList(support, projectPathList);
			} catch (IOException | UnsupportedFlavorException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		} else if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			imported = importFileList(support, transferable);
		}
		if(!imported) {
			Toolkit.getDefaultToolkit().beep();
		}
		return imported;
	}

	public boolean importProjectPathList(TransferSupport support, List<ProjectPath> projectPathList) {
		boolean retVal = true;
		for(ProjectPath projectPath:projectPathList) {
			retVal &= importProjectPath(support, projectPath);
		}
		return retVal;
	}
	
	public boolean importProjectPath(TransferSupport support, ProjectPath projectPath) {
		final ProjectWindow window = getWindow();
		final JList.DropLocation dropLocation = (JList.DropLocation)support.getDropLocation();
		final Component comp = support.getComponent();
		ProjectPath dstProjectPath = new ProjectPath(window.getProject());
		if(projectPath.isCorpusPath()) {
			if(comp != window.getCorpusList()) return false;
			final String dropCorpus = 
					(dropLocation.getIndex() >= 0 
					? window.getCorpusList().getModel().getElementAt(dropLocation.getIndex())
							: "");
			// import corpus including all sessions
			// don't continue if dropped on self
			if(projectPath.getProject() == dstProjectPath.getProject()
					&& dropCorpus.equals(projectPath.getCorpus())) return false;
			int idx = 1;
			String dupCorpusName = projectPath.getCorpus() + " (1)";
			while(dstProjectPath.getProject().getCorpora().contains(dupCorpusName)) {
				dupCorpusName = projectPath.getCorpus() + " (" + (++idx) + ")";
			}
			dstProjectPath.setCorpus(dupCorpusName);
			try {
				FileUtils.copyDirectory(new File(projectPath.getAbsolutePath()),
						new File(dstProjectPath.getAbsolutePath()));
				getWindow().refreshProject();
				return true;
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				return false;
			}
		} else if(projectPath.isSessionPath()) {
			String dstCorpus = null;
			// determine corpus
			if(comp == window.getCorpusList()) {
				// if no drop element, use corpus from destination
				dstCorpus = (dropLocation.getIndex() >= 0
						? window.getCorpusList().getModel().getElementAt(dropLocation.getIndex())
						: projectPath.getCorpus());
			} else if(comp == window.getSessionList()) {
				dstCorpus = window.getSelectedCorpus();
				if(dstCorpus == null) return false;
				
				String dropSession = (dropLocation.getIndex() >= 0
						? window.getSessionList().getModel().getElementAt(dropLocation.getIndex()) 
						: "");
				// don't continue if dropped on self
				if(projectPath.getProject() == dstProjectPath.getProject()
						&& dropSession.equals(projectPath.getSession())) return false;
			}
			dstProjectPath.setCorpus(dstCorpus);
			String dstSessionName = projectPath.getSession();
			int idx = 0;
			while(dstProjectPath.getProject().getCorpusSessions(
					dstProjectPath.getCorpus()).contains(dstSessionName)) {
				dstSessionName = projectPath.getSession() + " (" + (++idx) + ")";
			}
			dstProjectPath.setSession(dstSessionName);
			
			// create corpus if it does not exist
			if(!dstProjectPath.getProject().getCorpora().contains(dstCorpus)) {
				try {
					dstProjectPath.getProject().addCorpus(dstCorpus, 
							projectPath.getProject().getCorpusDescription(projectPath.getCorpus()));
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					return false;
				}
			}
			try {
				FileUtils.copyFile(new File(projectPath.getAbsolutePath()),
						new File(dstProjectPath.getAbsolutePath()));
				window.refreshProject();
				return true;
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				return false;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean importFileList(TransferSupport support, Transferable transferable) {
		List<File> files = new ArrayList<>();
		try {
			boolean retVal = true;
			files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
			for(File file:files) {
				retVal &= importFile(support, file);
			}
			return retVal;
		} catch (IOException | UnsupportedFlavorException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return false;
	}
	
	public boolean importFile(TransferSupport support, File file) throws IOException {
		final Component comp = support.getComponent();
		final Project project = getWindow().getProject();
		
		// TODO import file as project path if possible
		
		if(file.isDirectory()) {
			// copy folder if dropped on corpus list
			if(comp == getWindow().getCorpusList()) {
				int idx = 0;
				String corpusName = file.getName();
				while(project.getCorpora().contains(corpusName)) {
					corpusName = file.getName() + " (" + (++idx) + ")";
				}
				final File destFile = new File(project.getCorpusPath(corpusName));
				FileUtils.copyDirectory(file, destFile);
				window.refreshProject();
				return true;
			}
		} else if(file.isFile()) {
			if(comp == getWindow().getSessionList()) {
				final FileFilter xmlFileFilter = FileFilter.xmlFilter;
				if(xmlFileFilter.accept(file)) {
					String corpus = getWindow().getSelectedCorpus();
					if(corpus == null) return false;

					int idx = 0;
					String fileName = 
							file.getName().substring(0, file.getName().length()-4);
					String sessionName = fileName;
					while(project.getCorpusSessions(corpus).contains(sessionName)) {
						sessionName = fileName + " (" + (++idx) + ")";
					}
					final File destFile = new File(project.getSessionPath(corpus, sessionName));
					FileUtils.copyFile(file, destFile);
					window.refreshProject();
					return true;
				}
			}
		}
		return false;
	}
	
}
