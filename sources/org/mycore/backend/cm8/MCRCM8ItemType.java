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

package mycore.cm8;

import java.io.*;
import java.util.*;
import com.ibm.mm.sdk.server.*;
import com.ibm.mm.sdk.common.*;
import mycore.common.MCRConfiguration;
import mycore.common.MCRConfigurationException;
import mycore.common.MCRException;
import mycore.common.MCRPersistenceException;
import mycore.datamodel.MCRObject;
import mycore.datamodel.MCRObjectID;
import mycore.datamodel.MCRTypedContent;
import mycore.datamodel.MCRMetaDefault;
import mycore.cm8.MCRCM8ConnectionPool;
import mycore.cm8.MCRCM8MetaInterface;

/**
 * This class implements all methode for handling the ItemType for a
 * MCRObjectID type on IBM Content Manager 8.
 *
 * @author Jens Kupferschmidt
 *
 * @version $Revision$ $Date$
 **/
public final class MCRCM8ItemType implements DKConstantICM
{

// internal data
private static final String META_PACKAGE_NAME = "mycore.cm8.";

// from configuration

/**
 * The constructor of this class.
 **/
public MCRCM8ItemType()
  {
  }

/**
 * The methode create a new datastore based of given configuration.
 *
 * @param mcr_type    the MCRObjectID type as string
 * @param mcr_conf    the configuration XML stream as JDOM tree
 * @exception MCRConfigurationException if the configuration is not correct
 * @exception MCRPersistenceException if a persistence problem is occured
 **/
protected static void create(String mcr_type, org.jdom.Document mcr_conf)
  throws MCRConfigurationException, MCRPersistenceException
  {
  try {
  // read the configuration
  String sb = new String("MCR.persistence_cm8_"+mcr_type);
  String mcr_item_type_name = MCRConfiguration.instance().getString(sb); 
  String mcr_item_type_prefix = MCRConfiguration.instance()
    .getString(sb+"_prefix"); 
  // connect to server
  DKDatastoreICM connection = null;
  try {
    connection = MCRCM8ConnectionPool.getConnection();
    System.out.println("Info CM8 Datastore Creation: connected.");
    // create the Attribute for MCRObjectID
    if (!createAttributeVarChar(connection,mcr_item_type_prefix+"ID",
      MCRObjectID.MAX_LENGTH,false)) {
      System.out.println("Warning CM8 Datastore Creation: attribute "+
        mcr_item_type_prefix+"ID already exists."); }
    // create the Attribute for MCR_Label
    if (!createAttributeVarChar(connection,mcr_item_type_prefix+"label",
      MCRObject.MAX_LABEL_LENGTH,false)) {
      System.out.println("Warning CM8 Datastore Creation: attribute "+
        mcr_item_type_prefix+"label already exists."); }
    // create the Attribut for the XML byte array
    if (!createAttributeBlob(connection,mcr_item_type_prefix+"xml",
      100*1024,false)) {
      System.out.println("Warning CM8 Datastore Creation: attribute "+
        mcr_item_type_prefix+"xml already exists."); }
    // create the Attribut for the XML byte array
    if (!createAttributeClob(connection,mcr_item_type_prefix+"ts",
      100*1024,true)) {
      System.out.println("Warning CM8 Datastore Creation: attribute "+
        mcr_item_type_prefix+"ts already exists."); }
    // create the default attribute type
    if (!createAttributeVarChar(connection,mcr_item_type_prefix+"type",
      MCRMetaDefault.DEFAULT_TYPE_LENGTH,false)) {
      System.out.println("Warning CM8 Datastore Creation: attribute "+
        mcr_item_type_prefix+"type already exists."); }
    // create the default attribute lang
    if (!createAttributeVarChar(connection,mcr_item_type_prefix+"lang",
      MCRMetaDefault.DEFAULT_LANG_LENGTH,false)) {
      System.out.println("Warning CM8 Datastore Creation: attribute "+
        mcr_item_type_prefix+"lang already exists."); }
    // check for the root itemtype
    try {
      DKDatastoreDefICM dsDefICM = new DKDatastoreDefICM(connection);
      DKItemTypeDefICM isitemTypeDef = (DKItemTypeDefICM) dsDefICM
        .retrieveEntity(mcr_item_type_name);
      if (isitemTypeDef != null) {
        System.out.println("Warning CM8 Datastore Creation: itemtype "
          +mcr_item_type_name+" exist.");
        return; }
      }
    catch (DKException e) { 
      System.out.println("Warning CM8 Datastore Creation: itemtype "
        +mcr_item_type_name+" exist.");
      return; }

    // create the root itemtype
    DKItemTypeDefICM item_type = new DKItemTypeDefICM(connection);
    System.out.println("Info CM8 Datastore Creation: "+mcr_item_type_name);
    item_type.setName(mcr_item_type_name);
    item_type.setDescription(mcr_item_type_name);
    item_type.setClassification(DK_ICM_ITEMTYPE_CLASS_DOC_MODEL);
    item_type.setDeleteRule(DK_ICM_DELETE_RULE_CASCADE);
    DKDatastoreDefICM dsDefICM = new DKDatastoreDefICM(connection);
    DKAttrDefICM attr = (DKAttrDefICM) dsDefICM.retrieveAttr(
      mcr_item_type_prefix+"ID");
    attr.setNullable(false);
    attr.setUnique(true);
    item_type.addAttr(attr);
    attr = (DKAttrDefICM) dsDefICM.retrieveAttr(mcr_item_type_prefix+"label");
    attr.setNullable(false);
    attr.setUnique(false);
    item_type.addAttr(attr);
    attr = (DKAttrDefICM) dsDefICM.retrieveAttr(mcr_item_type_prefix+"xml");
    attr.setNullable(false);
    attr.setUnique(false);
    item_type.addAttr(attr);
    attr = (DKAttrDefICM) dsDefICM.retrieveAttr(mcr_item_type_prefix+"ts");
    attr.setNullable(false);
    attr.setUnique(false);
//    attr.setTextSearchable(true);
    item_type.addAttr(attr);

    // get the configuration JDOM root element
    org.jdom.Element mcr_root = mcr_conf.getRootElement();

    // set config element offset to metadata
    org.jdom.Element mcr_metadata = mcr_root.getChild("metadata");
    DKComponentTypeDefICM item_metadata = new DKComponentTypeDefICM(connection);
    item_metadata.setName(mcr_item_type_prefix+"metadata");
    item_metadata.setDeleteRule(DK_ICM_DELETE_RULE_CASCADE);
    attr = (DKAttrDefICM) dsDefICM.retrieveAttr(mcr_item_type_prefix+"lang");
    attr.setNullable(true);
    attr.setUnique(false);
    item_metadata.addAttr(attr);

    // over all elements
    List mcr_taglist = mcr_metadata.getChildren();
    for (int i=0;i<mcr_taglist.size();i++) {
      // the tag
      org.jdom.Element mcr_tag = (org.jdom.Element)mcr_taglist.get(i);
      String tagname = (String)mcr_tag.getAttribute("name").getValue();
      String parasearch = (String)mcr_tag.getAttribute("parasearch")
        .getValue();
      if (parasearch == null) { parasearch = "true"; }
      if (!parasearch.toLowerCase().equals("true")) { continue; }
      String textsearch = (String)mcr_tag.getAttribute("textsearch")
        .getValue();
      if (textsearch == null) { textsearch = "false"; }
      DKComponentTypeDefICM item_tag = new DKComponentTypeDefICM(connection);
      item_tag.setName(mcr_item_type_prefix+tagname);
      item_tag.setDeleteRule(DK_ICM_DELETE_RULE_CASCADE);
      // add lang attribute to tag
      attr = (DKAttrDefICM) dsDefICM.retrieveAttr(mcr_item_type_prefix+"lang");
      attr.setNullable(true);
      attr.setUnique(false);
      item_tag.addAttr(attr);
      // over all mcrmeta...
      List mcr_subtaglist = mcr_tag.getChildren();
      for (int j=0;j<mcr_subtaglist.size();j++) {
        org.jdom.Element mcr_subtag = (org.jdom.Element)mcr_subtaglist.get(j);
        String subtagname = mcr_subtag.getName();
        if (subtagname.length()<=7) { continue; }
        if (!subtagname.substring(0,7).equals("mcrmeta")) { continue; }
        String classname = (String)mcr_subtag.getAttribute("class").getValue();
        StringBuffer stb = new StringBuffer(128);
        stb.append(META_PACKAGE_NAME).append("MCRCM8").append(classname.
          substring(3,classname.length()));
        System.out.println("Info CM8 Datastore Creation: "+tagname+
          " with class "+stb.toString());
        Object obj = new Object();
        try {
          obj = Class.forName(stb.toString()).newInstance();
          DKComponentTypeDefICM item_subtag = ((MCRCM8MetaInterface)obj).
            createItemType(mcr_subtag,connection,dsDefICM,mcr_item_type_prefix);
          item_tag.addSubEntity(item_subtag);
          }
        catch (ClassNotFoundException e) {
          throw new MCRException(classname+" ClassNotFoundException"); }
        catch (IllegalAccessException e) {
          throw new MCRException(classname+" IllegalAccessException"); }
        catch (InstantiationException e) {
          throw new MCRException(classname+" InstantiationException"); }
        }
      item_metadata.addSubEntity(item_tag);
      }

    item_type.addSubEntity(item_metadata);

    // the service part
    org.jdom.Element mcr_service = mcr_root.getChild("service");
    DKComponentTypeDefICM item_service = new DKComponentTypeDefICM(connection);
    item_service.setName(mcr_item_type_prefix+"service");
    item_service.setDeleteRule(DK_ICM_DELETE_RULE_CASCADE);

    // over all elements
    mcr_taglist = mcr_service.getChildren();
    for (int i=0;i<mcr_taglist.size();i++) {
      // the tag
      org.jdom.Element mcr_tag = (org.jdom.Element)mcr_taglist.get(i);
      String tagname = (String)mcr_tag.getAttribute("name").getValue();
      String parasearch = (String)mcr_tag.getAttribute("parasearch")
        .getValue();
      if (parasearch == null) { parasearch = "true"; }
      if (!parasearch.toLowerCase().equals("true")) { continue; }
      String textsearch = (String)mcr_tag.getAttribute("textsearch")
        .getValue();
      if (textsearch == null) { textsearch = "false"; }
      DKComponentTypeDefICM item_tag = new DKComponentTypeDefICM(connection);
      item_tag.setName(mcr_item_type_prefix+tagname);
      item_tag.setDeleteRule(DK_ICM_DELETE_RULE_CASCADE);
      // add lang attribute to the tag
      attr = (DKAttrDefICM) dsDefICM.retrieveAttr(mcr_item_type_prefix+"lang");
      attr.setNullable(true);
      attr.setUnique(false);
      item_tag.addAttr(attr);
      // over all mcrmeta...
      List mcr_subtaglist = mcr_tag.getChildren();
      for (int j=0;j<mcr_subtaglist.size();j++) {
        org.jdom.Element mcr_subtag = (org.jdom.Element)mcr_subtaglist.get(j);
        String subtagname = mcr_subtag.getName();
        if (subtagname.length()<=7) { continue; }
        if (!subtagname.substring(0,7).equals("mcrmeta")) { continue; }
        String classname = (String)mcr_subtag.getAttribute("class").getValue();
        StringBuffer stb = new StringBuffer(128);
        stb.append(META_PACKAGE_NAME).append("MCRCM8").append(classname.
          substring(3,classname.length()));
        System.out.println("Info CM8 Datastore Creation: "+tagname+
          " with class "+stb.toString());
        Object obj = new Object();
        try {
          obj = Class.forName(stb.toString()).newInstance();
          DKComponentTypeDefICM item_subtag = ((MCRCM8MetaInterface)obj).
            createItemType(mcr_subtag,connection,dsDefICM,mcr_item_type_prefix);
          item_tag.addSubEntity(item_subtag);
          }
        catch (ClassNotFoundException e) {
          throw new MCRException(classname+" ClassNotFoundException"); }
        catch (IllegalAccessException e) {
          throw new MCRException(classname+" IllegalAccessException"); }
        catch (InstantiationException e) {
          throw new MCRException(classname+" InstantiationException"); }
        }
      item_service.addSubEntity(item_tag);
      }

    item_type.addSubEntity(item_service);

    item_type.add(); 
    System.out.println("Info CM8 Datastore Creation: "+mcr_item_type_name+
      " is created.");
    }
  finally {
    MCRCM8ConnectionPool.releaseConnection(connection); }
    }
  catch (Exception e) {
    throw new MCRPersistenceException(e.getMessage(),e); }
  }

/**
 * This methode is internal and create a DK_CM_VARCHAR attribute.
 *
 * @param connection the connection to the database
 * @param name the name of the attribute
 * @param len the len of the character field
 * @param search ist true, if the attribute should text searchable
 * @return If the attribute exists, false was returned, else true.
 **/
public static final boolean createAttributeVarChar(DKDatastoreICM connection,
  String name, int len, boolean search) throws Exception
  {
  DKAttrDefICM attr = new DKAttrDefICM(connection);
  try {
    attr.setName(name);
    attr.setType(DK_CM_VARCHAR);
    attr.setStringType(DK_CM_ATTR_VAR_ALPHANUM_EXT);
    attr.setSize(len);
    attr.setTextSearchable(search);
    attr.setNullable(true);
    attr.setUnique(false);
    attr.add(); }
  catch (DKException e) { return false; }
  return true;
  }

/**
 * This methode is internal and create a DK_CM_DATE attribute.
 *
 * @param connection the connection to the database
 * @param name the name of the attribute
 * @return If the attribute exists, false was returned, else true.
 **/
public static final boolean createAttributeDate(DKDatastoreICM connection,
  String name) throws Exception
  {
  DKAttrDefICM attr = new DKAttrDefICM(connection);
  try {
    attr.setName(name);
    attr.setType(DK_CM_DATE);
    attr.setNullable(true);
    attr.setUnique(false);
    attr.add(); }
  catch (DKException e) { return false; }
  return true;
  }

/**
 * This methode is internal and create a DK_CM_BLOB attribute.
 *
 * @param connection the connection to the database
 * @param name the name of the attribute
 * @param len the len of the character field
 * @param search ist true, if the attribute should text searchable
 * @return If the attribute exists, false was returned, else true.
 **/
private static final boolean createAttributeBlob(DKDatastoreICM connection,
  String name, int len, boolean search) throws Exception
  {
  DKAttrDefICM attr = new DKAttrDefICM(connection);
  try {
    attr.setName(name);
    attr.setType(DK_CM_BLOB);
    attr.setSize(len);
    attr.setTextSearchable(search);
    attr.setNullable(true);
    attr.setUnique(false);
    attr.add(); }
  catch (DKException e) { return false; }
  return true;
  }

/**
 * This methode is internal and create a DK_CM_CLOB attribute.
 *
 * @param connection the connection to the database
 * @param name the name of the attribute
 * @param len the len of the character field
 * @param search ist true, if the attribute should text searchable
 * @return If the attribute exists, false was returned, else true.
 **/
private static final boolean createAttributeClob(DKDatastoreICM connection,
  String name, int len, boolean search) throws Exception
  {
  DKAttrDefICM attr = new DKAttrDefICM(connection);
  try {
    attr.setName(name);
    attr.setType(DK_CM_CLOB);
    attr.setSize(len);
    attr.setTextSearchable(search);
    attr.setNullable(true);
    attr.setUnique(false);
    attr.add(); }
  catch (DKException e) { return false; }
  return true;
  }

}

