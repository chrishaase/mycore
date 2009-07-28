package org.mycore.importer.mapping.datamodel;

import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

public class MCRImportDatamodel1 extends MCRImportAbstractDatamodel {

    private static final Logger LOGGER = Logger.getLogger(MCRImportDatamodel1.class);

    public MCRImportDatamodel1(Document datamodel) {
        super(datamodel);
    }

    public String getEnclosingName(String metadataName) {
        Element metadataChild = findMetadataChild(metadataName);
        return metadataChild.getParentElement().getAttributeValue("name");
    }

    public String getClassname(String metadataName) {
        Element metadataChild = findMetadataChild(metadataName);
        return metadataChild.getAttributeValue("class");
    }

    /**
     * Datamodel1 doesnt support not inherit, so null
     * will be returned
     */
    public Boolean isNotinherit(String metadataName) {
        return null;
    }

    /**
     * Datamodel1 doesnt support not Heritable, so null
     * will be returned
     */
    public Boolean isHeritable(String metadataName) {
        return null;
    }

    public String getClassName(String metadataName) {
        Element metadataElement = findMetadataChild(metadataName);
        if(metadataElement == null) {
            LOGGER.error("Couldnt find metadata definition " + metadataName + " in datamodel " + datamodel.getBaseURI());
            return null;
        }
        return metadataElement.getAttributeValue("class");
    }

    protected boolean isCached(String metadataName) {
        Element cachedMetadaElement = getCachedMetadataElement();
        if( metadataName != null && !metadataName.equals("") &&
            cachedMetadaElement != null &&
            metadataName.equals(getCachedMetadataElement().getAttribute("name")))
            return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    protected Element findMetadataChild(String metadataName) {
        if (isCached(metadataName))
            return getCachedMetadataElement();
        Element rootElement = datamodel.getRootElement();
        Element metadataElement = rootElement.getChild("metadata");
        List<Element> metadataElements = metadataElement.getChildren("element");
        // go through all elements
        for (Element element : metadataElements) {
            // the metadataName is defined in the child of a metadataElement
            Element metadataChildElement = (Element) element.getContent(new ElementFilter()).get(0);
            if (metadataName.equals(metadataChildElement.getAttributeValue("name"))) {
                // right child found -> cache & return it
                setCachedMetadataElement(metadataChildElement);
                return metadataChildElement;
            }
        }
        return null;
    }

}