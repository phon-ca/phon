/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableModel;

import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;

import au.com.bytecode.opencsv.CSVReader;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionEditorEP;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.functor.Functor;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.query.report.csv.CSVTableDataWriter;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class BufferPanel extends JPanel implements IExtendable {
	
	public final static String SHOW_TABLE_CODE = "SHOW_TABLE";
	
	public final static String PACK_TABLE_COLUMNS = "PACK_COLUMNS";
	
	public final static String SHOW_BUFFER_CODE = "SHOW_BUFFER";
	
	public final static String SHOW_BUSY = "SHOW_BUSY";
	
	public final static String STOP_BUSY = "STOP_BUSY";
	
	private static final Logger LOGGER = Logger
			.getLogger(BufferPanel.class.getName());

	private static final long serialVersionUID = -153000974506461908L;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(BufferPanel.class, this);

	private JScrollPane logScroller;
	private LogBuffer logBuffer;
	
	private JScrollPane tableScroller;
	private JXTable dataTable;
	
	private boolean showingBuffer = true;
	
	private JButton saveButton;
	
	private BufferPanelButtons buttons;
	
	private JCheckBox firstRowAsHeaderBox;
	
	private JCheckBox openFileAfterSavingBox;
	
	private JXBusyLabel busyLabel = new JXBusyLabel(new Dimension(16, 16));
	
	public final static String OPEN_AFTER_SAVING_PROP = BufferPanel.class.getName() + ".openFileAfterSaving";
	
	private boolean openFileAfterSaving = 
			PrefHelper.getBoolean(OPEN_AFTER_SAVING_PROP, Boolean.TRUE);
	
	public final static String SHOWING_BUFFER_PROP = BufferPanel.class.getName() + ".showingBuffer";
	
	public BufferPanel(String name) {
		super();
		
		logBuffer = new LogBuffer(name);
		logBuffer.addEscapeCodeHandler(new LogEscapeCodeHandler() {
			
			@Override
			public void handleEscapeCode(String code) {
				final Runnable swapBuffer = new Runnable() {
					
					@Override
					public void run() {
						onSwapBuffer();
					}
					
				};
				if(SHOW_TABLE_CODE.equals(code) && isShowingBuffer()) {
					SwingUtilities.invokeLater(swapBuffer);
				} else if(SHOW_BUFFER_CODE.equals(code) && !isShowingBuffer()) {
					SwingUtilities.invokeLater(swapBuffer);
				} else if(PACK_TABLE_COLUMNS.equals(code) && !isShowingBuffer()) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							getDataTable().packAll();
						}
					});
				} else if(SHOW_BUSY.equals(code)) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							setBusy(true);
						}
					});
				} else if(STOP_BUSY.equals(code)) {
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							setBusy(false);
						}
					});
				}
			}
			
		});
		
		dataTable = new JXTable();
		dataTable.setColumnControlVisible(true);
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataTable.setFont(FontPreferences.getUIIpaFont());
		
		init();
		extSupport.initExtensions();
	}
	
	public boolean isShowingBuffer() {
		return showingBuffer;
	}
	
	public void clear() {
		getLogBuffer().setText("");
		if(!isShowingBuffer()) {
			onSwapBuffer();
		}
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final FormLayout topLayout = new FormLayout(
				"pref, pref,3dlu, pref, fill:pref:grow, pref, 3dlu, right:pref", "pref");
		final CellConstraints cc = new CellConstraints();
		final JPanel topPanel = new JPanel(topLayout);
		
		final PhonUIAction saveAct = new PhonUIAction(this, "onSaveBuffer");
		saveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save buffer...");
		saveAct.putValue(PhonUIAction.SMALL_ICON, 
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL));
		saveButton = new JButton(saveAct);
		
		openFileAfterSavingBox = new JCheckBox("Open after saving");
		openFileAfterSavingBox.setSelected(openFileAfterSaving);
		openFileAfterSavingBox.addChangeListener( e -> {
			BufferPanel.this.openFileAfterSaving = openFileAfterSavingBox.isSelected();
			PrefHelper.getUserPreferences().putBoolean(OPEN_AFTER_SAVING_PROP, BufferPanel.this.openFileAfterSaving);
		});
		
		final PhonUIAction firstRowAsHeaderAct = new PhonUIAction(this, "onToggleFirstRowAsHeader");
		firstRowAsHeaderAct.putValue(PhonUIAction.NAME, "Use first row as column header");
		firstRowAsHeaderAct.putValue(PhonUIAction.SELECTED_KEY, Boolean.TRUE);
		firstRowAsHeaderBox = new JCheckBox(firstRowAsHeaderAct);
		
		busyLabel.setVisible(false);
		
		buttons = new BufferPanelButtons(this);
		
		topPanel.add(firstRowAsHeaderBox, cc.xy(4, 1));
		topPanel.add(saveButton, cc.xy(1,1));
		topPanel.add(openFileAfterSavingBox, cc.xy(2, 1));
		topPanel.add(busyLabel, cc.xy(6, 1));
		topPanel.add(buttons, cc.xy(8, 1));
		
		add(topPanel, BorderLayout.NORTH);
		
		logScroller = new RTextScrollPane(logBuffer, true);
		add(logScroller, BorderLayout.CENTER);
		
		final ActionMap am = dataTable.getActionMap();
		final InputMap im = dataTable.getInputMap(JComponent.WHEN_FOCUSED);
		
		final String deleteRowsKey = "__delete_rows__";
		final PhonUIAction deleteRowsAct = new PhonUIAction(this, "deleteSelectedRows");
		am.put(deleteRowsKey, deleteRowsAct);
		
		final KeyStroke delKs = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		im.put(delKs, deleteRowsKey);

		final String defaultActKey = "__default_act__";
		final TableAction defaultAct = new TableAction(tableAct);
		am.put(defaultActKey, defaultAct);
		
		final KeyStroke defKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		im.put(defKs, defaultActKey);
		
		dataTable.setActionMap(am);
		dataTable.setInputMap(JComponent.WHEN_FOCUSED, im);
		dataTable.addMouseListener(new TableMouseAdapter(tableAct));

		tableScroller = new JScrollPane(dataTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
	
	public String getBufferName() {
		return logBuffer.getBufferName();
	}

	public LogBuffer getLogBuffer() {
		return logBuffer;
	}

	public JXTable getDataTable() {
		return dataTable;
	}
	
	public void setBusy(boolean busy) {
		busyLabel.setBusy(busy);
		busyLabel.setVisible(busy);
	}
	
	public void setFirstRowIsHeader(boolean firstRowIsColumnHeader) {
		final CSVTableModel model = (CSVTableModel)getDataTable().getModel();
		model.setUseFirstRowAsHeader(firstRowIsColumnHeader);
		model.fireTableStructureChanged();
		firstRowAsHeaderBox.setSelected(firstRowIsColumnHeader);
	}

	public void onToggleFirstRowAsHeader() {
		boolean isFirstRowHeader = firstRowAsHeaderBox.isSelected();
		setFirstRowIsHeader(isFirstRowHeader);
	}
	
	public void onSwapBuffer() {
		boolean wasShowingBuffer = isShowingBuffer();
		if(showingBuffer) {
			final CSVReader reader = new CSVReader(new StringReader(logBuffer.getText()));
			final CSVTableModel tableModel = new CSVTableModel(reader);
			tableModel.setUseFirstRowAsHeader(firstRowAsHeaderBox.isSelected());
			dataTable.setModel(tableModel);
			
			remove(logScroller);
			add(tableScroller, BorderLayout.CENTER);
			
			showingBuffer = false;
		} else {
			remove(tableScroller);
			
			add(logScroller, BorderLayout.CENTER);
			showingBuffer = true;
		}
		super.firePropertyChange(SHOWING_BUFFER_PROP, wasShowingBuffer, isShowingBuffer());
		revalidate();
		repaint();
	}
	
	public void onSaveBuffer() {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setRunAsync(false);
		props.setInitialFile(logBuffer.getBufferName());
		final FileFilter filter = 
				(showingBuffer ? new FileFilter("Text files (*.txt)", "txt") : FileFilter.csvFilter);
		props.setFileFilter(filter);
		props.setCanCreateDirectories(true);
		
		final String saveAs = NativeDialogs.showSaveDialog(props);
		
		if(saveAs != null && saveAs.length() > 0) {
			if(showingBuffer) {
				// save buffer contents as text
				try {
					final File f = new File(saveAs);
					final BufferedWriter out = 
							new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
					out.write(logBuffer.getText());
					out.flush();
					out.close();
				} catch (IOException e) {
					LOGGER
							.log(Level.SEVERE, e.getLocalizedMessage(), e);
					ToastFactory.makeToast(e.getLocalizedMessage()).start(logBuffer);
				}
			} else {
				// save table model as csv 
				final CSVTableDataWriter writer = new CSVTableDataWriter();
				try {
					writer.writeTableToFile(dataTable, new File(saveAs));
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					ToastFactory.makeToast(e.getLocalizedMessage()).start(logBuffer);
				}
			}
			
			if(openFileAfterSaving) {
				try {
					OpenFileLauncher.openURL(new File(saveAs).toURI().toURL());
				} catch (MalformedURLException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}
	}
	
	public void deleteSelectedRows() {
		final int selected[] = dataTable.getSelectedRows();
		final List<Integer> selectedRows = new ArrayList<Integer>();
		for(int viewSelected:selected) {
			int modelSelected = dataTable.convertRowIndexToModel(viewSelected);
			selectedRows.add(modelSelected);
		}
		Collections.sort(selectedRows);
		
		for(int rowIdx = selectedRows.size() - 1; rowIdx >= 0; rowIdx--) {
			final int row = selectedRows.get(rowIdx);
			final CSVTableModel model = (CSVTableModel)dataTable.getModel();
			model.deleteRow(row);
		}
	}
	
	/**
	 * Listener action for double clicks in the data table.
	 * 
	 * Returns: Void
	 * Params: Integer (table row index)
	 */
	private final Functor<Void, Integer> tableAct = new Functor<Void, Integer>() {

		@Override
		public Void op(Integer row) {
			final JXTable tbl = getDataTable();
			if(tbl == null) return null;
			
			final TableModel tblModel = tbl.getModel();
			if(tblModel == null) return null;
			if(row < 0 || row >= tblModel.getRowCount()) return null;
			
			// fix row number if sorted
			row = tbl.convertRowIndexToModel(row);
			
			// get project reference from parent window
			final CommonModuleFrame cmf = 
					(CommonModuleFrame)SwingUtilities.getAncestorOfClass(CommonModuleFrame.class, BufferPanel.this);
			if(cmf == null) return null; 
			
			final Project project = cmf.getExtension(Project.class);
			if(project == null) return null;
			
			// look for a session reference, if not found try to find a column
			// with name 'session'
			final Session primarySession = cmf.getExtension(Session.class);
			int sessionColumn = -1;
			int recordColumn = -1;
			int tierColumn = -1;
			int groupColumn = -1;
			int rangeColumn = -1;
			
			for(int i = 0; i < tblModel.getColumnCount(); i++) {
				final String colName = tblModel.getColumnName(i);
				
				if(colName.equalsIgnoreCase("session")) {
					sessionColumn = i;
				} else if(colName.equalsIgnoreCase("record")
						|| colName.equalsIgnoreCase("record #")) {
					recordColumn = i; 
				} else if(colName.equalsIgnoreCase("tier")
						|| colName.equalsIgnoreCase("tier name")) {
					tierColumn = i;
				} else if(colName.equalsIgnoreCase("group")) {
					groupColumn = i;
				} else if(colName.equalsIgnoreCase("range")) {
					rangeColumn = i;
				}
			}
			
			// check for required items
			if(primarySession == null && sessionColumn == -1) return null;
			if(recordColumn == -1) return null;
			
			// load session
			SessionEditor editor = null;
			// get values for each column
			
			SessionPath sp = new SessionPath();
			
			if(sessionColumn >= 0 && primarySession == null) {
				String sessionTxt = tblModel.getValueAt(row, sessionColumn).toString();
				if(sessionTxt == null || sessionTxt.length() == 0 || sessionTxt.indexOf('.') < 0) return null;
				String[] sessionPath = sessionTxt.split("\\.");
				if(sessionPath.length != 2) return null;
				sp.setCorpus(sessionPath[0]);
				sp.setSession(sessionPath[1]);
			} else if(primarySession != null) {
				sp.setCorpus(primarySession.getCorpus());
				sp.setSession(primarySession.getName());
			}
			// load session editor (if necessary)
			final EntryPointArgs epArgs = new EntryPointArgs();
			epArgs.put(EntryPointArgs.PROJECT_OBJECT, project);
			epArgs.put(EntryPointArgs.CORPUS_NAME, sp.getCorpus());
			epArgs.put(EntryPointArgs.SESSION_NAME, sp.getSession());
			try {
				PluginEntryPointRunner.executePlugin(SessionEditorEP.EP_NAME, epArgs);
			} catch (PluginException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				return null;
			}
			
			// find session editor
			for(CommonModuleFrame openWindow:CommonModuleFrame.getOpenWindows()) {
				if(openWindow instanceof SessionEditor) {
					final SessionEditor currentEditor = (SessionEditor)openWindow;
					if(currentEditor.getSession().getCorpus().equals(sp.getCorpus())
							&& currentEditor.getSession().getName().equals(sp.getSession())) {
						editor = (SessionEditor)openWindow;
						break;
					}
				}
			}
			if(editor == null) return null;
			
			// get record index
			String recordTxt = tblModel.getValueAt(row, recordColumn).toString();
			int recordNum = Integer.parseInt(recordTxt) - 1;
			if(recordNum < 0 || recordNum >= editor.getDataModel().getRecordCount()) return null;
			
			editor.setCurrentRecordIndex(recordNum);
			
			if(tierColumn >= 0 && groupColumn >= 0) {
				// TODO attempt to setup highlighting
			}
			
			return null;
		}
			
	};
	
	private final class TableMouseAdapter extends MouseInputAdapter {
		
		private Functor<Void, Integer> functor;
		
		public TableMouseAdapter(Functor<Void, Integer> functor) {
			this.functor = functor;
		}
		
		@Override
		public void mouseClicked(MouseEvent me) {
			if(me.getClickCount() == 2 && me.getButton() == MouseEvent.BUTTON1) {
				functor.op(getDataTable().getSelectedRow());
			}
		}
		
	}
	
	private final class TableAction extends AbstractAction {
		
		private Functor<Void, Integer> functor;
		
		public TableAction(Functor<Void, Integer> functor) {
			this.functor = functor;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			functor.op(getDataTable().getSelectedRow());
		}
		
	}

	public void setBufferName(String string) {
		logBuffer.setBufferName(string);
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

}
