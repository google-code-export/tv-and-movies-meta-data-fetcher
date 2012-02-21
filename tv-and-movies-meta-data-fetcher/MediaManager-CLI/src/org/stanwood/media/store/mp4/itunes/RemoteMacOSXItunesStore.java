package org.stanwood.media.store.mp4.itunes;


import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.store.mp4.Messages;

/**
 * <p>
 * This store is used to inform itunes of file changes in a media directory. It does this
 * by talking to a remote server running on the same machine as iTunes. The server details can
 * be found at {@link "http://code.google.com/p/itunes-remote-control-server/"}.
 * </p>
 * <p>This store has following parameters:
 * 	<ul>
 * 		<li>hostname - Required parameter giving hostname of the server</li>
 *      <li>port - Optional parameter giving port number of the server, defaults to 7000</li>
 *      <li>username - Required parameter giving name of user used to log into the server</li>
 *      <li>password - Required parameter giving password of user used to log into the server</li>
 *  </ul>
 * </p>
 */
public class RemoteMacOSXItunesStore implements IStore {

	private final static Log log = LogFactory.getLog(RemoteMacOSXItunesStore.class);

	private static final int MAX_FILE_COUNT = 100;

	private List<File>filesAdded = null;
	private List<File> filesDeleted = null;
	private List<File> filesUpdated = null;
	private InetAddress hostname;
	private int port = 7000;
	private String user;
	private String password;

	/** {@inheritDoc}
	 * <p>
	 * This will connect and login to the server
	 * </p>
	 */
	@Override
	public void init(File nativeDir) throws StoreException {
		filesAdded = new ArrayList<File>();
		filesDeleted = new ArrayList<File>();
		filesUpdated = new ArrayList<File>();
		checkParameters();
	}

