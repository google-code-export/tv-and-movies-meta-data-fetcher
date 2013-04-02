/*
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.server.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.Controller;
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.util.FileHelper;

@SuppressWarnings("nls")
public class TestImportMediaCommand extends XBMCAddonTestBase {

	@Test
	public void testNoMediaFiles() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File watchDir = FileHelper.createTmpDir("watchdir");
		File filmDir = FileHelper.createTmpDir("filmDir");
		File showDir = FileHelper.createTmpDir("showDir");

		ConfigReader config = createTestConfig(watchDir,filmDir,showDir);
		Controller controller = new Controller(config);
		controller.init(false);

		ImportMediaCommand cmd = new ImportMediaCommand(controller);
		cmd.setUseDefaults(true);
		StringBuilderCommandLogger logger = new StringBuilderCommandLogger();
		boolean result = cmd.execute(logger, new NullProgressMonitor());

		Assert.assertTrue(logger.getResult().toString().contains("INFO:Unable to find any media files"));
		Assert.assertFalse(result);
	}

	@Test
	public void testMediaImportIntoEmptyMediaDirs() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File watchDir = FileHelper.createTmpDir("watchdir");
		File filmDir = FileHelper.createTmpDir("filmDir");
		File showDir = FileHelper.createTmpDir("showDir");
		ConfigReader config = createTestConfig(watchDir,filmDir,showDir);
		Controller controller = new Controller(config);
		controller.init(false);

		ImportMediaCommand cmd = new ImportMediaCommand(controller);
		StringBuilderCommandLogger logger = new StringBuilderCommandLogger();

		File f = new File(watchDir,"Heroes S02E01 - Blah Blah Blah.avi");
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}
		f = new File(watchDir,"Heroes S01E01 - Blah Blah Blah.avi");
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}
		f = new File(watchDir,"iron.man.2009.dvdrip.xvid-amiable.avi");
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}

		boolean result = cmd.execute(logger, new NullProgressMonitor());
		System.out.println(logger.getResult().toString());
		Assert.assertTrue(result);

		List<String> files = FileHelper.listFilesAsStrings(watchDir);
		Collections.sort(files);
		System.out.println(files);
		Assert.assertEquals(0,files.size());

		files = FileHelper.listFilesAsStrings(showDir);
		Collections.sort(files);
		Assert.assertEquals(2,files.size());
		Assert.assertEquals(new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi").getAbsolutePath(),files.get(0));
		Assert.assertEquals(new File(showDir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"2x01 - Four Months Later....avi").getAbsolutePath(),files.get(1));

		files = FileHelper.listFilesAsStrings(filmDir);
		Collections.sort(files);
		System.out.println(files);
		Assert.assertEquals(1,files.size());

	}

	private void appendMediaDirectory(StringBuilder testConfig,File mediaDir,Mode mode,String sourceId,Map<String,String> sourceParams,String storeId,String dummy,String ... actions) {
		boolean ignoreSeen = true;
		String pattern = ConfigReader.DEFAULT_TV_FILE_PATTERN;
		if (mode == Mode.FILM) {
			pattern = ConfigReader.DEFAULT_FILM_FILE_PATTERN;
		}
		testConfig.append("  <mediaDirectory default=\"true\" directory=\""+mediaDir.getAbsolutePath()+"\" mode=\""+mode.toString()+"\" pattern=\""+pattern+"\" ignoreSeen=\""+ignoreSeen+"\" >"+FileHelper.LS);
		testConfig.append("    <sources>"+FileHelper.LS);
		if (sourceId!=null) {
			testConfig.append("      <source id=\""+sourceId+"\">"+FileHelper.LS);
			if (sourceParams!=null) {
				for (Entry<String,String> e : sourceParams.entrySet()) {
					testConfig.append("      <param name=\""+e.getKey()+"\" value=\""+e.getValue()+"\"/>"+FileHelper.LS);
				}
			}

			testConfig.append("      </source>"+FileHelper.LS);
		}
		testConfig.append("    </sources>"+FileHelper.LS);

		testConfig.append("    <stores>"+FileHelper.LS);
		if (storeId!=null) {
			testConfig.append("      <store id=\""+storeId+"\"/>"+FileHelper.LS);
		}
		testConfig.append("    </stores>"+FileHelper.LS);

		testConfig.append("    <actions>"+FileHelper.LS);
		if (actions!=null) {
			for (String action : actions) {
				testConfig.append("      <action id=\""+action+"\"/>"+FileHelper.LS);
			}
		}
		testConfig.append("    </actions>"+FileHelper.LS);

		testConfig.append("  </mediaDirectory>"+FileHelper.LS);

	}

	private ConfigReader createTestConfig(File watchDir,File filmDir,File showDir) throws IOException, ConfigException {
		File configDir = FileHelper.createTmpDir("configDir");
		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<mediaManager>"+FileHelper.LS);
		testConfig.append("  <global>"+FileHelper.LS);
		testConfig.append("    <configDirectory>"+configDir.getAbsolutePath()+"</configDirectory>"+FileHelper.LS);
		testConfig.append("  </global>"+FileHelper.LS);
		testConfig.append("  <watchDirectory directory=\""+watchDir.getAbsolutePath()+"\"/>"+FileHelper.LS);
		Map<String,String>params = new HashMap<String,String>();
		params.put("posters", "false");
		appendMediaDirectory(testConfig, filmDir, Mode.FILM, XBMCSource.class.getName()+"#metadata.themoviedb.org",params,null,"",RenameAction.class.getName());
		params = new HashMap<String,String>();
		appendMediaDirectory(testConfig, showDir, Mode.TV_SHOW, XBMCSource.class.getName()+"#metadata.tvdb.com",params,null,"",RenameAction.class.getName());
		testConfig.append("</mediaManager>"+FileHelper.LS);

		File configFile = createConfigFileWithContents(testConfig);

		InputStream is = null;
		try {
			is = new FileInputStream(configFile);
			ConfigReader configReader = new ConfigReader(is);
			configReader.parse();
			AbstractLauncher.setConfig(configReader);
			return configReader;
		}
		finally {
			if (is!=null) {
				is.close();
			}
		}
	}


	private static File createConfigFileWithContents(StringBuilder testConfig) throws IOException {
		File configFile = FileHelper.createTempFile("config", ".xml");
		FileHelper.appendContentsToFile(configFile, testConfig);
		return configFile;
	}
}
