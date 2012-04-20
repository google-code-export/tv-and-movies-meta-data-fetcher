package org.stanwood.media.store.mp4.itunes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.store.mp4.Messages;

/**
 * This is a client to the iTunes control server. It is used to send and receive messages.
 * Details of the server can be found at {@link "http://code.google.com/p/itunes-remote-control-server/"}.
 */
public class ITunesRemoteClient extends Thread {

	/** The default timeout for commands that don't take a long time in seconds */
	public static final long DEFAULT_TIMEOUT = 60 * 5;
	/** This can be used if no timeout is requried */
	public static final long NO_TIMEOUT = -1;

	private final static Log log = LogFactory.getLog(ITunesRemoteClient.class);
	private final static Pattern CODE_PATTERN = Pattern.compile("(\\d+).*"); //$NON-NLS-1$

	/** The HELO command text */
	public final static String CMD_HELO = "HELO"; //$NON-NLS-1$
	/** The QUIT command text */
	public final static String CMD_QUIT = "QUIT"; //$NON-NLS-1$
	/** The LOGIN command text */
	public final static String CMD_LOGIN = "LOGIN"; //$NON-NLS-1$
	/** The PASSWORD command text */
	public final static String CMD_PASSWORD = "PASSWORD"; //$NON-NLS-1$
	/** The CLEARFILES command text */
	public final static String CMD_CLEAR_FILES = "CLEARFILES"; //$NON-NLS-1$
	/** The ADDFILES command text */
	public final static String CMD_ADD_FILES = "ADDFILES"; //$NON-NLS-1$
	/** The REMOVEFILES command text */
	public final static String CMD_REMOVE_FILES = "REMOVEFILES"; //$NON-NLS-1$
	/** The REMOVEDEADFILES command text */
	public final static String CMD_REMOVE_DEAD_FILES = "REMOVEDEADFILES"; //$NON-NLS-1$
	/** The LISTDEADFILES command text */
	public final static String CMD_LIST_DEAD_FILES = "LISTDEADFILES"; //$NON-NLS-1$
	/** The REFRESHFILES command text */
	public final static String CMD_REFRESH_FILES = "REFRESHFILES"; //$NON-NLS-1$

	/** The FILE command text */
	public final static String CMD_FILE = "FILE"; //$NON-NLS-1$
	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;

	private List<String>lines = new ArrayList<String>();

	private ExecutorService threadPool = Executors.newFixedThreadPool(5);

	private boolean finished = false;

	/**
	 * The constructor
	 */
	public ITunesRemoteClient() {
		super("ITunes Remote Client"); //$NON-NLS-1$
	}

