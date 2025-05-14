package ca.phon.app.opgraph.macro;

import ca.phon.util.resources.URLListHandler;

import java.io.IOException;
import java.net.URL;

/**
 * Load macros from a remote URL list.
 */
public class RemoteMacroHandler extends URLListHandler<URL> {

    /**
     * Constructor
     */
    public RemoteMacroHandler(URL reportListURL) {
        super(reportListURL);
    }

    @Override
    public URL loadFromURL(URL url) throws IOException {
        return url;
    }

}
