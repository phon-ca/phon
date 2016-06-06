package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.model.api.DataSourceHandle;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;

import ca.phon.app.VersionInfo;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogBuffer;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.app.opgraph.nodes.log.BirtBufferPanelExtension;
import ca.phon.app.opgraph.nodes.log.BirtDesignEngine;
import ca.phon.app.opgraph.nodes.log.BirtReportEngine;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.project.Project;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.PrefHelper;

public class CreateReportAction extends HookableAction {
	
	private final static Logger LOGGER = Logger.getLogger(CreateReportAction.class.getName());
	
	private static final long serialVersionUID = 9222399609937613349L;

	private final static String TXT = "Printable report...";
	
	private final static String DESC = "Create printable HTML report of results";
	
	private final static String MASTER_REPORT = "birt/master.rptdesign";
	
	private final static String USER_REPORT_FOLDER = PrefHelper.getUserDataFolder() + 
			File.separator + "reports";
	
	private final static String DEFAULT_REPORT_FOLDER = "__res/reports";
	
	private NodeWizard wizard;
	
	public CreateReportAction(NodeWizard wizard) {
		super();
		
		this.wizard = wizard;
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	public String getReportFolder() {
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		String initialFolder = USER_REPORT_FOLDER;
		final Project project = wizard.getExtension(Project.class);
		if(project != null) {
			initialFolder = project.getLocation() + File.separator + DEFAULT_REPORT_FOLDER;
		}
		initialFolder += File.separator + wizard.getWizardExtension().getWizardTitle() 
				+ " (" + date.format(formatter) + ")";
		return initialFolder;
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setTitle("Select Report Folder");
		props.setMessage("Select folder for report output files");
		props.setPrompt("Select report folder");
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(false);
		props.setCanCreateDirectories(true);
		props.setAllowMultipleSelection(false);
		props.setParentWindow(wizard);
		
		final String folderPath = getReportFolder();
		final File folder = new File(folderPath);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		props.setInitialFolder(folderPath);
		
		props.setListener( (e) -> {
			if(e.getDialogData() != null) {
				try {
					saveBuffers((String)e.getDialogData());
					createHTMLReport((String)e.getDialogData());
					showReport((String)e.getDialogData());
				} catch (Exception e1) {
					LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
				}
			}
		});
		NativeDialogs.showOpenDialog(props);
	}
	
	private void saveBuffers(String folder) {
		final MultiBufferPanel buffers = wizard.getBufferPanel();
		for(String bufferName:buffers.getBufferNames()) {
			final File bufferFile = new File(folder, bufferName + ".csv");
			final LogBuffer logBuffer = buffers.getBuffer(bufferName).getLogBuffer();
			try {
				final FileOutputStream out = new FileOutputStream(bufferFile);
				final OutputStreamWriter writer = new OutputStreamWriter(out, logBuffer.getEncoding());
				writer.write(logBuffer.getText());
				writer.flush();
				writer.close();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	private void createHTMLReport(String folder) throws DesignFileException, SemanticException, IOException, EngineException, ScriptException {
		final ReportDesignHandle designHandle = createReportDesign(wizard.getWizardExtension().getWizardTitle(), folder);
		generateHTMLReport(designHandle, folder);
	}
	
	private void showReport(String folder) throws MalformedURLException {
		final String htmlFile = folder + File.separator + 
				wizard.getWizardExtension().getWizardTitle() + ".html";
		OpenFileLauncher.openURL((new File(htmlFile)).toURI().toURL());
	}
	
	private void generateHTMLReport(ReportDesignHandle designHandle, String folder) throws EngineException, ScriptException {
		BirtReportEngine reportEngine = new BirtReportEngine();
		IReportRunnable design = reportEngine.openReportDesign(designHandle);
		
		design.getDesignInstance().setUserProperty("reportName", wizard.getWizardExtension().getWizardTitle());
		
		reportEngine.renderHTMLDocument(wizard.getWizardExtension().getWizardTitle(), folder + File.separator + 
				wizard.getWizardExtension().getWizardTitle() + ".html", design);
	}
	
	private ReportDesignHandle createReportDesign(String name, String folder) throws DesignFileException, SemanticException, IOException {
		final BirtDesignEngine designEngine = new BirtDesignEngine();
		final ReportDesignHandle reportDesign = 
				designEngine.openReportDesign(MASTER_REPORT, getClass().getClassLoader().getResourceAsStream(MASTER_REPORT));
		
		reportDesign.setAuthor("Phon " + VersionInfo.getInstance().getLongVersion());
		reportDesign.setTitle(wizard.getWizardExtension().getWizardTitle());
		
		final String dsName = "Data Source";
		
		final DataSourceHandle dataSource = createDataSourceForFolder(dsName, new File(folder), reportDesign.getElementFactory());
		reportDesign.getDataSources().add(dataSource);
		
		for(String bufferName:wizard.getBufferPanel().getBufferNames()) {
			final BufferPanel bufferPanel = wizard.getBufferPanel().getBuffer(bufferName);
			final BirtBufferPanelExtension ext = bufferPanel.getExtension(BirtBufferPanelExtension.class);
			if(ext != null) {
				ext.addTableToReport(dsName, reportDesign);
			}
		}
		
		designEngine.saveReportDesign(reportDesign, 
				folder + File.separator + name + ".rptdesign");
		return reportDesign;
	}
	
	public DataSourceHandle createDataSourceForFolder(String name, File reportFolder, 
			ElementFactory elementFactory) throws SemanticException {
		OdaDataSourceHandle dataSource = elementFactory.newOdaDataSource(name, 
				"org.eclipse.datatools.connectivity.oda.flatfile");
		dataSource.setProperty("HOME", reportFolder.getAbsolutePath());
		dataSource.setProperty("DELIMTYPE", "COMMA");
		dataSource.setProperty("CHARSET", "UTF-8");
		dataSource.setProperty("INCLCOLUMNNAME", "YES");
		dataSource.setProperty("INCLTYPELINE", "NO");
		dataSource.setProperty("TRAILNULLCOLS", "NO");
		
		return dataSource;
	}

}
