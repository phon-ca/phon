package ca.phon.session.spi;

import ca.phon.session.Transcript;

public interface TranscriptSPI {

    public int getNumberOfElements();

    public Transcript.Element getElementAt(int idx);

    public void addElement(Transcript.Element element);

    public void addElement(int idx, Transcript.Element element);

    public void removeElement(Transcript.Element element);

    public Transcript.Element removeElement(int idx);

}
