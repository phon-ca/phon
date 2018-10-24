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
package ca.phon.app.opgraph.wizard;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.PrefHelper;

/**
 * UI for actions exporting report tables. 
 */
public class ReportTableExportDialog extends CommonModuleFrame {
	
	private DialogHeader header;
	
	private final static String OPEN_AFTER_EXPORT_PROP = ReportTableExportDialog.class.getName() + ".openAfterExport";
	private final static boolean DEFAULT_OPEN_AFTER_EXPORT = Boolean.TRUE;
	private boolean openAfterExport = PrefHelper.getBoolean(OPEN_AFTER_EXPORT_PROP, DEFAULT_OPEN_AFTER_EXPORT);
	
	private JCheckBox openAfterExportBox;

	private JPanel customOptionsPanel;
	
	private ReportTableCheckboxTree reportTableCheckboxTree;
	
	private JXBusyLabel busyLabel;
	private ReportTree reportTree;
	
	private JButton exportButton;
	
	private JButton cancelButton;
	
	private Supplier<String> locationFunction;
	private BiFunction<String, ReportTreeNode, Boolean> exportFunction;
	private Consumer<List<ReportTreeNode>> finishFunction;
	
	public ReportTableExportDialog(ReportTree reportTree, Supplier<String> locationFunction,
			BiFunction<String, ReportTreeNode, Boolean> exportFunction, Consumer<List<ReportTreeNode>> finishFunction) {
		super();
		setTitle("Export Report Tables");
		
		this.reportTree = reportTree;
		this.locationFunction = locationFunction;
		this.exportFunction = exportFunction;
		this.finishFunction = finishFunction;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("Export Tables", "Use checkboxes to select tables for export");
		add(header, BorderLayout.NORTH);
		
		reportTableCheckboxTree = new ReportTableCheckboxTree(reportTree);
		reportTableCheckboxTree.expandAll();
		
		final JScrollPane scroller = new JScrollPane(reportTableCheckboxTree);
		
		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		
		add(scroller, BorderLayout.CENTER);
		
		final PhonUIAction cancelAct = new PhonUIAction(this, "onCancel");
		cancelAct.putValue(PhonUIAction.NAME, "Close");
		cancelAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Cancel export and close window");
		cancelButton = new JButton(cancelAct);
		
		final PhonUIAction exportAct = new PhonUIAction(this, "onExport");
		exportAct.putValue(PhonUIAction.NAME, "Export");
		exportAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export selected tables");
		exportButton = new JButton(exportAct);
		
		openAfterExportBox = new JCheckBox("Open after export");
		openAfterExportBox.setSelected(openAfterExport);
		openAfterExportBox.addActionListener( (e) -> { 
			openAfterExport = openAfterExportBox.isSelected();
			PrefHelper.getUserPreferences().putBoolean(OPEN_AFTER_EXPORT_PROP, openAfterExport); 
		} );
		
		customOptionsPanel = new JPanel();
		final JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(exportButton, cancelButton, busyLabel, openAfterExportBox);
		
		final JPanel bottomPanel = new JPanel(new VerticalLayout());
		bottomPanel.add(customOptionsPanel);
		bottomPanel.add(buttonBar);
		
		add(bottomPanel, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(exportButton);
	}
	
	public void onCancel() {
		this.setVisible(false);
		this.dispose();
	}
	
	public void onExport() {
		String exportLocation = locationFunction.get();
		if(exportLocation == null || exportLocation.trim().length() == 0) return;
		
		List<TreePath> checkedPaths = reportTableCheckboxTree.getCheckedPaths();
		if(checkedPaths.size() == 0) {
			Toolkit.getDefaultToolkit().beep();
		}
		
		List<ReportTreeNode> tableNodes =
			checkedPaths.stream()
				.filter( (p) -> ((TristateCheckBoxTreeNode)p.getLastPathComponent()).isLeaf() )
				.map( (p) -> (ReportTreeNode)((TristateCheckBoxTreeNode)p.getLastPathComponent()).getUserObject() )
				.collect(Collectors.toList());
		ExportWorker worker = new ExportWorker(exportLocation, tableNodes);
		busyLabel.setBusy(true);
		worker.execute();
	}
	
	public void showDialog() {
		pack();
		setSize(512, 384);
		setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		setVisible(true);
	}
	
	public JPanel getCustomOptionsPanel() {
		return this.customOptionsPanel;
	}

	public DialogHeader getHeader() {
		return this.header;
	}
	
	private class ExportWorker extends SwingWorker<List<ReportTreeNode>, ReportTreeNode> {

		private List<ReportTreeNode> treeNodes;
		
		private String exportLocation;
		
		public ExportWorker(String exportLocation, List<ReportTreeNode> treeNodes) {
			super();
			this.exportLocation = exportLocation;
			this.treeNodes = treeNodes;
		}

		@Override
		protected List<ReportTreeNode> doInBackground() throws Exception {
			List<ReportTreeNode> processed = new ArrayList<>();
			for(ReportTreeNode treeNode:treeNodes) {
				boolean retVal = exportFunction.apply(exportLocation, treeNode);
				if(retVal) {
					processed.add(treeNode);
					publish(treeNode);
				}
			}
			return processed;
		}
		
		@Override
		protected void done() {
			Toolkit.getDefaultToolkit().beep();
			try {
				finishFunction.accept(get());
			} catch (InterruptedException | ExecutionException e1) {
				LogUtil.severe(e1);
			}
			busyLabel.setBusy(false);
			if(openAfterExport && Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().open(new File(exportLocation));
				} catch (IOException e) {
					LogUtil.severe(e);
					Toolkit.getDefaultToolkit().beep();
				}
			}
			
			onCancel();
		}
		
	}
	
}
