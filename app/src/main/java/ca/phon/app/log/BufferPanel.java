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
package ca.phon.app.log;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableModel;

import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.JXTable;

import com.sun.javafx.application.PlatformImpl;

import au.com.bytecode.opencsv.CSVReader;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.session.editor.*;
import ca.phon.extensions.*;
import ca.phon.functor.Functor;
import ca.phon.plugin.*;
import ca.phon.project.Project;
import ca.phon.query.db.*;
import ca.phon.query.report.csv.CSVTableDataWriter;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.session.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.*;
import javafx.application.Platform;
import javafx.beans.value.*;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import jxl.Workbook;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
import netscape.javascript.JSObject;

/**
 * A panel showing a single {@link LogBuffer} with options
 * for displaying the data in different formats.  CSV data
 * may be displayed in a table, while HTML data may be
 * rendered inside a {@link JEditorPane}.
 *
 */
public class BufferPanel extends JPanel implements IExtendable {

	private final static String TABLE_VIEW_ID = "table";

	public final static String SHOW_TABLE_CODE = "SHOW_TABLE";

	public final static String PACK_TABLE_COLUMNS = "PACK_COLUMNS";

	private final static String BUFFER_VIEW_ID = "buffer";

	public final static String SHOW_BUFFER_CODE = "SHOW_BUFFER";

	private final static String HTML_VIEW_ID = "html";

	public final static String SHOW_HTML_CODE = "SHOW_HTML";

	public final static String SHOW_BUSY = "SHOW_BUSY";

	public final static String STOP_BUSY = "STOP_BUSY";

	private static final Logger LOGGER = Logger
			.getLogger(BufferPanel.class.getName());

	private static final long serialVersionUID = -153000974506461908L;

	private final ExtensionSupport extSupport = new ExtensionSupport(BufferPanel.class, this);

	private final String name;

	private JPanel contentPanel;
	private CardLayout cardLayout;

	private LogBuffer logBuffer;

	/* Views */
	private JXTable dataTable;

	private JFXPanel fxPanel;

	private WebView htmlView;

	public final static String SHOWING_BUFFER_PROP = BufferPanel.class.getName() + ".showingBuffer";

	private JComponent currentView;

	private WeakReference<Object> userObjectRef = new WeakReference<Object>(null);

	public BufferPanel(String name) {
		super();

		this.name = name;

		init();
		extSupport.initExtensions();
	}

