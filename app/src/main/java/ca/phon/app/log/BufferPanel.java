package ca.phon.app.log;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.commons.io.filefilter.FileFileFilter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.JXTable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import au.com.bytecode.opencsv.CSVReader;
import ca.phon.app.log.actions.SaveLogBufferAction;
import ca.phon.query.report.csv.CSVTableDataWriter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.ui.text.TableSearchField;
import ca.phon.ui.toast.ToastFactory;

public class BufferPanel extends JPanel {
	
	private static final Logger LOGGER = Logger
			.getLogger(BufferPanel.class.getName());

	private static final long serialVersionUID = -153000974506461908L;

	private JScrollPane logScroller;
	private LogBuffer logBuffer;
	
	private JScrollPane tableScroller;
	private JXTable dataTable;
	
	private boolean showingBuffer = true;
	
	public BufferPanel(String name) {
		super();
		
		logBuffer = new LogBuffer(name);
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

	public void onSwapBuffer() {
		if(showingBuffer) {
			final CSVReader reader = new CSVReader(new StringReader(logBuffer.getText()));
			final CSVTableModel tableModel = new CSVTableModel(reader);
			dataTable.setModel(tableModel);
			
			remove(logScroller);
			add(tableScroller, BorderLayout.CENTER);
			
			showingBuffer = false;
		} else {
			remove(tableScroller);
			
			add(logScroller, BorderLayout.CENTER);
			showingBuffer = true;
		}
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
