package ca.phon.app.session;

import java.awt.Font;
import java.awt.image.*;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.imageio.*;

import ca.phon.app.excel.*;
import ca.phon.app.log.*;
import ca.phon.formatter.*;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.query.db.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.filter.*;
import ca.phon.ui.fonts.*;
import ca.phon.util.*;
import jxl.format.*;
import jxl.write.*;
import jxl.write.Label;

public class SessionToExcel extends SessionExporter {

	private WritableCellFormat defaultFormat;
	
	private WritableCellFormat columnHeaderFormat;
	
	private WritableCellFormat dateCellFormat;
	
	private WritableCellFormat tierDataFormat;
	
	private WritableCellFormat tierNameFormat;
	
//	private Map<TierViewItem, WritableCellFormat> tierFormats = new HashMap<>();
	
	public SessionToExcel() {
		super();
	}

	public SessionToExcel(SessionExportSettings settings) {
		super(settings);
	}
	
	public WritableCellFormat getDefaultFormat() {
		if(defaultFormat == null) {
			defaultFormat = WorkbookFormats.getDefaultFormat();
		}
		return defaultFormat;
	}
	
	public WritableCellFormat getColumnHeaderFormat() {
		if(columnHeaderFormat == null) {
			columnHeaderFormat = WorkbookFormats.getColumnHeaderFormat();
		}
		return columnHeaderFormat;
	}

	public WritableCellFormat getDateCellFormat() {
		if(dateCellFormat == null) {
			 dateCellFormat = WorkbookFormats.getDateCellFormat();
		}
		return dateCellFormat;
	}
	
