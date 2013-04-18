/*
 *
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.server.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.Controller;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.store.LoggingStore;
import org.stanwood.media.util.FileHelper;

@SuppressWarnings("nls")
public class TestManageMediaCommand extends XBMCAddonTestBase {


	@Test
	public void testManageMedia() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File watchDir = FileHelper.createTmpDir("watchdir");
		File filmDir = FileHelper.createTmpDir("filmDir");

		File showDir = FileHelper.createTmpDir("showDir");
		ConfigReader config = TestImportMediaCommand.createTestConfig(watchDir,filmDir,showDir,LoggingStore.class.getName(),null);
		Controller controller = new Controller(config);
		controller.init(false);

		Assert.assertEquals(0,LoggingStore.getEvents().size());

		StringBuilderCommandLogger logger = new StringBuilderCommandLogger();

		File f =new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - blah.avi");
		if (!f.getParentFile().mkdirs()) {
			throw new IOException("Unable to create dir : " + f.getParentFile().getAbsolutePath());
		}
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}
		f = new File(showDir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"2x01 - blah blah....avi");
		if (!f.getParentFile().mkdirs()) {
			throw new IOException("Unable to create dir : " + f.getParentFile().getAbsolutePath());
		}
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}
		f = new File(filmDir,"iron.man.2009.dvdrip.xvid-amiable.avi");
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}


		Queue<String> events = LoggingStore.getEvents();
		Assert.assertTrue(events.isEmpty());

		ManageMediaCommand mmCommand = new ManageMediaCommand(controller);
		List<File>mediaDirs = new ArrayList<File>();
		mediaDirs.add(filmDir);
		mediaDirs.add(showDir);
		mmCommand.setMediaDirectories(mediaDirs);
		EmptyResult result = mmCommand.execute(logger, new NullProgressMonitor());
		Assert.assertNotNull(result);



		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("upgrade()",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("upgrade()",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("getFilm("+filmDir.getAbsolutePath()+","+filmDir.getAbsolutePath()+File.separator+"iron.man.2009.dvdrip.xvid-amiable.avi)",events.remove());
		Assert.assertEquals("searchMedia(iron man,FILM,null,"+filmDir.getAbsolutePath()+"," +
	            new File(filmDir,"iron.man.2009.dvdrip.xvid-amiable.avi")+") -> null",events.remove());
		Assert.assertEquals("getFilm()",events.remove());
		Assert.assertEquals("cacheFilm("+filmDir.getAbsolutePath()+","+filmDir.getAbsolutePath()+File.separator+"iron.man.2009.dvdrip.xvid-amiable.avi)",events.remove());
		Assert.assertEquals("aboutToRenamedFile()",events.remove());
		Assert.assertEquals("renamedFile("+filmDir.getAbsolutePath()+File.separator+"iron.man.2009.dvdrip.xvid-amiable.avi,"+
										   filmDir.getAbsolutePath()+File.separator+"Iron Man (2008).avi)",events.remove());
		Assert.assertEquals("performedActions("+filmDir.getAbsolutePath()+")",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("searchMedia(Heroes,TV_SHOW,null,"+showDir.getAbsolutePath()+
	            ","+new File(showDir,"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - blah.avi")+") -> null",events.remove());
		Assert.assertEquals("getShow()",events.remove());
		Assert.assertEquals("cacheShow()",events.remove());
		Assert.assertEquals("getSeason()",events.remove());
		Assert.assertEquals("cacheSeason()",events.remove());
		Assert.assertEquals("getEpisode()",events.remove());
		Assert.assertEquals("cacheEpisode("+showDir+","+showDir+File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - blah.avi)",events.remove());
		Assert.assertEquals("aboutToRenamedFile()",events.remove());
		Assert.assertEquals("renamedFile("+showDir+File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - blah.avi,"+showDir+File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi)",events.remove());
		Assert.assertEquals("searchMedia(Heroes,TV_SHOW,null,"+showDir.getAbsolutePath()+
	            ","+new File(showDir,"Heroes"+File.separator+"Season 2"+File.separator+"2x01 - blah blah....avi")+") -> 79501:org.stanwood.media.source.xbmc.XBMCSource#metadata.tvdb.com - (http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip) - (null)",events.remove());
		Assert.assertEquals("getShow()",events.remove());
		Assert.assertEquals("getSeason()",events.remove());
		Assert.assertEquals("cacheSeason()",events.remove());
		Assert.assertEquals("getEpisode()",events.remove());
		Assert.assertEquals("cacheEpisode("+showDir+","+showDir+File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"2x01 - blah blah....avi)",events.remove());
		Assert.assertEquals("aboutToRenamedFile()",events.remove());
		Assert.assertEquals("renamedFile("+showDir+File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"2x01 - blah blah....avi,"+showDir+File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"2x01 - Four Months Later....avi)",events.remove());
		Assert.assertEquals("performedActions("+showDir+")",events.remove());

		LoggingStore.printEvents();

		Assert.assertTrue(events.isEmpty());
		Assert.assertTrue(controller.getSeenDB().isSeen(showDir, new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi")));
	}
}
