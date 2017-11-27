/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.common.xml;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.common.content.transformer.MCRFopper;
import org.mycore.common.content.transformer.MCRIdentityTransformer;
import org.mycore.common.content.transformer.MCRTransformerPipe;
import org.mycore.common.content.transformer.MCRXSLTransformer;

import com.google.common.collect.Lists;

/**
 * This class acts as a {@link MCRContentTransformer} factory for {@link MCRLayoutService}.
 * @author Thomas Scheffler (yagee)
 *
 */
public class MCRLayoutTransformerFactory {
    /** Map of transformer instances by ID */
    private static HashMap<String, MCRContentTransformer> transformers = new HashMap<>();

    private static Logger LOGGER = LogManager.getLogger(MCRLayoutTransformerFactory.class);

    private static MCRFopper fopper = new MCRFopper();

    private static final MCRIdentityTransformer NOOP_TRANSFORMER = new MCRIdentityTransformer("text/xml", "xml");

    /** 
     * Returns the transformer with the given ID. If the transformer is not instantiated yet,
     * it is created and initialized.
     */
    public static MCRContentTransformer getTransformer(String id) throws Exception {
        MCRContentTransformer transformer = transformers.get(id);
        if (transformer != null) {
            return transformer;
        }
        //try to get configured transformer
        transformer = MCRContentTransformerFactory.getTransformer(id.replaceAll("-default$", ""));
        if (transformer != null) {
            transformers.put(id, transformer);
            return transformer;
        }
        return buildLayoutTransformer(id);
    }

    private static MCRContentTransformer buildLayoutTransformer(String id) throws Exception {
        String idStripped = id.replaceAll("-default$", "");
        LOGGER.info("Configure property MCR.ContentTransformer.{}.Class if you do not want to use default behaviour.",
            idStripped);
        String stylesheet = getResourceName(id);
        if (stylesheet == null) {
            LOGGER.info("Using noop transformer for {}", idStripped);
            return NOOP_TRANSFORMER;
        }
        String[] stylesheets = getStylesheets(idStripped, stylesheet);
        MCRContentTransformer transformer = MCRXSLTransformer.getInstance(stylesheets);
        if ("application/pdf".equals(transformer.getMimeType())) {
            transformer = new MCRTransformerPipe(transformer, fopper);
            LOGGER.info("Using stylesheet '{}' for {} and MCRFopper for PDF output.", Lists.newArrayList(stylesheets),
                idStripped);
        } else {
            LOGGER.info("Using stylesheet '{}' for {}", Lists.newArrayList(stylesheets), idStripped);
        }
        transformers.put(id, transformer);
        return transformer;
    }

    @SuppressWarnings("unchecked")
    private static String[] getStylesheets(String id, String stylesheet) {
        List<String> ignore = MCRConfiguration.instance().getStrings("MCR.LayoutTransformerFactory.Default.Ignore",
            Collections.emptyList());
        List<String> defaults;
        if (ignore.contains(id)) {
            defaults = Collections.emptyList();
        } else {
            defaults = MCRConfiguration.instance().getStrings("MCR.LayoutTransformerFactory.Default.Stylesheets",
                (List<String>) Collections.EMPTY_LIST);
        }
        String[] stylesheets = new String[1 + defaults.size()];
        stylesheets[0] = stylesheet;
        for (int i = 0; i < defaults.size(); i++) {
            stylesheets[i + 1] = defaults.get(i);
        }
        return stylesheets;
    }

    /**
     * Builds the filename of the stylesheet to use, e. g. "playlist-simple.xsl"
     */
    private static String buildStylesheetName(String id) {
        return MessageFormat.format("xsl/{0}.xsl", id.replaceAll("-default$", ""));
    }

    private static String getResourceName(String id) {
        LOGGER.debug("MCRLayoutService using style {}", id);

        String styleName = buildStylesheetName(id);
        try {
            if (MCRXMLResource.instance().exists(styleName, MCRLayoutTransformerFactory.class.getClassLoader())) {
                return styleName;
            }
        } catch (Exception e) {
            throw new MCRException("Error while loading stylesheet: " + styleName, e);
        }

        // If no stylesheet exists, forward raw xml instead
        // You can transform raw xml code by providing a stylesheed named
        // [doctype]-xml.xsl now
        if (id.endsWith("-xml") || id.endsWith("-default")) {
            LOGGER.warn("XSL stylesheet not found: {}", styleName);
            return null;
        }
        throw new MCRException("XSL stylesheet not found: " + styleName);
    }

}
