/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.wizard;

import ca.gedge.opgraph.OpNode;

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
