package ca.phon.util.resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Load items from a remote URL list.  Lines that start with a # are ignored.
 * Items may be full urls or relative urls.  Relative urls are resolved against the
 * given list URLs parent directory.
 *
 * @param <T>
 */
public abstract class URLListHandler<T> extends URLHandler<T> {

    public URLListHandler(URL listURL) {
        super();
        loadReportsFromList(listURL);
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

                // is absolute?
                if(!line.startsWith("http:") && !line.startsWith("https:") && !line.startsWith("file:")) {
                    // relative
                    line = reportListURL.getProtocol() + "://" + reportListURL.getHost() + ":" + reportListURL.getPort() + "/" + line;
                }

                // add to list
                final URL reportURL = new URL(reportListURL, line);
                add(reportURL);
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Unable to load report list from URL: " + reportListURL, e);
        }
    }

}
