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
