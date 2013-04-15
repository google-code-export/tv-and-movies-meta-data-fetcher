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
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

/**
 * <p>
 * This store is used to inform itunes of file changes in a media directory. It does this
 * by talking to a remote server running on the same machine as iTunes. The server details can
 * be found at {@link "http://code.google.com/p/itunes-remote-control-server/"}.
 * </p>
 * <p>
 * The optional parameter file-separator can be used when media manager is running
 * on a different operating system to the remote client. So for example if media manager
 * is on a linux OS and the remote server is on a windows OS, then the file seperator
 * should be set to \. See the page {@link "http://en.wikipedia.org/wiki/Regular_expression"} for
 * more information on regular expression syntax.
 * </p>
 * <p>
 * The search and replace optional parameters can be used to the media directory
 * is access at a different location on the iTunes server machine to the machine
 * that media manager is running on.
 * </p>
 * <p>This store has following parameters:
 * 	<ul>
 * 		<li>hostname - Required parameter giving hostname of the server</li>
 *      <li>port - Optional parameter giving port number of the server, defaults to 7000</li>
 *      <li>username - Required parameter giving name of user used to log into the server</li>
 *      <li>password - Required parameter giving password of user used to log into the server</li>
 *      <li>search-pattern - Optional parameter that must be used with search-replace.
 *                           This parameter is used to perform a regular expression search and
 *                           replace on the file paths. This parameter is used to set the pattern.<li>
 *      <li>search-replace - Optional parameter that must be used with search-replace.
 *                           This parameter is used to perform a regular expression search and
 *                           replace on the file paths. This parameter is used to set the replacement text.<li>
 *		<li>file-separator - Optional parameter that is used to set the file seperator used in file names sent to the server.<li>
 *  </ul>
 * </p>
 */
public class RemoteMacOSXItunesStore implements IStore {

	private final static Log log = LogFactory.getLog(RemoteMacOSXItunesStore.class);

	private static final int MAX_FILE_COUNT = 20;

	private List<File>filesAdded = null;
	private List<File> filesDeleted = null;
	private List<File> filesUpdated = null;
	private InetAddress hostname;
	private int port = 7000;
	private String user;

	private String password;
	private Pattern pattern;
	private String replace;
	private String fileSeperator;
	private boolean inited = false;

	/** {@inheritDoc}
	 * <p>
	 * This will connect and login to the server
	 * </p>
	 */
	@Override
	public void init() throws StoreException {
		filesAdded = new ArrayList<File>();
		filesDeleted = new ArrayList<File>();
		filesUpdated = new ArrayList<File>();
		checkParameters();
		inited = true;
	}

