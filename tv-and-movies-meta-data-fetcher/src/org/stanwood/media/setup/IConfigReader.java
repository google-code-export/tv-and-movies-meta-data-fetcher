package org.stanwood.media.setup;

import java.util.List;

import org.stanwood.media.renamer.Controller;
import org.stanwood.media.source.ISource;
import org.stanwood.media.store.IStore;

public interface IConfigReader {

	public List<ISource> loadSourcesFromConfigFile(Controller controller) throws ConfigException;

	public List<IStore> loadStoresFromConfigFile(Controller controller) throws ConfigException;
}
