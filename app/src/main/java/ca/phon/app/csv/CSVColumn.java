package ca.phon.app.csv;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CSVColumn {
    int csvColumnIndex;
    CSVColumnType columnType;
    Map<String, String> options = new HashMap<>();
    boolean importThisColumn = true;
    final String id;

    public CSVColumn() {
        id = UUID.randomUUID().toString();
    }

    public void setOption(String name, String value) {
        options.put(name, value);
    }
    public String getOption(String name) {
        return options.get(name);
    }
}
