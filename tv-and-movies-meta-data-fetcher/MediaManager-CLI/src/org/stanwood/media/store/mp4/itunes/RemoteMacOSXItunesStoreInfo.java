package org.stanwood.media.store.mp4.itunes;

import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;

/**
 * This is the store information class for store {@link RemoteMacOSXItunesStore}
 */
public class RemoteMacOSXItunesStoreInfo extends ExtensionInfo<RemoteMacOSXItunesStore> {

	/** The parameter type information for the hostname parameter */
	public final static ParameterType PARAM_HOSTNAME = new ParameterType("hostname",String.class,true); //$NON-NLS-1$
	/** The parameter type information for the host port parameter */
	public final static ParameterType PARAM_PORT = new ParameterType("port",String.class,false); //$NON-NLS-1$
	/** The parameter type information for the username parameter */
	public final static ParameterType PARAM_USERNAME = new ParameterType("username",String.class,true); //$NON-NLS-1$
	/** The parameter type information for the password parameter */
	public final static ParameterType PARAM_PASSWORD = new ParameterType("password",String.class,true); //$NON-NLS-1$
	/** The parameter type information for the search-pattern parameter */
	public final static ParameterType PARAM_SEARCH_PATTERN = new ParameterType("search-pattern",String.class,false); //$NON-NLS-1$
	/** The parameter type information for the search-replace parameter */
	public final static ParameterType PARAM_SEARCH_REPLACE = new ParameterType("search-replace",String.class,false); //$NON-NLS-1$
	/** The parameter type information for the file-separator parameter */
	public final static ParameterType PARAM_FILE_SEPARATOR = new ParameterType("file-separator",String.class,false); //$NON-NLS-1$

	private final static ParameterType PARAM_TYPES[] = {PARAM_HOSTNAME,PARAM_PORT,PARAM_USERNAME,PARAM_PASSWORD,PARAM_SEARCH_PATTERN,PARAM_SEARCH_REPLACE};

	/**
	 * The constructor
	 */
	public RemoteMacOSXItunesStoreInfo() {
		super(RemoteMacOSXItunesStore.class.getName(),ExtensionType.STORE, PARAM_TYPES);
	}

	@Override
	protected RemoteMacOSXItunesStore createExtension() {
		return new RemoteMacOSXItunesStore();
	}
}
