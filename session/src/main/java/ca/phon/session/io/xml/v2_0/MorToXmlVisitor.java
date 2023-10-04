package ca.phon.session.io.xml.v2_0;

import ca.phon.orthography.mor.*;
import ca.phon.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;

public class MorToXmlVisitor implements Visitor<Mor> {

    private final ObjectFactory factory;

    private final List<XmlMorType> morTypeList = new ArrayList<>();

    public MorToXmlVisitor() {
        this(new ObjectFactory());
    }

    public MorToXmlVisitor(ObjectFactory factory) {
        this.factory = factory;
    }

    public List<XmlMorType> getXmlMorTypes() {
        return this.morTypeList;
    }

    @Override
    public void visit(Mor mor) {
        final XmlMorType morType = factory.createXmlMorType();
        for(MorPre morPre:mor.getMorPres()) {
            XmlMorphemicBaseType xmlMorPre = writeMorBase(morPre);
            morType.getMorPre().add(xmlMorPre);
        }
        if(mor.getElement() instanceof MorWord morWord) {
            morType.setMw(writeMorWord(morWord));
        } else if(mor.getElement() instanceof MorWordCompound morWordCompound) {
            morType.setMwc(writeMorWordCompound(morWordCompound));
        } else if(mor.getElement() instanceof MorTerminator morTerminator) {
            morType.setMt(writeMorTerminator(morTerminator));
        } else {
            throw new IllegalArgumentException("Invalid mor element type " + mor.getElement().getClass().getName());
        }
        for(MorPost morPost:mor.getMorPosts()) {
            XmlMorphemicBaseType xmlMorPost = writeMorBase(morPost);
            morType.getMorPost().add(xmlMorPost);
        }
        for(String translation:mor.getTranslations()) {
            final Menx menx = factory.createMenx();
            menx.setValue(translation);
            morType.getMenx().add(menx);
        }
        morTypeList.add(morType);
    }

    private XmlMorphemicBaseType writeMorBase(MorphemicBaseType morBase) {
        final XmlMorphemicBaseType morBaseType = factory.createXmlMorphemicBaseType();
        if(morBase.getElement() instanceof MorWord morWord) {
            morBaseType.setMw(writeMorWord(morWord));
        } else if(morBase.getElement() instanceof MorWordCompound morWordCompound) {
            morBaseType.setMwc(writeMorWordCompound(morWordCompound));
        } else if(morBase.getElement() instanceof MorTerminator morTerminator) {
            morBaseType.setMt(writeMorTerminator(morTerminator));
        } else {
            throw new IllegalArgumentException("Invalid mor element type " + morBase.getElement().getClass().getName());
        }
        for(String translation:morBase.getTranslations()) {
            final Menx menx = factory.createMenx();
            menx.setValue(translation);
            morBaseType.getMenx().add(menx);
        }
        return morBaseType;
    }

    private Mw writeMorWord(MorWord morWord) {
        final Mw mw = factory.createMw();
        for(String prefix:morWord.getPrefixList()) {
            final Mpfx mpfx = factory.createMpfx();
            mpfx.setValue(prefix);
            mw.getMpfx().add(mpfx);
        }
        mw.setPos(writePos(morWord.getPos()));
        mw.setStem(morWord.getStem());
        for(MorMarker marker:morWord.getMarkers()) {
            final XmlMkType mkType = factory.createXmlMkType();
            final XmlMkTypeType mkTypeType = switch (marker.getType()) {
                case Suffix -> XmlMkTypeType.SFX;
                case MorCategory -> XmlMkTypeType.MC;
                case SuffixFusion -> XmlMkTypeType.SFXF;
            };
            mkType.setType(mkTypeType);
            mkType.setValue(marker.getText());
            mw.getMk().add(mkType);
        }
        return mw;
    }

    private Mwc writeMorWordCompound(MorWordCompound morWordCompound) {
        final Mwc mwc = factory.createMwc();
        for(String prefix:morWordCompound.getPrefixList()) {
            final Mpfx mpfx = factory.createMpfx();
            mpfx.setValue(prefix);
            mwc.getMpfx().add(mpfx);
        }
        mwc.setPos(writePos(morWordCompound.getPos()));
        for(MorWord morWord:morWordCompound.getWords()) {
            mwc.getMw().add(writeMorWord(morWord));
        }
        return mwc;
    }

    private XmlBaseTerminatorType writeMorTerminator(MorTerminator morTerminator) {
        final XmlBaseTerminatorType xmlBaseTerminatorType = factory.createXmlBaseTerminatorType();
        final XmlTerminatorType type = switch (morTerminator.getType()) {
            case BROKEN_FOR_CODING -> XmlTerminatorType.BROKEN_FOR_CODING;
            case EXCLAMATION -> XmlTerminatorType.E;
            case INTERRUPTION -> XmlTerminatorType.INTERRUPTION;
            case INTERRUPTION_QUESTION -> XmlTerminatorType.INTERRUPTION_QUESTION;
            case NO_BREAK_TCU_CONTINUATION -> XmlTerminatorType.NO_BREAK_TCU_CONTINUATION;
            case PERIOD -> XmlTerminatorType.P;
            case QUESTION -> XmlTerminatorType.Q;
            case QUESTION_EXCLAMATION -> XmlTerminatorType.QUESTION_EXCLAMATION;
            case QUOTATION_NEXT_LINE -> XmlTerminatorType.QUOTATION_NEXT_LINE;
            case QUOTATION_PRECEDES -> XmlTerminatorType.QUOTATION_PRECEDES;
            case SELF_INTERRUPTION -> XmlTerminatorType.SELF_INTERRUPTION;
            case SELF_INTERRUPTION_QUESTION -> XmlTerminatorType.SELF_INTERRUPTION_QUESTION;
            case TECHNICAL_BREAK_TCU_CONTINUATION -> XmlTerminatorType.TECHNICAL_BREAK_TCU_CONTINUATION;
            case TRAIL_OFF -> XmlTerminatorType.TRAIL_OFF;
            case TRAIL_OFF_QUESTION -> XmlTerminatorType.TRAIL_OFF_QUESTION;
        };
        xmlBaseTerminatorType.setType(type);
        return xmlBaseTerminatorType;
    }

    private XmlPosType writePos(Pos pos) {
        final XmlPosType xmlPosType = factory.createXmlPosType();
        final XmlCategoryType xmlCategoryType = factory.createXmlCategoryType();
        xmlCategoryType.setValue(pos.getCategory());
        xmlPosType.setC(xmlCategoryType);
        for(String subc:pos.getSubCategories()) {
            final XmlSubcategoryType xmlSubcategoryType = factory.createXmlSubcategoryType();
            xmlSubcategoryType.setValue(subc);
            xmlPosType.getSubc().add(xmlSubcategoryType);
        }
        return xmlPosType;
    }

}
