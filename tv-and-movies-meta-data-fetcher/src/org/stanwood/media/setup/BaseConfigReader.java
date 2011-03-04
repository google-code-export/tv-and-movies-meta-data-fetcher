package org.stanwood.media.setup;

import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A base class for configuration readers
 */
public abstract class BaseConfigReader extends XMLParser implements IConfigReader {

	protected List<SourceConfig> readSources(Node configNode) throws XMLParserException {
		List<SourceConfig> sources = new ArrayList<SourceConfig>();
		for (Node sourceElement : selectNodeList(configNode, "sources/source")) {
			SourceConfig source = new SourceConfig();
			source.setID(((Element)sourceElement).getAttribute("id"));
			for (Node paramNode : selectNodeList(sourceElement, "param")) {
				String name = ((Element)paramNode).getAttribute("name");
				String value = ((Element)paramNode).getAttribute("value");
				source.addParam(name, value);
			}

			sources.add(source);
		}
		return sources;
	}

	protected List<StoreConfig>readStores(Node configNode) throws XMLParserException {
		List<StoreConfig>stores = new ArrayList<StoreConfig>();
		for (Node storeElement : selectNodeList(configNode, "stores/store")) {
			StoreConfig store = new StoreConfig();
			store.setID(((Element)storeElement).getAttribute("id"));

			for (Node paramNode : selectNodeList(storeElement, "param")) {
				String name = ((Element)paramNode).getAttribute("name");
				String value = ((Element)paramNode).getAttribute("value");
				store.addParam(name, value);
			}

			stores.add(store);
		}
		return stores;
	}

}
