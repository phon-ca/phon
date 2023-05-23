package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.session.spi.TranscriptSPI;

/**
 * A collection of interleaved {@link Comment}s and {@link Record}s
 *
 *
 */
public final class Transcript extends ExtendableObject {

    public final static class Element {
        final Comment comment;
        final Record record;

        public Element(Comment comment) {
            this.comment = comment;
            this.record = null;
        }

        public Element(Record record) {
            this.record = record;
            this.comment = null;
        }

        public boolean isComment() { return this.comment != null; }

        public boolean isRecord() { return this.record != null; }

        public Comment asComment() { return this.comment; }

        public Record asRecord() { return this.record; }

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
     * Get index of record
     *
     * @param record
     * @return element index of record, or -1 if not found
     */
    public int getRecordElementIndex(Record record) {
        for(int i = 0; i < getNumberOfElements(); i++) {
            if(getElementAt(i).isRecord() && getElementAt(i).asRecord() == record) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Add a new record to the list in the given position.
     *
     * @param pos
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
        final int eleIdx = getRecordElementIndex(record);
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
    public void removeRecord(int recordIndex) {
        final int eleIdx = getRecordElementIndex(recordIndex);
        if(eleIdx >= 0) {
            removeElement(eleIdx);
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
     * Get the position of the given record.
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

}
