package ca.phon.app.prefs;

import java.awt.BorderLayout;

import ca.phon.app.query.QueryAndReportWizardSettingsPanel;

public class QueryPrefsPanel extends PrefsPanel {

	private static final long serialVersionUID = -8358380385848301301L;

	private QueryAndReportWizardSettingsPanel settingsPanel;
	
	public QueryPrefsPanel() {
		super("Query");
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		settingsPanel = new QueryAndReportWizardSettingsPanel();
		add(settingsPanel, BorderLayout.CENTER);
	}

}
