<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="com.stratelia.silverpeas.versioning.model.Worker"%><%@ page import="javax.servlet.*,
                 com.stratelia.silverpeas.versioning.model.Document,
                 com.stratelia.silverpeas.versioning.model.DocumentVersion,
                 com.stratelia.webactiv.util.attachment.ejb.AttachmentPK,
                 com.stratelia.webactiv.util.attachment.control.AttachmentController,
                 com.stratelia.webactiv.util.attachment.model.AttachmentDetail,
                 com.stratelia.webactiv.util.publication.model.PublicationPK,
                 com.stratelia.silverpeas.versioning.model.Reader,
                 com.stratelia.silverpeas.silvertrace.SilverTrace,
                 com.stratelia.silverpeas.versioning.util.VersioningUtil"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.FileOutputStream"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.webactiv.util.publication.info.model.ModelDetail, java.util.Collection, java.util.Iterator, java.util.ArrayList, java.util.List, com.stratelia.webactiv.kmelia.control.KmeliaSessionController"%>
<%@ page import="com.stratelia.webactiv.util.publication.info.model.InfoDetail, com.stratelia.webactiv.util.publication.info.model.InfoAttachmentDetail"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="java.util.Enumeration "%>
<%@ page import="com.stratelia.webactiv.servlets.FileServer"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.silverpeas.util.ForeignPK"%>

<%!
void displayUserAttachmentsView(PublicationDetail pubDetail, String m_context, JspWriter out, int type, String user_id, boolean showIcon, ResourcesWrapper resources) throws IOException {

	//construction du path context
  int VERSIONING = 1;
  String ctx = "Images";
    if ( type == VERSIONING )
    {
        //construction d'une AttachmentPK (c'est une foreignKey) � partir  de la publication
        ForeignPK foreignKey = new ForeignPK(pubDetail.getPK());
        String space_id = foreignKey.getSpace();
        String component_id = foreignKey.componentName;
        VersioningUtil versioning_util = new VersioningUtil();

        List documents = versioning_util.getDocuments(foreignKey);

        Iterator iterator = documents.iterator();
        Document document;
        DocumentVersion document_version;
        Vector dv;
        if (iterator.hasNext()) {
              out.println("<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor>");
              out.println("<tr><td>");
              out.println("<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\" CLASS=intfdcolor4>");
              if (showIcon)
	              out.println("<TR><TD align=\"center\"><img src=\""+m_context+"/util/icons/attachedFiles.gif\"></td></TR>");
              while (iterator.hasNext()) {
                    document = (Document) iterator.next();
/*                    if (isUserReader( document, Integer.parseInt(user_id), versioning_util))
                    { */
                       document_version = versioning_util.getLastPublicVersion(document.getPk());
                       if ( document_version != null )
                       {
                           out.println("<TR>");
                           out.println("<TD><IMG alt=\"\" src=\""+versioning_util.getDocumentVersionIconPath(document_version.getPhysicalName())+"\" width=20>&nbsp;<A href=\""+versioning_util.getDocumentVersionURL(component_id, document_version.getLogicalName(), document.getPk().getId(), document_version.getPk().getId())+"\" target=\"_blank\">"+document.getName()+"</A>&nbsp;(v"+document_version.getMajorNumber()+"."+document_version.getMinorNumber()+")<BR>");
                           String separator = "";
                           if (!"no".equals(resources.getSetting("showFileSize")))
                           {
														out.println(" "+FileRepositoryManager.formatFileSize(document_version.getSize()));
														separator = " / ";
												   }
												   if (!"no".equals(resources.getSetting("showDownloadEstimation")))
                           		out.println(separator+versioning_util.getDownloadEstimation(document_version.getSize()));

                           if (document_version.getMajorNumber() > 1)
                           {
                           		if (showIcon)
	                           		out.println("<BR> >> <a href=\"javaScript:viewPublicVersions("+document.getPk().getId()+")\">Toutes les versions...</a>");
	                           	else
	                           		out.println(" (<a href=\"javaScript:viewPublicVersions("+document.getPk().getId()+")\">Toutes les versions...</a>)");
		                       }
	                         out.println("</TD></TR>");
	                       }
//	                   }
	              }
              out.println("</TABLE>");
              out.println("</td></tr>");
              out.println("</TABLE>");
         }
    }
    else
    {
        displayUserAttachmentsView(pubDetail, m_context, out, showIcon, resources);
    }
}

