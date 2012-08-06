package ca.phon.gui.ipamap;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.ipamap.io.IpaGrids;
import ca.phon.ui.ipamap.io.ObjectFactory;
import ca.phon.ui.painter.CmpPainter;
import ca.phon.util.OSInfo;

public class IpaTest extends JPanel {
	
//	public IpaTest() {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		
		// setup L&F on Windows
    	if(OSInfo.isWindows()) {
    		// use windows L&F on windows
    		try {
    			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    			//(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
    		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
    			ex.printStackTrace();
    		} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
    		
    	} else if(OSInfo.isNix()) {
    		// use GTK L&F on linux (assuming desktop Ubuntu running Gnome)
    		try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
						//new com.sun.java.swing.plaf.gtk.GTKLookAndFeel());
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
    	}

    	JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
		Unmarshaller unmarshaller = ctx.createUnmarshaller();
		final InputStream gridStream = IpaTest.class.getResourceAsStream("ipagrids.xml");
		
//		BufferedReader in = new BufferedReader(new InputStreamReader(gridStream, "UTF-8"));
//		String line = null;
//		while((line = in.readLine()) != null) {
//			System.out.println(line);
//		}
		if(gridStream != null) {
			final IpaGrids grids = (IpaGrids)unmarshaller.unmarshal(gridStream);
	    	
	    	Runnable r =  new Runnable() { public void run() {
	    	
			Grid g = grids.getGrid().get(1);
	
	//		IpaMap map = new IpaMap()vowel;
	//		IPAGridPanel gp = new IPAGridPanel(map, g);
	//		gp.setFont(map.getFont());
	//		
	//		StripePainter sp = new StripePainter();
	//		VowelGridPainter vp = new VowelGridPainter();
	//		
	//		CmpPainter<IPAGridPanel> painter = new CmpPainter<IPAGridPanel>(sp, vp);
	//		gp.setPainter(painter);
			
			IpaMapFrame testFrame = new IpaMapFrame();
			testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//		testFrame.setFocusable(false);
	//		testFrame.setFocusableWindowState(false);
	//		testFrame.add(new IpaMap());
			
	//		testFrame.add(map);
			
	//		int w = map.getPreferredSize().width + ((new JScrollPane()).getVerticalScrollBar().getPreferredSize().width);
	//		int h = 600;
			
	//		testFrame.setBounds(0, 0, w, h);
			
			testFrame.pack();
			testFrame.setVisible(true);
	    	}};
	    	SwingUtilities.invokeLater(r);
		}
	}

}
