package org.stanwood.media.actions;

import java.io.File;

import org.stanwood.media.renamer.MediaDirectory;

public interface IAction {

	public File perform(MediaDirectory dir, File file) throws ActionException;

	void setParameter(String key, String value) throws ActionException;
}