public boolean isUserReader(Document document, int user_id, VersioningUtil versioning_util )
    {
        try
        {
            ArrayList readers = (ArrayList)document.getReadList().clone();
            ArrayList writers = versioning_util.getAllNoReader( document );

            for ( int i=0; i<readers.size(); i++ )
            {
                Reader user = (Reader) readers.get(i);
                if ( user.getUserId() == user_id )
                {
                    return true;
                }
            }

            for ( int i=0; i<writers.size(); i++ )
            {
                Reader user = (Reader) writers.get(i);
                if ( user.getUserId() == user_id )
                {
                    return true;
                }
            }
        } catch (Exception e)
        {
            SilverTrace.error( "kmelia", "attachmentUtils.jsp", "root.EX_REMOTE_EXCEPTION", e );
        }

        return false;
    }


  void displayUserAttachmentsView(PublicationDetail pubDetail, String m_context, JspWriter out, boolean showIcon, ResourcesWrapper resources) throws IOException
  {
  	//construction du path context
    String ctx = "Images";
    //construction d'une AttachmentPK (c'est une foreignKey) � partir  de la publication
    AttachmentPK foreignKey =  new AttachmentPK(pubDetail.getPK().getId(), pubDetail.getPK().getSpace(), pubDetail.getPK().getComponentName());

    Collection attachmentList = AttachmentController.searchAttachmentByPKAndContext(foreignKey, ctx);
    Iterator iterator = attachmentList.iterator();
    if (iterator.hasNext())
    {
			out.println("<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor>");
			out.println("<tr><td>");
			out.println("<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\" CLASS=intfdcolor4>");
			if (showIcon)
	      out.println("<TR><TD align=\"center\"><img src=\""+m_context+"/util/icons/attachedFiles.gif\"></td></TR>");
		  AttachmentDetail attachmentDetail = null;
		  String author = "";
		  String title  = "";
		  String info	= "";
      while (iterator.hasNext()) {
        attachmentDetail = (AttachmentDetail) iterator.next();
				title	= attachmentDetail.getTitle();
				info	= attachmentDetail.getInfo();
        out.println("<TR>");
        out.println("<TD><IMG src=\""+attachmentDetail.getAttachmentIcon()+"\" width=20>&nbsp;");
				out.println("<A href=\"" + m_context +attachmentDetail.getAttachmentURL()+"\" target=\"_blank\">");
				if (title != null && title.length()>0)
					out.print(title);
				else
					out.print(attachmentDetail.getLogicalName());
				out.print("</A>"+author+"<BR>");

				String separator = "";
				if (title != null && title.length()>0) {
					if (!"no".equals(resources.getSetting("showTitle")))
					{
						out.println(attachmentDetail.getLogicalName());
						separator = " / ";
					}
				}

				if (!"no".equals(resources.getSetting("showFileSize")))
               	{
					out.println(separator + attachmentDetail.getAttachmentFileSize());
					separator = " / ";
			   	}

			   	if (!"no".equals(resources.getSetting("showDownloadEstimation")))
               		out.println(separator + attachmentDetail.getAttachmentDownloadEstimation());

                if (info != null && info.length()>0)
                {
                	if (!"no".equals(resources.getSetting("showInfo")))
						out.println("<br><i>"+info+"</i>");
				}
				out.println("</TD></TR>");
				author = "";
          }
          out.println("</TABLE>");
          out.println("</td></tr>");
          out.println("</TABLE>");
     }
  }

%>