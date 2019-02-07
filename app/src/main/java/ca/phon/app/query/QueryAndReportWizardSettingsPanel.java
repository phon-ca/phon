package ca.phon.app.query;

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.query.QueryAndReportWizardSettings.ReportLoadStrategy;

/**
 * UI for editing {@link QueryAndReportWizardSettings}
 *
 */
public class QueryAndReportWizardSettingsPanel extends JPanel {
	
	private final QueryAndReportWizardSettings settings;
	
	private JCheckBox loadPreviousParamsBox;
	
	private JCheckBox cleanHistoryOnCloseBox;
	
	private JRadioButton loadDefaultReportButton;
	private JRadioButton loadPreviousReportButton;
	
	public QueryAndReportWizardSettingsPanel() {
		super();
		this.settings = new QueryAndReportWizardSettings();
		
		init();
	}
	
	private void init() {
		setLayout(new VerticalLayout(5));
		
		JPanel queryHistoryOptions = new JPanel(new VerticalLayout());
		queryHistoryOptions.setBorder(BorderFactory.createTitledBorder("History"));
		loadPreviousParamsBox = new JCheckBox("Load most recent entry in query history when opening a new wizard");
		loadPreviousParamsBox.setSelected(settings.isLoadPreviousExecutionOnStartup());
		loadPreviousParamsBox.addActionListener( (e) -> {
			settings.setLoadPreviousExecutionOnStartup(loadPreviousParamsBox.isSelected());
			settings.savePreferences();
		});
		
		cleanHistoryOnCloseBox = new JCheckBox("Clean history when closing window");
		cleanHistoryOnCloseBox.setSelected(settings.isCleanHistoryOnClose());
		cleanHistoryOnCloseBox.addActionListener( (e) -> {
			settings.setCleanHistoryOnClose(cleanHistoryOnCloseBox.isSelected());
			settings.savePreferences();
		});
		queryHistoryOptions.add(loadPreviousParamsBox);
		queryHistoryOptions.add(cleanHistoryOnCloseBox);
		
		JPanel reportOptions = new JPanel(new VerticalLayout());
		reportOptions.setBorder(BorderFactory.createTitledBorder("Report"));
		
		loadDefaultReportButton = new JRadioButton("Load default report when opening a new wizard");
		loadDefaultReportButton.setSelected(settings.getReportLoadStrategy() == ReportLoadStrategy.LoadDefaultReport);
		
		loadPreviousReportButton = new JRadioButton("Load previous report when opening a new wizard");
		loadPreviousReportButton.setSelected(settings.getReportLoadStrategy() == ReportLoadStrategy.LoadPreviousReport);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(loadDefaultReportButton);
		bg.add(loadPreviousReportButton);
		
		ActionListener reportLoadListener = (e) -> {
			if(loadDefaultReportButton.isSelected()) {
				settings.setReportLoadStrategy(ReportLoadStrategy.LoadDefaultReport);
			} else if(loadPreviousReportButton.isSelected()) {
				settings.setReportLoadStrategy(ReportLoadStrategy.LoadPreviousReport);
			}
			settings.savePreferences();
		};
		loadDefaultReportButton.addActionListener(reportLoadListener);
		loadPreviousReportButton.addActionListener(reportLoadListener);
		
		reportOptions.add(loadDefaultReportButton);
		reportOptions.add(loadPreviousReportButton);
		
		add(queryHistoryOptions);
		add(reportOptions);
	}

}
