package ca.phon.app.opgraph.report;

import ca.phon.app.log.LogUtil;
import ca.phon.util.resources.URLHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Load reports from a remote URL list.
 */
public class RemoteReportHandler extends URLHandler<URL> {

    /**
     * Constructor
     */
    public RemoteReportHandler(URL reportListURL) {
        super();
        loadReportsFromList(reportListURL);
    }

    /**
     * Load reports from the given list
     *
     * @param reportListURL
     */
    private void loadReportsFromList(URL reportListURL) {
        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(reportListURL.openStream(), "UTF-8"))) {
            // read each line, antyhing that starts with a # is ignored
            String line;
            while((line = reader.readLine()) != null) {
                line = line.trim();
                if(line.startsWith("#")) continue;
                if(line.length() == 0) continue;

                // add to list
                final URL reportURL = new URL(reportListURL, line);
                add(reportURL);
            }
        } catch (Exception e) {
            LogUtil.severe("Unable to load report list from URL: " + reportListURL);
            LogUtil.severe(e);
        }
    }

    @Override
    public URL loadFromURL(URL url) throws Exception {
        return url;
    }

}
