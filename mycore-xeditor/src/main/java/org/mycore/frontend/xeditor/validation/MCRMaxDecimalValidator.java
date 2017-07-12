/*
* This file is part of *** M y C o R e ***
* See http://www.mycore.de/ for details.
*
* This program is free software; you can use it, redistribute it
* and / or modify it under the terms of the GNU General Public License
* (GPL) as published by the Free Software Foundation; either version 2
* of the License or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program, in a file called gpl.txt or license.txt.
* If not, write to the Free Software Foundation Inc.,
* 59 Temple Place - Suite 330, Boston, MA 02111-1307 USA
*/

package org.mycore.frontend.xeditor.validation;

/**
 * Validates input to be a decimal number and not bigger than a given value. 
 * The number format is specified by a given locale ID.
 * Example: &lt;xed:validate type="decimal" locale="de" max="1.999,99"... /&gt;
 *  * 
 * @author Frank L\u00FCtzenkirchen
 */
public class MCRMaxDecimalValidator extends MCRDecimalValidator {

    private static final String ATTR_MAX = "max";

    private double max;

    @Override
    public boolean hasRequiredAttributes() {
        return super.hasRequiredAttributes() && hasAttributeValue(ATTR_MAX);
    }

    @Override
    public void configure() {
        super.configure();
        max = converter.string2double(getAttributeValue(ATTR_MAX));
    }

    @Override
    protected boolean isValid(String value) {
        Double d = converter.string2double(value);
        if (d == null) {
            return false;
        } else {
            return d.doubleValue() <= max;
        }
    }
}