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

package org.mycore.common.inject;

import org.mycore.common.events.MCRStartupHandler;
import org.mycore.common.processing.MCRProcessableRegistry;
import org.mycore.common.processing.impl.MCRCentralProcessableRegistry;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

/**
 * Default module to bind mycore guice dependencies.
 * 
 * @author Matthias Eichner
 */
public class MCRDefaultModule extends AbstractModule {

    @Override
    protected void configure() {

        // PROCESSING API
        bind(MCRProcessableRegistry.class).to(MCRCentralProcessableRegistry.class);
        if (MCRStartupHandler.isWebApp()) {
            install(new ServletModule());
        }
    }

}
