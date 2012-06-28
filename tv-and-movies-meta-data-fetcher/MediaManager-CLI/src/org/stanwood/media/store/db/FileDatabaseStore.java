package org.stanwood.media.store.db;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.stanwood.media.Controller;
import org.stanwood.media.database.DBHelper;
import org.stanwood.media.database.DatabaseException;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.DBResource;
import org.stanwood.media.setup.SchemaCheck;
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
		try {
			File file = new File(controller.getConfigDir(),"mediaInfo.db"); //$NON-NLS-1$
			DBResource resource = new DBResource();
			resource.setDialect("org.hibernate.dialect.HSQLDialect"); //$NON-NLS-1$
			resource.setUsername("sa"); //$NON-NLS-1$
			resource.setPassword(""); //$NON-NLS-1$
			resource.setUrl("jdbc:hsqldb:file:"+file.getAbsolutePath()); //$NON-NLS-1$
			resource.setResourceId("file-"+file.getAbsolutePath()); //$NON-NLS-1$
			resource.setSchemaCheck(SchemaCheck.NONE);

			if (!file.exists()) {
				try {
					if (!file.createNewFile() && !file.exists()) {
						throw new StoreException(MessageFormat.format(Messages.getString("FileDatabaseStore.UnableCreateStoreFile"),file)); //$NON-NLS-1$
					}
					Configuration configuration = DBHelper.getInstance().getConfiguration(resource);
					new SchemaExport(configuration).create(false, true);
				}
				catch (IOException e) {
					throw new StoreException(MessageFormat.format(Messages.getString("FileDatabaseStore.UnableCreateStoreFile"),file),e); //$NON-NLS-1$
				} catch (DatabaseException e) {
					throw new StoreException(MessageFormat.format(Messages.getString("FileDatabaseStore.UnableCreateStoreFile"),file),e); //$NON-NLS-1$
				}
			}

			init(resource);
		} catch (ConfigException e) {
			throw new StoreException(Messages.getString("FileDatabaseStore.UnableFindConfigDir")); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setParameter(String key, String value) throws StoreException {
		throw new StoreException(MessageFormat.format(Messages.getString("FileDatabaseStore.KnownParam"), key)); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) throws StoreException {
		throw new StoreException(MessageFormat.format(Messages.getString("FileDatabaseStore.KnownParam"), key)); //$NON-NLS-1$
	}
}
