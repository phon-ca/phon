package ca.phon.app.session.editor;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.ipa.Diacritic;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.OpgraphIO;
import ca.phon.project.Project;
import ca.phon.query.script.params.*;
import ca.phon.session.*;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.wizard.WizardStep;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ITRWizard extends NodeWizard {

    public final static String TITLE= "Inter-transcriber Reliability";

    private final static String GRAPH_FILE = "ITRGraph.xml";
    private final static String PROJECT = "_project";
    private final static String SESSION = "_session";
    private final static String TRANSCRIBERS = "_transcribers";
    private final static String GROUPORWORD = "_groupOrWord";
    private final static String IGNOREDIACRITICS = "_ignoreDiacritics";
    private final static String ONLYOREXCEPT = "_onlyOrExcept";
    private final static String SELECTEDDIACRITICS = "_selectedDiacritics";
    private final static String INCLUDECONSONANTAGREEEMENT = "_includeConsonantAgreement";
    private final static String INCLUDEVOWELAGREEMENT = "_includeVowelAgreement";
    private final static String INCLUDEDIACRITICAGREEMENT = "_includeDiacriticAgreement";
    private final static String INCLUDETONENUMBERAGREEMENT = "_includeToneNumberAgreement";

    private Project project;

    private Session session;

    private WizardStep optionsStep;
    private JRadioButton byGroupBtn;
    private JRadioButton byWordBtn;
    private Map<Transcriber, JCheckBox> transcriberBoxMap =
            new LinkedHashMap<>();
    private DiacriticOptionsPanel diacriticOptionsPanel;
    private JCheckBox includeCAgreementBox;
    private JCheckBox includeVAgreementBox;
    private JCheckBox includeDiaAgreementBox;
    private JCheckBox includeTNAgreementBox;

    public ITRWizard(Project project, Session session) {
        super(TITLE);
        setWindowName(TITLE + ": " + session.getCorpus() + "." + session.getName());

        this.project = project;
        putExtension(Project.class, project);
        this.session = session;

        init();
    }

    private static OpGraph loadITRGraph() {
        try (InputStream is = ITRWizard.class.getResourceAsStream(GRAPH_FILE)) {
            return OpgraphIO.read(is);
        } catch (IOException e) {
            LogUtil.warning(e);
        }
        return new OpGraph();
    }

    private void init() {
        this.optionsStep = createOptionsStep();
        this.optionsStep.setNextStep(1);
        addWizardStep(optionsStep);

        loadGraph(loadITRGraph());
        overridesButton.setVisible(false);
    }

    private WizardStep createOptionsStep() {
        WizardStep retVal = new WizardStep();
        retVal.setTitle("Options");
        retVal.setLayout(new BorderLayout());

        JPanel domainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup btnGrp = new ButtonGroup();
        byGroupBtn = new JRadioButton("By Group");
        byGroupBtn.setSelected(false);
        btnGrp.add(byGroupBtn);
        domainPanel.add(byGroupBtn);
        byWordBtn = new JRadioButton("By Word");
        byWordBtn.setSelected(true);
        btnGrp.add(byWordBtn);
        domainPanel.add(byWordBtn);

        JPanel transcriberPanel = new JPanel(new VerticalLayout());
        for(Transcriber transcriber:session.getTranscribers()) {

            String transcriberInfo =
                    (transcriber.getRealName().length() > 0
                        ? String.format("%s (%s)", transcriber.getRealName(), transcriber.getUsername())
                        : transcriber.getUsername());

            JCheckBox transcriberBox = new JCheckBox(transcriberInfo);
            transcriberBox.setSelected(true);
            transcriberBoxMap.put(transcriber, transcriberBox);
            transcriberPanel.add(transcriberBox);
        }
        transcriberPanel.setBorder(BorderFactory.createTitledBorder("Select Transcribers"));

        JPanel optionalsPanel = new JPanel(new VerticalLayout());
        diacriticOptionsPanel = new DiacriticOptionsPanel();
        optionalsPanel.add(diacriticOptionsPanel);
        includeCAgreementBox = new JCheckBox("Include consonant agreement");
        includeCAgreementBox.setSelected(true);
        optionalsPanel.add(includeCAgreementBox);
        includeVAgreementBox = new JCheckBox("Include vowel agreement");
        includeVAgreementBox.setSelected(true);
        optionalsPanel.add(includeVAgreementBox);
        includeDiaAgreementBox = new JCheckBox("Include diacritic agreement");
        includeDiaAgreementBox.setSelected(false);
        optionalsPanel.add(includeDiaAgreementBox);
        includeTNAgreementBox = new JCheckBox("Include tone number agreement");
        includeTNAgreementBox.setSelected(false);
        optionalsPanel.add(includeTNAgreementBox);
        optionalsPanel.setBorder(BorderFactory.createTitledBorder("Metrics"));

        JPanel contentPanel = new JPanel(new VerticalLayout());
        contentPanel.add(domainPanel);
        contentPanel.add(transcriberPanel);
        contentPanel.add(optionalsPanel);

        TitledPanel centerPanel = new TitledPanel("Select Transcribers and Metrics", contentPanel);
        retVal.add(centerPanel, BorderLayout.CENTER);

        return retVal;
    }

    private List<Transcriber> getSelectedTranscribers() {
        return transcriberBoxMap.entrySet().stream()
                .filter( (entry) -> entry.getValue().isSelected() )
                .map( (entry) -> entry.getKey() )
                .collect(Collectors.toList());
    }

    private boolean isGroupOrWord() {
        return byGroupBtn.isSelected();
    }

    private boolean isIgnoreDiacritics() {
        return diacriticOptionsPanel.getDiacriticOptions().isIgnoreDiacritics();
    }

    private boolean isOnlyOrExcept() {
        return diacriticOptionsPanel.getDiacriticOptions().getSelectionMode() == DiacriticOptionsScriptParam.SelectionMode.ONLY;
    }

    private Collection<Diacritic> getSelectedDiacritics() {
        return diacriticOptionsPanel.getDiacriticOptions().getSelectedDiacritics();
    }

    private boolean isIncludeConsonantAgreement() {
        return includeCAgreementBox.isSelected();
    }

    private boolean isIncludeVowelAgreement() {
        return includeVAgreementBox.isSelected();
    }

    private boolean isIncludeDiacriticAgreement() {
        return includeDiaAgreementBox.isSelected();
    }

    private boolean isIncludeToneNumberAgreement() {
        return includeTNAgreementBox.isSelected();
    }

    @Override
    protected void setupContext(OpContext ctx) {
        super.setupContext(ctx);

        ctx.put(PROJECT, project);
        ctx.put(SESSION, session);
        ctx.put(TRANSCRIBERS, getSelectedTranscribers());
        ctx.put(GROUPORWORD, isGroupOrWord());
        ctx.put(IGNOREDIACRITICS, isIgnoreDiacritics());
        ctx.put(ONLYOREXCEPT, isOnlyOrExcept());
        ctx.put(SELECTEDDIACRITICS, getSelectedDiacritics());
        ctx.put(INCLUDECONSONANTAGREEEMENT, isIncludeConsonantAgreement());
        ctx.put(INCLUDEVOWELAGREEMENT, isIncludeVowelAgreement());
        ctx.put(INCLUDEDIACRITICAGREEMENT, isIncludeDiacriticAgreement());
        ctx.put(INCLUDETONENUMBERAGREEMENT, isIncludeToneNumberAgreement());
    }

}
