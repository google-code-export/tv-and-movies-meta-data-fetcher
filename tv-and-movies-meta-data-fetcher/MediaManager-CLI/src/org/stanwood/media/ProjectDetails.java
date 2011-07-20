package org.stanwood.media;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.Version;

/**
 * Used to get the information about the project
 */
public class ProjectDetails {

	private static final String APP_URL = "http://code.google.com/p/tv-and-movies-meta-data-fetcher/"; //$NON-NLS-1$

	private String versionFileContents;

	/**
	 * The constructor
	 * @throws IOException Thrown if thier is a problem reading the version informatiom
	 */
	public ProjectDetails() throws IOException {
		versionFileContents = FileHelper.readFileContents(Controller.class.getResourceAsStream("VERSION")).trim(); //$NON-NLS-1$
	}

	/**
	 * Returns the version of the project
	 * @return The version of the project
	 */
	public Version getVersion() {
		return new Version(versionFileContents.substring(versionFileContents.lastIndexOf(' ')+1));
	}

	/**
	 * Returns the title of the project
	 * @return the title of the project
	 */
	public String getTitle()  {
		return versionFileContents.substring(0,versionFileContents.lastIndexOf(' '));
	}

	/**
	 * Used to get the copyright message
	 * @return the copyright message
	 */
	public String getCopyright() {
		StringBuilder result = new StringBuilder();
		result.append("Copyright (C) 2011 John-Paul Stanford <dev@stanwood.org.uk>"+FileHelper.LS); //$NON-NLS-1$
		result.append("License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>."+FileHelper.LS); //$NON-NLS-1$
		result.append("This is free software: you are free to change and redistribute it."+FileHelper.LS); //$NON-NLS-1$
		result.append("There is NO WARRANTY"); //$NON-NLS-1$
		return result.toString();
	}

	/**
	 * Get a list of authors
	 * @return The authors
	 */
	public List<Author> getAuthors() {
		List<Author>authors = new ArrayList<Author>();
		authors.add(new Author("John-Paul Stanford","dev@stanwood.org.uk",Messages.getString("ProjectDetails.ROLE_LEAD_DEV_AND_CREATOR"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return authors;
	}

	/**
	 * Used to get the application URL
	 * @return The URL of the applications website
	 */
	public String getProjectURL() {
		return APP_URL;
	}

	/**
	 * Used to get the project description
	 * @return The project description
	 */
	public String getDescription() {
		return MessageFormat.format(Messages.getString("ProjectDetails.DESCRIPTION"),getTitle()); //$NON-NLS-1$
	}
}
