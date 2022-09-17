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
package ca.phon.app.opgraph.nodes.report;

import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.opgraph.*;
import ca.phon.opgraph.exceptions.ProcessingException;

public abstract class ReportSectionNode extends OpNode {

	public final InputField sectionNameInput = 
			new InputField("sectionName", "Title of element in ToC", true, true, String.class);
	
	public final InputField parentNodeInput = 
			new InputField("parent", "Parent report tree node", true, true, ReportTreeNode.class);
	
	public final InputField addToBeginningInput =
			new InputField("addToBeginning", "Add new section node to start of child list", true, true, Boolean.class);
	
	public final OutputField parentNodeOutput =
			new OutputField("parent", "Parent report tree node", true, ReportTreeNode.class);
	
	public final OutputField sectionNodeOutput = 
			new OutputField("section", "Section report node created", true, ReportTreeNode.class);
	
	public ReportSectionNode() {
		super();
		
		putField(sectionNameInput);
		putField(parentNodeInput);
		putField(addToBeginningInput);
		
		putField(parentNodeOutput);
		putField(sectionNodeOutput);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final ReportTreeNode parentNode = 
				(context.containsKey(parentNodeInput) ? (ReportTreeNode)context.get(parentNodeInput) : null);
		final Boolean addToBeginning = 
				(context.get(addToBeginningInput) != null ? (Boolean)context.get(addToBeginningInput) : false);
		
		final ReportTreeNode sectionNode = createReportSectionNode(context);
		
		if(parentNode != null) {
			if(addToBeginning)
				parentNode.add(0, sectionNode);
			else
				parentNode.add(sectionNode);
		}
				
		context.put(parentNodeOutput, parentNode);
		context.put(sectionNodeOutput, sectionNode);
	}

	protected abstract ReportTreeNode createReportSectionNode(OpContext context);

}
