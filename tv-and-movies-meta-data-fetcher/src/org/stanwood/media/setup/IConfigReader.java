package org.stanwood.media.setup;

import java.util.List;

import org.stanwood.media.source.ISource;
import org.stanwood.media.store.IStore;

public interface IConfigReader {

	public List<ISource> loadSourcesFromConfigFile() throws ConfigException;

	public List<IStore> loadStoresFromConfigFile() throws ConfigException;
}
