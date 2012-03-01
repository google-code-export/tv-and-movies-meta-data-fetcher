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
package org.stanwood.media.info;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.testdata.Data;

@SuppressWarnings("nls")
public class TestMediaFileInfoFetcher {

	private File nativeDir = null;

	public TestMediaFileInfoFetcher() {
		if (System.getProperty("NATIVE_DIR")!=null) {
			nativeDir = new File(System.getProperty("NATIVE_DIR"));
		}
	}

	@Test
	public void testVideoM4VFile() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		URL url = Data.class.getResource("videoWithMetaData.mp4");
		File file = new File(url.toURI());
		Assert.assertTrue(file.exists());

		MediaFileInfoFetcher infoFetcher = new MediaFileInfoFetcher(nativeDir);
		IVideoFileInfo info = (IVideoFileInfo)infoFetcher.getInformation(file);
		Assert.assertEquals(14603, info.getFileSize());
		Assert.assertEquals(100, info.getWidth());
		Assert.assertEquals(100, info.getHeight());
		Assert.assertEquals(15.0F, info.getFrameRate(),0);
		Assert.assertEquals(AspectRatio.Ratio_4_3, info.getAspectRatio());
	}

	@Test
	public void testVideoAVIFile() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		URL url = Data.class.getResource("a_video.avi");
		File file = new File(url.toURI());
		Assert.assertTrue(file.exists());

		MediaFileInfoFetcher infoFetcher = new MediaFileInfoFetcher(nativeDir);
		IVideoFileInfo info = (IVideoFileInfo)infoFetcher.getInformation(file);
		Assert.assertEquals(34180, info.getFileSize());
		Assert.assertEquals(100, info.getWidth());
		Assert.assertEquals(100, info.getHeight());
		Assert.assertEquals(15.0F, info.getFrameRate(),0);
		Assert.assertEquals(AspectRatio.Ratio_4_3, info.getAspectRatio());
	}
}
