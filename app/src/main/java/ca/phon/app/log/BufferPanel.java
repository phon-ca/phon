package ca.phon.app.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;

import au.com.bytecode.opencsv.CSVReader;
import ca.phon.query.report.csv.CSVTableDataWriter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class BufferPanel extends JPanel {
	
	public final static String SHOW_TABLE_CODE = "SHOW_TABLE";
	
	public final static String PACK_TABLE_COLUMNS = "PACK_COLUMNS";
	
	public final static String SHOW_BUFFER_CODE = "SHOW_BUFFER";
	
	public final static String SHOW_BUSY = "SHOW_BUSY";
	
	public final static String STOP_BUSY = "STOP_BUSY";
	
	private static final Logger LOGGER = Logger
			.getLogger(BufferPanel.class.getName());

	private static final long serialVersionUID = -153000974506461908L;

	private JScrollPane logScroller;
	private LogBuffer logBuffer;
	
	private JScrollPane tableScroller;
	private JXTable dataTable;
	
	private boolean showingBuffer = true;
	
	private JButton saveButton;
	
	private BufferPanelButtons buttons;
	
	private JCheckBox firstRowAsHeaderBox;
	
	private JXBusyLabel busyLabel = new JXBusyLabel(new Dimension(16, 16));
	
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
		
		init();
	}
	
	public boolean isShowingBuffer() {
		return showingBuffer;
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final FormLayout topLayout = new FormLayout(
				"pref, pref, fill:pref:grow, pref, 3dlu, right:pref", "pref");
		final CellConstraints cc = new CellConstraints();
		final JPanel topPanel = new JPanel(topLayout);
		
		final PhonUIAction saveAct = new PhonUIAction(this, "onSaveBuffer");
		saveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save buffer...");
		saveAct.putValue(PhonUIAction.SMALL_ICON, 
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL));
		saveButton = new JButton(saveAct);
		
		final PhonUIAction firstRowAsHeaderAct = new PhonUIAction(this, "onToggleFirstRowAsHeader");
		firstRowAsHeaderAct.putValue(PhonUIAction.NAME, "Use first row as column header");
		firstRowAsHeaderAct.putValue(PhonUIAction.SELECTED_KEY, Boolean.TRUE);
		firstRowAsHeaderBox = new JCheckBox(firstRowAsHeaderAct);
		
		busyLabel.setVisible(false);
		
		buttons = new BufferPanelButtons(this);
		
		topPanel.add(firstRowAsHeaderBox, cc.xy(2, 1));
		topPanel.add(saveButton, cc.xy(1,1));
		topPanel.add(busyLabel, cc.xy(4, 1));
		topPanel.add(buttons, cc.xy(6, 1));
		
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
		
		dataTable.setActionMap(am);
		dataTable.setInputMap(JComponent.WHEN_FOCUSED, im);
		
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
	
}
