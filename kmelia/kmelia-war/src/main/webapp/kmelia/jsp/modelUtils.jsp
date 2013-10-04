<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.silverpeas.util.FileUtil"%>
<%@page import="com.stratelia.webactiv.util.FileServerUtils"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.stratelia.webactiv.util.publication.info.model.InfoDetail, com.stratelia.webactiv.util.publication.info.model.ModelDetail, com.stratelia.webactiv.util.publication.info.model.InfoTextDetail, com.stratelia.webactiv.util.publication.info.model.InfoImageDetail"%>
<%@ page import="java.util.Iterator, java.util.ArrayList, java.util.Collection, java.util.Date, com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.EJBException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="com.stratelia.webactiv.kmelia.control.KmeliaSessionController"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>

<%!

void displayKmaxViewInfoModel(JspWriter out, ModelDetail model, InfoDetail infos, ResourceLocator settings, ResourceLocator publicationSettings, String m_context) throws IOException {
  String toParse=model.getHtmlDisplayer();
  Iterator<InfoTextDetail> textIterator = infos.getInfoTextList().iterator();
  Iterator<InfoImageDetail> imageIterator = infos.getInfoImageList().iterator();

  int posit  = toParse.indexOf("%WA");
  while (posit != -1) {
    if (posit > 0) {
      out.println(toParse.substring(0, posit));
      toParse = toParse.substring(posit);
    }
    if (toParse.startsWith("%WATXTDATA%")) {
      if (textIterator.hasNext()) {
        InfoTextDetail textDetail = textIterator.next();
        out.println(EncodeHelper.javaStringToHtmlParagraphe(textDetail.getContent()));
      }
      toParse = toParse.substring(11);
    }
    else
    if (toParse.startsWith("%WAIMGDATA%")) {
      if (imageIterator.hasNext()) {
        InfoImageDetail imageDetail = imageIterator.next();
        String logicalName = imageDetail.getLogicalName();
        if (FileUtil.isImage(logicalName)) {
            String url = FileServerUtils.getUrl(imageDetail.getPK().getComponentName(), imageDetail.getLogicalName(), imageDetail.getPhysicalName(), imageDetail.getType(), publicationSettings.getString("imagesSubDirectory"));
            out.println("<IMG BORDER=\"0\" SRC=\""+url+"\">");
        } else
            out.println("<B>"+settings.getString("FileNotImage")+"</B>");
      }
      toParse = toParse.substring(11);
    }

    // et on recommence
    posit  = toParse.indexOf("%WA");
  }
  out.println(toParse);
}

void displayViewInfoModel(JspWriter out, ModelDetail model, InfoDetail infos, ResourcesWrapper resources, ResourceLocator publicationSettings, String m_context) throws IOException {
  String toParse=model.getHtmlDisplayer();
  Iterator<InfoTextDetail> textIterator = infos.getInfoTextList().iterator();
  Iterator<InfoImageDetail> imageIterator = infos.getInfoImageList().iterator();

  int posit  = toParse.indexOf("%WA");
  while (posit != -1) {
    if (posit > 0) {
      out.println(toParse.substring(0, posit));
      toParse = toParse.substring(posit);
    }
    if (toParse.startsWith("%WATXTDATA%")) {
      if (textIterator.hasNext()) {
        InfoTextDetail textDetail = textIterator.next();
        out.println(EncodeHelper.javaStringToHtmlParagraphe(textDetail.getContent()));
      }
      toParse = toParse.substring(11);
    }
    else
    if (toParse.startsWith("%WAIMGDATA%")) {
      if (imageIterator.hasNext()) {
        InfoImageDetail imageDetail = imageIterator.next();
        String logicalName = imageDetail.getLogicalName();
        String physicalName = imageDetail.getPhysicalName();
        String mimeType = imageDetail.getType();
        if (FileUtil.isImage(logicalName)) {
            String url = FileServerUtils.getUrl(imageDetail.getPK().getComponentName(), logicalName, physicalName, mimeType, publicationSettings.getString("imagesSubDirectory"));
            out.println("<IMG BORDER=\"0\" SRC=\""+url+"\">");
        } else
            out.println("<B>"+resources.getString("FileNotImage")+"</B>");
      }
      toParse = toParse.substring(11);
    }

    // et on recommence
    posit  = toParse.indexOf("%WA");
  }
  out.println(toParse);
}
%>