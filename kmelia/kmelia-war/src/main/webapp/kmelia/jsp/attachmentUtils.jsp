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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

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
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.silverpeas.util.ForeignPK"%>
<%@ page import="org.silverpeas.attachment.model.SimpleDocument" %>
<%@ page import="org.silverpeas.attachment.AttachmentServiceFactory" %>

<%!
void displayUserAttachmentsView(PublicationDetail pubDetail, String webContext, JspWriter out,
                                String lang, boolean showIcon, ResourcesWrapper resources) throws IOException {


        ForeignPK foreignKey = new ForeignPK(pubDetail.getPK());
        List<SimpleDocument> documents =
                AttachmentServiceFactory.getAttachmentService().listDocumentsByForeignKey(foreignKey,lang);


        if (!documents.isEmpty()) {
              out.println("<table ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor>");
              out.println("<tr><td>");
              out.println("<table ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\" CLASS=intfdcolor4>");
              if (showIcon) {
	              out.println("<tr><td align=\"center\"><img src=\""+ webContext + "/util/icons" +
                          "/attachedFiles.gif\"></td></tr>");   }
              for (SimpleDocument document : documents) {
                  SimpleDocument document_version =document.getLastPublicVersion();
                       if ( document_version != null ) {
                           String title	= document_version.getTitle();
                           if(!StringUtil.isDefined(title)) {
                               title = document_version.getFilename();
                           }
                           out.println("<tr>");
                           out.print("<td><img alt=\"\" src=\""+document_version.getDisplayIcon()+"\" width=20>&nbsp;<A href=\""+document_version.getAttachmentURL()+"\" target=\"_blank\">"+title+"</a>");
                           if(document_version.isVersioned()) {
                               out.println("&nbsp;(v"+document_version.getMajorVersion()+"."+document_version.getMinorVersion()+")<br/>");
                           } else {
                               out.println("&nbsp;<br/>");
                           }
                           String separator = "";
                           if (!"no".equals(resources.getSetting("showFileSize"))) {
                               out.println(" " +FileRepositoryManager.formatFileSize(document_version.getSize()));
                               separator = " / ";
                           }
                           if (!"no".equals(resources.getSetting("showDownloadEstimation"))) {
                               out.println(separator+FileRepositoryManager.getFileDownloadTime(document_version.getSize()));
                           }
                           if (StringUtil.isDefined(document_version.getDescription()))
                           {
                               if (!"no".equals(resources.getSetting("showInfo"))) {
                                   out.println("<br><i>"+document_version.getDescription()+"</i>");
                               }

                           }
                           if (document_version.isVersioned()
                                   && document_version.getMajorVersion() > 1) {
                           		if (showIcon) {
                                       out.println("<br/> >> <a href=\"javaScript:viewPublicVersions("+document.getPk().getId()+")\">Toutes les versions...</a>");
                                } else {
                                       out.println(" (<a href=\"javaScript:viewPublicVersions("+document.getPk().getId()+")\">Toutes les versions...</a>)");
                                   }
		                       }
	                         out.println("</td></tr>");
	                       }
	              }
              out.println("</table>");
              out.println("</td></tr>");
              out.println("</table>");
         }

}
%>