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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.stanwood.media.FileHelper;
import org.stanwood.media.testdata.Data;

/**
 * Used to test the {@link Renamer} class.
 */
public class TestRenamer extends TestCase {

	protected static int exitCode;

	
	
	@Override
	protected void setUp() throws Exception {
		Main.doInit = false;
		Main.exitHandler = new IExitHandler() {
			@Override
			public void exit(int exitCode) {
				TestRenamer.exitCode = exitCode; 
			}				
		};	
		exitCode = 0;
		Controller.initWithDefaults();
	}

	@Override
	protected void tearDown() throws Exception {
		Main.doInit = false;
		Controller.destoryController();
	}

	/**
	 * Test the media TV files are correctly renamed using the details stored in the source.
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testRenamerUsingXMLSourceTV() throws Exception {
		
		File dir = FileHelper.createTmpDir("show");
		try {
			File eurekaDir = new File(dir, "Eureka");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directoru : " + eurekaDir.getAbsolutePath());
			}
			
			File f = new File(eurekaDir,"101 - Blah Blah Blah.avi");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			f = new File(eurekaDir,"S01E02 - Hello this is a test.mkv");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			f = new File(eurekaDir,"s02e02 - Hello this is a test.mpg");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			
			FileHelper.copy(Data.class.getResourceAsStream("eureka.xml"),
					new File(eurekaDir, ".show.xml"));
		
			String args[] = new String[] {"-s","17552","-d",eurekaDir.getAbsolutePath()};

							
			Main.main(args);
			
			List<String>files = new ArrayList<String>();
			for (File file : eurekaDir.listFiles()) {
				files.add(file.getName());
			}
			
			Collections.sort(files);
			
			assertEquals(5,files.size());
			assertEquals(".films.xml",files.get(0));
			assertEquals(".show.xml",files.get(1));
			assertEquals("1 01 - Pilot.avi",files.get(2));
			assertEquals("1 02 - Many Happy Returns.mkv",files.get(3));
			assertEquals("2 02 - Phoenix Rising.mpg",files.get(4));
			
			assertEquals("Check exit code",0,exitCode);
		} finally {
			FileHelper.deleteDir(dir);			
		}	
	}
	
	/**
	 * Test the media Film files are correctly renamed using the details stored in the source.
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testRenamerUsingXMLSourceFilm() throws Exception {
		File tmpDir = FileHelper.createTmpDir("Films");
		try {
			File filmDir = new File(tmpDir,"Films");
			if (!filmDir.mkdir()) {
				throw new IOException("Unable to make film dir" + filmDir.getAbsolutePath());
			}
			File f = new File(filmDir,"[divx].dvdrip.The.Usual_susPEcts.avi");
			if (!f.exists() && !f.createNewFile()) {
                throw new IOException("couldn't create file " + f);
            }
			
			FileHelper.copy(Data.class.getResourceAsStream("films.xml"),new File(filmDir, ".films.xml"));
			
			String args[] = new String[] {"-d",filmDir.getAbsolutePath()};
			Main.main(args);
			
			List<String>files = new ArrayList<String>();
			for (File file : filmDir.listFiles()) {
				files.add(file.getName());
			}
			
			Collections.sort(files);

			assertEquals(2,files.size());
			assertEquals(".films.xml",files.get(0));
			assertEquals("The Usual Suspects.avi",files.get(1));
		} finally {
			FileHelper.deleteDir(tmpDir);			
		}
		
	}
}
