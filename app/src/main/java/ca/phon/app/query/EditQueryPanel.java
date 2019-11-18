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
package ca.phon.app.query;

import java.lang.ref.WeakReference;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.query.db.Query;
import ca.phon.session.util.DateFormatter;
import ca.phon.ui.StarBox;
import ca.phon.util.icons.IconSize;

/**
 * Edit query name and comments.
 *
 */
public class EditQueryPanel extends JPanel {
	
	private static final long serialVersionUID = -6143337704518903828L;

	/**
	 * Query 
	 */
	private WeakReference<Query> queryRef;
	
	/**
	 * Query name field
	 */
	private JTextField queryNameField;
	
	/**
	 * Query comments field
	 */
	private JTextArea queryCommentsArea;
	
	/**
	 * Date label
	 */
	private JLabel dateLabel;
	
	/**
	 * Uuid label
	 */
	private JLabel uuidLabel;
	
	/**
	 * Star box
	 */
	private StarBox starBox;
	
	/**
	 * Constructor
	 */
	public EditQueryPanel() {
		init();
	}
	
	public EditQueryPanel(Query q) {
		init();
		setQuery(q);
	}
	
	public void setQuery(Query q) {
		this.queryRef = new WeakReference<Query>(q);
		updateForm();
	}
	
	public Query getQuery() {
		return (this.queryRef != null ? queryRef.get() : null);
	}
	
	public JTextField getQueryNameField() {
		return this.queryNameField;
	}
	
	private void init() {
		final FormLayout layout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, fill:pref:grow");
		final CellConstraints cc = new CellConstraints();
		setLayout(layout);
		
		starBox = new StarBox(IconSize.SMALL);
		starBox.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(getQuery() != null) {
					getQuery().setStarred(starBox.isSelected());
				}
			}
		});
		
		queryNameField = new JTextField();
		queryNameField.selectAll();
		queryNameField.requestFocusInWindow();
		queryNameField.getDocument().addDocumentListener(new DocumentListener() {
			
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
			}
			
			private void updateName() {
				if(getQuery() != null)
					getQuery().setName(queryNameField.getText());
			}
		});
		
		queryCommentsArea = new JTextArea();
		queryCommentsArea.setRows(5);
		queryCommentsArea.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateComments();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateComments();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
			}
			
			private void updateComments() {
				if(getQuery() != null)
					getQuery().setComments(queryCommentsArea.getText());
			}
		});
		
		uuidLabel = new JLabel();
		
		dateLabel = new JLabel();
		
		add(starBox, cc.xy(1, 1));
		add(queryNameField, cc.xy(3, 1));
		
		add(new JLabel("UUID:"), cc.xy(1, 3));
		add(uuidLabel, cc.xy(3, 3));
		
		add(new JLabel("Date:"), cc.xy(1, 5));
		add(dateLabel, cc.xy(3, 5));
		
		add(new JLabel("Comments:"), cc.xy(1, 7));
		add(new JScrollPane(queryCommentsArea), cc.xywh(3, 7, 1, 2));
	}

	private void updateForm() {
		if(getQuery() != null) {
			queryNameField.setText(getQuery().getName());
			queryCommentsArea.setText(getQuery().getComments());
			uuidLabel.setText(getQuery().getUUID().toString());
			starBox.setSelected(getQuery().isStarred());
			final String dateText = DateFormatter.dateTimeToString(getQuery().getDate());
			dateLabel.setText(dateText);
		}
	}

}
