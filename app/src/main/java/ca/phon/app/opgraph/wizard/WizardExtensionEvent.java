/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.wizard;

import ca.phon.opgraph.OpNode;

/**
 * Event class for {@link WizardExtension} changes.
 *
 */
public class WizardExtensionEvent {

	public static enum EventType {
		TITLE_CHANGED,
		NODE_TITLE_CHANGED,
		NODE_ADDED_TO_SETTINGS,
		NODE_REMOVED_FROM_SETTINGS,
		NODE_MARKED_AS_REQUIRED,
		NODE_MAKRED_AS_NOT_REQUIRED,
		NODE_MAKRED_AS_OPTIONAL,
		NODE_MAKRED_AS_NONOPTIONAL,
		REPORT_TEMPLATE_ADDED,
		REPORT_TEMPLATE_CHANGED,
		REPORT_TEMPLATE_REMOVED
	};
	
	private Object source;
	
	private final EventType eventType;
	
	private String oldTitle;
	
	private String newTitle;
	
	private String reportName;
	
	private String oldReportContent;
	
	private String reportContent;
	
	private OpNode node;
	
	/**
	 * Constructor for TITLE_CHANGED events.
	 * 
	 * @param oldTitle
	 * @param newTitle
	 */
	public WizardExtensionEvent(String oldTitle, String newTitle) {
		super();
		
		this.eventType = EventType.TITLE_CHANGED;
		this.oldTitle = oldTitle;
		this.newTitle = newTitle;
	}
	
	/**
	 * Constructor for NODE_TITLE_CHANGED events
	 * 
	 * @param node
	 * @param oldTitle
	 * @param newTitle
	 */
	public WizardExtensionEvent(OpNode node, String oldTitle, String newTitle) {
		super();
		
		this.eventType = EventType.NODE_TITLE_CHANGED;
		this.oldTitle = oldTitle;
		this.newTitle = newTitle;
	}
	
	/**
	 * Constructor for node events
	 * 
	 * @param eventType
	 * @param node
	 */
	public WizardExtensionEvent(EventType eventType, OpNode node) {
		super();
		
		this.eventType = eventType;
		this.node = node;
	}
	
	/**
	 * Constructor for REPORT_TEMPLATE events
	 * 
	 * @param eventType
	 * @param reportName
	 */
	public WizardExtensionEvent(EventType eventType, String reportName, String oldContent, String reportContent) {
		super();
		
		this.eventType = eventType;
		this.reportName = reportName;
		this.oldReportContent = oldContent;
		this.reportContent = reportContent;
	}
	
	public Object getSource() {
		return this.source;
	}
	
	public void setSource(Object source) {
		this.source = source;
	}

	public String getOldTitle() {
		return oldTitle;
	}

	public void setOldTitle(String oldTitle) {
		this.oldTitle = oldTitle;
	}

	public String getNewTitle() {
		return newTitle;
	}

	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getOldReportContent() {
		return oldReportContent;
	}

	public void setOldReportContent(String oldReportContent) {
		this.oldReportContent = oldReportContent;
	}

	public String getReportContent() {
		return reportContent;
	}

	public void setReportContent(String reportContent) {
		this.reportContent = reportContent;
	}

	public OpNode getNode() {
		return node;
	}

	public void setNode(OpNode node) {
		this.node = node;
	}

	public EventType getEventType() {
		return eventType;
	}
	
}