	/** {@inheritDoc} */
	@Override
	public void cacheEpisode(File rootMediaDir, File episodeFile,
			IEpisode episode) throws StoreException {
		filesAdded.add(episodeFile);
		if (filesDeleted.size()>MAX_FILE_COUNT || filesAdded.size()>MAX_FILE_COUNT) {
			updateItunes();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void cacheFilm(File rootMediaDir, File filmFile, IFilm film,
			Integer part) throws StoreException {
		filesAdded.add(filmFile);
		if (filesDeleted.size()>MAX_FILE_COUNT || filesAdded.size()>MAX_FILE_COUNT) {
			updateItunes();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void renamedFile(File rootMediaDir, File oldFile, File newFile) throws StoreException {
		filesAdded.remove(oldFile);
		filesAdded.add(newFile);
		filesDeleted.add(oldFile);
		if (filesDeleted.size()>MAX_FILE_COUNT || filesAdded.size()>MAX_FILE_COUNT) {
			updateItunes();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void fileDeleted(MediaDirectory dir, File file) throws StoreException {
		filesDeleted.add(file);
		if (filesDeleted.size()>MAX_FILE_COUNT || filesAdded.size()>MAX_FILE_COUNT) {
			updateItunes();
		}
	}

	/** {@inheritDoc}
	 * This will disconnect from the server when done
	 */
	@Override
	public void performedActions(MediaDirectory dir) throws StoreException {
		updateItunes();
	}

	protected void updateItunes() throws StoreException {
		if (filesAdded.size()==0 && filesDeleted.size()==0 && filesUpdated.size()==0) {
			if (log.isDebugEnabled()) {
				log.debug("No changes to send to the server");				 //$NON-NLS-1$
			}
			return;
		}
		ITunesRemoteClient client = new ITunesRemoteClient();
		client.connect(hostname,port);
		client.login(user, password);
		try {
			updateITunes(client);
		} catch (StoreException e) {
			log.error(Messages.getString("RemoteMacOSXItunesStore.UNABLE_UPDATE"),e); //$NON-NLS-1$
		}
		client.disconnect();
	}

	private void checkParameters() throws StoreException {
		if (hostname==null) {
			throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.MISSING_PARAMETER"),RemoteMacOSXItunesStoreInfo.PARAM_HOSTNAME.getName())); //$NON-NLS-1$
		}
		if (user==null) {
			throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.MISSING_PARAMETER"),RemoteMacOSXItunesStoreInfo.PARAM_USERNAME.getName())); //$NON-NLS-1$
		}
		if (password==null) {
			throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.MISSING_PARAMETER"),RemoteMacOSXItunesStoreInfo.PARAM_PASSWORD.getName())); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setParameter(String key, String value) throws StoreException {
		if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_HOSTNAME.getName())) {
			try {
				this.hostname = InetAddress.getByName(value);
			} catch (UnknownHostException e) {
				throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.INVALID_HOSTNAME"), hostname),e); //$NON-NLS-1$
			}
		}
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_PORT.getName())) {
			try {
				this.port = Integer.parseInt(value);
			}
			catch (NumberFormatException e) {
				throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.INVALID_PORT") , value),e); //$NON-NLS-1$
			}
		}
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_USERNAME.getName())) {
			user = value;
		}
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_PASSWORD.getName())) {
			password = value;
		}
		else {
			throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.UNSUPPORTED_PARAM"),key)); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) throws StoreException {
		if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_HOSTNAME.getName())) {
			return hostname.toString();
		}
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_PORT.getName())) {
			return String.valueOf(port);
		}
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_USERNAME.getName())) {
			return user;
		}
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_PASSWORD.getName())) {
			return password;
		}
		else {
			throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.UNSUPPORTED_PARAM"),key)); //$NON-NLS-1$
		}
	}

	private void updateITunes(ITunesRemoteClient client) throws StoreException{
		// Check were still connected
		client.sendCommand(ITunesRemoteClient.CMD_HELO, 220, ITunesRemoteClient.DEFAILT_TIMEOUT);
		if (filesDeleted.size()>0) {
			for (File file : filesDeleted) {
				client.sendCommand(ITunesRemoteClient.CMD_FILE+":"+file.getAbsolutePath(),220,ITunesRemoteClient.DEFAILT_TIMEOUT); //$NON-NLS-1$
			}
			client.sendCommand(ITunesRemoteClient.CMD_REMOVE_FILES,220,ITunesRemoteClient.NO_TIMEOUT);
			filesDeleted.clear();
		}
		if (filesAdded.size()>0) {
			for (File file : filesAdded) {
				client.sendCommand(ITunesRemoteClient.CMD_FILE+":"+file.getAbsolutePath(),220,ITunesRemoteClient.DEFAILT_TIMEOUT); //$NON-NLS-1$
			}
			client.sendCommand(ITunesRemoteClient.CMD_ADD_FILES,220,ITunesRemoteClient.NO_TIMEOUT);
			filesAdded.clear();
		}
		if (filesUpdated.size()>0) {
			for (File file : filesUpdated) {
				client.sendCommand(ITunesRemoteClient.CMD_FILE+":"+file.getAbsolutePath(),220,ITunesRemoteClient.DEFAILT_TIMEOUT); //$NON-NLS-1$
			}
			client.sendCommand(ITunesRemoteClient.CMD_REFRESH_FILES,220,ITunesRemoteClient.NO_TIMEOUT);
			filesUpdated.clear();
		}
		client.sendCommand(ITunesRemoteClient.CMD_QUIT,221,ITunesRemoteClient.DEFAILT_TIMEOUT);
	}

	/** {@inheritDoc} */
	@Override
	public void cacheSeason(File rootMediaDir, File episodeFile, ISeason season)
			throws StoreException {
	}

	/** {@inheritDoc} */
	@Override
	public void cacheShow(File rootMediaDir, File episodeFile, IShow show)
			throws StoreException {

	}

	/** {@inheritDoc} */
	@Override
	public IEpisode getEpisode(File rootMediaDir, File episodeFile,
			ISeason season, int episodeNum) throws StoreException,
			MalformedURLException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public ISeason getSeason(File rootMediaDir, File episodeFile, IShow show,
			int seasonNum) throws StoreException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IShow getShow(File rootMediaDir, File episodeFile, String showId)
			throws StoreException, MalformedURLException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IEpisode getSpecial(File rootMediaDir, File episodeFile,
			ISeason season, int specialNumber) throws MalformedURLException,
			IOException, StoreException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(String name, Mode mode, Integer part,
			MediaDirConfig dirConfig, File mediaFile) throws StoreException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IFilm getFilm(File rootMediaDir, File filmFile, String filmId)
			throws StoreException, MalformedURLException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IEpisode getEpisode(MediaDirectory dir, File file)
			throws StoreException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IFilm getFilm(MediaDirectory dir, File file) throws StoreException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<IEpisode> listEpisodes(MediaDirConfig dirConfig,
			IProgressMonitor monitor) throws StoreException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<IFilm> listFilms(MediaDirConfig dirConfig,
			IProgressMonitor monitor) throws StoreException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void upgrade(MediaDirectory mediaDirectory) throws StoreException {

	}

	/** {@inheritDoc} */
	@Override
	public void fileUpdated(MediaDirectory mediaDirectory, File file) throws StoreException {
		filesUpdated.add(file);
		if (filesDeleted.size()>MAX_FILE_COUNT || filesAdded.size()>MAX_FILE_COUNT) {
			updateItunes();
		}
	}
}
