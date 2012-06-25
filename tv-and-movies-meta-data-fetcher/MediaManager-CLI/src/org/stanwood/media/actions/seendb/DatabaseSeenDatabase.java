/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.actions.seendb;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.stanwood.media.database.DBHelper;
import org.stanwood.media.database.DatabaseException;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.setup.DBResource;

public class DatabaseSeenDatabase implements ISeenDatabase{

	private Session session;

	public DatabaseSeenDatabase(DBResource resource) throws SeenDBException {
		try {
			this.session = DBHelper.getInstance().getSession(resource);
		} catch (DatabaseException e) {
			throw new SeenDBException("Unable to create database session",e);
		}
	}

	/** {@inheritDoc} */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean isSeen(File mediaDirectory, File file) throws SeenDBException {
		Transaction trans = session.beginTransaction();
		try {
			Query q = session.createQuery(" from SeenEntry where fileName = :path and lastModified = :modified"); //$NON-NLS-1$
			q.setLong("modified", file.lastModified()); //$NON-NLS-1$
			q.setString("path",file.getAbsolutePath()); //$NON-NLS-1$
			List result = q.list();
			if (result.size()>0) {
				return true;
			}
			return false;
		}
		finally {
			trans.commit();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void markAsSeen(File mediaDirectory, File file) throws SeenDBException {
		Transaction trans = session.beginTransaction();
		long lastModified = file.lastModified();
		String path = file.getAbsolutePath();
		markAsSeen(mediaDirectory, lastModified, path);
		trans.commit();
	}

	@SuppressWarnings("rawtypes")
	private void markAsSeen(File mediaDirectory,  long lastModified, String path) {
		Query q = session.createQuery(" from SeenEntry where fileName = :path"); //$NON-NLS-1$
		q.setString("path",path); //$NON-NLS-1$
		List result = q.list();
		SeenEntry entry;
		if (result.size()==0) {
			entry = new SeenEntry();
			entry.setFileName(path);
		}
		else {
			entry = (SeenEntry) result.get(0);
		}
		entry.setLastModified(lastModified);
		session.saveOrUpdate(entry);
	}

	/** {@inheritDoc} */
	@Override
	public void write(IProgressMonitor parentMonitor) throws SeenDBException {
		session.flush();
	}

	/** {@inheritDoc} */
	@Override
	public void read(IProgressMonitor progress) throws SeenDBException {

	}

	/** {@inheritDoc} */
	@Override
	public void renamedFile(File mediaDirectory, File oldFile, File newFile)
			throws SeenDBException {
		Transaction trans = session.beginTransaction();
		Query q = session.createQuery("update SeenEntry set fileName = :newFile where fileName = :oldFile"); //$NON-NLS-1$
		q.setString("newFile",oldFile.getAbsolutePath()); //$NON-NLS-1$
		q.setString("oldFile",newFile.getAbsolutePath()); //$NON-NLS-1$
		q.executeUpdate();
		trans.commit();
	}

	/** {@inheritDoc} */
	@Override
	public void removeFile(File mediaDirectory, File file)
			throws SeenDBException {
		Transaction trans = session.beginTransaction();
		Query q = session.createQuery("delete from SeenEntry where fileName = :path"); //$NON-NLS-1$
		q.setString("path",file.getAbsolutePath()); //$NON-NLS-1$
		q.executeUpdate();
		trans.commit();
	}

	@Override
	public Collection<SeenEntry> getEntries() {
		Transaction trans = session.beginTransaction();
		Query q = session.createQuery("from SeenEntry"); //$NON-NLS-1$
		return q.list();
	}

}