	/** {@inheritDoc} */
	@Override
	public void cacheEpisode(File rootMediaDir, File episodeFile,File oldFileName,
			IEpisode episode) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("Cache Episode: "+ episodeFile); //$NON-NLS-1$
		}
		if (!inited) {
			init();
		}

		filesAdded.add(episodeFile);
		if (shouldUpdateItunes()) {
			updateItunes();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void cacheFilm(File rootMediaDir, File filmFile,File oldFileName, IFilm film,
			Integer part) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("Caching Film: "+filmFile); //$NON-NLS-1$
		}
		if (!inited) {
			init();
		}

		filesAdded.add(filmFile);
		if (shouldUpdateItunes()) {
			updateItunes();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void renamedFile(File rootMediaDir, File oldFile, File newFile) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("RemoteMaxOSXItunesStore - Rename File: "+oldFile+" -> " +newFile ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!inited) {
			init();
		}

		filesAdded.remove(oldFile);
		filesAdded.add(newFile);
		filesDeleted.add(oldFile);
		if (shouldUpdateItunes()) {
			updateItunes();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void fileDeleted(MediaDirectory dir, File file) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("File Deleted: "+file); //$NON-NLS-1$
		}
		if (!inited) {
			init();
		}

		filesDeleted.add(file);
		if (shouldUpdateItunes()) {
			updateItunes();
		}
	}

	/** {@inheritDoc}
	 * This will disconnect from the server when done
	 */
	@Override
	public void performedActions(MediaDirectory dir) throws StoreException {
		log.info(Messages.getString("RemoteMacOSXItunesStore.ItunesTold")); //$NON-NLS-1$
		updateItunes();
	}

	protected void updateItunes() throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("Updating iTunes"); //$NON-NLS-1$
		}
		if (filesAdded.size()==0 && filesDeleted.size()==0 && filesUpdated.size()==0) {
			if (log.isDebugEnabled()) {
				log.debug("No changes to send to the server");				 //$NON-NLS-1$
			}
			return;
		}
		log.info(Messages.getString("RemoteMacOSXItunesStore.ConnectingItunes")); //$NON-NLS-1$
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

		if (pattern!=null && replace==null) {
			throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.PARAM_SET_BOTH_NEEDED"),RemoteMacOSXItunesStoreInfo.PARAM_SEARCH_PATTERN.getName(),RemoteMacOSXItunesStoreInfo.PARAM_SEARCH_REPLACE.getName())); //$NON-NLS-1$
		}
		if (replace!=null && pattern==null) {
			throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.PARAM_SET_BOTH_NEEDED"),RemoteMacOSXItunesStoreInfo.PARAM_SEARCH_REPLACE.getName(),RemoteMacOSXItunesStoreInfo.PARAM_SEARCH_PATTERN.getName())); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setParameter(String key, String value) throws StoreException {
		if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_HOSTNAME.getName())) {
			try {
				this.hostname = InetAddress.getByName(value);
			} catch (UnknownHostException e) {
				throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.INVALID_HOSTNAME"), value),e); //$NON-NLS-1$
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
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_SEARCH_PATTERN.getName())) {
			pattern = Pattern.compile(value);
		}
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_SEARCH_REPLACE.getName())) {
			replace = value;
		}
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_FILE_SEPARATOR.getName())) {
			fileSeperator = value;
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
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_SEARCH_PATTERN.getName())) {
			return pattern.pattern();
		}
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_SEARCH_REPLACE.getName())) {
			return replace;
		}
		else if (key.equals(RemoteMacOSXItunesStoreInfo.PARAM_FILE_SEPARATOR.getName())) {
			return fileSeperator;
		}
		else {
			throw new StoreException(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.UNSUPPORTED_PARAM"),key)); //$NON-NLS-1$
		}
	}

	private void updateITunes(ITunesRemoteClient client) throws StoreException{
		try {
			log.info(MessageFormat.format(Messages.getString("RemoteMacOSXItunesStore.UpdatingItunes"),hostname.toString(),filesDeleted.size(),filesAdded.size(),filesUpdated.size())); //$NON-NLS-1$
			// Check were still connected
			client.sendCommand(ITunesRemoteClient.CMD_HELO, 220, ITunesRemoteClient.DEFAULT_TIMEOUT);
			if (filesDeleted.size()>0) {
				for (File file : filesDeleted) {
					client.sendCommand(ITunesRemoteClient.CMD_FILE+":"+getFilePath(file.getAbsolutePath()),220,ITunesRemoteClient.DEFAULT_TIMEOUT); //$NON-NLS-1$
				}
				client.sendCommand(ITunesRemoteClient.CMD_REMOVE_FILES,220,ITunesRemoteClient.NO_TIMEOUT);
			}
			if (filesAdded.size()>0) {
				for (File file : filesAdded) {
					if (file.exists()) {
						client.sendCommand(ITunesRemoteClient.CMD_FILE+":"+getFilePath(file.getAbsolutePath()),220,ITunesRemoteClient.DEFAULT_TIMEOUT); //$NON-NLS-1$
					}
					else {
						if (log.isDebugEnabled()) {
							log.debug("Unable to find file, so itunes not told: " + file); //$NON-NLS-1$
						}
					}
				}
				client.sendCommand(ITunesRemoteClient.CMD_ADD_FILES,220,ITunesRemoteClient.NO_TIMEOUT);
			}
			if (filesUpdated.size()>0) {
				for (File file : filesUpdated) {
					client.sendCommand(ITunesRemoteClient.CMD_FILE+":"+getFilePath(file.getAbsolutePath()),220,ITunesRemoteClient.DEFAULT_TIMEOUT); //$NON-NLS-1$
				}
				client.sendCommand(ITunesRemoteClient.CMD_REFRESH_FILES,220,ITunesRemoteClient.NO_TIMEOUT);
			}
			client.sendCommand(ITunesRemoteClient.CMD_QUIT,221,ITunesRemoteClient.DEFAULT_TIMEOUT);
		}
		finally {
			filesDeleted.clear();
			filesAdded.clear();
			filesUpdated.clear();
		}
	}

	private String getFilePath(String orgPath) {
		String path = orgPath;
		if (pattern !=null && replace!=null) {
			StringBuffer newPath = new StringBuffer();
			Matcher m = pattern.matcher(path);
			while (m.find()) {
				m.appendReplacement(newPath, replace);
				path = m.replaceAll(replace);
			}
			m.appendTail(newPath);
			path = newPath.toString();
		}

		if (fileSeperator!=null) {
			StringBuilder newPath = new StringBuilder();
			StringTokenizer tok = new StringTokenizer(path,File.separator,true);
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				if (token.equals(File.separator)) {
					newPath.append(fileSeperator);
				}
				else {
					newPath.append(token);
				}
			}
			path = newPath.toString();
		}

		return path;
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
			ISeason season, List<Integer> episodeNums) throws StoreException,
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
			ISeason season, List<Integer> specialNumbers) throws MalformedURLException,
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
		if (log.isDebugEnabled()) {
			log.debug("File Updated: "+ file); //$NON-NLS-1$
		}

		if (!inited) {
			init();
		}
		filesUpdated.add(file);
		if (shouldUpdateItunes()) {
			updateItunes();
		}
	}

	protected boolean shouldUpdateItunes() {
		return filesDeleted.size()+filesAdded.size()+filesUpdated.size()>MAX_FILE_COUNT;
	}

	/** {@inheritDoc}} */
	@Override
	public boolean fileKnownByStore(MediaDirectory mediaDirectory, File file) throws StoreException {
		if (getEpisode(mediaDirectory,file)!=null) {
			return true;
		}
		if (getFilm(mediaDirectory, file)!=null) {
			return true;
		}
		return false;
	}
}
