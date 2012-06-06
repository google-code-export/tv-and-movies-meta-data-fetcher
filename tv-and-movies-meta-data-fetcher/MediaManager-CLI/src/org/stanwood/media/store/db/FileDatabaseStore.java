package org.stanwood.media.store.db;

import java.io.File;
import java.text.MessageFormat;

import org.stanwood.media.Controller;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.DBResource;
import org.stanwood.media.store.StoreException;



public class FileDatabaseStore extends DatabaseStore {

	@Override
	public void init(Controller controller, File nativeDir) throws StoreException {
		File file;
		try {
			file = new File(controller.getConfigDir(),"mediaInfo.db");
		} catch (ConfigException e) {
			throw new StoreException("Unable to find configuration directory");
		}
		DBResource resource = new DBResource();
		resource.setDialect("org.hibernate.dialect.HSQLDialect");
		resource.setUsername("sa");
		resource.setPassword("");
		resource.setUrl("jdbc:hsqldb:file:"+file.getAbsolutePath());
		init(resource);
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
