package ca.phon.ipa.xml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ca.phon.ipa.IPATranscript;
import ca.phon.xml.XMLLexer;
import ca.phon.xml.XMLSerializer;

public class IPATranscriptXMLSerializer implements XMLSerializer {

	@Override
	public <T> T read(Class<T> type, InputStream input) throws IOException {
		if(type != declaredType())
			throw new IOException("Invalid type specified.");
		
		final IPATranscript transcript = new IPATranscript();
		
		final Properties tokenProps = new Properties();
		final InputStream propsStream = ClassLoader.getSystemClassLoader().getResourceAsStream("antlr3/tokens/Pho.tokens");
		tokenProps.load(propsStream);
		
		final XMLLexer lexer = XMLLexer.fromStream(input, "UTF-8", tokenProps);
		final TokenStream tokenStream = new CommonTokenStream(lexer);
		final PhoParser phoParser = new PhoParser(tokenStream);
		
		try {
			final PhoParser.pho_return phoRet = phoParser.pho();
			final CommonTree tree = phoRet.tree;
			final CommonTreeNodeStream nodeStream = new CommonTreeNodeStream(tree);
			final Xml2IPATranscript treeParser = new Xml2IPATranscript(nodeStream);
			treeParser.setTranscript(transcript);
			treeParser.pho();
		} catch (RecognitionException e) {
			throw new IOException(e);
		}
		
		return type.cast(transcript);
	}

	@Override
	public <T> void write(Class<T> type, T obj, OutputStream output) throws IOException {
		if(type != declaredType())
			throw new IOException("Invalid type specified.");
		
		final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(output));
		
		final IPATranscript transcript = IPATranscript.class.cast(obj);
		final PhoCommonTreeBuilder treeBuilder = new PhoCommonTreeBuilder();
		transcript.accept(treeBuilder);
		
		final CommonTree tree = treeBuilder.getTree();
		final CommonTreeNodeStream nodeStream = new CommonTreeNodeStream(tree);
		final IPATranscript2Xml xmlTemplate = new IPATranscript2Xml(nodeStream);
//		xmlTemplate.setNamespacePrefix("pho");
//		xmlTemplate.setIncludeNamespace(true);
		IPATranscript2Xml.pho_return phoRet;
		try {
			phoRet = xmlTemplate.pho();

			final String xmlData = phoRet.st.toString();
			out.write(xmlData);
			out.flush();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Class<?> declaredType() {
		return IPATranscript.class;
	}

//	/**
//	 * Visitor for creating JAXB objects of phonetic transcriptions.
//	 */
//	public class IPATranscriptXMLVisitor extends VisitorAdapter<IPAElement> {
//		
//		private final PhoType pho;
//		
//		private PhoneticWordType pw;
//		
//		final ca.phon.ipa.xml.jaxb.ObjectFactory factory = new ca.phon.ipa.xml.jaxb.ObjectFactory();
//		
//		public IPATranscriptXMLVisitor() {
//			pho = factory.createPhoType();
//			// add an initial pw element
//			pw = factory.createPhoneticWordType();
//			pho.getPw().add(pw);
//		}
//		
//		public JAXBElement<PhoType> getPho() {
//			final JAXBElement<PhoType> phoEle = factory.createPho(pho);
//			return phoEle;
//		}
//		
//		@Override
//		public void fallbackVisit(IPAElement obj) {
//		}
//		
//		@Visits
//		public void visitCompoundPhone(CompoundPhone cp) {
//			final CompoundPhoneType cpType = factory.createCompoundPhoneType();
//			
//			if(cp.getLigature() == '\u0361') {
//				cpType.setLig(LigatureType.TOP);
//			}
//			final PhoneType p1 = createPhoneType(cp.getFirstPhone());
//			cpType.getPh().add(p1);
//			final PhoneType p2 = createPhoneType(cp.getSecondPhone());
//			cpType.getPh().add(p2);
//			
//			pw.getPhOrCpOrPause().add(cpType);
//		}
//		
//		private PhoneType createPhoneType(Phone p) {
//			final PhoneType retVal = factory.createPhoneType();
//			
//			retVal.setContent(p.getBase());
//			
//			if(p.getCombining().length() > 0) {
//				for(Character c:p.getCombining().toCharArray()) {
//					final String escSeq = "&#x" + Integer.toHexString(c) + ";";
//					retVal.getCombining().add(escSeq);
//				}
//			}
//			
//			if(p.getPrefix().length() > 0) {
//				final String escSeq = "&#x" + Integer.toHexString(p.getPrefix().charAt(0)) + ";";
//				retVal.setPrefix(escSeq);
//			}
//			
//			if(p.getSuffix().length() > 0) {
//				final String escSeq = "&#x" + Integer.toHexString(p.getSuffix().charAt(0)) + ";";
//				retVal.setSuffix(escSeq);
//			}
//			
//			if(p.getLength() > 0) {
//				retVal.setLength(p.getLength());
//			}
//			
//			return retVal;
//		}
//		
//		@Visits
//		public void visitPause(Pause p) {
//			final PauseType pauseType = factory.createPauseType();
//			
//			if(p.getLength() == PauseLength.MEDIUM)
//				pauseType.setLength(PauseTypeLength.MEDIUM);
//			else if(p.getLength() == PauseLength.LONG)
//				pauseType.setLength(PauseTypeLength.LONG);
//			
//			pw.getPhOrCpOrPause().add(pauseType);
//		}
//		
//		@Visits
//		public void visitPhone(Phone ph) {
//			final PhoneType phType = createPhoneType(ph);
//			pw.getPhOrCpOrPause().add(phType);
//		}
//		
//		@Visits
//		public void visitStress(StressMarker ss) {
//			final SyllableStressType stressType = factory.createSyllableStressType();
//			if(ss.getType() == StressType.SECONDARY)
//				stressType.setType("2");
//			pw.getPhOrCpOrPause().add(stressType);
//		}
//		
//		@Visits
//		public void visitSyllableBoundary(SyllableBoundary sb) {
//			pw.getPhOrCpOrPause().add(factory.createSyllableBoundaryType());
//		}
//		
//		@Visits
//		public void visitWordBoundary(WordBoundary wb) {
//			pw = factory.createPhoneticWordType();
//			pho.getPw().add(pw);
//		}
//	}
	
}
