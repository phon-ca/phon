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
package ca.phon.app.session;

import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.filter.RecordFilter;
import ca.phon.ui.action.PhonUIAction;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A reusable panel that provides UI for selecting record filter options.
 * Displays radio buttons for "All records" and "Selected records",
 * and a button to open the RecordFilterDialog when "Selected records" is chosen.
 */
public class RecordFilterSelectorPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    private Project project;
    private Session session;
    
    private ButtonGroup radioGroup;
    private JRadioButton allRecordsButton;
    private JRadioButton selectedRecordsButton;
    
    private JButton modifyFilterButton;
    
    private RecordFilter recordFilter;
    
    /**
     * Constructor
     * 
     * @param project the project
     * @param session the session (may be null if not available yet)
     */
    public RecordFilterSelectorPanel(Project project, Session session) {
        this.project = project;
        this.session = session;
        
        init();
    }
    
    /**
     * Constructor without session (for cases where session is set later)
     * 
     * @param project the project
     */
    public RecordFilterSelectorPanel(Project project) {
        this(project, null);
    }
    
    private void init() {
        setLayout(new VerticalLayout(5));
        
        radioGroup = new ButtonGroup();
        
        allRecordsButton = new JRadioButton("All records");
        allRecordsButton.setSelected(true);
        allRecordsButton.addActionListener(new RadioButtonListener());
        radioGroup.add(allRecordsButton);
        
        selectedRecordsButton = new JRadioButton("Selected records");
        selectedRecordsButton.addActionListener(new RadioButtonListener());
        radioGroup.add(selectedRecordsButton);
        
        PhonUIAction<Void> modifyFilterAction = PhonUIAction.eventConsumer(this::onModifyFilter);
        modifyFilterAction.putValue(Action.NAME, "Modify record filter...");
        modifyFilterAction.putValue(Action.SHORT_DESCRIPTION, "Configure record filter settings");
        modifyFilterButton = new JButton(modifyFilterAction);
        modifyFilterButton.setVisible(false);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(modifyFilterButton);
        
        add(allRecordsButton);
        add(selectedRecordsButton);
        add(buttonPanel);
    }
    
    /**
     * Set the session for the record filter panel.
     * 
     * @param session the session
     */
    public void setSession(Session session) {
        this.session = session;
    }
    
    /**
     * Get the current session.
     * 
     * @return the session
     */
    public Session getSession() {
        return session;
    }
    
    /**
     * Set the record filter.
     * 
     * @param filter the record filter
     */
    public void setRecordFilter(RecordFilter filter) {
        this.recordFilter = filter;
        if(filter != null) {
            selectedRecordsButton.setSelected(true);
            modifyFilterButton.setVisible(true);
        } else {
            allRecordsButton.setSelected(true);
            modifyFilterButton.setVisible(false);
        }
    }
    
    /**
     * Get the current record filter based on the selected option.
     * 
     * @return the record filter, or null if "All records" is selected
     */
    public RecordFilter getRecordFilter() {
        if(allRecordsButton.isSelected()) {
            return null;
        }
        return recordFilter;
    }
    
    /**
     * Check if all records are selected.
     * 
     * @return true if all records are selected
     */
    public boolean isAllRecordsSelected() {
        return allRecordsButton.isSelected();
    }
    
    /**
     * Check if selected records option is chosen.
     * 
     * @return true if selected records is chosen
     */
    public boolean isSelectedRecordsSelected() {
        return selectedRecordsButton.isSelected();
    }
    
    private void onModifyFilter(ActionEvent e) {
        if(session == null) {
            JOptionPane.showMessageDialog(this,
                "No session available for record filtering.",
                "Session Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        RecordFilterDialog dialog = new RecordFilterDialog(
            SwingUtilities.getWindowAncestor(this) instanceof Frame ? 
                (Frame)SwingUtilities.getWindowAncestor(this) : null,
            project, session);
        
        // If we have an existing filter, we could set it in the dialog
        // (would require adding a setter method to RecordFilterDialog)
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setModal(true);
        dialog.setVisible(true);
        
        if(!dialog.wasCanceled()) {
            recordFilter = dialog.getRecordFilter();
        }
    }
    
    /**
     * Radio button listener to show/hide the modify filter button.
     */
    private class RadioButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            modifyFilterButton.setVisible(selectedRecordsButton.isSelected());
        }
    }
}
