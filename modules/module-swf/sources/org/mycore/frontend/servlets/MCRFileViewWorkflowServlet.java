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

// package
package org.mycore.frontend.servlets;

// Imported java classes
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUtils;
import org.mycore.datamodel.ifs.MCRContentInputStream;
import org.mycore.datamodel.ifs.MCRFileContentTypeFactory;
import org.mycore.user.MCRUserMgr;

/**
 * This servlet read a digital object from the workflow and put it to the web.<br />
 * Call this servlet with <b>.../servlets/MCRFileViewWorkflowServlet/type=...&amp;file=...</b>
 *
 * @author  Jens Kupferschmidt
 * @version $Revision$ $Date$
 */

public class MCRFileViewWorkflowServlet extends MCRServlet
{
// The configuration
private static Logger LOGGER=Logger.getLogger(MCRFileViewWorkflowServlet.class.getName());

/** 
 * This method overrides doGetPost of MCRServlet and responds the file
 */
public void doGetPost(MCRServletJob job) throws Exception
  {
  // get the type
  String file = getProperty(job.getRequest(),"file").trim();
  LOGGER.debug( "MCRFileViewWorkflowServlet : file = " + file );
  String type = getProperty(job.getRequest(),"type").trim();
  LOGGER.debug( "MCRFileViewWorkflowServlet : type = " + type );
  
  // check the privileg
  boolean haspriv = false;
  MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
  String userid = mcrSession.getCurrentUserID();
  //userid = "administrator";
  LOGGER.debug("Curren user for list workflow = "+userid);
  ArrayList privs = MCRUserMgr.instance().retrieveAllPrivsOfTheUser(userid);
  if (privs.contains("modify-"+type)) { haspriv = true; }

  // read the file and write to output
  String dirname = CONFIG.getString("MCR.editor_"+type+"_directory",null);
  if ((dirname != null) && haspriv) {
    File in =new File(dirname,file);
    if (in.isFile()) {
      FileInputStream fin = new FileInputStream(in);
      MCRContentInputStream cis = new MCRContentInputStream(fin);
      byte[] header = cis.getHeader();
      String mime = MCRFileContentTypeFactory.detectType(in.getName(),header)
        .getMimeType();
      LOGGER.debug("MimeType = "+mime);
      job.getResponse().setContentType(mime);
      fin = new FileInputStream(in);
      job.getResponse().setContentLength( (int)( in.length() ) );
      OutputStream out = new BufferedOutputStream( job.getResponse().getOutputStream() );
      MCRUtils.copyStream(fin,out);
      out.close();
      }
    else {
      LOGGER.warn("File "+in.getName()+" not found."); }
    }
  }

}

