package org.stanwood.media.actions.rename;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.util.FileHelper;

/** Used to test the class {@link FileNameParser} */
@SuppressWarnings("nls")
public class TestFileNameParser {

	/**
	 * Used to test that show episode and season numbers can be parsed correctly from the file name
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testFileNameParser() throws Exception {
		File dir = FileHelper.createTmpDir("shows");
		try {
			MediaDirConfig dirConfig = new MediaDirConfig();
			dirConfig.setMediaDir(dir);
			dirConfig.setPattern("%n/Season %s/%s %e - %t.%x");
			dirConfig.setMode(Mode.TV_SHOW);

			SearchResult result = new SearchResult("1234","xbmc","http://blah",null,Mode.TV_SHOW);
			result.setEpisode(6);
			result.setSeason(4);
			assertEpisode(4,6,dirConfig, new File(dir,"show.2009.406.hdtv-lol.avi"),result);
			assertEpisode(5,15,dirConfig, new File(dir,"The Show/Season 5/5 15 - Cycle 5.avi"),null);
			assertEpisode(3,1,dirConfig, new File(dir,"A.Show.2008.S03E01.The.Show.Title.m4v"),null);
			assertEpisode(20,1,dirConfig, new File(dir,"2001 - A title.m4v"),null);
			assertEpisode(20,1,dirConfig, new File(dir,"The show 2001 - A title.m4v"),null);
			assertEpisode(3,1,dirConfig, new File(dir,"301 - A title.m4v"),null);
			assertEpisode(3,1,dirConfig, new File(dir,"The show 301 - A title.m4v"),null);
			assertEpisode(10,6,dirConfig, new File(dir,"The Show-2/10 06 - 300.avi"),null);
			assertEpisode(4,23,dirConfig, new File(dir,"The Show/Season 4/The.Show.S04E23.The Episode (1).HDTV.XviD-BiA.avi"),null);
			assertEpisode(4,24,dirConfig, new File(dir,"The Show/Season 4/The.Show.S04E24.The' & 'Episode (2).HDTV.XviD-BiA.avi"),null);
			assertEpisode(4,13,dirConfig, new File(dir,"Show/Season 4 (22 Episodes)/Sliders.4x13. Title.Episode!HiRes.DivX.TVRip.ENG.topiq.avi"),null);
			assertEpisode(9,22,dirConfig, new File(dir,"Show/Season 9/Show.s09e22.hdtv.xvid-2hd.m4v"),null);
			assertEpisode(2,17,dirConfig, new File(dir,"The Show (Special Series)/Season 2 (6 Episodes)/The Show Special Seriess S02E17 The Title.avi"),null);
			assertEpisode(2,21,dirConfig, new File(dir,"The Show/Season 2 (6 Episodes)/The Show Special Series S02E21 The tile's.avi"),null);
			assertEpisode(1,30,dirConfig, new File(dir,"The Show/Series 1 (30 Episodes)/The.Show.1x30 The-Blah!.avi"),null);
			assertEpisode(2,12,dirConfig, new File(dir,"The Show/Series 2 (26 Episodes)/The.Show.2x12The Deadly Years.avi"),null);
			assertEpisode(3,22,dirConfig, new File(dir,"The Show/Season 3/The Show - S3 E22 - The Blah.avi"),null);
			assertEpisode(1,13,dirConfig, new File(dir,"Show 12/Season 1/Warehouse.13.S01E13.TheBlah.DVDRip.XviD-BLAH.avi"),null);
			assertEpisode(5,15,dirConfig, new File(dir,"Show's The Show/Season 5/5 15 - Blah title.avi"),null);
			assertEpisode(3,1,dirConfig, new File(dir,"The show 3-01 A episode title title.m4v"),null);
			assertEpisode(3,1,dirConfig, new File(dir,"The show 3x01 132.m4v"),null);
			assertEpisode(10,1,dirConfig, new File(dir,"The show 10x01 - A title.m4v"),null);
			assertEpisode(9,11,dirConfig, new File(dir,"The show season 9 episode 11 - A title.m4v"),null);
			assertEpisode(20,11,dirConfig, new File(dir,"The show season 20 episode 11 - A title.m4v"),null);


			Assert.assertNull(FileNameParser.parse(dirConfig, new File(dir,"A Film (2011).m4v"),null));
		}
		finally {
			FileHelper.delete(dir);
		}
	}

	private void assertEpisode(int expectedSeason, int expectedEpisode,MediaDirConfig dirConfig,File file,SearchResult result) {
		ParsedFileName parse =  FileNameParser.parse(dirConfig, file,result);
		Assert.assertEquals("Check season",expectedSeason,parse.getSeason());
		Assert.assertEquals("Check episode",expectedEpisode,parse.getEpisode());
	}
}
