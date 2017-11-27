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

package org.mycore.pi.doi;

import static org.junit.Assert.assertEquals;
import static org.mycore.pi.doi.MCRDigitalObjectIdentifier.TEST_DOI_PREFIX;

import java.util.Map;

import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;

/**
 * @author Thomas Scheffler (yagee)
 */
public class MCRMapObjectIDDOIGeneratorTest extends MCRTestCase {
    MCRMapObjectIDDOIGenerator doiGenerator;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        doiGenerator = new MCRMapObjectIDDOIGenerator("MapObjectIDDOI");
    }

    @Test
    public void generate() throws Exception {
        MCRObjectID junitTest00004711 = MCRObjectID.getInstance("junit_test_00004711");
        MCRObjectID myTest00000815 = MCRObjectID.getInstance("my_test_00000815");
        assertEquals(TEST_DOI_PREFIX + "/4711", doiGenerator.generate(junitTest00004711, null).asString());
        assertEquals(TEST_DOI_PREFIX + "/my.815", doiGenerator.generate(myTest00000815, null).asString());
    }

    @Test(expected = MCRPersistentIdentifierException.class)
    public void missingMappingTest() throws Exception {
        MCRObjectID brandNewTest00000001 = MCRObjectID.getInstance("brandNew_test_00000001");
        doiGenerator.generate(brandNewTest00000001, null);
    }

    @Override
    protected Map<String, String> getTestProperties() {
        Map<String, String> testProperties = super.getTestProperties();
        testProperties.put("MCR.Metadata.Type.test", Boolean.TRUE.toString());
        testProperties.put("MCR.PI.Generator.MapObjectIDDOI.Prefix.junit_test", TEST_DOI_PREFIX);
        testProperties.put("MCR.PI.Generator.MapObjectIDDOI.Prefix.my_test", TEST_DOI_PREFIX + "/my.");
        return testProperties;
    }
}
