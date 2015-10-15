package ca.phon.syllabifier.opgraph.editor;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.VerticalLayout;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.components.ContextViewerPanel;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.opgraph.editor.OpgraphEditorModel;
import ca.phon.opgraph.editor.OpgraphEditorModelFactory;
import ca.phon.syllabifier.opgraph.OpGraphSyllabifier;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.SyllabificationDisplay;

/**
 * Editor model for syllabifiers implemented with opgraph.
 *
 */
public class OpGraphSyllabifierEditorModel extends OpgraphEditorModel {
	
	/**
	 * Settings panel
	 */
	private JPanel syllabifierPanel;
	
	private SyllabifierSettingsPanel settingsPanel;

	private JTextField ipaField;
	
	private SyllabificationDisplay syllabificationDisplay;
	
	private ContextViewerPanel customDebugPanel;
	
	public OpGraphSyllabifierEditorModel() {
		this(new OpGraph());
	}

	public OpGraphSyllabifierEditorModel(OpGraph opgraph) {
		super(opgraph);
	}
	
	private void updateIPA() {
		final String txt = ipaField.getText().trim();
		final IPATranscript transcript = 
				(new IPATranscriptBuilder()).append(txt).toIPATranscript();
		syllabificationDisplay.setTranscript(transcript);
	}
	
	public JPanel getSyllabifierPanel() {
		if(syllabifierPanel == null) {
			syllabifierPanel = new JPanel();
			
			ipaField = new JTextField();
			ipaField.setFont(FontPreferences.getUIIpaFont());
			ipaField.getDocument().addDocumentListener(new DocumentListener() {
				
				@Override
				public void removeUpdate(DocumentEvent e) {
					updateIPA();
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateIPA();
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
				}
			});
			
			syllabificationDisplay = new SyllabificationDisplay();
			getDocument().addPropertyChangeListener(GraphDocument.PROCESSING_CONTEXT, (e) -> {
				syllabificationDisplay.repaint();
				syllabificationDisplay.revalidate();
			});
			
			settingsPanel = new SyllabifierSettingsPanel();
			final SyllabifierSettings settings = getDocument().getGraph().getExtension(SyllabifierSettings.class);
			if(settings != null) {
				settingsPanel.loadSettings(settings);
			}
			settingsPanel.setBorder(BorderFactory.createTitledBorder("Syllabifier Settings"));
			
			final FormLayout layout = new FormLayout(
					"right:pref, 3dlu, fill:pref:grow", "pref, 3dlu, pref, pref");
			final CellConstraints cc = new CellConstraints();
			final JPanel ipaPanel = new JPanel(layout);
			ipaPanel.add(new JLabel("Transcript:"), cc.xy(1, 1));
			ipaPanel.add(ipaField, cc.xy(3, 1));
			ipaPanel.add(new JLabel("Syllabification:"), cc.xy(1, 3));
			ipaPanel.add(syllabificationDisplay, cc.xyw(1, 4, 3));
			ipaPanel.setBorder(BorderFactory.createTitledBorder("Debug Settings"));
			
			syllabifierPanel.setLayout(new BorderLayout());
			syllabifierPanel.add(settingsPanel, BorderLayout.NORTH);
			syllabifierPanel.add(new JScrollPane(ipaPanel), BorderLayout.CENTER);
		}
		return syllabifierPanel;
	}
	
	@Override
	public void setupContext(OpContext context) {
		updateIPA();
		context.put(OpGraphSyllabifier.IPA_CONTEXT_KEY, syllabificationDisplay.getTranscript());
	}

	@Override
	protected Map<String, JComponent> getViewMap() {
		final Map<String, JComponent> retVal = super.getViewMap();
		retVal.put("Syllabifier", getSyllabifierPanel());
		return retVal;
	}
	
	@Override
	public boolean isViewVisibleByDefault(String viewName) {
		boolean retVal = super.isViewVisibleByDefault(viewName);
		if(viewName.equals("Syllabifier")) retVal = true;
		return retVal;
	}

	@Override
	public boolean validate() {
		boolean retVal = super.validate();
		
		// setup extensions
		final SyllabifierSettings settings = settingsPanel.getSyllabifierSettings();
		getDocument().getGraph().putExtension(SyllabifierSettings.class, settings);
		
		return retVal;
	}
	
}
