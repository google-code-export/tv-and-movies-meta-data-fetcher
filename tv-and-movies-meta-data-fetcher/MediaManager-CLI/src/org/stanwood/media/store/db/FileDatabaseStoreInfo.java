package org.stanwood.media.store.db;

import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;

/**
 * Extension information about the store {@link FileDatabaseStore}
 */
public class FileDatabaseStoreInfo extends ExtensionInfo<FileDatabaseStore>  {

	private final static ParameterType PARAM_TYPES[] = new ParameterType[0];

	/**
	 * The constructor
	 */
	public FileDatabaseStoreInfo() {
		super(FileDatabaseStore.class.getName(),ExtensionType.STORE, PARAM_TYPES);
	}

	@Override
	protected FileDatabaseStore createExtension() throws ExtensionException {
		return new FileDatabaseStore();
	}

}
