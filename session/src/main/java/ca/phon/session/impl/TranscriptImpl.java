package ca.phon.session.impl;

import ca.phon.session.Transcript;
import ca.phon.session.spi.TranscriptSPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TranscriptImpl implements TranscriptSPI {

    /**
     * List of comments and records
     */
    private final List<Transcript.Element> elements;

    public TranscriptImpl() {
        super();
        this.elements = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public int getNumberOfElements() {
        return elements.size();
    }

    @Override
    public Transcript.Element getElementAt(int idx) {
        return elements.get(idx);
    }

    @Override
    public void addElement(Transcript.Element element) {
        elements.add(element);
    }

    @Override
    public void addElement(int idx, Transcript.Element element) {
        elements.add(idx, element);
    }

    @Override
    public void removeElement(Transcript.Element element) {
        elements.remove(element);
    }

    @Override
    public Transcript.Element removeElement(int idx) {
        return elements.remove(idx);
    }

}
