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
package ca.phon.app.opgraph.nodes.query;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.query.report.ReportEditor;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.ResultSet;
import ca.phon.query.report.ReportBuilder;
import ca.phon.query.report.ReportBuilderException;
import ca.phon.query.report.ReportBuilderFactory;
import ca.phon.query.report.io.ReportDesign;
import ca.phon.util.PrefHelper;

@OpNodeInfo(
		name="Report",
		category="Report",
		description="Create report from a pre-defined design file",
		showInLibrary=true
)
public class ReportDesignNode extends OpNode implements NodeSettings {
	
	private final static Logger LOGGER = Logger.getLogger(ReportDesignNode.class.getName());
	
	public final static String AUTOSAVE_FILENAME = "lastreport.xml";
	
	private InputField projectInputField = 
			new InputField("project", "Project", false, true, Project.class);
	
	private InputField queryInputField =
			new InputField("query", "Query", false, true, Query.class);

	private InputField resultSetsField =
			new InputField("results", "Result sets from query", false, true, ResultSet[].class);
	
	private OutputField projectOutputField = 
			new OutputField("project", "Project", true, Project.class);
	

	private OutputField reportField = 
			new OutputField("report", "Generated report as a string", true, String.class);
	
	private ReportDesign reportDesign;
	
	/**
	 * If <code>true</code> the report created using this node will be
	 * saved as the 'last report.'  The 'last report' will also be loaded
	 * with this node
	 */
	private boolean useLastReport = false;
	
	private ReportEditor reportEditor;
	
	private JCheckBox useLastReportBox;
	
	private JPanel settingsPanel;
	
	public ReportDesignNode() {
		this(new ReportDesign());
	}
	
	public ReportDesignNode(ReportDesign reportDesign) {
		super();

		this.reportDesign = reportDesign;
		
		putField(projectInputField);
		putField(queryInputField);
		putField(resultSetsField);
		putField(projectOutputField);
		putField(reportField);
		
		putExtension(NodeSettings.class, this);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final Project project = (Project)context.get(projectInputField);
		if(project == null) throw new ProcessingException(null, "Project cannot be null");
		
		final Query query = (Query)context.get(queryInputField);
		if(query == null) throw new ProcessingException(null, "Query cannot be null");
		
		final ResultSet[] resultSets = (ResultSet[])context.get(resultSetsField);
		if(resultSets == null || resultSets.length == 0)
			throw new ProcessingException(null, "No result sets given");
		
		final ReportBuilder builder = ReportBuilderFactory.getInstance().getBuilder("CSV");
		try {
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			builder.buildReport(getReportDesign(), project, query, resultSets, bout);
			
			context.put(reportField, new String(bout.toByteArray(), "UTF-8"));
		} catch (ReportBuilderException | UnsupportedEncodingException e) {
			throw new ProcessingException(null, e);
		}
		
		context.put(projectOutputField, project);
		
		if(isUseLastReport()) {
			// save report
			// use jaxb to save to element
			try {
				JAXBContext ctx = JAXBContext.newInstance("ca.phon.query.report.io");
				Marshaller marshaller = ctx.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				
				QName reportDesignQName = new QName("http://phon.ling.mun.ca/ns/report", "report-design");
				JAXBElement<ReportDesign> reportDesignEle = 
						new JAXBElement<ReportDesign>(reportDesignQName, ReportDesign.class, reportDesign);
				marshaller.marshal(reportDesignEle, new File(PrefHelper.getUserDataFolder(), AUTOSAVE_FILENAME));
			} catch (JAXBException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	public boolean isUseLastReport() {
		return (this.useLastReportBox != null ? this.useLastReportBox.isSelected() : this.useLastReport);
	}
	
	public void setUseLastReport(boolean useLastReport) {
		this.useLastReport = useLastReport;
		if(this.useLastReportBox != null)
			this.useLastReportBox.setSelected(this.useLastReport);
	}
	
	public ReportDesign getReportDesign() {
		return 
				(this.reportEditor != null ? this.reportEditor.getReportDesign() : this.reportDesign);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(this.settingsPanel == null) {
			this.settingsPanel = new JPanel(new BorderLayout());
			
			useLastReportBox = new JCheckBox("Remember outline");
			useLastReportBox.setSelected(useLastReport);

			this.settingsPanel.add(useLastReportBox, BorderLayout.NORTH);
			
			this.reportEditor = 
					(this.reportDesign == null ? new ReportEditor() : new ReportEditor(reportDesign));
			
			this.settingsPanel.add(this.reportEditor, BorderLayout.CENTER);
		}
		return this.settingsPanel;
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {
		
	}

}
