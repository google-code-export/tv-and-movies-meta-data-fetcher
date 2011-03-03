package org.stanwood.media.setup;

import java.io.File;
import java.util.List;

import org.stanwood.media.model.Mode;

public class MediaDirConfig {

	private File mediaDir;
	private String pattern;
	private Mode mode;
	private List<StoreConfig>stores;
	private List<SourceConfig>sources;

	public File getMediaDir() {
		return mediaDir;
	}
	public void setMediaDir(File mediaDir) {
		this.mediaDir = mediaDir;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public Mode getMode() {
		return mode;
	}
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	public List<StoreConfig> getStores() {
		return stores;
	}
	public void setStores(List<StoreConfig> stores) {
		this.stores = stores;
	}
	public List<SourceConfig> getSources() {
		return sources;
	}
	public void setSources(List<SourceConfig> sources) {
		this.sources = sources;
	}


}
