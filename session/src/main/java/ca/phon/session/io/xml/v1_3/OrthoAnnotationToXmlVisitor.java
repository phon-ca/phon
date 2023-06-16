package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.orthography.Langs;
import ca.phon.util.Language;
import ca.phon.visitor.annotation.Visits;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class OrthoAnnotationToXmlVisitor extends AbstractOrthographyAnnotationVisitor {

    final ObjectFactory factory = new ObjectFactory();

    private List<Object> annotations;

    private ca.phon.session.io.xml.v1_3.Langs langs;

    public OrthoAnnotationToXmlVisitor() {
        this(new ArrayList<>());
    }

    public ca.phon.session.io.xml.v1_3.Langs getLangs() {
        return this.langs;
    }

    public OrthoAnnotationToXmlVisitor(List<Object> annotations) {
        this.annotations = annotations;
    }

    @Override
    @Visits
    public void visitDuration(Duration duration) {
        final long durationMs = (long)(duration.getDuration() * 1000.0f);
        annotations.add(factory.createDuration(BigDecimal.valueOf(durationMs)));
    }

    @Override
    @Visits
    public void visitError(Error error) {
        annotations.add(factory.createError(error.getData()));
    }

    @Override
    @Visits
    public void visitMarker(Marker marker) {
        final K xmlMarker = factory.createK();
        final ca.phon.session.io.xml.v1_3.MarkerType type = switch (marker.getType()) {
            case BEST_GUESS -> ca.phon.session.io.xml.v1_3.MarkerType.BEST_GUESS;
            case CONTRASTIVE_STRESSING -> ca.phon.session.io.xml.v1_3.MarkerType.CONTRASTIVE_STRESSING;
            case EXCLUDE -> ca.phon.session.io.xml.v1_3.MarkerType.MOR_EXCLUDE;
            case FALSE_START -> ca.phon.session.io.xml.v1_3.MarkerType.FALSE_START;
            case RETRACING -> ca.phon.session.io.xml.v1_3.MarkerType.RETRACING;
            case RETRACING_REFORMULATION -> ca.phon.session.io.xml.v1_3.MarkerType.RETRACING_REFORMULATION;
            case RETRACING_UNCLEAR -> ca.phon.session.io.xml.v1_3.MarkerType.RETRACING_UNCLEAR;
            case RETRACING_WITH_CORRECTION -> ca.phon.session.io.xml.v1_3.MarkerType.RETRACING_WITH_CORRECTION;
            case STRESSING -> ca.phon.session.io.xml.v1_3.MarkerType.STRESSING;
        };
        xmlMarker.setType(type);
        annotations.add(xmlMarker);
    }

    @Override
    @Visits
    public void visitGroupAnnotation(GroupAnnotation groupAnnotation) {
        final Ga xmlGa = factory.createGa();
        final GroupAnnotationTypeType type = switch (groupAnnotation.getType()) {
            case ALTERNATIVE -> GroupAnnotationTypeType.ALTERNATIVE;
            case COMMENTS -> GroupAnnotationTypeType.COMMENTS;
            case EXPLANATION -> GroupAnnotationTypeType.EXPLANATION;
            case PARALINGUISTICS -> GroupAnnotationTypeType.PARALINGUISTICS;
        };
        xmlGa.setType(type);
        xmlGa.setContent(groupAnnotation.getData());
        annotations.add(xmlGa);
    }

    @Override
    @Visits
    public void visitOverlap(ca.phon.orthography.Overlap overlap) {
        final ca.phon.session.io.xml.v1_3.Overlap xmlOverlap = factory.createOverlap();
        final ca.phon.session.io.xml.v1_3.OverlapType type = switch (overlap.getType()) {
            case OVERLAP_FOLLOWS -> ca.phon.session.io.xml.v1_3.OverlapType.OVERLAP_FOLLOWS;
            case OVERLAP_PRECEDES -> ca.phon.session.io.xml.v1_3.OverlapType.OVERLAP_PRECEDES;
        };
        xmlOverlap.setType(type);
        if(overlap.getIndex() >= 0)
            xmlOverlap.setIndex(BigInteger.valueOf(overlap.getIndex()));
        annotations.add(xmlOverlap);
    }

    @Visits
    @Override
    public void visitLangs(LangsAnnotation langsAnnotation) {
        final Langs langs = langsAnnotation.getLangs();
        final ca.phon.session.io.xml.v1_3.Langs xmlLangs = factory.createLangs();
        if(langs.getType() == Langs.LangsType.SINGLE) {
            xmlLangs.setSingle(langs.getLangs().get(0).toString());
        } else if(langs.getType() == Langs.LangsType.MULTIPLE) {
            langs.getLangs().stream().map(Language::toString).forEach(xmlLangs.getMultiple()::add);
        } else if(langs.getType() == Langs.LangsType.AMBIGUOUS) {
            langs.getLangs().stream().map(Language::toString).forEach(xmlLangs.getAmbiguous()::add);
        }
        this.langs = xmlLangs;
    }

    @Override
    public void fallbackVisit(OrthographyAnnotation obj) {

    }

}
