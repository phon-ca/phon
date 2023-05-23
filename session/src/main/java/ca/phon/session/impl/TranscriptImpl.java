package ca.phon.session.impl;

import ca.phon.session.Transcript;
import ca.phon.session.spi.TranscriptSPI;

public class TranscriptImpl implements TranscriptSPI {

    @Override
    public int getNumberOfElements() {
        return 0;
    }

    @Override
    public Transcript.Element getElementAt(int idx) {
        return null;
    }

    @Override
    public void addElement(Transcript.Element element) {

    }

    @Override
    public void addElement(int idx, Transcript.Element element) {

    }

    @Override
    public void insertElement(int idx, Transcript.Element element) {

    }

    @Override
    public void removeElement(Transcript.Element element) {

    }

    @Override
    public Transcript.Element removeElement(int idx) {
        return null;
    }
}
