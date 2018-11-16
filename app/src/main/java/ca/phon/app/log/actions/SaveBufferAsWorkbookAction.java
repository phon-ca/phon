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
package ca.phon.app.log.actions;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.ImageIcon;

import com.teamdev.jxbrowser.chromium.JSValue;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.HTMLToWorkbookWriter;
import ca.phon.app.log.LogUtil;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;
import jxl.Workbook;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class SaveBufferAsWorkbookAction extends HookableAction {

	private static final long serialVersionUID = -2827879669257916438L;

	private final static String CMD_NAME = "Export as Excel...";

	private final static String SHORT_DESC = "Export as Excel workbook";

	private final MultiBufferPanel container;

	private final String bufferName;

	public SaveBufferAsWorkbookAction(MultiBufferPanel bufferPanel) {
		this(bufferPanel, null);
	}

	public SaveBufferAsWorkbookAction(MultiBufferPanel bufferPanel, String bufferName) {
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);

		ImageIcon excelIcn = IconManager.getInstance().getSystemIconForFileType("xls", IconSize.SMALL);
		putValue(SMALL_ICON, excelIcn);

		this.bufferName = bufferName;
		this.container = bufferPanel;
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final BufferPanel panel =
				(this.bufferName == null ? this.container.getCurrentBuffer()
						: this.container.getBuffer(bufferName));
		if(panel == null) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		
		FileFilter filter = FileFilter.excelFilter;
		final String illegalCharRegex = "[\\\\/\\[\\]*?:]";
		String initialFilename = panel.getBufferName();
		initialFilename = initialFilename.trim().replaceAll(illegalCharRegex, "_");
		if(initialFilename.endsWith("." + filter.getDefaultExtension())) {
			initialFilename = initialFilename.substring(0, initialFilename.length()-(filter.getDefaultExtension().length()+1));
		}

		final SaveDialogProperties props = new SaveDialogProperties();
		props.setInitialFile(initialFilename + "." + filter.getDefaultExtension());
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.excelFilter);
		props.setRunAsync(true);
		props.setListener( (e) -> {
			if(e.getDialogResult() == NativeDialogEvent.OK_OPTION && e.getDialogData() != null) {
				final String saveAs = e.getDialogData().toString();
				PhonWorker.getInstance().invokeLater( () -> saveWorkbook(saveAs) );
			}
		});

		NativeDialogs.showSaveDialog(props);
	}
	
	private void saveWorkbook(String saveAs) {
		final BufferPanel panel =
				(this.bufferName == null ? this.container.getCurrentBuffer()
						: this.container.getBuffer(bufferName));
		if(panel == null) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		
		try {
			final WritableWorkbook workbook = Workbook.createWorkbook(new File(saveAs));
			if(panel.isShowingTable()) {
				panel.createSheetInExcelWorkbook(workbook);
			} else if(panel.isShowingHtml()) {
				String html = panel.getHTML();
				
				
				final Map<String, DefaultTableDataSource> tableMap = new HashMap<>();
				final JSValue tableMapObj = panel.getBrowser().executeJavaScriptAndReturnValue("window.tableMap");
				if(tableMapObj != null) {
					final Map<?, ?> objMap = (Map<?, ?>)tableMapObj.asJavaObject();
					for(Object key:objMap.keySet()) {
						Object val = objMap.get(key);
						if(val != null && val instanceof DefaultTableDataSource) {
							tableMap.put(key.toString(), (DefaultTableDataSource)val);
						}
					}
				}
				
				final HTMLToWorkbookWriter writer = new HTMLToWorkbookWriter(tableMap);
				writer.writeToWorkbook(workbook, html);
			} else if(panel.isShowingBuffer()) {
				// TODO write text data to new sheet
			}
			workbook.write();
			workbook.close();

			if(this.container.isOpenAfterSaving() && Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(new File(saveAs));
			}
		} catch (IOException | WriteException ex) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(ex);
		}
	}
	
}
