/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
 *
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
package org.stanwood.media.store.xmlstore;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.stanwood.media.Controller;
import org.stanwood.media.Helper;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.cli.manager.TestCLIMediaManager;
import org.stanwood.media.cli.manager.TestNFOFilms;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.source.xbmc.cli.CLIManageAddons;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test that the XMLStore2 can be upgraded from older versions
 * @author jp
 *
 */
@SuppressWarnings("nls")
public class TestXMLStore2Upgrade extends XBMCAddonTestBase {

	private final static String LS = System.getProperty("line.separator");

	/**
	 * Used to setup the exit handler
	 * @throws Exception Thrown if their are any problems
	 */
	@Before
	public void setUpApp() throws Exception {
		CLIManageAddons.setExitHandler(new IExitHandler() {
			@Override
			public void exit(int exitCode) {

			}
		});
	}

	/**
	 * Used to test that a XMLStore2 version 2.0 can be upgraded to the latest when it contains TV Shows
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testUpgrade20TVShow() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		File dir = FileHelper.createTmpDir("show");
		String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
		Map<String,String>params = new HashMap<String,String>();
		params.put("posters", "false");
		ConfigReader configReader = TestCLIMediaManager.setupTestController(false,dir,pattern,Mode.TV_SHOW,XBMCSource.class.getName()+"#metadata.tvdb.com",params,XMLStore2.class.getName(),"",RenameAction.class.getName());
		try {
			File cacheFile = new File(dir,".mediaManager-xmlStore.xml");
			params = new HashMap<String,String>();
			params.put("rootMedia", dir.getAbsolutePath());
			FileHelper.copy(TestXMLStore2.class.getResourceAsStream("expectedXmlStoreResults20.xml"),cacheFile,params);
			Controller controller = new Controller(configReader);
			controller.init(false);
			MediaDirectory mediaDir = new MediaDirectory(controller, configReader, dir);
			Helper.assertXMLEquals(TestXMLStore2.class.getResourceAsStream("expectedXmlStoreResults21-3.xml"),new FileInputStream(cacheFile),params);

			IShow upgradedShow = mediaDir.getSources().get(0).getShow("79501", new URL("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"), null);
			Assert.assertEquals("NBC",upgradedShow.getStudio());

		} finally {
			FileHelper.delete(dir);
		}
	}

	/**
	 * Used to test that a XMLStore2 version 2.1 can be upgraded to the latest revision when it contains TV Shows
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testUpgrade2_1_1_to_2_1_2TVShow() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		File dir = FileHelper.createTmpDir("show");
		String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
		Map<String,String>params = new HashMap<String,String>();
		params.put("posters", "false");
		ConfigReader configReader = TestCLIMediaManager.setupTestController(false,dir,pattern,Mode.TV_SHOW,XBMCSource.class.getName()+"#metadata.tvdb.com",params,XMLStore2.class.getName(),"",RenameAction.class.getName());
		try {
			File cacheFile = new File(dir,".mediaManager-xmlStore.xml");
			params = new HashMap<String,String>();
			params.put("rootMedia", dir.getAbsolutePath());
			FileHelper.copy(TestXMLStore2.class.getResourceAsStream("expectedXmlStoreResults21.xml"),cacheFile,params);
			Controller controller = new Controller(configReader);
			controller.init(false);
			MediaDirectory mediaDir = new MediaDirectory(controller, configReader, dir);
			Helper.assertXMLEquals(TestXMLStore2.class.getResourceAsStream("expectedXmlStoreResults21-3.xml"),new FileInputStream(cacheFile),params);

			IShow upgradedShow = mediaDir.getSources().get(0).getShow("79501", new URL("http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip"), null);
			Assert.assertEquals("NBC",upgradedShow.getStudio());

		} finally {
			FileHelper.delete(dir);
		}
	}

	/**
	 * Used to test that a XMLStore2 version 2.0 can be upgraded to the latest when it contains Films
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testUpgrade20Film() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		File dir = FileHelper.createTmpDir("films");
		String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
		Map<String,String>params = new HashMap<String,String>();
		params.put("posters", "false");
		ConfigReader configReader = TestCLIMediaManager.setupTestController(false,dir,pattern,Mode.FILM,XBMCSource.class.getName()+"#metadata.imdb.com",params,XMLStore2.class.getName(),"",RenameAction.class.getName());
		TestNFOFilms.mmXBMCCmd(dir, pattern,"--log_config","NOINIT","install","metadata.imdb.com");

		try {
			File cacheFile = new File(dir,".mediaManager-xmlStore.xml");
			params = new HashMap<String,String>();
			params.put("rootMedia", dir.getAbsolutePath());
			FileHelper.copy(TestXMLStore2.class.getResourceAsStream("expected-film-rename-output20.xml"),cacheFile,params);
			Controller controller = new Controller(configReader);
			controller.init(false);
			MediaDirectory mediaDir = new MediaDirectory(controller, configReader, dir);
			Helper.assertXMLEquals(TestXMLStore2.class.getResourceAsStream("expected-film-rename-output21-3.xml"),new FileInputStream(cacheFile),params);

			IFilm upgradedShow = mediaDir.getSources().get(0).getFilm("tt0371746", new URL("http://akas.imdb.com/title/tt0371746/"), null);
			Assert.assertEquals("Paramount Pictures",upgradedShow.getStudio());

		} finally {
			FileHelper.delete(dir);
		}
	}

	/**
	 * Used to test that a XMLStore2 version 2.1.1 can be upgraded to the latest revision when it contains Films
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testUpgrade21_1_to_21_2Film() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		File dir = FileHelper.createTmpDir("films");
		String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
		Map<String,String>params = new HashMap<String,String>();
		params.put("posters", "false");
		ConfigReader configReader = TestCLIMediaManager.setupTestController(false,dir,pattern,Mode.FILM,XBMCSource.class.getName()+"#metadata.imdb.com",params,XMLStore2.class.getName(),"",RenameAction.class.getName());
		TestNFOFilms.mmXBMCCmd(dir, pattern,"--log_config","NOINIT","install","metadata.imdb.com");

		try {
			File cacheFile = new File(dir,".mediaManager-xmlStore.xml");
			params = new HashMap<String,String>();
			params.put("rootMedia", dir.getAbsolutePath());
			FileHelper.copy(TestXMLStore2.class.getResourceAsStream("expected-film-rename-output21.xml"),cacheFile,params);
			Controller controller = new Controller(configReader);
			controller.init(false);
			MediaDirectory mediaDir = new MediaDirectory(controller, configReader, dir);
			Helper.assertXMLEquals(TestXMLStore2.class.getResourceAsStream("expected-film-rename-output21-3.xml"),new FileInputStream(cacheFile),params);

			IFilm upgradedShow = mediaDir.getSources().get(0).getFilm("tt0371746", new URL("http://akas.imdb.com/title/tt0371746/"), null);
			Assert.assertEquals("Paramount Pictures",upgradedShow.getStudio());

		} finally {
			FileHelper.delete(dir);
		}
	}

}
