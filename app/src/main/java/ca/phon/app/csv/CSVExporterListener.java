package ca.phon.app.csv;

import java.io.IOException;

public interface CSVExporterListener {
    void writingError(
        IOException e
    );
}
