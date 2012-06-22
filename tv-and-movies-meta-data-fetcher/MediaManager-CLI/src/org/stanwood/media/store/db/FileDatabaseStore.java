package org.stanwood.media.store.db;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.stanwood.media.Controller;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.DBResource;
import org.stanwood.media.store.StoreException;

/**
 * <p>
 * The file database store. This store is used to store show/film information in a
 * database located in a file. The file is stored in the configuration directory and
 * called &quot;mediaInfo.db.&quot;
 * </p>
 * <p>
 * This store has no parameters
 * </p>
 */
public class FileDatabaseStore extends DatabaseStore {

	/** {@inheritDoc} */
	@Override
	public void init(Controller controller, File nativeDir) throws StoreException {
		File file;
		try {
			file = new File(controller.getConfigDir(),"mediaInfo.db"); //$NON-NLS-1$
			if (!file.exists()) {
				try {
					if (!file.createNewFile() && !file.exists()) {
						throw new StoreException(MessageFormat.format("Unable to create store file: {0}",file));
					}
				}
				catch (IOException e) {
					throw new StoreException(MessageFormat.format("Unable to create store file: {0}",file),e);
				}
			}
		} catch (ConfigException e) {
			throw new StoreException("Unable to find configuration directory");
		}
		DBResource resource = new DBResource();
		resource.setDialect("org.hibernate.dialect.HSQLDialect"); //$NON-NLS-1$
		resource.setUsername("sa"); //$NON-NLS-1$
		resource.setPassword(""); //$NON-NLS-1$
		resource.setUrl("jdbc:hsqldb:file:"+file.getAbsolutePath()); //$NON-NLS-1$
		setHbm2ddlAuto("none"); //$NON-NLS-1$
		init(resource,true);
	}

	/** {@inheritDoc} */
	@Override
	public void setParameter(String key, String value) throws StoreException {
		throw new StoreException(MessageFormat.format("Unknown parameter {0}", key));
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) throws StoreException {
		throw new StoreException(MessageFormat.format("Unknown parameter {0}", key));
	}
}
