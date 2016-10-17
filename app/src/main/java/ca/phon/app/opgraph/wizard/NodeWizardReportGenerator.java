package ca.phon.app.opgraph.wizard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import ca.phon.app.VersionInfo;

public class NodeWizardReportGenerator {
	
	private final NodeWizard wizard;
	
	private final String reportName;
	
	private String outputPath;
	
	public NodeWizardReportGenerator(NodeWizard wizard, String reportName, String outputPath) {
		super();
		
		this.wizard = wizard;
		this.reportName = reportName;
		this.outputPath = outputPath;
	}
	
	public void generateReport()
		throws NodeWizardReportException {
		
		final WizardExtension wizardSettings = wizard.getWizardExtension();
		final NodeWizardReportTemplate template = wizardSettings.getReportTemplate(reportName);
		if(template == null) {
			throw new NodeWizardReportException("No report named " + reportName + " found.");
		}
		
		final NodeWizardReportContext ctx = new NodeWizardReportContext();
		wizard.setupReportContext(ctx);
		wizardSettings.setupReportContext(ctx);
		
		final String reportAsMarkdown = template.merge(ctx);
		final String reportAsHTML = markdownToHTML(reportAsMarkdown);
		
		final StringBuilder sb = new StringBuilder();
		sb.append(htmlPrefix());
		sb.append(reportAsHTML);
		sb.append(htmlSuffix());
		
		File outputFile = null;
		if(this.outputPath == null) {
			try {
				outputFile = 
						(this.outputPath == null ? File.createTempFile("phon", "report") : new File(outputPath));
				outputFile.deleteOnExit();
			} catch (IOException e) {
				throw new NodeWizardReportException(e);
			}
		} else {
			outputFile = new File(outputPath);
		}
		
		try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
			out.write(sb.toString());
			out.flush();
		} catch (IOException e) {
			throw new NodeWizardReportException(e);
		}
	}
	
	private String htmlPrefix() {
		final StringBuilder sb = new StringBuilder();
		
		final char nl = '\n';
		sb.append("<!doctype html>").append(nl);
		sb.append("<html>").append(nl);
		sb.append("<head>").append(nl);
		sb.append("<meta name=\"author\" content=\"Phon ")
		  .append(VersionInfo.getInstance().getShortVersion())
		  .append("\"/>").append(nl);
		sb.append("<meta charset=\"UTF-8\"/>").append(nl);
		
		sb.append("<style>").append(nl);
		try(BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("wizard.css")))) {
			String line = null;
			while((line = in.readLine()) != null) {
				sb.append(line).append(nl);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		sb.append("</style>").append(nl);
		
		sb.append("</head>").append(nl);
		sb.append("<body>").append(nl);
		
		return sb.toString();
	}
	
	private String htmlSuffix() {
		return "</body>\n</html>";
	}
	
	private String markdownToHTML(String md) {
		List<Extension> extensions = Arrays.asList(TablesExtension.create());

		final Parser parser = Parser.builder().extensions(extensions).build();
		final Node doc = parser.parse(md);
		final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
		return renderer.render(doc);
	}
}
