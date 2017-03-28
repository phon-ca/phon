package ca.phon.app.opgraph.wizard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import ca.phon.app.VersionInfo;

public class NodeWizardReportGenerator {

	private final static Logger LOGGER = Logger.getLogger(NodeWizardReportGenerator.class.getName());

	private final NodeWizard wizard;

	private final String reportTemplate;

	private OutputStream stream;

	public NodeWizardReportGenerator(NodeWizard wizard, String reportTemplate, String outputPath)
		throws IOException {
		this(wizard, reportTemplate, new FileOutputStream(outputPath));
	}

	public NodeWizardReportGenerator(NodeWizard wizard, String reportTemplate, OutputStream stream) {
		super();

		this.wizard = wizard;
		this.reportTemplate = reportTemplate;
		this.stream = stream;
	}

	public void generateReport()
		throws NodeWizardReportException {

		final WizardExtension wizardSettings = wizard.getWizardExtension();
		final NodeWizardReportContext ctx = new NodeWizardReportContext();
		wizard.setupReportContext(ctx);
		wizardSettings.setupReportContext(ctx);

		final NodeWizardReportTemplate template = new NodeWizardReportTemplate("temp", this.reportTemplate);

		final String reportAsMarkdown = template.merge(ctx);
		final String reportAsHTML = markdownToHTML(reportAsMarkdown);

		final StringBuilder sb = new StringBuilder();
		sb.append(htmlPrefix(wizardSettings));
		sb.append(reportAsHTML);
		sb.append(htmlSuffix());

		try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"))) {
			out.write(sb.toString());
			out.flush();
		} catch (IOException e) {
			throw new NodeWizardReportException(e);
		}
	}

	private String htmlPrefix(WizardExtension ext) {
		final StringBuilder sb = new StringBuilder();

		final char nl = '\n';
		sb.append("<!doctype html>").append(nl);
		sb.append("<html lang='en'>").append(nl);
		sb.append("<head>").append(nl);
		sb.append("<title>").append("Phon - " + ext.getWizardTitle()).append("</title>").append(nl);
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
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		sb.append("</style>").append(nl);

		sb.append("<script>\n");
		// read in wizard javascript file
		try(BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("wizard.js")))) {
			String line = null;
			while((line = in.readLine()) != null) {
				sb.append(line).append(nl);
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		sb.append("\n</script>");

		sb.append("</head>").append(nl);
		sb.append("<body onload='page_init()'><div id='sidenav' class='sidenav'>").append(nl);
		sb.append("<span class='menubtn' onClick='toggleNav()'><img id='menuicon' width='16' height='16'></img></span>");
		sb.append(nl).append("<div id='toc'></div>").append(nl);
		sb.append("</div><div id='main'>").append(nl);

		return sb.toString();
	}

	private String htmlSuffix() {
		return "</div></body>\n</html>";
	}

	private String markdownToHTML(String md) {
		List<Extension> extensions = Arrays.asList(TablesExtension.create());

		final Parser parser = Parser.builder().extensions(extensions).build();
		final Node doc = parser.parse(md);
		final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
		return renderer.render(doc);
	}

}
