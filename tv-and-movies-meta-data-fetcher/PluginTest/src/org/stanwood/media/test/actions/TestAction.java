package org.stanwood.media.test.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.actions.IActionEventHandler;
import org.stanwood.media.extensions.ParameterType;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;

public class TestAction implements IAction {

	private static List<String>events = new ArrayList<String>();
	
	public void perform(MediaDirectory dir, File file,IActionEventHandler eventHandler) throws ActionException {
		events.add("perform()");		
	}

	@Override
	public void setParameter(String key, String value) throws ActionException {
		events.add("setParameter()");
	}

	public static List<String>getEvents() {
		return events;
	}

	@Override
	public void setTestMode(boolean testMode) {
		events.add("setTestMode()");
	}

	@Override
	public boolean isTestMode() {
		events.add("isTestMode()");
		return false;
	}

	@Override
	public void performOnDirectory(MediaDirectory dir, File file,
			IActionEventHandler actionEventHandler) {
		events.add("performOnDirectory()");	
	}

	@Override
	public void init(MediaDirectory dir) {
		events.add("init()");
	}

	@Override
	public void finished(MediaDirectory dir) {
		events.add("finished()");
	}
	
	@Override
	public void perform(MediaDirectory dir, IEpisode episode, File mediaFile,
			IActionEventHandler actionEventHandler) throws ActionException {
		perform(dir,mediaFile,actionEventHandler);
	}

	@Override
	public void perform(MediaDirectory dir, IFilm film, File mediaFile, Integer part,
			IActionEventHandler actionEventHandler) throws ActionException {
		perform(dir,mediaFile,actionEventHandler);
	}

}
