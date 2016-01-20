package ca.phon.app.opgraph.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTitledSeparator;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.ProcessorEvent;
import ca.gedge.opgraph.ProcessorListener;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.app.opgraph.nodes.log.PrintBufferNode;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.worker.PhonWorker;

public class NodeWizard extends WizardFrame {
	
	private final Processor processor;
	
	private final OpGraph graph;
	
	private MultiBufferPanel bufferPanel;
	
	private JXBusyLabel busyLabel;
	
	private JLabel statusLabel;
	
	private WizardStep reportStep;
	
	public NodeWizard(String title, Processor processor, OpGraph graph) {
		super(title);
		setWindowName(title);
		
		this.processor = processor;
		this.graph = graph;
		init();
	}
	
	private void init() {
		bufferPanel = new MultiBufferPanel();
		
		final DialogHeader header = new DialogHeader(super.getTitle(), "");
		add(header, BorderLayout.NORTH);
		
		final JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		busyLabel = new JXBusyLabel(new Dimension(22, 22));
		statusLabel = new JLabel();
		
		statusPanel.setOpaque(false);
		statusPanel.add(busyLabel);
		statusPanel.add(statusLabel);
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 24, 5, 2);
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 1;
		
		header.add(statusPanel, gbc);
		
		final WizardExtension nodeWizardList = 
				graph.getExtension(WizardExtension.class);
		int stepIdx = 0;
		if(nodeWizardList != null) {
			for(OpNode node:nodeWizardList) {
				final WizardStep step = createStep(node);
				if(step != null) {
					step.setPrevStep(stepIdx-1);
					step.setNextStep(stepIdx+1);
					++stepIdx;
					
					addWizardStep(step);
				}
			}
		}
		
		reportStep = createReportStep();
		reportStep.setPrevStep(stepIdx-1);
		reportStep.setNextStep(-1);
		addWizardStep(reportStep);
		
		super.btnFinish.setVisible(false);
	}
	
	public MultiBufferPanel getBufferPanel() {
		return this.bufferPanel;
	}
	
	public OpGraph getGraph() {
		return this.graph;
	}
	
	public WizardExtension getWizardExtension() {
		return this.graph.getExtension(WizardExtension.class);
	}
	
	final ProcessorListener processorListener =  (ProcessorEvent pe) -> {
		if(pe.getType() == ProcessorEvent.Type.BEGIN_NODE) {
			final String nodeName = pe.getNode().getName();
			SwingUtilities.invokeLater( () -> {
				if(!busyLabel.isBusy()) {
					busyLabel.setBusy(true);
				}
				statusLabel.setText(nodeName);
				btnBack.setEnabled(false);
			});
		} else if(pe.getType() == ProcessorEvent.Type.FINISH_NODE) {
			
		} else if(pe.getType() == ProcessorEvent.Type.COMPLETE) {
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(false);
				statusLabel.setText("");
				btnBack.setEnabled(true);
			});
		}
	};
	
	protected void executeGraph() throws ProcessingException {
		setupContext(processor.getContext());
		if(!processor.hasNext()) {
			processor.reset();
		}
		processor.addProcessorListener(processorListener);
		try {
			processor.stepAll();
		} catch (ProcessingException pe) {
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(false);
				statusLabel.setText(pe.getLocalizedMessage());
				
				final BufferPanel errPanel = bufferPanel.createBuffer("Error");
				final PrintWriter writer = new PrintWriter(errPanel.getLogBuffer().getStdErrStream());
				pe.printStackTrace(writer);
				writer.flush();
				writer.close();
			});
			throw pe;
		}
	}

	protected void setupContext(OpContext ctx) {
		ctx.put(PrintBufferNode.BUFFERS_KEY, bufferPanel);
	}
	
	protected WizardStep createStep(OpNode node) {
		final NodeSettings settings = node.getExtension(NodeSettings.class);
		if(settings != null) {
			try {
				final Component comp = settings.getComponent(null);
				
				final List<OpNode> nodePath = 
						graph.getNodePath(node.getId());
				final String path = 
						nodePath.stream()
							.map( n -> n.getName() )
							.collect(Collectors.joining("/"));
				
				final WizardStep step = new WizardStep();
				step.setLayout(new BorderLayout());
				
				final JXTitledSeparator stepTitle = 
						new JXTitledSeparator(path);
				step.add(stepTitle, BorderLayout.NORTH);
				step.add(new JScrollPane(comp), BorderLayout.CENTER);
				
				return step;
			} catch (NullPointerException e) {
				// we have no document, this may cause an exception
				// depending on implementation - ignore it.
			}
		}
		return null;
	}
	
	protected WizardStep createReportStep() {
		final WizardStep retVal = new WizardStep();
		
		retVal.setLayout(new BorderLayout());
		
		
		
//		retVal.add(statusPanel, BorderLayout.NORTH);
		retVal.add(getBufferPanel(), BorderLayout.CENTER);
		
		return retVal;
	}
	
	@Override
	public void gotoStep(int step) {
		super.gotoStep(step);
		
		final Runnable inBg = () -> {
			if(super.getCurrentStep() == reportStep) {
				try {
					executeGraph();
				} catch (ProcessingException e) {
					e.printStackTrace();
				}
			}
		};
		
		final Runnable onEDT = () -> {
			PhonWorker.getInstance().invokeLater(inBg);
		};
		
		SwingUtilities.invokeLater(onEDT);
	}
	
}
