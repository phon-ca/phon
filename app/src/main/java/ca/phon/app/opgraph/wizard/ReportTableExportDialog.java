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
package ca.phon.app.opgraph.wizard;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.report.tree.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.PrefHelper;
import org.jdesktop.swingx.*;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * UI for actions exporting report tables. 
 */
public class ReportTableExportDialog extends CommonModuleFrame {
	
	private DialogHeader header;
	
	private final static String OPEN_AFTER_EXPORT_PROP = ReportTableExportDialog.class.getName() + ".openAfterExport";
	private final static boolean DEFAULT_OPEN_AFTER_EXPORT = Boolean.TRUE;
	private boolean openAfterExport = PrefHelper.getBoolean(OPEN_AFTER_EXPORT_PROP, DEFAULT_OPEN_AFTER_EXPORT);

	private final static String USE_INTEGER_FOR_BOOLEAN_PROP = ReportTableExportDialog.class.getName() + ".useIntegerForBoolean";
	private final static boolean DEFAULT_USE_INTEGER_FOR_BOOLEAN = Boolean.TRUE;
	private boolean useIntegerForBoolean = PrefHelper.getBoolean(USE_INTEGER_FOR_BOOLEAN_PROP, DEFAULT_USE_INTEGER_FOR_BOOLEAN);
	
	private JCheckBox openAfterExportBox;

	private JCheckBox useIntegerForBooleanBox;

	private JPanel customOptionsPanel;
	
	private ReportTableCheckboxTree reportTableCheckboxTree;
	
	private JXBusyLabel busyLabel;
	private ReportTree reportTree;
	
	private JButton exportButton;
	
	private JButton cancelButton;
	
	private Supplier<String> locationFunction;
	private ReportTreeExportFunction exportFunction;
	private Consumer<List<ReportTreeNode>> finishFunction;
	
	private boolean includeExcelExportable = false;

	public ReportTableExportDialog(ReportTree reportTree, Supplier<String> locationFunction,
			ReportTreeExportFunction exportFunction, Consumer<List<ReportTreeNode>> finishFunction, boolean includeExcelExportable) {
		super();
		setWindowName("Export Report Tables");
		
		this.reportTree = reportTree;
		this.locationFunction = locationFunction;
		this.exportFunction = exportFunction;
		this.finishFunction = finishFunction;
		this.includeExcelExportable = includeExcelExportable;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("Export Tables", "Use checkboxes to select tables for export");
		add(header, BorderLayout.NORTH);
		
		reportTableCheckboxTree = new ReportTableCheckboxTree(reportTree, includeExcelExportable);
		reportTableCheckboxTree.expandAll();
		
		final JScrollPane scroller = new JScrollPane(reportTableCheckboxTree);
		
		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		
		add(scroller, BorderLayout.CENTER);
		
		final PhonUIAction<Void> cancelAct = PhonUIAction.runnable(this::onCancel);
		cancelAct.putValue(PhonUIAction.NAME, "Close");
		cancelAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Cancel export and close window");
		cancelButton = new JButton(cancelAct);
		
		final PhonUIAction<Void> exportAct = PhonUIAction.runnable(this::onExport);
		exportAct.putValue(PhonUIAction.NAME, "Export");
		exportAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export selected tables");
		exportButton = new JButton(exportAct);
		
		openAfterExportBox = new JCheckBox("Open after export");
		openAfterExportBox.setSelected(openAfterExport);
		openAfterExportBox.addActionListener( (e) -> { 
			openAfterExport = openAfterExportBox.isSelected();
			PrefHelper.getUserPreferences().putBoolean(OPEN_AFTER_EXPORT_PROP, openAfterExport); 
		} );

		useIntegerForBooleanBox = new JCheckBox("Use integers (1/0) for boolean values");
		useIntegerForBooleanBox.setSelected(useIntegerForBoolean);
		useIntegerForBooleanBox.addActionListener( (e) -> {
			useIntegerForBoolean = useIntegerForBooleanBox.isSelected();
			PrefHelper.getUserPreferences().putBoolean(USE_INTEGER_FOR_BOOLEAN_PROP, useIntegerForBoolean);
		});

		customOptionsPanel = new JPanel(new VerticalLayout());
		customOptionsPanel.add(useIntegerForBooleanBox);
		customOptionsPanel.add(openAfterExportBox);

		final JComponent exportBtnPanel = new JPanel(new HorizontalLayout(3));
		exportBtnPanel.add(busyLabel);
		exportBtnPanel.add(exportButton);
		final JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(exportBtnPanel, cancelButton);
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
		List<TreePath> checkedPaths = reportTableCheckboxTree.getCheckedPaths();
		if(checkedPaths.size() == 0) {
			showErrorMessage("Please select at least one table");
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		String exportLocation = locationFunction.get();
		if(exportLocation == null || exportLocation.trim().length() == 0) return;

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
				boolean retVal = exportFunction.exportReportTree(exportLocation, treeNode, useIntegerForBoolean);
				if(retVal) {
					processed.add(treeNode);
					publish(treeNode);
				}
			}
			return processed;
		}
		
		@Override
		protected void done() {
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
