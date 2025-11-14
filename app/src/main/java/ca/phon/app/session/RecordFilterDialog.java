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
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * A dialog for configuring record filters.
 * Displays a RecordFilterPanel and OK/Cancel buttons.
 */
public class RecordFilterDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    private RecordFilterPanel recordFilterPanel;
    
    private DialogHeader header;
    
    private JButton okButton;
    
    private JButton cancelButton;
    
    private boolean wasCanceled = true;
    
    /**
     * Constructor
     * 
     * @param project the project
     * @param session the session
     */
    public RecordFilterDialog(Project project, Session session) {
        super();
        super.setTitle("Record Filter");
        
        recordFilterPanel = new RecordFilterPanel(project, session);
        
        init();
    }
    
    /**
     * Constructor
     * 
     * @param owner the owner frame
     * @param project the project
     * @param session the session
     */
    public RecordFilterDialog(Frame owner, Project project, Session session) {
        super(owner);
        super.setTitle("Record Filter");
        
        recordFilterPanel = new RecordFilterPanel(project, session);
        
        init();
    }
    
    private void init() {
        setLayout(new BorderLayout());
        
        header = new DialogHeader("Record Filter", "Select which records to include.");
        
        PhonUIAction<Void> okAct = PhonUIAction.eventConsumer(this::onOk);
        okAct.putValue(Action.NAME, "Ok");
        okAct.putValue(Action.SHORT_DESCRIPTION, "Apply filter and close");
        okButton = new JButton(okAct);
        
        PhonUIAction<Void> cancelAct = PhonUIAction.eventConsumer(this::onCancel);
        cancelAct.putValue(Action.NAME, "Cancel");
        cancelAct.putValue(Action.SHORT_DESCRIPTION, "Cancel and close");
        cancelButton = new JButton(cancelAct);
        
        final ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addGlue();
        builder.addButton(okButton);
        builder.addButton(cancelButton);
        
        add(header, BorderLayout.NORTH);
        add(recordFilterPanel, BorderLayout.CENTER);
        add(builder.getPanel(), BorderLayout.SOUTH);
    }
    
    private void onOk(ActionEvent e) {
        if(recordFilterPanel.validatePanel()) {
            wasCanceled = false;
            setVisible(false);
        }
    }
    
    private void onCancel(ActionEvent e) {
        wasCanceled = true;
        setVisible(false);
    }
    
    /**
     * Check if the dialog was canceled.
     * 
     * @return true if canceled, false otherwise
     */
    public boolean wasCanceled() {
        return wasCanceled;
    }
    
    /**
     * Get the configured record filter.
     * 
     * @return the record filter, or null if dialog was canceled
     */
    public RecordFilter getRecordFilter() {
        if(wasCanceled) {
            return null;
        }
        return recordFilterPanel.getRecordFilter();
    }
    
    /**
     * Get the record filter panel.
     * 
     * @return the record filter panel
     */
    public RecordFilterPanel getRecordFilterPanel() {
        return recordFilterPanel;
    }
}
