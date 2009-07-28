package org.mycore.importer.mapping.mapper;

import org.jdom.Element;
import org.mycore.importer.MCRImportRecord;
import org.mycore.importer.mapping.MCRImportObject;
import org.mycore.importer.mapping.resolver.metadata.MCRImportMetadataResolver;

/**
 * 
 * @author Matthias Eichner
 */
public class MCRImportMetadataMapper extends MCRImportAbstractMapper {

    public void map(MCRImportObject importObject, MCRImportRecord record, Element map) {
        super.map(importObject, record, map);
        MCRImportMetadataResolver resolver = createResolverInstance();

        if(resolver != null) {
            Element metadataChild = resolver.resolve(map, fields);
            if(metadataChild != null)
                importObject.addMetadataChild(metadataChild);
        }
    }

    public String getType() {
        return "metadata";
    }
}