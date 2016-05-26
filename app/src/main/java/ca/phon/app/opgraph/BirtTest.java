package ca.phon.app.opgraph;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DataSourceHandle;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.TableHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;

import com.ibm.icu.util.ULocale;

import au.com.bytecode.opencsv.CSVReader;

public class BirtTest {
	
	private final static String MASTER_REPORT = "birt/master.rptdesign";
	
	public BirtTest() {
		super();
	}

		
	public IDesignEngine startEngine() {
		DesignConfig designConfig = new DesignConfig();
		IDesignEngine retVal = null;
		try {
			Platform.startup(designConfig);
			IDesignEngineFactory factory = (IDesignEngineFactory) Platform.createFactoryObject( IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY );
	        retVal = factory.createDesignEngine( designConfig );
		} catch (BirtException e) {
			
		}
		return retVal;
	}
	
	public ReportDesignHandle openMasterReport(IDesignEngine engine) throws DesignFileException {
       SessionHandle session = engine.newSessionHandle( ULocale.ENGLISH ) ;
       return session.openDesign(MASTER_REPORT, getClass().getClassLoader().getResourceAsStream(MASTER_REPORT));
	}
	
	public void generateDynamicReport(ReportDesignHandle reportDesign, File reportFolder)
		throws SemanticException, IOException {
		String srcName = reportFolder.getName();
		
		final ElementFactory elementFactory = reportDesign.getElementFactory();
		
		DataSourceHandle srcHandle = createDataSourceForFolder(srcName, reportFolder, elementFactory);
		reportDesign.getDataSources().add(srcHandle);
		reportDesign.setTitle("Khan-Lewis Phonlogical Analysis");
	
		for(File csvFile:reportFolder.listFiles( (f,s) -> s.endsWith(".csv") )) {
			String dsName = csvFile.getName().substring(0, csvFile.getName().length()-4);
			DataSetHandle dsHandle = createDataSetForFile(srcName, dsName, csvFile, elementFactory);
			reportDesign.getDataSets().add(dsHandle);
			
			createTableForFile(csvFile, reportDesign, elementFactory);
		}
	}
	
	public void createTableForFile(File csvFile,
			ReportDesignHandle designHandle, ElementFactory elementFactory) throws SemanticException {
		try(CSVReader csvReader = 
				new CSVReader(new InputStreamReader(new FileInputStream(csvFile), "UTF-8"))) {
			String[] headerRow = csvReader.readNext();
			
			String dsName = csvFile.getName().substring(0, csvFile.getName().length()-4);
			
			LabelHandle titleLabel = elementFactory.newLabel("lbl_"+dsName);
			titleLabel.setText(dsName);
			titleLabel.setProperty("fontSize", "12pt");
			titleLabel.setProperty("fontWeight", "bold");
			designHandle.getBody().add(titleLabel);
			
			TableHandle table = elementFactory.newTableItem( "table", headerRow.length );
	        table.setWidth( "100%" );
	        table.setDataSet( designHandle.findDataSet( dsName ) );
	        table.setProperty("borderBottomStyle", "solid");
	        table.setProperty("borderBottomWidth", "medium");
	        table.setProperty("borderLeftStyle", "solid");
	        table.setProperty("borderLeftWidth", "medium");
	        table.setProperty("borderTopStyle", "solid");
	        table.setProperty("borderTopWidth", "medium");
	        table.setProperty("borderRightStyle", "solid");
	        table.setProperty("borderRightWidth", "medium");
	        table.setProperty("marginBottom", "10px");
	        
	        // setup column expressions
	        PropertyHandle computedSet = table.getColumnBindings();
	        for(int i=0; i < headerRow.length; i++) {
	        	ComputedColumn col = StructureFactory.createComputedColumn();
	        	col.setName(headerRow[i]);
	        	col.setExpression("dataSetRow[\"" + headerRow[i] + "\"]");
	        	computedSet.addItem(col);
	        }
	        
	        // header row
	        RowHandle tableheader = (RowHandle) table.getHeader( ).get( 0 );
	        tableheader.setProperty("borderBottomStyle", "solid");
	        tableheader.setProperty("borderBottomWidth", "thin");
	        tableheader.setProperty("backgroundColor", "#F0F8FF");
			for (int i = 0; i < headerRow.length; i++) {
				LabelHandle label = elementFactory.newLabel(headerRow[i]);
				label.setText(headerRow[i]);
				CellHandle cell = (CellHandle) tableheader.getCells().get(i);
				cell.getContent().add(label);
			}
			
			 // table detail
			RowHandle tabledetail = (RowHandle) table.getDetail().get(0);
			for (int i = 0; i < headerRow.length; i++) {
				CellHandle cell = (CellHandle) tabledetail.getCells().get(i);
				DataItemHandle data = elementFactory.newDataItem("data_" + headerRow[i]);
				data.setResultSetColumn(headerRow[i]);
				cell.getContent().add(data);
				if(i == 0) {
					cell.setProperty("borderRightStyle", "solid");
					cell.setProperty("borderRightWidth", "thin");
				}
			}
			
			designHandle.getBody().add(table);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DataSourceHandle createDataSourceForFolder(String name, File reportFolder, 
			ElementFactory elementFactory) throws SemanticException {
		OdaDataSourceHandle dataSource = elementFactory.newOdaDataSource(name, 
				"org.eclipse.datatools.connectivity.oda.flatfile");
		dataSource.setProperty("HOME", reportFolder.toURI().toASCIIString());
		dataSource.setProperty("DELIMTYPE", "COMMA");
		dataSource.setProperty("CHARSET", "UTF-8");
		dataSource.setProperty("INCLCOLUMNNAME", "YES");
		dataSource.setProperty("INCLTYPELINE", "NO");
		dataSource.setProperty("TRAILNULLCOLS", "NO");
		
		return dataSource;
	}
	
	public DataSetHandle createDataSetForFile(String dsName, String name, File csvFile, 
			ElementFactory elementFactory) throws IOException, SemanticException {
		OdaDataSetHandle dataSet = elementFactory.newOdaDataSet(name, 
				"org.eclipse.datatools.connectivity.oda.flatfile.dataSet");
		dataSet.setDataSource(dsName);
		dataSet.setQueryText("select * from \"" + csvFile.getName() + "\"");
		
		return dataSet;
	}
	
	public void saveReport(ReportDesignHandle reportDesign, File file) throws IOException {
		reportDesign.saveAs(file.getAbsolutePath());
	}
	
	public static void main(String[] args) throws Exception {
		final File rptFolder = new File("/Users/ghedlund/Desktop/BirtTest2");
		
		BirtTest bt = new BirtTest();
		IDesignEngine engine = bt.startEngine();
		ReportDesignHandle designHandle = bt.openMasterReport(engine);
		bt.generateDynamicReport(designHandle, rptFolder);
		
		bt.saveReport(designHandle, new File(rptFolder, "Test.rptdesign"));
		designHandle.close();
	}

}
