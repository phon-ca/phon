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
package ca.phon.app.query.report;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.painter.*;

import ca.phon.query.report.io.*;
import ca.phon.ui.*;


/**
 * Implements common options for report section editors.
 * This panel uses a border layout.  Subclasses should
 * place their contents in BorderLayout.CENTER
 *
 */
public class SectionPanel<T extends Section> extends JPanel {
	
	private static final long serialVersionUID = -1332139278513976981L;

	/** List of section listeners */
	private List<SectionListener> sectionListeners = 
		new ArrayList<SectionListener>();
	
	/** The section */
	private T section;
	
	private JTextField nameField;
	
	/**
	 * Information panel
	 */
//	private JXLabel infoLabel;
	protected HidablePanel infoLabel = new HidablePanel("ca.phon.report.SectionPanel.infoLabel");
	
	public SectionPanel(T section) {
		super();
		
		this.section = section;
		
		initPanel();
	}
	
	private void initPanel() {
		setLayout(new BorderLayout());
		
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BorderLayout());
		
		namePanel.setBorder(BorderFactory.createTitledBorder("Title"));
		
		nameField = new JTextField();
		
		GridLayout gl = new GridLayout(1, 1);
		JXPanel infoPanel = new JXPanel(gl) {
			@Override
			public Insets getInsets() {
				return new Insets(5, 5, 5, 5);
			}
		};
		
		MattePainter matte = new MattePainter(UIManager.getColor("control"));
		RectanglePainter rectPainter = new RectanglePainter(1, 1, 1, 1);
		rectPainter.setFillPaint(PhonGuiConstants.PHON_SHADED);
		CompoundPainter<JXLabel> cmpPainter = new CompoundPainter<JXLabel>(matte, rectPainter);
		
		infoPanel.add(infoLabel);
		infoPanel.setBackgroundPainter(cmpPainter);
		
		if(section.getName() != null) {
			nameField.setText(section.getName());
		}
		
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateName();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateName();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateName();
			}
			
			private void updateName() {
				section.setName(nameField.getText());
				fireNameChanged();
			}
		});
		
		namePanel.add(nameField, BorderLayout.NORTH);
		add(namePanel, BorderLayout.NORTH);
		
//		add(infoPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Set text for the information label.
	 * 
	 * @param text
	 */
	public void setInformationText(String prop, String text) {
		if(infoLabel != null) {
			remove(infoLabel);
		}
		infoLabel = new HidablePanel(prop);
		infoLabel.setBottomLabelText(text);
		
		add(infoLabel, BorderLayout.SOUTH);
		super.invalidate();
		super.validate();
	}
	
	/* Listeners */
	public void addSectionListener(SectionListener listener) {
		synchronized(sectionListeners) {
		sectionListeners.add(listener);
		}
	}
	
	public void removeSectionListener(SectionListener listener) {
		synchronized(sectionListeners) {
		sectionListeners.remove(listener);
		}
	}
	
	public SectionListener[] getListeners() {
		SectionListener[] retVal = new SectionListener[0];
		
		synchronized(sectionListeners) {
			retVal = sectionListeners.toArray(retVal);
		}
		
		return retVal;
	}
	
	public void fireNameChanged() {
		for(SectionListener listener:getListeners()) {
			listener.nameChanged(section);
		}
	}
	
	public T getSection() {
		return this.section;
	}
}
