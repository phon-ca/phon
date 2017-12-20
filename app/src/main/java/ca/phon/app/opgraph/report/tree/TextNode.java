package ca.phon.app.opgraph.report.tree;

public class TextNode extends ReportTreeNode {
	
	private String text;
	
	public TextNode(String title, String text) {
		super(title);
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getReportTemplateBlock() {
		final StringBuffer buffer = new StringBuffer();
		
		buffer.append("#h").append(getLevel()).append("(\"").append(getTitle()).append("\")\n\n");
		buffer.append(getText());
		
		return buffer.toString();
	}

}