	/**
	 * Used to connect to the server
	 * @param hostname The hostname of the server
	 * @param port The port number of the host
	 * @throws StoreException Thrown if their is a problem
	 */
	public void connect(InetAddress hostname,int port) throws StoreException {
		try {
			try {
				socket = new Socket(hostname, port);
			}
			catch (ConnectException e) {
				throw new StoreException(MessageFormat.format("Unable to connect to remote itunes controller at host {0} port {1}",hostname,port),e);
			}
			out = new PrintWriter(socket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			log.info(MessageFormat.format(Messages.getString("ITunesRemoteClient.CONNECTION_TO_SERVER"),hostname,port)); //$NON-NLS-1$
			start();
			if (log.isDebugEnabled()) {
				log.debug("Connected, wating for response..."); //$NON-NLS-1$
			}
			waitForCode(null, 1,DEFAULT_TIMEOUT);
			if (log.isDebugEnabled()) {
				log.debug("Connected to server " + hostname+":" + port); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		catch (IOException e) {
			disconnect();
			throw new StoreException(Messages.getString("ITunesRemoteClient.UNABLE_CONNECT"),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to login to the server
	 * @param username The username of the user connecting to the server
	 * @param password The password of the user connecting to the server
	 * @throws StoreException Thrown if their is a problem
	 */
	public void login(String username,String password) throws StoreException {
		try {
			sendCommand(CMD_LOGIN+":"+username,222,DEFAULT_TIMEOUT); //$NON-NLS-1$
			sendCommand(CMD_PASSWORD+":"+password,223,DEFAULT_TIMEOUT); //$NON-NLS-1$
		}
		catch (StoreException e) {
			throw new StoreException(Messages.getString("ITunesRemoteClient.UNABLE_LOGIN"),e); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		try {
			String line;
			while (finished==false && (line=in.readLine())!=null) {
				Thread.sleep(200);
				if (log.isDebugEnabled()) {
					log.debug("Read line from server: "+line); //$NON-NLS-1$
				}
				synchronized (this) {
					lines.add(line);
				}
			}
		}
		catch (SocketException e) {
			if (!finished) {
				log.error(Messages.getString("ITunesRemoteClient.UNABLE_COMUNICATE"),e); //$NON-NLS-1$
			}
		}
		catch (IOException e) {
			log.error(Messages.getString("ITunesRemoteClient.UNABLE_COMUNICATE"),e); //$NON-NLS-1$
		}
		catch (InterruptedException e) {
			log.error(Messages.getString("ITunesRemoteClient.UNABLE_COMUNICATE"),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to send a command to the server
	 * @param cmd The command to set to the server
	 * @param expectedCode The expected return code if their are no errors
	 * @param timeout the timeout in seconds, or -1 for no timeout
	 * @throws StoreException Thrown if their is a problem
	 */
	public void sendCommand(String cmd, int expectedCode,long timeout) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("Sending command '"+cmd+"'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		out.println(cmd);
		waitForCode(cmd, expectedCode,timeout);
	}

	protected void waitForCode(String cmd, int expectedCode,long timeout) throws StoreException {
		int code;
		try {
			code = nextMessage(timeout);
		}
		catch (StoreException e) {
			throw new StoreException(MessageFormat.format(Messages.getString("ITunesRemoteClient.UNABLE_GET_RESULT_CMD"), cmd)); //$NON-NLS-1$
		}
		if (code!=expectedCode) {
			if (cmd==null) {
				throw new StoreException(MessageFormat.format(Messages.getString("ITunesRemoteClient.UNEXPECTED_CODE"),expectedCode,code)); //$NON-NLS-1$
			}
			else {
				throw new StoreException(MessageFormat.format(Messages.getString("ITunesRemoteClient.UNEXPECTED_CODE_CMD"),expectedCode,code,cmd)); //$NON-NLS-1$
			}
		}
	}

	private int nextMessage(long timeout) throws StoreException {
		Callable<Integer> task = new Callable<Integer>() {
		   @Override
		public Integer call() {
			for (;;) {
				String line = null;
				synchronized (this) {
					if (lines.size()>0) {
						line = lines.remove(0);
						if (log.isDebugEnabled()) {
							log.debug("POP: "+line); //$NON-NLS-1$
						}
					}
				}
				if (line!=null) {
					Matcher m=CODE_PATTERN.matcher(line);
					if (m.matches()) {
						return Integer.parseInt(m.group(1));
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return null;
				}
			}
		   }
		};
		Future<Integer> future = threadPool.submit(task);
		Integer code;
		try {
			if (timeout==NO_TIMEOUT) {
				code = future.get();
			}
			else {
				code = future.get(timeout, TimeUnit.SECONDS);
			}
			if (code==null) {
				throw new StoreException(Messages.getString("ITunesRemoteClient.UNABLE_GET_MESSAGE")); //$NON-NLS-1$
			}
		} catch (InterruptedException e) {
			throw new StoreException(Messages.getString("ITunesRemoteClient.UNABLE_GET_MESSAGE"),e); //$NON-NLS-1$
		} catch (ExecutionException e) {
			throw new StoreException(Messages.getString("ITunesRemoteClient.UNABLE_GET_MESSAGE"),e); //$NON-NLS-1$
		} catch (TimeoutException e) {
			throw new StoreException(Messages.getString("ITunesRemoteClient.UNABLE_GET_MESSAGE"),e); //$NON-NLS-1$
		}
		return code;
	}

	/**
	 * Used to disconnect from the server
	 */
	public void disconnect() {
		if (log.isDebugEnabled()) {
			log.debug("Disconnecting from server"); //$NON-NLS-1$
		}
		finished = true;
		if (socket!=null) {
			try {
				socket.close();
			} catch (IOException e) {
				log.error(Messages.getString("ITunesRemoteClient.UNABLE_CLOSE_SOCKET"),e); //$NON-NLS-1$
			}
		}
		try {
			join();
		} catch (InterruptedException e1) {
			log.error(Messages.getString("ITunesRemoteClient.UNABLE_CLOSE_SOCKET"),e1); //$NON-NLS-1$
		}
		threadPool.shutdown();
		if (in!=null) {
			try {
				in.close();
			} catch (IOException e) {
				log.error(Messages.getString("ITunesRemoteClient.UNABLE_CLOSE_SOCKET"),e); //$NON-NLS-1$
			}
		}
		if (out!=null) {
			out.close();
		}
		synchronized (this) {
			lines = null;
		}
	}
}
