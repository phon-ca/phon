/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.query.report;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.RectanglePainter;

import ca.phon.query.report.io.Section;
import ca.phon.ui.HidablePanel;
import ca.phon.ui.PhonGuiConstants;


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
		
		MattePainter matte = new MattePainter(PhonGuiConstants.PHON_UI_STRIP_COLOR);
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
