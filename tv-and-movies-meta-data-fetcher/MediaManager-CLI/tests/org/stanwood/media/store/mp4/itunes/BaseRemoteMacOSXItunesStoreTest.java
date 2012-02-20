package org.stanwood.media.store.mp4.itunes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.stanwood.media.util.FileHelper;

/**
 * This is a base test class for all tests that need to talk to the dummy itunes control server
 */
@SuppressWarnings("nls")
public class BaseRemoteMacOSXItunesStoreTest {

	protected final static String USER="itunes";
	protected final static String PASSWORD="blah";
	private static final int MIN_PORT_NUMBER = 1000;
	private static final int MAX_PORT_NUMBER = 9000;

	private Integer port;
	private Thread severThread = null;
	private ScriptEngine jruby;
	protected Throwable exception;
	private boolean started;

	/**
	 * Used to start the dummy ruby itunes server before tests run
	 * @throws Throwable Thrown if their is a problrm
	 */
	@Before
	public void setupServer() throws Throwable {
		System.out.println("******** Starting server");
		started = false;
		severThread = new Thread("dummyServer.rb") {

			@Override
			public void run() {
				try {
					System.setProperty("org.jruby.embed.compat.version", "RUBY1_9");
					ScriptEngineManager scriptMgr = new ScriptEngineManager();
					jruby = scriptMgr.getEngineByName("jruby");
					Assert.assertNotNull(jruby);
					port = getPortNumber();
					Assert.assertNotNull(port);

					startServer();
				}
				catch (Throwable e) {
					exception = e;
				}
			}
		};
		severThread.start();

		while (!started && exception==null) {
			Thread.sleep(100);
		}
		while (available(port)) {
			Thread.sleep(100);
		}

		if (exception!=null) {
			throw exception;
		}
		else {
			System.out.println("******** Server started on port: " + port);
		}
	}

	protected File createConfigFile(String username,String password) throws IOException, FileNotFoundException {
		File configFile = FileHelper.createTempFile("config", ".xml");
		PrintStream ps = null;
		try {
			ps = new PrintStream(configFile);
			ps.println("<itunesController>"+FileHelper.LS);
		    ps.println("    <users>"+FileHelper.LS);
			ps.println("        <user username=\""+username+"\" password=\""+password+"\"/>"+FileHelper.LS);
			ps.println("    </users>"+FileHelper.LS);
			ps.println("</itunesController>"+FileHelper.LS);
			ps.flush();
		}
		finally {
			if (ps!=null) {
				ps.close();
			}
		}
		return configFile;
	}

	private static Integer getPortNumber() {
		for (int port = MIN_PORT_NUMBER;port<MAX_PORT_NUMBER;port++) {
			if (available(port)) {
				return port;
			}
		}
		return null;
	}

	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port the port to check for availability
	 * @return true if the port is avaliable, otherwise false
	 */
	public static boolean available(int port) {
	    if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
	        throw new IllegalArgumentException("Invalid start port: " + port);
	    }

	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
	}

	/**
	 * Used to close down the dummy ruby itunes control server
	 * @throws Throwable Thrown if their is a problem
	 */
	@After
	public void tearDownServer() throws Throwable {
		killRubyThreads();
		System.out.println("******** Killing server");
		if (severThread!=null) {
			severThread.interrupt();
			severThread.join();
			severThread = null;
			if (exception!=null) {
				throw exception;
			}
		}
		while (!available(port)) {
			Thread.sleep(100);
		}

		port = null;
		System.out.println("******** Server killed");
	}



	/**
	 * Used to get the port number the server was started on
	 * @return The port number the server was started on
	 */
	protected int getPort() {
		return port;
	}

	private void startServer() throws  IOException, ScriptException {
		final File configFile = createConfigFile(USER,PASSWORD);
//		jruby.getContext().setAttribute(ScriptEngine.ARGV, new String[] {"--config",configFile.getAbsolutePath(),"--port",String.valueOf(port)}, ScriptContext.ENGINE_SCOPE);
//		URL url = getClass().getClassLoader().getResource("/bin/dummyiTunesController.rb");
//		jruby.eval(new InputStreamReader(url.openStream()));
		StringBuilder script = new StringBuilder();
		script.append("require 'itunesController/config'\n");
		script.append("require 'itunesController/dummy_itunescontroller'\n");
		script.append("require 'itunesController/controllserver'\n");
		script.append("require 'itunesController/version'\n");
		script.append("\n");

		script.append("config=ItunesController::ServerConfig.readConfig(\""+configFile.getAbsolutePath()+"\")\n");
		script.append("\n");
		script.append("controller = ItunesController::DummyITunesController.new\n");
		script.append("SERVER=ItunesController::ITunesControlServer.new(config,"+port+",controller)\n");
		script.append("SERVER.start\n");

		jruby.eval(script.toString());
		script = new StringBuilder();
		started = true;
		script.append("SERVER.join\n");
		jruby.eval(script.toString());

//		jruby.eval("SERVER.join\n");

	}



	/**
	 * Used to get the ruby scripting engine
	 * @return The ruby scripting engine
	 */
	protected ScriptEngine getRuby() {
		return jruby;
	}

	/**
	 * Used to get a list of commands that were executed by the server
	 * @return The list of commands
	 * @throws ScriptException thrown if thier is a problem
	 */
	@SuppressWarnings("unchecked")
	public List<String>getCommandLog() throws ScriptException {
		List<String>commandLog = (List<String>) getRuby().eval("ItunesController::DummyITunesController::COMMAND_LOG");
		return commandLog;
	}

	/**
	 * Used to reset the command log before each test
	 * @throws ScriptException Thrown if their is a problem
	 */
	protected void resetCommandLog() throws ScriptException {
		if (started) {
			getRuby().eval("ItunesController::DummyITunesController::COMMAND_LOG = []");
		}
	}

	private void killRubyThreads() throws ScriptException {
		if (started) {
			StringBuilder script = new StringBuilder();
			script.append("SERVER.stop\n");
			getRuby().eval(script.toString());
			started = false;
		}
	}
}
