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
import ca.phon.app.session.editor.view.transcript.TranscriptView;
import ca.phon.plugin.*;
import ca.phon.session.Session;
import ca.phon.session.Transcript;
import ca.phon.session.check.*;
import ca.phon.session.position.TranscriptElementLocation;
import ca.phon.ui.FlatButton;
import ca.phon.ui.IconStrip;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.*;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
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

		setupEditorActions();
	}

	private void setupEditorActions() {
//		getEditor().getEventManager().registerActionForEvent(EditorEventType.EditorFinishedLoading, this::onSessionFinishedLoading, EditorEventManager.RunOn.AWTEventDispatchThread);
//		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TierChange, this::onTierChange, EditorEventManager.RunOn.AWTEventDispatchThread);
	}

	private void onSessionFinishedLoading(EditorEvent<Void> ee) {
		refresh();
	}

	private void onSessionChanged(EditorEvent<Session> ee) {
		refresh();
	}

	private void onTierChange(EditorEvent<EditorEventType.TierChangeData> ee) {
		EditorEventType.TierChangeData data = ee.getData().orElse(null);
		if(data == null) return;
		if(data.valueAdjusting()) return;
		final int elementIndex = getEditor().getSession().getRecordElementIndex(data.record());
		if(elementIndex >= 0) {
			tableModel.removeEventsForElement(elementIndex);
		}
		SessionCheckWorker worker = new SessionCheckWorker(elementIndex);
		worker.execute();
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
		sessionCheckTable.setDefaultRenderer(ValidationEvent.Severity.class, new SessionCheckSeverityRenderer());
		sessionCheckTable.addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					int row = sessionCheckTable.rowAtPoint(e.getPoint());
					if(row >= 0) {
						ValidationEvent ve = tableModel.events.get(row);
						if(ve.getElementIndex() >= 0) {
							Transcript.Element element = getEditor().getSession().getTranscript().getElementAt(ve.getElementIndex());
							if(element.isRecord()) {
								TranscriptElementLocation loc = new TranscriptElementLocation(ve.getElementIndex(), ve.getTierName(), 0);
								TranscriptView tv = (TranscriptView) getEditor().getViewModel().getView(TranscriptView.VIEW_NAME);
								int charPos = tv.getTranscriptEditor().sessionLocationToCharPos(loc);
								if(charPos >= 0) {
									tv.getTranscriptEditor().setCaretPosition(charPos);
									tv.getTranscriptEditor().requestFocus();
								}
							} else {

							}
						}
					}
				}
			}
		});

		add(iconStrip, BorderLayout.NORTH);
		add(new JScrollPane(sessionCheckTable), BorderLayout.CENTER);
	}

	public void refresh() {
		if(tableModel.events.size() > 0)
			tableModel.reset();
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

		private final int elementIndex;

		public SessionCheckWorker() {
			this(-1);
		}

		public SessionCheckWorker(int elementIndex) {
			super();
			this.elementIndex = elementIndex;
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

			if(elementIndex >= 0) {
				validator.validate(getEditor().getSession(), elementIndex);
			} else {
				validator.validate(getEditor().getSession());
			}
			
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

	private class SessionCheckSeverityRenderer extends DefaultTableCellRenderer {

		@Override
		protected void setValue(Object value) {
			if(value instanceof ValidationEvent.Severity ves) {
				switch (ves) {
					case ERROR:
						setIcon(IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName,
								"error", IconSize.MEDIUM, Color.red));
						break;

					case WARNING:
						setIcon(IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName,
								"warning", IconSize.MEDIUM, Color.orange));
						break;

					default:
						setIcon(null);
						break;
				}
			} else {
				super.setValue(value);
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
				retVal = ve.getSeverity();
				break;

			case 1:
				int elementIndex = ve.getElementIndex();
				Transcript.Element element = getEditor().getSession().getTranscript().getElementAt(elementIndex);
				if(element.isRecord()) {
					retVal = getEditor().getSession().getTranscript().getRecordIndex(elementIndex)+1;
				} else {
					retVal = -1;
				}
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

		public Class<?> getColumnClass(int col) {
			Class<?> retVal = Object.class;

			switch(col) {
			case 0:
				retVal = ValidationEvent.Severity.class;
				break;

			case 1:
				retVal = Integer.class;
				break;
			}

			return retVal;
		}

		public void addEvents(List<ValidationEvent> events) {
			events.stream().forEach(this::insertEvent);
		}

		public void reset() {
			this.events.clear();
			fireTableDataChanged();
		}

		public void insertEvent(ValidationEvent ve) {
			// insert event at correct position in list based on element index
			int idx = 0;
			for(; idx < events.size(); idx++) {
				if(events.get(idx).getElementIndex() > ve.getElementIndex()) {
					break;
				}
			}
			events.add(idx, ve);
			fireTableRowsInserted(idx, idx);
		}

		public void removeEventsForElement(int elementIdx) {
			for(int i = 0; i < events.size(); i++) {
				if(events.get(i).getElementIndex() == elementIdx) {
					events.remove(i);
					fireTableRowsDeleted(i, i);
					i--;
				}
			}
		}

	}

}
