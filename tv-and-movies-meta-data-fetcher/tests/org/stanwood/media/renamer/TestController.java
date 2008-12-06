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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.store.FakeStore;

/**
 * Used to test the {@link Controller} class.
 */
public class TestController extends TestCase {

	private final static String LS = System.getProperty("line.separator");
	
	/** 
	 * Test that the store parameters can be read from the configuration and set
	 * on the store.
	 * @throws Exception Thrown if the test produces any errors
	 */
	public void testStoreWithParams() throws Exception{
		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<config>"+LS);
		testConfig.append("    <sources>"+LS);
		testConfig.append("  <source id=\"org.stanwood.media.source.TVCOMSource\"/>"+LS);
		testConfig.append("  </sources>"+LS);
		testConfig.append("  <stores>"+LS);
		testConfig.append("	   <store id=\"org.stanwood.media.store.FakeStore\">"+LS);
		testConfig.append("	     <param name=\"TeSTPaRAm1\" value=\"/testPath/blah\"/>"+LS);
		testConfig.append("	   </store>"+LS);
		testConfig.append("  </stores>"+LS);
		testConfig.append("</config>"+LS);		
		FakeStore.testParam1 = null; 
			
		File configFile = createConfigFileWithContents(testConfig);
		
		ConfigReader configReader = new ConfigReader(configFile);
		configReader.parse();
		Controller.destoryController();
		Controller.initFromConfigFile(configReader);
		
		assertEquals("/testPath/blah",FakeStore.testParam1);
		
		Controller.destoryController();
	}

	private File createConfigFileWithContents(StringBuilder testConfig) throws IOException, FileNotFoundException {
		File configFile = File.createTempFile("config", ".xml");
		configFile.deleteOnExit();
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(configFile);
			PrintStream ps = new PrintStream(os);
			ps.print(testConfig.toString());
		}
		finally {
			os.close();
		}
		return configFile;
	}
}