	private LogBuffer createLogBuffer() {
		LogBuffer retVal = new LogBuffer(getName());
		retVal.addEscapeCodeHandler(new LogEscapeCodeHandler() {

			@Override
			public void handleEscapeCode(String code) {
				if(SHOW_TABLE_CODE.equals(code)) {
					SwingUtilities.invokeLater(BufferPanel.this::showTable);
				} else if(SHOW_BUFFER_CODE.equals(code)) {
					SwingUtilities.invokeLater(BufferPanel.this::showBuffer);
				} else if(SHOW_HTML_CODE.equals(code))  {
					SwingUtilities.invokeLater(BufferPanel.this::showHtml);
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
		return retVal;
	}

	public String getName() {
		return this.name;
	}

	public Object getUserObject() {
		return userObjectRef.get();
	}

	public void setUserObject(Object obj) {
		userObjectRef = new WeakReference<Object>(obj);
	}

	public boolean isShowingBuffer() {
		return currentView != null && currentView == logBuffer;
	}

	public void showBuffer() {
		JComponent oldComp = currentView;

		if(logBuffer == null) {
			logBuffer = createLogBuffer();
		}

		cardLayout.show(contentPanel, BUFFER_VIEW_ID);
		currentView = logBuffer;

		logBuffer.scrollRectToVisible(new Rectangle(0, 0, 0, 0));

//		firstRowAsHeaderBox.setVisible(false);

		firePropertyChange(SHOWING_BUFFER_PROP, oldComp, currentView);
	}

	public void clear() {
		getLogBuffer().setText("");
		showBuffer();
	}

	private JXTable createTable() {
		final JXTable retVal = new JXTable();

		retVal.setColumnControlVisible(true);
		retVal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		retVal.setFont(FontPreferences.getUIIpaFont());

		final ActionMap am = retVal.getActionMap();
		final InputMap im = retVal.getInputMap(JComponent.WHEN_FOCUSED);

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

		retVal.setActionMap(am);
		retVal.setInputMap(JComponent.WHEN_FOCUSED, im);
		retVal.addMouseListener(new TableMouseAdapter(tableAct));

		return retVal;
	}

	public boolean isShowingTable() {
		return currentView != null && currentView == dataTable;
	}

	public void showTable() {
		JComponent oldComp = currentView;

		if(dataTable == null) {
			dataTable = createTable();
			final JScrollPane tableScroller = new JScrollPane(dataTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			contentPanel.add(tableScroller, TABLE_VIEW_ID);
		}
		final CSVReader reader = new CSVReader(new StringReader(logBuffer.getText()));
		final CSVTableModel model = new CSVTableModel(reader);
		model.setUseFirstRowAsHeader(true);
		dataTable.setModel(model);

		dataTable.scrollCellToVisible(0, 0);

		cardLayout.show(contentPanel, TABLE_VIEW_ID);
		currentView = dataTable;

//		firstRowAsHeaderBox.setVisible(true);
		firePropertyChange(SHOWING_BUFFER_PROP, oldComp, currentView);
	}

	@SuppressWarnings("restriction")
	private Tuple<JFXPanel, WebView> createHtmlPane() {
		final JFXPanel fxpanel = new JFXPanel();
		final AtomicReference<JFXPanel> panelRef = new AtomicReference<JFXPanel>(fxpanel);
		final AtomicReference<WebView> viewRef = new AtomicReference<WebView>();
		PlatformImpl.runAndWait( () -> {
			final WebView webView = new WebView();
			fxpanel.setScene(new Scene(webView));

			webView.getEngine().setOnError( (evt) -> {
				LOGGER.log(Level.WARNING, evt.toString());
			});


			panelRef.set(fxpanel);
			viewRef.set(webView);
		});
		contentPanel.add(panelRef.get(), HTML_VIEW_ID);
		return new Tuple<>(panelRef.get(), viewRef.get());
	}

	public void copyTextToClipboard(String text) {
		final StringSelection stringSelection = new StringSelection(text);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, stringSelection);
	}

	public boolean isShowingHtml() {
		return currentView != null && currentView == fxPanel;
	}

	public void showHtml() {
		showHtml(true);
	}
	
	public void showHtml(boolean loadTextContent) {
		JComponent oldComp = currentView;

		final WebView htmlView = getWebView();
		Platform.runLater( () -> {
			htmlView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

				@Override
				public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
					if(newValue == State.SUCCEEDED) {
						JSObject window = (JSObject) htmlView.getEngine().executeScript("window");
						window.setMember("buffer", BufferPanel.this);
					}
				}

			});

			if(loadTextContent)
				htmlView.getEngine().loadContent(logBuffer.getText());

			SwingUtilities.invokeLater(() -> {
				currentView = fxPanel;
				cardLayout.show(contentPanel, HTML_VIEW_ID);
				firePropertyChange(SHOWING_BUFFER_PROP, oldComp, currentView);
			});
		});
	}

	private void init() {
		setLayout(new BorderLayout());

//		final FormLayout topLayout = new FormLayout(
//				"pref, pref,3dlu, pref, fill:pref:grow, pref, 3dlu, right:pref", "pref");
//		final CellConstraints cc = new CellConstraints();
//		final JPanel topPanel = new JPanel(topLayout);
//
//		final PhonUIAction firstRowAsHeaderAct = new PhonUIAction(this, "onToggleFirstRowAsHeader");
//		firstRowAsHeaderAct.putValue(PhonUIAction.NAME, "Use first row as column header");
//		firstRowAsHeaderAct.putValue(PhonUIAction.SELECTED_KEY, Boolean.TRUE);
//		firstRowAsHeaderBox = new JCheckBox(firstRowAsHeaderAct);
//		firstRowAsHeaderBox.setVisible(false);
//
//		busyLabel.setVisible(false);
//
//		buttons = new BufferPanelButtons(this);
//
//		topPanel.add(firstRowAsHeaderBox, cc.xy(4, 1));
//		topPanel.add(busyLabel, cc.xy(6, 1));
//		topPanel.add(buttons, cc.xy(8, 1));
//
//		add(topPanel, BorderLayout.NORTH);

		cardLayout = new CardLayout();
		contentPanel = new JPanel(cardLayout);

		logBuffer = createLogBuffer();
		final JScrollPane logScroller = new RTextScrollPane(logBuffer, true);
		contentPanel.add(logScroller, BUFFER_VIEW_ID);

		currentView = logBuffer;
		add(contentPanel, BorderLayout.CENTER);
	}
	
//	public void updateSaveButtonText() {
//		final StringBuffer buffer = new StringBuffer();
//		buffer.append("Save ").append(getBufferName());
//		buffer.append(" as");
//		
//		if(buttons.tableButton.isSelected()) {
//			buffer.append(" CSV");
//		} else if(buttons.textButton.isSelected()) {
//			buffer.append(" Text");
//		} else if(buttons.htmlButton.isSelected()) {
//			buffer.append(" HTML");
//		}
//		
//		saveButton.setText(buffer.toString());
//	}

	public String getBufferName() {
		return logBuffer.getBufferName();
	}

	public LogBuffer getLogBuffer() {
		return logBuffer;
	}

	public WebView getWebView() {
		if(fxPanel == null) {
			final Tuple<JFXPanel, WebView> webTuple = createHtmlPane();
			fxPanel = webTuple.getObj1();
			htmlView = webTuple.getObj2();
		}
		return this.htmlView;
	}

	public JXTable getDataTable() {
		return dataTable;
	}

//	public boolean isOpenAfterSave() {
//		return openFileAfterSaving;
//	}

	public void setBusy(boolean busy) {
//		busyLabel.setBusy(busy);
//		busyLabel.setVisible(busy);
	}

	public void setFirstRowIsHeader(boolean firstRowIsColumnHeader) {
		final CSVTableModel model = (CSVTableModel)getDataTable().getModel();
		model.setUseFirstRowAsHeader(firstRowIsColumnHeader);
		model.fireTableStructureChanged();
//		firstRowAsHeaderBox.setSelected(firstRowIsColumnHeader);
	}

	public void onToggleFirstRowAsHeader() {
		boolean isFirstRowHeader = ((CSVTableModel)getDataTable().getModel()).isUseFirstRowAsHeader();
		setFirstRowIsHeader(!isFirstRowHeader);
	}

	/**
	 * Returns the appropriate extension based on the current
	 * view of the data.
	 *
	 * @return one of 'txt', 'csv', or 'html'
	 */
	public String getDefaultExtension() {
		String retVal = "txt";
		if(isShowingTable()) {
			retVal = "csv";
		} else if(isShowingHtml()) {
			retVal = "html";
		}
		return retVal;
	}

	public void writeToTextFile(String file, String encoding) throws IOException {
		final File f = new File(file);
		final BufferedWriter out =
				new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), encoding));
		out.write(logBuffer.getText());
		out.flush();
		out.close();
	}
	
	public void writeToCSV(String file, String encoding) throws IOException {
		final CSVTableDataWriter writer = new CSVTableDataWriter(encoding);
		writer.writeTableToFile(dataTable, new File(file));
	}
	
	public void writeToExcelWorkbook(String file) throws IOException {
		// create a new workbook at the file
		final WritableWorkbook workbook = Workbook.createWorkbook(new File(file));
		try {
			createSheetInExcelWorkbook(workbook);
			workbook.write();
			workbook.close();
		} catch (WriteException e) {
			throw new IOException(e);
		}
	}
	
	public void createSheetInExcelWorkbook(WritableWorkbook workbook) throws RowsExceededException, WriteException {
		final WritableSheet sheet = workbook.createSheet(getName(), workbook.getNumberOfSheets());
		
		if(getUserObject() != null && getUserObject() instanceof DefaultTableDataSource) {
			final DefaultTableDataSource table = (DefaultTableDataSource)getUserObject();
			WorkbookUtils.addTableToSheet(sheet, 0, table);
		} else {
			final CSVTableModel tableModel = (CSVTableModel)dataTable.getModel();
			WorkbookUtils.addTableToSheet(sheet, 0, tableModel);			
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

			for(int i = 0; i < tblModel.getColumnCount(); i++) {
				final String colName = tblModel.getColumnName(i);

				if(colName.equalsIgnoreCase("session")) {
					sessionColumn = i;
				} else if(colName.equalsIgnoreCase("record")
						|| colName.equalsIgnoreCase("record #")) {
					recordColumn = i;
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

			// setup result highlighting if we have the appropriate column and user object
			if(getUserObject() != null && getUserObject() instanceof DefaultTableDataSource) {
				final DefaultTableDataSource tableData = (DefaultTableDataSource)getUserObject();
				final int resultColumn = tableData.getColumnIndex("Result");
				if(resultColumn >= 0) {
					final Object resultVal = tableData.getValueAt(row, resultColumn);
					if(resultVal != null && resultVal instanceof Result) {
						final Result result = (Result)resultVal;

						// setup highlighting
						final EditorSelectionModel selectionModel = editor.getSelectionModel();
						selectionModel.clear();
						for(ResultValue rv:result) {
							final Range range = new Range(rv.getRange().getFirst(), rv.getRange().getLast(), false);
							final SessionEditorSelection selection =
									new SessionEditorSelection(result.getRecordIndex(), rv.getTierName(),
											rv.getGroupIndex(), range);
							selectionModel.addSelection(selection);
						}
					}
				}
			}

			return null; /* Void return type */
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

	public Object getExtension(String classname) {
		Object retVal = null;

		for(Class<?> ext:getExtensions()) {
			if(ext.toString().equals("class " + classname)) {
				retVal = getExtension(ext);
				break;
			}
		}

		return retVal;
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
