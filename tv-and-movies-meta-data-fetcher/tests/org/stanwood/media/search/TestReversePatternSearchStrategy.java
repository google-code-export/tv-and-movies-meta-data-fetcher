package org.stanwood.media.search;

import java.io.File;

import org.junit.Test;
import org.stanwood.media.actions.rename.Token;

public class TestReversePatternSearchStrategy {

	@Test
	public void testSearch() {
		TestFilmSearcher.assertSearchDetails("The Movie", null, null, doSearch("The Movie.m4v", "%t.%x"));
		TestFilmSearcher.assertSearchDetails("A Movie", "2009", 1, doSearch("A Movie (2009) Part 1.avi", "%t{ (%y)}{ Part %p}.%x"));
		TestFilmSearcher.assertSearchDetails("The Movie", "2010", 2, doSearch("The Movie (2010) Part 2.m4v", "%t{ (%y)}{ Part %p}.%x"));
		TestFilmSearcher.assertSearchDetails("The Movie", null, 2, doSearch("The Movie Part 2.m4v", "%t{ (%y)}{ Part %p}.%x"));
		TestFilmSearcher.assertSearchDetails("The Movie", "2010", null, doSearch("The Movie (2010).m4v", "%t{ (%y)}{ Part %p}.%x"));
		TestFilmSearcher.assertSearchDetails("The Movie", null, null, doSearch("The Movie.m4v", "%t{ (%y)}{ Part %p}.%x"));
	}

	private TSearchDetails doSearch(String filename, String pattern) {
		ReversePatternSearchStrategy strategy = new ReversePatternSearchStrategy(Token.TITLE,false);
		File rootMediaDir = new File(File.separator+"media");
		File originalFile = new File(rootMediaDir,filename);
		SearchDetails searchDetails = strategy.getSearch(originalFile, rootMediaDir, pattern, null);
		TSearchDetails sd = new TSearchDetails(originalFile, searchDetails.getTerm(), searchDetails.getYear(), searchDetails.getPart());
		return sd;
	}
}
