package ca.phon.app.opgraph.analysis;

import ca.phon.util.resources.URLListHandler;

import java.net.URL;

/**
 * Load reports from a remote URL list.
 */
public class RemoteAnalysisHandler extends URLListHandler<URL> {

    /**
     * Constructor
     */
    public RemoteAnalysisHandler(URL reportListURL) {
        super(reportListURL);
    }

    @Override
    public URL loadFromURL(URL url) {
        return url;
    }

}
