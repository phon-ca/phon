package ca.phon.opgraph.editor;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

public final class NewGraphDialog extends JDialog {

	private static final long serialVersionUID = 2222190314079179259L;

	private DialogHeader header;
	
	private JTabbedPane tabbedPane;
	
	private JButton okButton;
	
	private JButton cancelButton;
	
	private boolean wasCanceled = false;
	
	public NewGraphDialog(OpgraphEditor editor) {
		super(editor, "New Graph");
		
		setModal(true);
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("New Graph", "");
		add(header, BorderLayout.NORTH);
		
		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
		
		for(IPluginExtensionPoint<NewDialogPanel> extPt:PluginManager.getInstance().getExtensionPoints(NewDialogPanel.class)) {
			final IPluginExtensionFactory<NewDialogPanel> panelFactory = extPt.getFactory();
			final NewDialogPanel panel = panelFactory.createObject();
			tabbedPane.addTab(panel.getTitle(), panel);
		}
		
		final PhonUIAction okAct = new PhonUIAction(this, "onOk");
		okAct.putValue(PhonUIAction.NAME, "Ok");
		okButton = new JButton(okAct);
		
		final PhonUIAction cancelAct = new PhonUIAction(this, "onCancel");
		cancelAct.putValue(PhonUIAction.NAME, "Cancel");
		cancelButton = new JButton(cancelAct);
		
		final JComponent btnBar = ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton);
		add(btnBar, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(okButton);
	}
	
	public void onOk() {
		wasCanceled = false;
		setVisible(false);
		dispose();
	}
	
	public void onCancel() {
		wasCanceled = true;
		setVisible(false);
		dispose();
	}
	
	public boolean wasCanceled() {
		return this.wasCanceled;
	}
	
	public NewDialogPanel getSelectedPanel() {
		return (NewDialogPanel)tabbedPane.getSelectedComponent();
	}
	
}