	public WritableCellFormat getTierNameFormat() throws WriteException {
		if(tierNameFormat == null) {
			tierNameFormat = new WritableCellFormat();
	        WritableFont tierNameFont = new WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD);
	        tierNameFont.setItalic(true);
	        tierNameFormat.setFont(tierNameFont);
		}
		return tierNameFormat;
	}
	
	public WritableCellFormat getTierDataFormat() throws WriteException {
		if(tierDataFormat == null) {
			Font javaFont =	FontPreferences.getTierFont();
			WritableFont tierFont = new WritableFont(WritableFont.createFont(javaFont.getFontName()), 
					WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD);
			tierDataFormat = new WritableCellFormat();
			tierDataFormat.setFont(tierFont);
		}
		return tierDataFormat;
	}
	
	private Tuple<Integer, Integer> appendParticipantTable(WritableWorkbook wb, WritableSheet sheet, int rowIdx, Session session) 
		throws WriteException {
		// setup header
		WritableCellFormat columnHeaderFormat = getColumnHeaderFormat();
        WritableCellFormat defaultFormat = getDefaultFormat();
        WritableCellFormat dateCellFormat = getDateCellFormat();
		
        int colIdx = 0;
        Label nameHeaderLbl = new Label(colIdx++, rowIdx, "Participant", columnHeaderFormat);
        sheet.addCell(nameHeaderLbl);
        
        if(getSettings().isIncludeAge()) {
        	Label headerLbl = new Label(colIdx++, rowIdx, "Age", columnHeaderFormat);
        	sheet.addCell(headerLbl);
		}
		if(getSettings().isIncludeBirthday()) {
			Label headerLbl = new Label(colIdx++, rowIdx, "Birthday", columnHeaderFormat);
        	sheet.addCell(headerLbl);
		}
		if(getSettings().isIncludeRole()) {
			Label headerLbl = new Label(colIdx++, rowIdx, "Role", columnHeaderFormat);
        	sheet.addCell(headerLbl);
		}
		if(getSettings().isIncludeSex()) {
			Label headerLbl = new Label(colIdx++, rowIdx, "Sex", columnHeaderFormat);
        	sheet.addCell(headerLbl);
		}
		if(getSettings().isIncludeLanguage()) {
			Label headerLbl = new Label(colIdx++, rowIdx, "Language", columnHeaderFormat);
        	sheet.addCell(headerLbl);
		}
		if(getSettings().isIncludeGroup()) {
			Label headerLbl = new Label(colIdx++, rowIdx, "Group", columnHeaderFormat);
        	sheet.addCell(headerLbl);
		}
		if(getSettings().isIncludeEducation()) {
			Label headerLbl = new Label(colIdx++, rowIdx, "Education", columnHeaderFormat);
        	sheet.addCell(headerLbl);
		}
		if(getSettings().isIncludeSES()) {
			Label headerLbl = new Label(colIdx++, rowIdx, "SES", columnHeaderFormat);
        	sheet.addCell(headerLbl);
		}
		
		for(var i = 0; i < session.getParticipantCount(); i++) {
			colIdx = 0;
			++rowIdx;
			var participant = session.getParticipant(i);
			
			Label nameLbl = new Label(colIdx++, rowIdx, participant.toString(), defaultFormat);
        	sheet.addCell(nameLbl);
			
	        if(getSettings().isIncludeAge()) {
	        	String str = 
	        			(participant.getBirthDate() != null ? FormatterUtil.format(participant.getAge(session.getDate())) : "N/A");
	        	Label lbl = new Label(colIdx, rowIdx, str, defaultFormat);
	        	sheet.addCell(lbl);
	        	++colIdx;
			}
	        
			if(getSettings().isIncludeBirthday()) {
				if(participant.getBirthDate() != null) {
					DateTime bdayCell = new DateTime(colIdx, rowIdx, 
							Date.from(session.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()), dateCellFormat);
					sheet.addCell(bdayCell);
				}
				++colIdx;
			}
			
			if(getSettings().isIncludeRole()) {
				if(participant.getRole() != null) {
					Label lbl = new Label(colIdx, rowIdx, participant.getRole().toString(), defaultFormat);
		        	sheet.addCell(lbl);
				}
				++colIdx;
			}
			
			if(getSettings().isIncludeSex()) {
				if(participant.getSex() != null) {
					Label lbl = new Label(colIdx, rowIdx, participant.getSex().toString(), defaultFormat);
		        	sheet.addCell(lbl);
				}
				++colIdx;
			}
			
			if(getSettings().isIncludeLanguage()) {
				if(participant.getLanguage() != null) {
					Label lbl = new Label(colIdx, rowIdx, participant.getLanguage(), defaultFormat);
		        	sheet.addCell(lbl);
				}
				++colIdx;
			}
			
			if(getSettings().isIncludeGroup()) {
				if(participant.getGroup() != null) {
					Label lbl = new Label(colIdx, rowIdx, participant.getGroup(), defaultFormat);
		        	sheet.addCell(lbl);
				}
				++colIdx;
			}
			
			if(getSettings().isIncludeEducation()) {
				if(participant.getEducation() != null) {
					Label lbl = new Label(colIdx, rowIdx, participant.getEducation(), defaultFormat);
		        	sheet.addCell(lbl);
				}
				++colIdx;
			}
			if(getSettings().isIncludeSES()) {
				if(participant.getSES() != null) {
					Label lbl = new Label(colIdx, rowIdx, participant.getSES(), defaultFormat);
		        	sheet.addCell(lbl);
				}
				++colIdx;
			}
		}
		
        return new Tuple<>(colIdx-1, rowIdx);
	}
	
	private Tuple<Integer, Integer> appendQueryResult(WritableWorkbook wb, WritableSheet sheet, int rowIdx,
			Record record, Result result, int resultIdx) throws WriteException {
		var resultStr = ReportHelper.createPrimaryResultString(result);
		
		jxl.write.NumberFormat indexNumberFormat = new NumberFormat("#");
		WritableCellFormat indexFormat = new WritableCellFormat(indexNumberFormat);
		
		WritableCellFormat columnHeaderFormat = new WritableCellFormat();
        WritableFont font = new WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE, WritableFont.BOLD);
        columnHeaderFormat.setFont(font);
		
		WritableCellFormat tierValueFormat = getTierDataFormat();
		
		int colIdx = 0;
		int maxCol = 0;
		if(resultIdx == 0) {
			Label indexHeaderLbl = new Label(colIdx, rowIdx-1, "Index", columnHeaderFormat);
			sheet.addCell(indexHeaderLbl);
		}
		
		jxl.write.Number indexLbl = new jxl.write.Number(colIdx++, rowIdx, resultIdx+1, indexFormat);
		sheet.addCell(indexLbl);
		
		if(resultIdx == 0) {
			Label resultHeaderLbl = new Label(colIdx, rowIdx-1, "Result", columnHeaderFormat);
			sheet.addCell(resultHeaderLbl);
		}
		
		Label resultLbl = new Label(colIdx++, rowIdx, resultStr, tierValueFormat);
		sheet.addCell(resultLbl);
		
		// add 'other' tiers
		var rvList = ReportHelper.getExtraResultValues(result);
		for(var rv:rvList) {
			if(getSettings().getResultValues().size() > 0) {
				boolean inList = getSettings().getResultValues().contains(rv.getName());
				if(inList && getSettings().isExcludeResultValues()) continue;
				else if(!inList && !getSettings().isExcludeResultValues()) continue;
			}
			
			if(resultIdx == 0) {
				Label colLbl = new Label(colIdx, rowIdx-1, rv.getName(), columnHeaderFormat);
				sheet.addCell(colLbl);
			}
			
			Label rvLbl = new Label(colIdx++, rowIdx, rv.getData(), tierValueFormat);
			sheet.addCell(rvLbl);
		}
		
		for(String metadataKey:result.getMetadata().keySet()) {
			if(getSettings().getResultValues().size() > 0) {
				boolean inList = getSettings().getResultValues().contains(metadataKey);
				if(inList && getSettings().isExcludeResultValues()) continue;
				else if(!inList && !getSettings().isExcludeResultValues()) continue;
			}
			
			final String metadataValue = result.getMetadata().get(metadataKey);
			
			// print column headers
			if(resultIdx == 0) {
				Label colLbl = new Label(colIdx, rowIdx-1, metadataKey, columnHeaderFormat);
				sheet.addCell(colLbl);
			}
			
			Label mlbl = new Label(colIdx++, rowIdx, metadataValue, tierValueFormat);
			sheet.addCell(mlbl);
			
			maxCol = Math.max(maxCol, colIdx-1);
		}
				
		return new Tuple<>(maxCol, rowIdx);
	}
	
	private Tuple<Integer, Integer> appendRecord(WritableWorkbook wb, WritableSheet sheet, int rowIdx, 
			Session session, int recordIndex, ResultSet resultSet) throws WriteException {
		WritableCellFormat columnHeaderFormat = getColumnHeaderFormat();
        WritableCellFormat tierNameFormat = getTierNameFormat();
        
		final Record record = session.getRecord(recordIndex);
		List<Result> resultsForRecord = new ArrayList<>();
		if(getSettings().isIncludeQueryResults() && resultSet != null) {
			resultsForRecord = StreamSupport.stream(resultSet.spliterator(), false)
				.filter( (result) -> result.getRecordIndex() == recordIndex )
				.collect( Collectors.toList() );
		}
		
		int maxCol = 0;
		int colIdx = 0;
		int recordNum = recordIndex + 1;
		var groupCount = record.numberOfGroups();
		
		String titleString = String.format("%d. %s", recordNum, record.getSpeaker());
		if(getSettings().isIncludeQueryResults() && resultsForRecord.size() > 0) {
			titleString += String.format(" (%d result%s)", 
					resultsForRecord.size(), resultsForRecord.size() > 1 ? "s" : "");
		}
		Label titleLabel = new Label(colIdx, rowIdx++, titleString, columnHeaderFormat);
		sheet.addCell(titleLabel);
		
		if(getSettings().isShowQueryResultsFirst() && getSettings().isIncludeQueryResults() && resultSet != null) {
			++rowIdx;
			for(int rIdx = 0; rIdx < resultsForRecord.size(); rIdx++) {
				var lastPos = appendQueryResult(wb, sheet, rowIdx, record, resultsForRecord.get(rIdx), rIdx);
				rowIdx = lastPos.getObj2()+1;
				maxCol = Math.max(maxCol, lastPos.getObj1());
			}
			
			if(resultsForRecord.size() > 0 && getSettings().isIncludeTierData()) {
				++rowIdx;
			}
		}
		
		if(getSettings().isIncludeTierData()) {
			var tierView = (getSettings().getTierView() != null ? getSettings().getTierView() : session.getTierView());
			
			for(var tierIdx = 0; tierIdx < tierView.size(); tierIdx++) {
				colIdx = 0;
				var tvi = tierView.get(tierIdx);
				
				CellFormat tierFormat = getTierDataFormat();
//				if(tierFormat == null) {
//					WritableCellFormat fmt = new WritableCellFormat();
//					Font javaFont =
//							(tvi.getTierFont().equals("default") ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));
//					WritableFont tierFont = new WritableFont(WritableFont.createFont(javaFont.getFontName()), 
//							javaFont.getSize());
//					fmt.setFont(tierFont);
//					
//					tierFormats.put(tvi, fmt);
//					tierFormat = fmt;
//				}
				
				if(getSettings().getTierList().size() > 0) {
					boolean inList = getSettings().getTierList().contains(tvi.getTierName());
					if(!inList && !getSettings().isExcludeResultValues()) continue;
					else if(inList && getSettings().isExcludeResultValues()) continue;
				}
				if(!tvi.isVisible()) continue;
				
				var tier = record.getTier(tvi.getTierName());
				
				Label lbl = new Label(colIdx++, rowIdx, tvi.getTierName(), tierNameFormat);
				sheet.addCell(lbl);
				if(tier != null) {
					if(tier.isGrouped()) {
						for(var i = 0; i < record.numberOfGroups(); i++) {
							var groupVal = tier.getGroup(i);
							Label grpLbl = new Label(colIdx++, rowIdx, groupVal.toString(), tierFormat);
							sheet.addCell(grpLbl);
						}
					} else {
						Label grpLbl = new Label(colIdx, rowIdx, tier.getGroup(0).toString(), tierFormat);
						sheet.addCell(grpLbl);
						sheet.mergeCells(colIdx, rowIdx, groupCount, rowIdx);
					}
				}
				++rowIdx;
				
				if(getSettings().isIncludeSyllabification() && (tvi.getTierName().equals("IPA Target") || tvi.getTierName().equals("IPA Actual"))) {
					colIdx = 1;
					for(var i = 0; i < record.numberOfGroups(); i++) {
						IPATranscript ipa = (IPATranscript)tier.getGroup(i);
						BufferedImage img = createSyllabificationImage(ipa);
						byte[] imgData = imgToByteArray(img);
						if(imgData.length > 0) {
							WritableImage xlsImg = new WritableImage(colIdx++, rowIdx, 1, 1, imgData);
							sheet.addImage(xlsImg);
						}
					}
					rowIdx++;
				}
			}
			if(getSettings().isIncludeAlignment()) {
				// alignment
				colIdx = 0;
				Label alignLbl = new Label(colIdx++, rowIdx, "Alignment", tierNameFormat);
				sheet.addCell(alignLbl);
				
				var alignmentTier = record.getTier("Alignment");
				for(var i = 0; i < record.numberOfGroups(); i++) {
					final PhoneMap alignment = (PhoneMap)alignmentTier.getGroup(i);
					BufferedImage img = createAlignmentImage(alignment);
					byte[] imgData = imgToByteArray(img);
					if(imgData.length > 0) {
						WritableImage xlsImg = new WritableImage(colIdx++, rowIdx, 1, 1, imgData);
						sheet.addImage(xlsImg);
					}
				}
				rowIdx++;
			}
		}
		if(!getSettings().isShowQueryResultsFirst() && getSettings().isIncludeQueryResults() && resultSet != null) {
			if(resultsForRecord.size() > 0 && getSettings().isIncludeTierData()) {
				rowIdx += 2;
			}
			
			for(int rIdx = 0; rIdx < resultsForRecord.size(); rIdx++) {
				var lastPos = appendQueryResult(wb, sheet, rowIdx, record, resultsForRecord.get(rIdx), rIdx);
				rowIdx = lastPos.getObj2() + 1;
				maxCol = Math.max(maxCol, lastPos.getObj1());
			}
		}
		--rowIdx;
		
		maxCol = Math.max(maxCol, colIdx-1);
		
		return new Tuple<>(maxCol, rowIdx);
	}
	
	private byte[] imgToByteArray(BufferedImage img) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", bos);
		} catch (IOException e) {
			LogUtil.warning(e.getLocalizedMessage(), e);
		}
		return bos.toByteArray();
	}
	
	/**
	 * Create a new sheet in a writable excel workbook
	 * 
	 * @param wb
	 * @param session
	 */
	public WritableSheet createSheetInWorkbook(WritableWorkbook wb, Session session) throws WriteException {
		final String sheetName = 
				WorkbookUtils.sanitizeTabName(session.getCorpus() + "." + session.getName());
		WritableSheet sheet = wb.createSheet(sheetName, wb.getNumberOfSheets());
		
		int maxCol = 0;
		int rowIdx = 0;
		if(getSettings().isIncludeParticipantInfo()) {
			var lastPos = appendParticipantTable(wb, sheet, rowIdx, session);
			rowIdx = lastPos.getObj2() + 1;
			maxCol = Math.max(maxCol, lastPos.getObj1());
		}
		
		RecordFilter queryFilter = null;
		if(getSettings().isFilterRecordsUsingQueryResults() && getSettings().getResultSet() != null) {
			queryFilter = new ResultSetRecordFilter(session, getSettings().getResultSet());
		}
		
		rowIdx += 2;
		for(int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
			var utt = session.getRecord(rIdx);
			// check filters
			if(getSettings().getRecordFilter() != null && !getSettings().getRecordFilter().checkRecord(utt)) continue;
			if(queryFilter != null && !queryFilter.checkRecord(utt)) continue;
			var lastPos = appendRecord(wb, sheet, rowIdx, session, rIdx, getSettings().getResultSet());
			rowIdx = lastPos.getObj2() + 2;
			maxCol = Math.max(maxCol, lastPos.getObj1());
		}
	
		// try to autosize everything
		for(int i = 0; i < rowIdx; i++) {
			sheet.getRowView(i).setAutosize(true);
		}
		for(int i = 0; i < maxCol; i++) {
			sheet.getColumnView(i).setAutosize(true);
		}
		
		return sheet;
	}

}
