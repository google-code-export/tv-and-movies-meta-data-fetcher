package org.stanwood.media.store.db;

import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;

/**
 * Extension information about the store {@link DatabaseStore}
 */
public class DatabaseStoreInfo extends ExtensionInfo<DatabaseStore>  {

	/** Parameter key for resourceId */
	public static final ParameterType PARAM_DATABASE_RESOURCE_ID = new ParameterType("resourceId",String.class,false); //$NON-NLS-1$

	private final static ParameterType PARAM_TYPES[] = new ParameterType[]{PARAM_DATABASE_RESOURCE_ID};

	/**
	 * The constructor
	 */
	public DatabaseStoreInfo() {
		super(DatabaseStore.class.getName(),ExtensionType.STORE, PARAM_TYPES);
	}

	@Override
	protected DatabaseStore createExtension() throws ExtensionException {
		return new DatabaseStore();
	}

}
