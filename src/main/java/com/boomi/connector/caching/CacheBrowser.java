package com.boomi.connector.caching;

import java.net.URL;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.boomi.connector.api.ConnectorException;
import com.boomi.connector.api.ObjectDefinition;
import com.boomi.connector.api.ObjectDefinitionRole;
import com.boomi.connector.api.ObjectDefinitions;
import com.boomi.connector.api.ObjectType;
import com.boomi.connector.api.ObjectTypes;
import com.boomi.connector.util.BaseBrowser;
import com.boomi.proserv.caching.CacheUtils;

public class CacheBrowser extends BaseBrowser {

	private static final String TYPE_ELEMENT = "type";


	protected CacheBrowser(CacheConnection conn) {
		super(conn);
	}

	@Override
	public ObjectTypes getObjectTypes() {
		try {
			URL url = this.getClass().getClassLoader().getResource("metadata.xml");
			Document typeDoc = CacheUtils.parse(url.openStream());
			NodeList typeList = typeDoc.getElementsByTagName(TYPE_ELEMENT);
			ObjectTypes types = new ObjectTypes();
			for (int i = 0; i < typeList.getLength(); ++i) {
				Element typeEl = (Element) typeList.item(i);
				String typeName = typeEl.getTextContent().trim();
				ObjectType type = new ObjectType();
				type.setId(typeName);
				types.getTypes().add(type);
			}
			return types;
		} catch (Exception e) {
			throw new ConnectorException(e);
		}
	}

	@Override
	public ObjectDefinitions getObjectDefinitions(String objectTypeId,
			Collection<ObjectDefinitionRole> roles) {
		try {
			URL url = this.getClass().getClassLoader().getResource(objectTypeId.toLowerCase() + ".xsd");
            Document defDoc = CacheUtils.parse(url.openStream());
            ObjectDefinitions defs = new ObjectDefinitions();
            ObjectDefinition def = new ObjectDefinition();
            def.setSchema(defDoc.getDocumentElement());
            def.setElementName(objectTypeId);
            defs.getDefinitions().add(def);

            return defs;

        }
        catch (Exception e) {
            throw new ConnectorException(e);
        }
	}

	@Override
	public CacheConnection getConnection() {
		return (CacheConnection) super.getConnection();
	}

	public static void main(String[] args) {
		ObjectTypes o = new CacheBrowser(null).getObjectTypes();
		System.out.println(o);
	}
}