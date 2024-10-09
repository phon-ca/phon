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
package ca.phon.app.session.editor.view.check;

import ca.phon.app.log.*;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.check.actions.SessionCheckRefreshAction;
import ca.phon.csv.CSVWriter;
import ca.phon.plugin.*;
import ca.phon.session.Session;
import ca.phon.session.check.*;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.FlatButton;
import ca.phon.ui.IconStrip;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.*;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class SessionCheckView extends EditorView {
	
	public final static String VIEW_NAME = "Session Check";
	
	public final static String ICON_NAME = IconManager.GoogleMaterialDesignIconsFontName + ":error";

	private SessionCheckTableModel tableModel;
	private JXTable sessionCheckTable;

	public SessionCheckView(SessionEditor editor) {
		super(editor);

		init();
		refresh();

		setupEditorActions();
	}

	private void setupEditorActions() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
	}

	private void onSessionChanged(EditorEvent<Session> ee) {
		refresh();
	}

	private void init() {
		setLayout(new BorderLayout());

		final IconStrip iconStrip = new IconStrip();

		final PhonUIAction refreshAct = PhonUIAction.runnable(this::refresh);
		refreshAct.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		refreshAct.putValue(FlatButton.ICON_NAME_PROP, "refresh");
		refreshAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		final FlatButton refreshBtn = new FlatButton(refreshAct);
		refreshBtn.setFocusable(false);
		iconStrip.add(refreshBtn, IconStrip.IconStripPosition.LEFT);

		tableModel = new SessionCheckTableModel();
		sessionCheckTable = new JXTable(tableModel);

		add(iconStrip, BorderLayout.NORTH);
		add(new JScrollPane(sessionCheckTable), BorderLayout.CENTER);
	}

	public void refresh() {
		SessionCheckWorker worker = new SessionCheckWorker();
		worker.execute();
	}
	
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getFontIcon(ICON_NAME, IconSize.MEDIUM, Color.darkGray);
	}

	@Override
	public JMenu getMenu() {
		JMenu retVal = new JMenu();
		
		retVal.add(new SessionCheckRefreshAction(this));
		
		return retVal;
	}
	
	public void setupStatusBar(List<ValidationEvent> events) {
		final SessionEditorStatusBar statusBar = getEditor().getStatusBar();
		
		JLabel lbl = null;
		for(Component comp:statusBar.getExtrasPanel().getComponents()) {
			if(comp.getName() != null && comp.getName().equals(VIEW_NAME)) {
				lbl = (JLabel)comp;
				break;
			}
		}
		
		if(lbl == null) {
			lbl = new JLabel();
			lbl.setName(VIEW_NAME);
			final ImageIcon icn = IconManager.getInstance().getFontIcon(ICON_NAME, IconSize.XSMALL, Color.darkGray);
			lbl.setIcon(icn);
			if(events.size() > 0) {
				lbl.setText("<html><u>" +
						events.size() + " warning" + 
						(events.size() == 1 ? "" : "s") + "</u></html>");
				lbl.setToolTipText("Show warnings");
				lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				lbl.addMouseListener(new MouseInputAdapter() {
	
					@Override
					public void mouseClicked(MouseEvent e) {
						getEditor().getViewModel().showView(VIEW_NAME);
					}
	
				});
				statusBar.getExtrasPanel().add(lbl);
			}
		} else {
			if(events.size() == 0)
				statusBar.getExtrasPanel().remove(lbl);
			else {
				lbl.setText("<html><u>" +
						events.size() + " warning" + 
						(events.size() == 1 ? "" : "s") + "</u></html>");
			}
		}
		statusBar.revalidate();
	}
	
	private class SessionCheckWorker extends SwingWorker<List<ValidationEvent>, ValidationEvent> {

		public SessionCheckWorker() {
		}
		
		@Override
		protected List<ValidationEvent> doInBackground() throws Exception {
			List<ValidationEvent> events = new ArrayList<>();

			List<SessionCheck> checks = new ArrayList<>();
			for(IPluginExtensionPoint<SessionCheck> extPt:PluginManager.getInstance().getExtensionPoints(SessionCheck.class)) {
				SessionCheck check = extPt.getFactory().createObject();
				checks.add(check);
			}

			SessionValidator validator = new SessionValidator(checks);
			validator.putExtension(SessionEditor.class, getEditor());
			validator.addValidationListener( (e) -> {
				events.add(e);
				publish(e);
			});
					
			validator.validate(getEditor().getSession());
			
			return events;
		}

		@Override
		protected void process(List<ValidationEvent> chunks) {
			tableModel.addEvents(chunks);
		}

		@Override
		protected void done() {

			try {
				setupStatusBar(get());
			} catch (InterruptedException | ExecutionException e) {
				LogUtil.warning(e);
			}

		}
		
	}

	private class SessionCheckTableModel extends DefaultTableModel {

		private List<ValidationEvent> events = new ArrayList<>();

		public SessionCheckTableModel() {
			super(new String[] { "Type", "Record #", "Tier", "Message" }, 0);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public int getRowCount() {
			if(this.events == null) return 0; // on super initialization
			return events.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object retVal = null;

			ValidationEvent ve = events.get(row);
			switch(col) {
			case 0:
				retVal = ve.getSeverity().toString();
				break;

			case 1:
				retVal = ve.getRecord();
				break;

			case 2:
				retVal = ve.getTierName();
				break;

			case 3:
				retVal = ve.getMessage();
				break;
			}

			return retVal;
		}

		public void addEvents(List<ValidationEvent> events) {
			int start = getRowCount();
			this.events.addAll(events);
			fireTableRowsInserted(start, events.size()-1);
		}

		public void reset() {
			int end = getRowCount()-1;
			this.events.clear();
			fireTableRowsDeleted(0, end);
		}

	}

}
