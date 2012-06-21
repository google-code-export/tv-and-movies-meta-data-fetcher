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
	/** Parameter key for schema check */
	public static final ParameterType PARAM_SCHEMA_CHECK = new ParameterType("schemaCheck",Enum.class,false); //$NON-NLS-1$

	private final static ParameterType PARAM_TYPES[] = new ParameterType[]{PARAM_DATABASE_RESOURCE_ID,PARAM_SCHEMA_CHECK};



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
