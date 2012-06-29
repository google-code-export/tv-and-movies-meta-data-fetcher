package org.stanwood.media.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.FileNameParser;
import org.stanwood.media.actions.rename.ParsedFileName;

/**
 * This searching strategy will search using common show naming conventions
 */
public class EpisodeFileNameStraregy implements ISearchStrategy {

	private final static Log log = LogFactory.getLog(EpisodeFileNameStraregy.class);

	private final static Pattern PATTERN_WEB_ADDRESS = Pattern.compile("^(\\[ *www\\..*\\.com *] *\\- *).*",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

	/** {@inheritDoc} */
	@Override
	public SearchDetails getSearch(File mediaFile, File rootMediaDir,String renamePattern, MediaDirectory mediaDir) {
		StringBuilder term = new StringBuilder(mediaFile.getName());
		if (mediaFile.isDirectory()) { // Is it a NFO dir
			stripWebAddresses(term);
		}
		SearchHelper.replaceWithSpaces(term);
		SearchHelper.replaceHyphens(term);
		List<Pattern> stripTokens;
		if (mediaDir!=null) {
			stripTokens = mediaDir.getMediaDirConfig().getStripTokens();
		}
		else {
			stripTokens = new ArrayList<Pattern>();
		}
		SearchDetails result = null;
		ParsedFileName parsedFile = FileNameParser.parse(term.toString());
		if (parsedFile!=null) {
			if (parsedFile.getTerm().trim().length()>0) {
				result = createSearchDetails(stripTokens,parsedFile.getTerm(),parsedFile.getSeason(),parsedFile.getEpisodes());
			}
		}
		if (log.isDebugEnabled()) {
			if (result==null) {
				log.debug("Unable to find search details for "+mediaFile.getAbsolutePath()); //$NON-NLS-1$
			}
		}

		return result;
	}

	private void stripWebAddresses(StringBuilder term) {
		Matcher m = PATTERN_WEB_ADDRESS.matcher(term);
		if (m.matches()) {
			term.replace(0, m.group(1).length(), ""); //$NON-NLS-1$
		}
	}

	protected SearchDetails createSearchDetails(List<Pattern>stripTokens,String rawTerm,int season,List<Integer> episodes) {
		StringBuilder term = new StringBuilder(rawTerm);
		SearchHelper.removeStripTokens(stripTokens,term);
		SearchHelper.trimRubishFromEnds(term);
		SearchDetails details = new SearchDetails(term.toString(), null, null);
		details.setSeason(season);
		details.setEpisodes(episodes);
		return details;
	}

}
