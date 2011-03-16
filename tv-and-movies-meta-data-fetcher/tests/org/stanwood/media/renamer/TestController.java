/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.renamer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.store.FakeStore;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the {@link Controller} class.
 */
public class TestController extends XBMCAddonTestBase  {

	private final static String LS = System.getProperty("line.separator");

	/**
	 * Test that the store parameters can be read from the configuration and set
	 * on the store.
	 * @throws Exception Thrown if the test produces any errors
	 */
	@Test
	public void testStoreWithParams() throws Exception{
		File tmpDir = FileHelper.createTmpDir("tmddir");
		try {
			LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+LS);
			testConfig.append("  <mediaDirectory directory=\""+tmpDir.getAbsolutePath()+"\" mode=\"TV_SHOW\" pattern=\"%e.%x\"  >"+LS);
			testConfig.append("    <sources>"+LS);
			testConfig.append("      <source id=\"org.stanwood.media.renamer.FakeSource\"/>"+LS);
			testConfig.append("    </sources>"+LS);
			testConfig.append("    <stores>"+LS);
			testConfig.append("	     <store id=\"org.stanwood.media.store.FakeStore\">"+LS);
			testConfig.append("	       <param name=\"TeSTPaRAm1\" value=\"/testPath/blah\"/>"+LS);
			testConfig.append("	     </store>"+LS);
			testConfig.append("    </stores>"+LS);
			testConfig.append("  </mediaDirectory>"+LS);
			testConfig.append("</mediaManager>"+LS);

			FakeStore.setFakeParam(null);
			File configFile = FileHelper.createTmpFileWithContents(testConfig);
			InputStream is = null;
			try {
				is = new FileInputStream(configFile);
				ConfigReader configReader = new ConfigReader(is);
				configReader.parse();
				Controller controller = new Controller(configReader);

				controller.init();
				controller.getMediaDirectory(tmpDir);
				Assert.assertNotNull(controller);
				Assert.assertEquals("/testPath/blah",FakeStore.getFakeParam());
			}
			finally {
				if (is!=null) {
					is.close();
				}

			}
		}
		finally {
			FileHelper.delete(tmpDir);
		}
	}


}
