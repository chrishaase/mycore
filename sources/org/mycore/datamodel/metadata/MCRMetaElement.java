/**
 * $RCSfile$
 * $Revision$ $Date$
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 **/

package mycore.datamodel;

import java.util.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import mycore.common.MCRException;
import mycore.common.MCRUtils;

/**
 * This class is designed to to have a basic class for all metadata.
 * The class has inside a ArrayList that holds all metaddata elements
 * for one XML tag.
 *
 * @author Jens Kupferschmidt
 * @version $Revision$ $Date$
 **/
public class MCRMetaElement
{

// common data
private static String NL =
  new String((System.getProperties()).getProperty("line.separator"));
private static String DEFAULT_LANGUAGE = "de";
private String META_PACKAGE_NAME = "mycore.datamodel.";

// MetaElement data
private String lang = null;
private String classname = null;
private String tag = null;
private boolean heritable;
private ArrayList list = null;

/**
 * This is the constructor of the MCRMetaElement class. 
 * The default language for the element was set to <b>en</b>.
 **/
public MCRMetaElement()
  {
  lang = DEFAULT_LANGUAGE; 
  classname = "";
  tag = "";
  heritable = false;
  list = new ArrayList();
  }

/**
 * This is the constructor of the MCRMetaElement class. 
 * The default language for the element was set. If the default languge
 * is empty or false <b>de</b> was set.
 *
 * @param default_lang     the default language
 **/
public MCRMetaElement(String default_lang)
  {
  if ((default_lang == null) || 
    ((default_lang = default_lang.trim()).length() ==0)) {
    lang = DEFAULT_LANGUAGE; }
  else {
    lang = default_lang; }
  classname = "";
  tag = "";
  heritable = false;
  list = new ArrayList();
  }

/**
 * This methode return the name of this metadata class as string.
 *
 * @return the name of this metadata class as string
 **/
public final String getClassName()
  { return classname; }

/**
 * This methode return the instance of an element from the list with index i.
 *
 * @return the instance of an element, if index is out of range return null
 **/
public final MCRMetaInterface getElement(int index)
  { 
  if ((index<0) || (index>list.size())) { return null; }
  return (MCRMetaInterface)list.get(index); 
  }

/**
 * This methode return the heritable of this metadata as boolean value.
 *
 * @return the heritable of this metadata class
 **/
public final boolean getHeritable()
  { return heritable; }

/**
 * This methode return the default language of this metadata class as string.
 *
 * @return the default language of this metadata class as string
 **/
public final String getLang()
  { return lang; }

/**
 * This methode return the tag of this metadata class as string.
 *
 * @return the tag of this metadata class as string
 **/
public final String getTag()
  { return tag; }

/**
 * This methode set the heritable for the metadata class.
 *
 * @param heritable            the heritable as boolean value
 */
public void setHeritable(boolean heritable)
  {
  this.heritable = false;
  if (heritable) { this.heritable = heritable; return; }
  }

/**
 * This methode set the heritable for the metadata class.
 *
 * @param heritable            the heritable as string
 */
public void setHeritable(String heritable)
  {
  this.heritable = false;
  if ((heritable == null) || ((heritable = heritable.trim()).length() ==0))
    { return; }
  if (heritable.equals("true")) { this.heritable = true; }
  }

/**
 * This methode set the tag for the metadata class.
 *
 * @param tag                   the tag for the metadata class
 */
public void setTag(String tag)
  {
  if ((tag == null) || ((tag = tag.trim()).length() ==0)) { return; }
  this.tag = tag;
  }

/**
 * This methode set the element class name for the metadata elements.
 *
 * @param classname             the class name for the metadata elements
 */
public void setClassName(String classname)
  {
  if ((classname == null) || ((classname = classname.trim()).length() ==0)) {
    return; }
  this.classname = classname;
  }

/**
 * This methode read the XML input stream part from a DOM part for the
 * metadata of the document.
 *
 * @param metadata_element_node     a relevant DOM element for the metadata
 * @exception MCRException if the class can't loaded
 **/
public final void setFromDOM(Node metadata_element_node) throws MCRException
  {
  tag = metadata_element_node.getNodeName(); 
  classname = ((Element)metadata_element_node).getAttribute("class");
  String fullname = META_PACKAGE_NAME+classname;
  setHeritable((String)((Element)metadata_element_node)
    .getAttribute("heritable"));
  NodeList metadata_elements = metadata_element_node.getChildNodes();
  int len = metadata_elements.getLength();
  for (int i=0;i<len;i++) {
    Node metadata_item = metadata_elements.item(i);
    if (metadata_item.getNodeType() != Node.ELEMENT_NODE) { continue; }
    Object obj = new Object();
    try {
      obj = Class.forName(fullname).newInstance();
      ((MCRMetaInterface)obj).setLang(lang);
      ((MCRMetaInterface)obj).setFromDOM(metadata_item);
      }
    catch (ClassNotFoundException e) {
      throw new MCRException(classname+" ClassNotFoundException"); }
    catch (IllegalAccessException e) {
      throw new MCRException(classname+" IllegalAccessException"); }
    catch (InstantiationException e) {
      throw new MCRException(classname+" InstantiationException"); }
    list.add(obj);
    }
  }

/**
 * This methode create a XML stream for all data in this class, defined
 * by the MyCoRe XML MCRLangText definition for the given subtag.
 *
 * @exception MCRException if the content of this class is not valid
 * @return a JDOM Element with the XML Element part
 **/
public final org.jdom.Element createXML() throws MCRException
  {
  if (!isValid()) {
    throw new MCRException("MCRMetaElement : The content is not valid."); }
  org.jdom.Element elm = new org.jdom.Element(tag);
  elm.setAttribute("class",classname);
  elm.setAttribute("heritable",new Boolean(heritable).toString());
  for (int i=0;i<list.size();i++) {
    elm.addContent(((MCRMetaInterface)list.get(i)).createXML()); }
  return elm;
  }

/**
 * This methode create a typed content list for all data in this instance.
 *
 * @param parametric true if the data should parametric searchable
 * @param textsearch true if the data should text searchable
 * @exception MCRException if the content of this class is not valid
 * @return a MCRTypedContent with the data of the MCRObject data
 **/
public final MCRTypedContent createTypedContent() throws MCRException
  {
  if (!isValid()) {
    throw new MCRException("MCRMetaElement : The content is not valid."); }
  MCRTypedContent tc = new MCRTypedContent();
  tc.addTagElement(tc.TYPE_TAG,tag);
  tc.addStringElement(tc.TYPE_ATTRIBUTE,"class",classname,true,false);
  for (int i=0;i<list.size();i++) {
    tc.addMCRTypedContent(((MCRMetaInterface)list.get(i))
      .createTypedContent(true,true)); }
  return tc;
  }

/**
 * This methode check the validation of the content of this class.
 * The methode returns <em>true</em> if
 * <ul>
 * <li> the classname is not null or empty
 * <li> the tag is not null or empty
 * <li> if the list is empty
 * <li> the lang value was supported
 * </ul>
 * otherwise the methode return <em>false</em>
 *
 * @return a boolean value
 **/
public final boolean isValid()
  {
  if ((classname == null) || ((classname = classname.trim()).length() ==0)) {
    return false; }
  if (list.size() == 0) { return false; }
  if ((tag == null) || ((tag = tag.trim()).length() ==0)) {
    return false; }
  if (!MCRUtils.isSupportedLang(lang)) { return false; }
  return true;
  }

/**
 * This methode print all elements of the metadata class.
 **/
public final void debug()
  {
  System.out.println("MCRMetaElement debug start:");
  System.out.println("<classname>"+classname+"</classname>");
  System.out.println("<lang>"+lang+"</lang>");
  System.out.println("<tag>"+tag+"</tag>");
  System.out.println("<heritable>"+heritable+"</heritable>");
  for (int i=0;i<list.size();i++) {
    ((MCRMetaInterface)list.get(i)).debug(); }
  System.out.println("MCRMetaElement debug end"+NL);
  }

}

