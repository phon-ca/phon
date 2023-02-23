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
		
		buffer.append(String.format("#h%d(\"%s\" \"%s\")\n", getLevel(), getTitle(), getPath().toString())).append("\n");
		buffer.append(getText());
		
		return buffer.toString();
	}

	@Override
	public ReportTreeNode cloneWithoutChildren() {
		final TextNode tn = new TextNode(super.getTitle(), this.text);
		return tn;
	}

}
