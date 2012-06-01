package org.stanwood.media.store.db;

import org.stanwood.media.extensions.ParameterType;

/**
 * Extension information about the store {@link DatabaseStore}
 */
public class DatabaseStoreInfo {

	public static final ParameterType PARAM_DATABASE_RESOURCE_ID = new ParameterType("resourceId",String.class,false); //$NON-NLS-1$

	private final static ParameterType PARAM_TYPES[] = new ParameterType[]{PARAM_DATABASE_RESOURCE_ID};

}
