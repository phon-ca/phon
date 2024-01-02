package ca.phon.app.session.editor.view.transcriptEditor.actions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptDocument;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptEditor;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptStyleConstants;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.ui.FontFormatter;
import ca.phon.ui.fonts.FontPreferences;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;

public class ExportAsPDFAction extends TranscriptAction {
    private static final long serialVersionUID = -6337537539656747666L;
    private final Paragraph spacerParagraph = new Paragraph("\n");
    private final TranscriptEditor transcriptEditor;

    public ExportAsPDFAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        transcriptEditor = view.getTranscriptEditor();
        putValue(NAME, "Export as PDF");
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
//        System.out.println("Exporting as PDF...");
//        try {
//            PdfFontFactory.registerSystemDirectories();
//
//            String dest = "/Users/cyates/desktop/sample.pdf";
//            PdfWriter writer = new PdfWriter(dest);
//            PdfDocument pdfDoc = new PdfDocument(writer);
//
//            pdfDoc.addNewPage();
//            Document document = new Document(pdfDoc);
//
//            TranscriptDocument transcriptDoc = transcriptEditor.getTranscriptDocument();
//
//            UnitValue[] pointColumnWidths = {UnitValue.createPercentValue(20), UnitValue.createPercentValue(80)};
//            Table table = new Table(pointColumnWidths);
//
//            Record record = null;
//
//            boolean inHeaders = true;
//
////            var headerTierMap = transcriptDoc.getHeaderTierMap();
//
//            var root = transcriptDoc.getDefaultRootElement();
//            for (int i = 0; i < root.getElementCount(); i++) {
//                var elem = root.getElement(i);
//                String elemText = transcriptDoc.getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());
//                if (elemText.length() < labelColumnWidth) continue;
//                AttributeSet attrs = transcriptDoc
//                    .getCharacterElement(elem.getStartOffset() + labelColumnWidth + 2)
//                    .getAttributes();
//                Record currentRecord = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
//                Tier<?> genericTier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_GENERIC);
////                boolean isHeaderTier = false;
////                if (inHeaders) {
////                    isHeaderTier = headerTierMap.containsValue(genericTier);
////                }
////                if (record != currentRecord || (inHeaders && !isHeaderTier)) {
////                    if (inHeaders && !isHeaderTier) {
////                        inHeaders = false;
////                    }
////                    table.addCell(createSpacerCell());
////                    table.addCell(createSpacerCell());
////                }
//                record = currentRecord;
//                String labelText = elemText.substring(0, labelColumnWidth).strip();
//                String contentText = elemText.substring(labelColumnWidth + 2).strip();
//
//                Cell labelCell = new Cell();
//                labelCell.setBorder(Border.NO_BORDER);
//                labelCell.setTextAlignment(TextAlignment.RIGHT);
//                Text labelTextObj = new Text(labelText);
////                labelTextObj.setFont(monospaceFont);
//                labelCell.add(new Paragraph(labelTextObj));
//                table.addCell(labelCell);
//
//                Cell contentCell = new Cell();
//                contentCell.setBorder(Border.NO_BORDER);
//                contentCell.setBorderLeft(new SolidBorder(Color.createColorWithColorSpace(new float[]{1, 1, 1}), 5));
//                Text contentTextObj = new Text(contentText);
//                contentCell.add(new Paragraph(contentTextObj));
//                table.addCell(contentCell);
//            }
//
//            document.add(table);
//
//            document.close();
//
//            System.out.println("PDF Created");
//        }
//        catch (Exception error) {
//            LogUtil.severe(error);
//        }
    }

    private Cell createSpacerCell() {
        Cell spacerCell = new Cell();
        spacerCell.setBorder(Border.NO_BORDER);
        spacerCell.add(spacerParagraph);
        return spacerCell;
    }
}