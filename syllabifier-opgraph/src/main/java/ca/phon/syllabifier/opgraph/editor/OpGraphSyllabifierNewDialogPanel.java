package ca.phon.syllabifier.opgraph.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.VerticalLayout;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.editor.NewDialogPanel;
import ca.phon.opgraph.editor.OpgraphEditorModel;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.syllabifier.basic.BasicSyllabifier;
import ca.phon.syllabifier.basic.io.SonorityValues;
import ca.phon.syllabifier.basic.io.SonorityValues.SonorityClass;
import ca.phon.syllabifier.opgraph.OpGraphSyllabifier;
import ca.phon.syllabifier.opgraph.SyllabifierConverter;
import ca.phon.syllabifier.opgraph.nodes.SonorityNode;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.Language;

public class OpGraphSyllabifierNewDialogPanel extends NewDialogPanel {

	private final static Logger LOGGER = Logger.getLogger(OpGraphSyllabifierNewDialogPanel.class.getName());
	
	private static final long serialVersionUID = 5145167830615338657L;
	
	private final static String IMG_PATH = "data/img/syllabifier-example.png";
	
	private Image bgImg = null;
	
	private JLabel titleLabel;
	
	private JLabel descLabel;
	
	private JComboBox<Syllabifier> syllabifierBox;
	
	private JRadioButton emptySyllabifierBtn;
	
	private JRadioButton fromSyllabifierBtn;
	
	public OpGraphSyllabifierNewDialogPanel() {
		super();
		
		init();
	}

	@Override
	public String getTitle() {
		return "Syllabifier";
	}
	
	private Image getBgImage() {
		if(bgImg == null) {
			final URL imgURL = getClass().getClassLoader().getResource(IMG_PATH);
			try {
				bgImg = ImageIO.read(imgURL);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		return bgImg;
	}

	private void init() {
		setLayout(new BorderLayout());
		
		setBackground(Color.white);
		setOpaque(true);
		
		titleLabel = new JLabel("<html><b>New Syllabifier</b></html>");
		titleLabel.setFont(FontPreferences.getTitleFont());
		add(titleLabel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel(new VerticalLayout());
		descLabel = new JLabel();
		
		centerPanel.add(descLabel);
		centerPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		
		final ButtonGroup btnGrp = new ButtonGroup();
		emptySyllabifierBtn = new JRadioButton("Start with empty syllabifier");
		fromSyllabifierBtn = new JRadioButton("Start with existing syllabifier");
		btnGrp.add(emptySyllabifierBtn);
		btnGrp.add(fromSyllabifierBtn);
		emptySyllabifierBtn.setSelected(true);
		
		final ChangeListener cl = (e) -> {
			syllabifierBox.setEnabled(fromSyllabifierBtn.isSelected());
		};
		fromSyllabifierBtn.addChangeListener(cl);
		
		centerPanel.add(emptySyllabifierBtn);
		centerPanel.add(fromSyllabifierBtn);
		
		final SyllabifierLibrary syllabifierLibrary = SyllabifierLibrary.getInstance();
		final Language syllLangPref = syllabifierLibrary.defaultSyllabifierLanguage();
		Syllabifier defSyllabifier = null;
		final Iterator<Syllabifier> syllabifiers = syllabifierLibrary.availableSyllabifiers();
		List<Syllabifier> sortedSyllabifiers = new ArrayList<Syllabifier>();
		while(syllabifiers.hasNext()) {
			final Syllabifier syllabifier = syllabifiers.next();
			if(syllabifier.getLanguage().equals(syllLangPref))
				defSyllabifier = syllabifier;
			sortedSyllabifiers.add(syllabifier);
		}
		Collections.sort(sortedSyllabifiers, (Syllabifier o1, Syllabifier o2) -> {
			return o1.getLanguage().toString().compareTo(o2.getLanguage().toString());
		});
		
		syllabifierBox = new JComboBox<>(sortedSyllabifiers.toArray(new Syllabifier[0]));
		if(defSyllabifier != null)
			syllabifierBox.setSelectedItem(defSyllabifier);
		syllabifierBox.setRenderer(new SyllabifierCellRenderer());
		syllabifierBox.setEnabled(false);
		
		centerPanel.add(syllabifierBox);
		
		add(centerPanel, BorderLayout.CENTER);
	}
	
	private OpGraph createDefaultGraph() {
		final OpGraph graph = new OpGraph();
		
		// add IPASourceNode
		final ObjectNode sourceNode = new ObjectNode(IPATranscript.class);
		sourceNode.setContextKey(OpGraphSyllabifier.IPA_CONTEXT_KEY);
		graph.add(sourceNode);
		
		// setup sonority scale
		final SonorityNode sonorityNode = new SonorityNode();
		graph.add(sonorityNode);
		
		try {
			final OpLink link = new OpLink(sourceNode, sourceNode.getOutputFieldWithKey("obj"),
					sonorityNode, sonorityNode.getInputFieldWithKey("ipa"));
			graph.add(link);
		} catch (ItemMissingException | CycleDetectedException | VertexNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		return graph;
	}
	
	@Override
	public OpgraphEditorModel createModel() {
		OpGraph graph = createDefaultGraph();
		if(fromSyllabifierBtn.isSelected()) {
			final Syllabifier syllabifier = (Syllabifier)syllabifierBox.getSelectedItem();
			
			if(syllabifier instanceof BasicSyllabifier) {
				final SyllabifierConverter converter = new SyllabifierConverter();
				graph = converter.syllabifierToGraph((BasicSyllabifier)syllabifier);
			} else if(syllabifier instanceof OpGraphSyllabifier) {
				graph = ((OpGraphSyllabifier)syllabifier).getGraph();
			}
		}
		return new OpGraphSyllabifierEditorModel(graph);
	}
	
	private class SyllabifierCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			if(value != null) {
				final Syllabifier syllabifier = (Syllabifier)value;
				final String text = syllabifier.getName() + " (" + syllabifier.getLanguage().toString() + ")";
				retVal.setText(text);
			}
			
			return retVal;
		}
		
	}
}
