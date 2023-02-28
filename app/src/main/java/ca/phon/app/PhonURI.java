package ca.phon.app;

import ca.phon.app.actions.PhonURISchemeHandler;
import ca.phon.session.SessionPath;
import ca.phon.util.Range;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class PhonURI {

    public static final String PHON_URI_SCHEME = "phon";

    private String projectLocation;

    private String corpus;

    private String session;

    private int recordIndex;

    private List<Integer> groups;

    private List<String> tierNames;

    private List<Range> ranges;

    public PhonURI(String projectLocation, String corpus, String session, int recordIndex, List<Integer> groups, List<String> tierNames, List<Range> ranges) {
        super();

        this.projectLocation = (new File(projectLocation)).toURI().getPath();
        this.corpus = corpus;
        this.session = session;
        this.groups = groups;
        this.recordIndex = recordIndex;
        this.tierNames = tierNames;
        this.ranges = ranges;
    }

    private String encodeURIPathComponent(String path) {
        try {
            return new URI(null, null, path, null).toASCIIString();
        } catch (URISyntaxException e) {}
        return "";
    }

    public URI toURI() throws URISyntaxException {
        final StringBuilder s = new StringBuilder();
        s.append(PHON_URI_SCHEME).append(":");
        s.append(projectLocation);
        s.append("/").append(encodeURIPathComponent(corpus));
        s.append("/").append(encodeURIPathComponent(session));
        s.append("?");
        s.append("record=").append(recordIndex);
        if (groups.size() >= 0) {
            s.append("&group=").append(groups.stream().map((i) -> i.toString()).collect(Collectors.joining(",")));
            if (tierNames.size() > 0) {
                s.append("&tier=").append(tierNames.stream().map((tier) -> encodeURIPathComponent(tier)).collect(Collectors.joining(",")));
                if (ranges.size() > 0) {
                    s.append("&range=").append(ranges.stream().map((r) -> r.toString()).collect(Collectors.joining(",")));
                }
            }
        }
        return new URI(s.toString());
    }

}
