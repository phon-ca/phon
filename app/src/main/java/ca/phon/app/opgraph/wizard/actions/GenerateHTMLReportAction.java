package ca.phon.app.opgraph.wizard.actions;

import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.ReportTreeToHTMLDialog;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import java.awt.event.ActionEvent;

public class GenerateHTMLReportAction extends NodeWizardAction {

    private final ReportTree reportTree;

    public GenerateHTMLReportAction(NodeWizard wizard, ReportTree reportTree) {
        super(wizard);

        this.reportTree = reportTree;

        putValue(PhonUIAction.NAME, "Generate HTML Report...");
        putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getSystemIconForFileType(".html", "mimetypes/text-html", IconSize.SMALL));
    }

    @Override
    public void hookableActionPerformed(ActionEvent ae) {
        final ReportTreeToHTMLDialog dlg = new ReportTreeToHTMLDialog(getNodeWizard(), reportTree);
        dlg.showDialog();
    }

}
