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
package org.stanwood.media.store.xmlstore;

import java.io.File;

import org.stanwood.media.model.IVideoFile;
import org.w3c.dom.Element;

public class XMLVideoFile implements IVideoFile {

	private Element fileNode;
	private File location;
	private File rootMediaDir;
	private File orgLocation;
	private Integer part;

	public XMLVideoFile(File rootMediaDir,Element fileNode) {
		this.fileNode = fileNode;
		this.rootMediaDir = rootMediaDir;
	}


	/** {@inheritDoc } */
	@Override
	public File getLocation() {
		if (this.location==null) {
			String location = (fileNode).getAttribute("location"); //$NON-NLS-1$
			this.location=new File(rootMediaDir,location);
		}
		return this.location;
	}

	/** {@inheritDoc } */
	@Override
	public File getOrginalLocation() {
		String originalLocation = fileNode.getAttribute("orginalLocation"); //$NON-NLS-1$
		if (this.orgLocation!=null) {
			if (!originalLocation.equals("")) { //$NON-NLS-1$
				orgLocation = new File(rootMediaDir,originalLocation);
			}
		}
		return orgLocation;
	}

	/** {@inheritDoc } */
	@Override
	public void setOrginalLocation(File orginalLocation) {
		fileNode.setAttribute("orginalLocation", orginalLocation.getAbsolutePath()); //$NON-NLS-1$
		this.orgLocation = orginalLocation;
	}

	/** {@inheritDoc } */
	@Override
	public void setLocation(File location) {
		fileNode.setAttribute("location", makePathRelativeToMediaDir(location,rootMediaDir)); //$NON-NLS-1$
		this.location = location;
	}

	private String makePathRelativeToMediaDir(File episodeFile, File rootMediaDir) {
		String path = rootMediaDir.getAbsolutePath();
		int len = path.length();
		if (episodeFile.getAbsolutePath().startsWith(path)) {
			return episodeFile.getAbsolutePath().substring(len+1);
		}
		else {
			return episodeFile.getAbsolutePath();
		}
	}

	/** {@inheritDoc } */
	@Override
	public Integer getPart() {
		if (part==null) {
			String strPart = fileNode.getAttribute("part"); //$NON-NLS-1$
			if (!strPart.equals("")) { //$NON-NLS-1$
				part = Integer.parseInt(strPart);
			}
		}
		return part;
	}

	/** {@inheritDoc } */
	@Override
	public void setPart(Integer part) {
		if (part!=null) {
			this.part = part;
			fileNode.setAttribute("part", String.valueOf(part)); //$NON-NLS-1$
		}
		else {
			this.part = null;
			fileNode.removeAttribute("part"); //$NON-NLS-1$
		}
	}

}
