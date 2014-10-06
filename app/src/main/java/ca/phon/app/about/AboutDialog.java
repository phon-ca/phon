package ca.phon.app.about;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import ca.phon.app.VersionInfo;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

public class AboutDialog extends CommonModuleFrame {
	
	private static final Logger LOGGER = Logger
			.getLogger(AboutDialog.class.getName());

	private static final long serialVersionUID = 1L;
	
	private final static String INFO_FILE = "about_phon.htm";
	
	private JEditorPane infoPane;
	
	private DialogHeader header;
	
	private JButton closeButton;
	
	public AboutDialog() {
		super("About Phon");
		
		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("About Phon", "Version: " + VersionInfo.getInstance().getLongVersion());
		add(header, BorderLayout.NORTH);
		
		final URL aboutPhonURL = getClass().getResource(INFO_FILE);
		try {
			infoPane = new JEditorPane(aboutPhonURL);
			infoPane.setEditable(false);
			final JScrollPane sp = new JScrollPane(infoPane);
			add(sp, BorderLayout.CENTER);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		final PhonUIAction closeAction = new PhonUIAction(this, "close");
		closeAction.putValue(PhonUIAction.NAME, "Close");
		closeButton = new JButton(closeAction);
		add(ButtonBarBuilder.buildOkBar(closeButton), BorderLayout.SOUTH);
	}
	
}
