package ca.phon.app.log;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.io.StringReader;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.jdesktop.swingx.JXTable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import au.com.bytecode.opencsv.CSVReader;
import ca.phon.app.log.actions.SaveLogBufferAction;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.text.TableSearchField;

public class BufferPanel extends JPanel {

	private static final long serialVersionUID = -153000974506461908L;

	private JToolBar toolBar;
	
	private JScrollPane logScroller;
	private LogBuffer logBuffer;
	
	private JScrollPane tableScroller;
	private JXTable dataTable;
	
	private boolean showingBuffer = true;
	
	public BufferPanel(String name) {
		super();
		
		logBuffer = new LogBuffer(name);
		dataTable = new JXTable();
		
		setupToolbar();
		init();
	}
	
	private void setupToolbar() {
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(new SaveLogBufferAction(logBuffer));
		
		final PhonUIAction swapAct = new PhonUIAction(this, "onSwapBuffer");
		swapAct.putValue(PhonUIAction.NAME, "Show as table");
		swapAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Read buffer contents as CSV and show as a table");
		swapAct.putValue(PhonUIAction.SELECTED_KEY, false);
		
		toolBar.addSeparator();
		toolBar.add(new JToggleButton(swapAct));
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final FormLayout topLayout = new FormLayout(
				"left:pref, right:pref:grow", "pref");
		final CellConstraints cc = new CellConstraints();
		final JPanel topPanel = new JPanel(topLayout);
		topPanel.add(toolBar, cc.xy(1,1));
		
		add(topPanel, BorderLayout.NORTH);
		
		logScroller = new JScrollPane(logBuffer);
		add(logScroller, BorderLayout.CENTER);
		
		tableScroller = new JScrollPane(dataTable);
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
	
}
