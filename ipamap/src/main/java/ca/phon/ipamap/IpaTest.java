package ca.phon.ipamap;

import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import ca.phon.ui.ipamap.io.Grid;
import ca.phon.ui.ipamap.io.IpaGrids;
import ca.phon.ui.ipamap.io.ObjectFactory;

public class IpaTest extends JPanel {
	
//	public IpaTest() {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		

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
			testFrame.getMapContents().addListener(new IpaMapRobot());
			testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//		testFrame.setFocusable(false);
	//		testFrame.setFocusableWindowState(false);
	//		testFrame.add(new IpaMap());
			
	//		testFrame.add(map);
			
	//		int w = map.getPreferredSize().width + ((new JScrollPane()).getVerticalScrollBar().getPreferredSize().width);
	//		int h = 600;
			
	//		testFrame.setBounds(0, 0, w, h);
			
			testFrame.showWindow();
	    	}};
	    	SwingUtilities.invokeLater(r);
		}
	}

}
