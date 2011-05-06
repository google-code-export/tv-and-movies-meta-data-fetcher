package org.stanwood.media.actions;

import java.io.File;

public interface IActionEventHandler {

	public void sendEventNewFile(File file) throws ActionException;

	public void sendEventDeletedFile(File file) throws ActionException;

	public void sendEventRenamedFile(File oldName,File newName) throws ActionException;
}
