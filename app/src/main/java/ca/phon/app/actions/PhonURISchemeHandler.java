package ca.phon.app.actions;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.*;
import ca.phon.app.session.editor.SessionEditorEP;
import ca.phon.plugin.*;
import ca.phon.query.db.*;
import ca.phon.query.db.xml.XMLQueryFactory;
import ca.phon.util.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Open url with phon:// scheme.  The url may have the following parts:
 *  1) phon: - scheme
 *  2) /path/to/session/file - session file or project folder to open
 *  3) ?GETVARS - information about record number and search result highlight
 *     a) record=#
 *     b) group=#[,#...]
 *     c) tier=tier1[,tier2...]
 *     c) range=(#..#)[,(#..#)...]
 */
public final class PhonURISchemeHandler {

	private final static List<String> GET_VARS = List.of("record",  "tier", "group","range");
	private final static List<String> GET_VAR_PATTERNS = List.of("[0-9]+", "\\w[ \\w]+(,\\w[ \\w]+)*", "[0-9]+(,[0-9]+)*", "\\([0-9]+\\.{2,3}[0-9]+\\)(,\\([0-9]+\\.{2,3}[0-9]+\\))*");

	public PhonURISchemeHandler() {

	}

	/**
	 * Check to make sure:
	 *  1) url scheme is phon://
	 *  2) file exists
	 *  3) validate getvars
	 *
	 * @param uri to parse
	 *
	 * @return the localFile to open along with a map of query variables
	 */
	private Tuple<File, Map<String, String>> parseURI(URI uri) throws MalformedURLException, FileNotFoundException {
		if(!uri.getScheme().equals("phon")) {
			throw new MalformedURLException("URI scheme must be phon");
		}
		final String filePath = uri.getPath();
		final File localFile = new File(filePath);
		if(!localFile.exists()) {
			throw new FileNotFoundException(localFile.getAbsolutePath());
		}

		final Map<String, String> getVars = new LinkedHashMap<>();
		final List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(uri, "utf-8");
		for(NameValuePair nvp:nameValuePairs) {
			final String name = nvp.getName();
			final String value = nvp.getValue();

			if(!GET_VARS.contains(name)) {
				throw new MalformedURLException("Invalid get var " + name);
			}
			final String pattern = GET_VAR_PATTERNS.get(GET_VARS.indexOf(name));
			if(!value.matches(pattern)) {
				throw new MalformedURLException("Invalid data for get var " + name + " '" + value + "'");
			}
			getVars.put(name, value);
		}

		return new Tuple<>(localFile, getVars);
	}

	private void setupEpArgs(Map<String, String> queryVars, EntryPointArgs epArgs) {
		if(queryVars.containsKey("record")) {
			final int recordIndex = Integer.parseInt(queryVars.get("record"));
			epArgs.put(SessionEditorEP.RECORD_INDEX_PROPERY, recordIndex);

			if(queryVars.containsKey("tier") && queryVars.containsKey("group") && queryVars.containsKey("range")) {
				final QueryFactory factory = new XMLQueryFactory();
				final Result tempResult = factory.createResult();
				tempResult.setRecordIndex(recordIndex);

				final String tierVal = queryVars.get("tier");
				final String[] tiers = tierVal.split(",");

				final String groupVal = queryVars.get("group");
				final String[] groups = groupVal.split(",");

				final String rangeVal = queryVars.get("range");
				final String[] ranges = rangeVal.split(",");

				if(tiers.length == groups.length && groups.length == ranges.length) {
					for(int i = 0; i < tiers.length; i++) {
						final String tier = tiers[i];
						final String group = groups[i];
						final String range = ranges[i];

						final ResultValue rv = factory.createResultValue();
						rv.setTierName(tier);
						rv.setGroupIndex(Integer.parseInt(group));
						rv.setRange(Range.fromString(range));

						tempResult.addResultValue(rv);
					}

					epArgs.put(SessionEditorEP.RESULT_VALUES_PROPERTY, new Result[] {tempResult});
				}
			}
		}
	}

	public void openURI(URI uri) throws MalformedURLException, FileNotFoundException, PluginException {
		final Tuple<File, Map<String, String>> openInfo = parseURI(uri);

		final File toOpen = openInfo.getObj1();
		final Map<String, String> queryVars = openInfo.getObj2();

		final EntryPointArgs epArgs = new EntryPointArgs();
		if(toOpen.isDirectory()) {
			// open as project
			epArgs.put(EntryPointArgs.PROJECT_LOCATION, toOpen.getAbsolutePath());
			PluginEntryPointRunner.executePlugin(OpenProjectEP.EP_NAME, epArgs);
		} else {
			setupEpArgs(queryVars, epArgs);
			// try to open using file open handler
			epArgs.put(OpenFileEP.INPUT_FILE, toOpen);
			PluginEntryPointRunner.executePlugin(OpenFileEP.EP_NAME, epArgs);
		}
	}

}
