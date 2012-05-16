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
package org.stanwood.media.store.mp4.atomicparsley;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4AtomKey;
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class is used to parse the output from the atomic parsley command.
 * The output is generated using the option --outputXML.
 */
public class AtomicParsleyOutputParser extends XMLParser {

	private final static Log log = LogFactory.getLog(AtomicParsleyOutputParser.class);
	private Document doc;

	/**
	 * The constructor
	 * @param output The output XML of atomic parsley
	 * @throws MP4Exception
	 */
	public AtomicParsleyOutputParser(String output) throws MP4Exception  {
		try {
			doc = XMLParser.strToDom(output);
		}
		catch (XMLParserException e) {
			throw new MP4Exception(Messages.getString("AtomicParsleyOutputParser.UnableParseAtomicParsleyOutput"),e); //$NON-NLS-1$
		}
	}

	/**
	 * Used to list the atoms parsed from the output
	 * @return List of atoms
	 * @throws MP4Exception
	 */
	public List<IAtom> listAtoms() throws  MP4Exception {
		try {
			List<IAtom> atoms = new ArrayList<IAtom>();
			for (Node node : selectNodeList(doc, "AtomicParsley/atoms/*")) { //$NON-NLS-1$
				IAtom atom = parseAtom((Element)node);
				if (atom!=null) {
					atoms.add(atom);
				}
			}

			return atoms;
		}
		catch (XMLParserException e) {
			throw new MP4Exception(Messages.getString("AtomicParsleyOutputParser.UnableParseAtomicParsleyOutput"),e); //$NON-NLS-1$
		}
	}

	private IAtom parseAtom(Element node) throws MP4Exception {
		String name = node.getAttribute("name"); //$NON-NLS-1$
		if (name.length()==0) {
			throw new MP4Exception(Messages.getString("AtomicParsleyOutputParser.UnableGetNameOfAtom")); //$NON-NLS-1$
		}
		String rDnsDomain = node.getAttribute("reverseDNSdomain"); //$NON-NLS-1$
		String rDnsName = node.getAttribute("reverseDNSname"); //$NON-NLS-1$
		MP4AtomKey key;
		if (rDnsDomain!=null && rDnsDomain.length()>0 &&
				rDnsName!=null && rDnsName.length()>0) {
			key= MP4AtomKey.fromRDNS(rDnsName, rDnsDomain);
		}
		else {
			key = MP4AtomKey.fromKey(name);
		}
		if (key==null) {
			log.warn(MessageFormat.format(Messages.getString("AtomicParsleyOutputParser.UnableFindAtom"),name,rDnsDomain,rDnsName)); //$NON-NLS-1$
			return null;
		}
		if (node.getNodeName().equals("atomString")) { //$NON-NLS-1$
			return new APAtomString(key, node.getTextContent());
		}
		else if (node.getNodeName().equals("atomRange")) { //$NON-NLS-1$
			short count = (short)parseIntAttribute(node, name,"count"); //$NON-NLS-1$
			short max = 0;
			if (node.hasAttribute("max")) { //$NON-NLS-1$
				max = (short)parseIntAttribute(node, name,"max"); //$NON-NLS-1$
			}
			return new APAtomRange(key, count, max);
		}
		else if (node.getNodeName().equals("atomNumber")) { //$NON-NLS-1$
			return new APAtomNumber(key,parseLongAttribute(node, name,"value")); //$NON-NLS-1$
		}
		else if (node.getNodeName().equals("atomBoolean")) { //$NON-NLS-1$
			String value = node.getAttribute("value"); //$NON-NLS-1$
			if (value.equals("1") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return new APAtomBoolean(key, true);
			}
			else {
				return new APAtomBoolean(key, false);
			}
		}
		else if (node.getNodeName().equals("atomArtwork")) { //$NON-NLS-1$
			return new APAtomArtworkSummary(key, 1);
		}
		throw new MP4Exception(MessageFormat.format(Messages.getString("AtomicParsleyOutputParser.UnsupportedAtomNode"),node.getNodeName())); //$NON-NLS-1$
	}

	protected int parseIntAttribute(Element node, String name,String attributeName)
			throws MP4Exception {
		String value = node.getAttribute(attributeName);
		if (value.length()==0) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("AtomicParsleyOutputParser.EmptyValue"),name)); //$NON-NLS-1$
		}
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("AtomicParsleyOutputParser.UnableParseValue"),value,name)); //$NON-NLS-1$
		}
	}

	protected long parseLongAttribute(Element node, String name,String attributeName)
			throws MP4Exception {
		String value = node.getAttribute(attributeName);
		if (value.length()==0) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("AtomicParsleyOutputParser.EmptyValue"),name)); //$NON-NLS-1$
		}
		try {
			return Long.parseLong(value);
		}
		catch (NumberFormatException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("AtomicParsleyOutputParser.UnableParseValue"),value,name)); //$NON-NLS-1$
		}
	}
}
