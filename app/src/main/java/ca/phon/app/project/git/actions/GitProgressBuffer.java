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
package ca.phon.app.project.git.actions;

import java.awt.BorderLayout;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.eclipse.jgit.lib.ProgressMonitor;

import ca.phon.app.log.BufferPanel;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

/**
 * Frame for displaying progress from a git command.
 */
public class GitProgressBuffer extends BufferPanel implements ProgressMonitor {

	private static final long serialVersionUID = 4035140624499246804L;
	
	private BufferPanel bufferPanel;
	
	private PrintWriter printer;
	
	private PrintWriter errPrinter;
	
	private volatile boolean canceled = false;
	
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
		printer.println(title);
		printer.flush();
	}

	@Override
	public void update(int completed) {
	}

	@Override
	public void endTask() {
	}

	@Override
	public boolean isCancelled() {
		return canceled;
	}
	
}
