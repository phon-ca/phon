package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.session.spi.TranscriptSPI;
import ca.phon.visitor.Visitable;
import ca.phon.visitor.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A collection of interleaved {@link Comment}s, {@link Gem}s and {@link Record}s
 *
 *
 */
public final class Transcript extends ExtendableObject implements Iterable<Transcript.Element>, Visitable<Transcript.Element> {

    /**
     * Wrapper object for transcript elements including Comments, Gems and Records
     */
    public final static class Element {
        final Comment comment;
        final Record record;
        final Gem gem;

        public Element(Comment comment) {
            this.comment = comment;
            this.record = null;
            this.gem = null;
        }

        public Element(Record record) {
            this.record = record;
            this.comment = null;
            this.gem = null;
        }

        public Element(Gem gem) {
            this.record = null;
            this.comment = null;
            this.gem = gem;
        }

        public boolean isComment() { return this.comment != null; }

        public boolean isRecord() { return this.record != null; }

        public Comment asComment() { return this.comment; }

        public Record asRecord() { return this.record; }

        public boolean isGem() { return this.gem != null; }

        public Gem asGem() { return this.gem; }

        @Override
        public int hashCode() {
            return (isComment()
                    ? asComment().hashCode() : isGem()
                        ? asGem().hashCode() : isRecord() ? asRecord().hashCode() : 0);
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Element)) return false;
            return (isComment()
                    ? asComment() == ((Element)obj).asComment() : isGem()
                        ? asGem() == ((Element)obj).asGem() : asRecord() == ((Element)obj).asRecord());
        }
    }

    private final TranscriptSPI spi;

    Transcript(TranscriptSPI spi) {
        super();
        this.spi = spi;
    }

    /**
     * Get number of transcript elements
     *
     * @return number of comments and records
     */
    public int getNumberOfElements() {
        return spi.getNumberOfElements();
    }

    /**
     * Get element at specified index
     *
     * @param idx
     * @return element at given index
     * @throws ArrayIndexOutOfBoundsException
     */
    public Element getElementAt(int idx) {
        return spi.getElementAt(idx);
    }

    /**
     * Add element to end of list
     *
     * @param element
     */
    public void addElement(Element element) {
        spi.addElement(element);
    }

    /**
     * Add element and insert at specified location.
     *
     * @param idx
     * @param element
     * @throws ArrayIndexOutOfBoundsException
     */
    public void addElement(int idx, Element element) {
        spi.addElement(idx, element);
    }

    /**
     * Remove specified element from transcript
     *
     * @param element
     */
    public void removeElement(Element element) {
        spi.removeElement(element);
    }

    /**
     * Remove element at specified index
     *
     * @param idx
     * @return session element that was removed
     * @throws ArrayIndexOutOfBoundsException
     */
    public Element removeElement(int idx) {
        return spi.removeElement(idx);
    }

    /**
     * Return the element index
     *
     * @param ele
     * @return element index in transcript or -1 if not found
     */
    public int getElementIndex(Element ele) {
        for(int i = 0; i < getNumberOfElements(); i++) {
            if(getElementAt(i).equals(ele))
                return i;
        }
        return -1;
    }

    /**
     * Return the element index
     *
     * @param r
     * @return element index in transcript or -1 if not found
     */
    public int getElementIndex(Record r) {
        final Element testEle = new Element(r);
        for(int i = 0; i < getNumberOfElements(); i++) {
            if(getElementAt(i).equals(testEle))
                return i;
        }
        return -1;
    }

    /**
     * Return the element index
     *
     * @param comment
     * @return element index in transcript or -1 if not found
     */
    public int getElementIndex(Comment comment) {
        final Element testEle = new Element(comment);
        for(int i = 0; i < getNumberOfElements(); i++) {
            if(getElementAt(i).equals(testEle))
                return i;
        }
        return -1;
    }

    /**
     * Return the element index
     *
     * @param gem
     * @return element index in transcript or -1 if not found
     */
    public int getElementIndex(Gem gem) {
        final Element testEle = new Element(gem);
        for(int i = 0; i < getNumberOfElements(); i++) {
            if(getElementAt(i).equals(testEle))
                return i;
        }
        return -1;
    }

    /**
     * Add a new comment to end of the transcript
     *
     * @param comment
     */
    public void addComment(Comment comment) {
        addElement(new Element(comment));
    }

    /**
     * Add new comment at the given element index. Note this
     * is different from asRecord(int recordIndex, Record record) which is
     * a helper function to insert a record at the correct location between comments
     * and records.
     *
     * @param elementIndex
     * @param comment
     * @thorws ArrayIndexOutOfBoundsException
     */
    public void addComment(int elementIndex, Comment comment) {
        addElement(elementIndex, new Element(comment));
    }

    /**
     * Remove given comment
     *
     * @param comment
     */
    public Comment removeComment(Comment comment) {
        int eleIdx = getElementIndex(new Element(comment));
        if(eleIdx >= 0) {
            return removeElement(eleIdx).asComment();
        }
        return null;
    }

    /**
     * Add a new gem to the end of the transcript
     *
     * @param gem
     */
    public void addGem(Gem gem) { addElement(new Element(gem)); }

    /**
     * Add gem at given position in transcript
     *
     * @param elementIndex
     * @param gem
     */
    public void addGem(int elementIndex, Gem gem) {
        addElement(elementIndex, new Element(gem));
    }

    public Gem removeGem(Gem gem) {
        int eleIdx = getElementIndex(new Element(gem));
        if(eleIdx >= 0) {
            return removeElement(eleIdx).asGem();
        }
        return null;
    }

    /**
     * Return element index for start of given gem label
     *
     * @param label
     * @return element index of BeginGem (@Bg) or -1 if not found
     */
    public int findBeginGem(String label) {
        for(int i = 0; i < getNumberOfElements(); i++) {
            final Element ele = getElementAt(i);
            if(ele.isGem() && ele.asGem().getType() == GemType.Begin && ele.asGem().getLabel().equals(label)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return element index for end of given gem label
     *
     * @param label
     * @return element index of EndGem (@Eg) or -1 if not found
     */
    public int findEndGem(String label) {
        for(int i = 0; i < getNumberOfElements(); i++) {
            final Element ele = getElementAt(i);
            if(ele.isGem() && ele.asGem().getType() == GemType.End && ele.asGem().getLabel().equals(label)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return element index for lazy gem with given label
     *
     * @param label
     * @return element index of EndGem (@Eg) or -1 if not found
     */
    public int findLazyGem(String label) {
        for(int i = 0; i < getNumberOfElements(); i++) {
            final Element ele = getElementAt(i);
            if(ele.isGem() && ele.asGem().getType() == GemType.Lazy && ele.asGem().getLabel().equals(label)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Add a new record to the session
     *
     * @param record
     */
    public void addRecord(Record record) {
        addElement(new Element(record));
    }

    /**
     * Get index of record
     *
     * @param recordIndex
     * @return element index of record, or -1 if not found
     */
    public int getRecordElementIndex(int recordIndex) {
        int rIdx = -1;
        for(int i = 0; i < getNumberOfElements() && rIdx < recordIndex; i++) {
            if(getElementAt(i).isRecord()) ++rIdx;
            if(rIdx == recordIndex) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the record index of the given element index or the highest record index
     *
     * @param elementIndex
     */
    public int getRecordIndex(int elementIndex) {
        int rIdx = -1;
        for (int i = 0; i < getNumberOfElements() && i <= elementIndex; i++) {
            if (getElementAt(i).isRecord()) ++rIdx;
        }
        return rIdx;
    }

    /**
     * Add a new record to the list in the given position.
     *
     * @param recordIndex
     * @param record
     * @throws ArrayIndexOutOfBoundsException
     */
    public void addRecord(int recordIndex, Record record) {
        // first, find record at recordIndex - 1
        if(recordIndex < 0) throw new ArrayIndexOutOfBoundsException(recordIndex);
        int idx = 0;
        if(recordIndex == 0) {
            // insert after initial comments
            for(int i = idx; i < getNumberOfElements(); i++) {
                if(getElementAt(i).isComment())
                    ++idx;
                else
                    break;
            }
        } else if(recordIndex > 0) {
            int prevRecordIndex = getRecordElementIndex(recordIndex-1);
            // may be -1, insert at 0 if that's the case
            idx = prevRecordIndex + 1;
        }
        addElement(idx, new Element(record));
    }

    /**
     * Remove a record from the session.
     *
     * @param record
     */
    public void removeRecord(Record record) {
        final int eleIdx = getElementIndex(record);
        if(eleIdx >= 0) {
            removeElement(eleIdx);
        }
    }

    /**
     * Remove a record from the session
     *
     * @param recordIndex
     * @throws ArrayIndexOutOfBoundsException
     */
    public Record removeRecord(int recordIndex) {
        final int eleIdx = getRecordElementIndex(recordIndex);
        if(eleIdx >= 0) {
            return removeElement(eleIdx).asRecord();
        } else {
            throw new ArrayIndexOutOfBoundsException(recordIndex);
        }
    }

    /**
     * Return the record at the given index.
     *
     * @param recordIndex
     * @return the specified record
     * @throws ArrayIndexOutOfBoundsException
     */
    public Record getRecord(int recordIndex) {
        final int eleIdx = getRecordElementIndex(recordIndex);
        if(eleIdx >= 0) {
            return getElementAt(eleIdx).asRecord();
        } else {
            throw new ArrayIndexOutOfBoundsException(recordIndex);
        }
    }

    /**
     * Return the number of records.
     *
     * @return the number of records
     */
    public int getRecordCount() {
        int cnt = 0;
        for(int i = 0; i < getNumberOfElements(); i++) {
            if(getElementAt(i).isRecord()) ++cnt;
        }
        return cnt;
    }

    /**
     * Get the record index of the given record.
     *
     * @param record
     * @return record index or -1 if not found
     */
    public int getRecordPosition(Record record) {
        int rIdx = -1;
        for(int i = 0; i < getNumberOfElements(); i++) {
            if(getElementAt(i).isRecord()) {
                ++rIdx;
                if(getElementAt(i).asRecord() == record)
                    return rIdx;
            }
        }
        return -1;
    }

    /**
     * Set the position of the given record.  This will remove the record
     * from the element list and then re-add it at the requested record
     * index
     *
     * @param record
     * @param recordIndex
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setRecordPosition(Record record, int recordIndex) {
        final int eleIdx = getRecordElementIndex(recordIndex);
        if(eleIdx >= 0) {
            addRecord(recordIndex, record);
        } else {
            throw new ArrayIndexOutOfBoundsException(eleIdx);
        }
    }

    private final class TranscriptIterator implements Iterator<Element> {

        private int currentElement = 0;

        @Override
        public boolean hasNext() {
            return currentElement < getNumberOfElements();
        }

        @Override
        public Element next() {
            return getElementAt(currentElement++);
        }

        @Override
        public void remove() {
            removeElement(currentElement-1);
        }

    }

    @Override
    public void accept(Visitor<Transcript.Element> visitor) {
        for(Element ele:this) visitor.visit(ele);
    }

    @NotNull
    @Override
    public Iterator<Element> iterator() {
        return new TranscriptIterator();
    }

    public Stream<Transcript.Element> stream() {
        return stream(false);
    }

    public Stream<Transcript.Element> stream(boolean parallel) {
        return StreamSupport.stream(spliterator(), parallel);
    }

}
