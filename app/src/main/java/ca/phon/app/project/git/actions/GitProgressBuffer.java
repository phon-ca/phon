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
package ca.phon.app.project.git.actions;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.eclipse.jgit.lib.ProgressMonitor;

import ca.phon.app.log.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.layout.*;

/**
 * Frame for displaying progress from a git command.
 */
public class GitProgressBuffer extends BufferPanel implements ProgressMonitor {

	private static final long serialVersionUID = 4035140624499246804L;
	
	private BufferPanel bufferPanel;
	
	private PrintWriter printer;
	
	private PrintWriter errPrinter;
	
	private volatile boolean canceled = false;
	
	private Stack<String> taskStack = new Stack<>();
	
	public GitProgressBuffer(String title) {
		super(title);
		
		init();
	}
	
	private void init() {
		printer = new PrintWriter(getLogBuffer().getStdOutStream());
		errPrinter = new PrintWriter(getLogBuffer().getStdErrStream());
	}
	
	public CommonModuleFrame createWindow() {
		final CommonModuleFrame cmf = new CommonModuleFrame(super.getBufferName());
		cmf.setLayout(new BorderLayout());
		
		final DialogHeader header = new DialogHeader(super.getBufferName(), "");
		cmf.add(header, BorderLayout.NORTH);
		
		cmf.add(this, BorderLayout.CENTER);
		
		final PhonUIAction act = new PhonUIAction(cmf, "setVisible", false);
		act.putValue(PhonUIAction.NAME, "Close");
		final JComponent btnPanel = ButtonBarBuilder.buildOkBar(new JButton(act));
		add(btnPanel, BorderLayout.SOUTH);
		
		return cmf;
	}
	
	public BufferPanel getBufferPanel() {
		return this.bufferPanel;
	}
	
	public PrintWriter getPrinter() {
		return this.printer;
	}
	
	public PrintWriter getErrPrinter() {
		return this.errPrinter;
	}

	@Override
	public void start(int totalTasks) {
	}

	@Override
	public void beginTask(String title, int totalWork) {
		if(!taskStack.isEmpty()) {
			printer.println();
			printer.flush();
		}
		
		System.out.println(title + " " + totalWork);
		printer.print(LogBuffer.ESCAPE_CODE_PREFIX);
		printer.print(BufferPanel.SHOW_BUSY);
		printer.flush();
		
		printer.print(title);
		printer.flush();
		
		taskStack.push(title);
	}

	@Override
	public void update(int completed) {
		System.out.println("Completed " + completed);
		if(completed == 1) {
			printer.print(".");
		} else {
			printer.println(completed);
		}
		printer.flush();
	}

	@Override
	public void endTask() {
		taskStack.pop();
		
		printer.println();
		printer.flush();
		
		printer.print(LogBuffer.ESCAPE_CODE_PREFIX);
		printer.print(BufferPanel.STOP_BUSY);
		printer.flush();
	}

	@Override
	public boolean isCancelled() {
		return canceled;
	}
	
}
