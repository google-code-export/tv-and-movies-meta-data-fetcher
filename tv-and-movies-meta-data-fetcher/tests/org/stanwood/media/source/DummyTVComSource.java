package org.stanwood.media.source;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stanwood.media.testdata.Data;

import au.id.jericho.lib.html.Source;

/**
 * This is a dummy tv show source that pulls it's data from files within the test
 * packages.
 */
public class DummyTVComSource extends TVCOMSource {

	private static final Pattern SEASON_PATTERN = Pattern.compile(".*\\.com\\/(.*)\\/show\\/(.*)\\/episode.html\\?season=(\\d+)");
	private static final Pattern PRINT_GUIDE_PATTERN = Pattern.compile(".*\\.com\\/(.*)\\/show\\/(.*)\\/episode_guide.html\\?printable=(\\d+)");
	private final static Pattern SHOW_PATTERN = Pattern.compile(".*\\.com\\/(.*)\\/show\\/(.*)\\/summary\\.html.*");

	@Override
	Source getSource(URL url) throws IOException {
		String strUrl = url.toExternalForm();
		Matcher seasonMatcher = SEASON_PATTERN.matcher(strUrl);
		Matcher printGuideMatcher = PRINT_GUIDE_PATTERN.matcher(strUrl);
		Matcher showMatcher = SHOW_PATTERN.matcher(strUrl);

		if (showMatcher.matches()) {
			return new Source(Data.class.getResource(showMatcher.group(2)+"-summary.html"));
		}
		else if (seasonMatcher.matches()) {
			return new Source(Data.class.getResource(seasonMatcher.group(2)+"-episode_listings-season="+seasonMatcher.group(3)+".html"));
		}
		else if (printGuideMatcher.matches()) {
			return new Source(Data.class.getResource(printGuideMatcher.group(2)+"-episode_guide-printable="+printGuideMatcher.group(3)+".html"));
		}
		else if (strUrl.indexOf("http://www.tv.com/search.php?type=Search&stype=ajax_search")!=-1) {
			if (strUrl.endsWith("qs=Eureka")) {
				return new Source(Data.class.getResource("eureka-search.html"));
			}
		}
		System.err.println("Unable to match url:" + strUrl);
		return null;
	}
}
