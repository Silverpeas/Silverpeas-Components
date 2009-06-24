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
<%@ page import="com.stratelia.webactiv.servlets.FileServer"%>
<%@ page import="com.stratelia.webactiv.kmelia.control.KmeliaSessionController"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>

<%!

void displayKmaxViewInfoModel(JspWriter out, ModelDetail model, InfoDetail infos, ResourceLocator settings, ResourceLocator publicationSettings, String m_context) throws IOException {
  String toParse=model.getHtmlDisplayer();
  Iterator textIterator = infos.getInfoTextList().iterator();
  Iterator imageIterator = infos.getInfoImageList().iterator();
  
  int posit  = toParse.indexOf("%WA");
  while (posit != -1) {
    if (posit > 0) {
      out.println(toParse.substring(0, posit));
      toParse = toParse.substring(posit);
    }
    if (toParse.startsWith("%WATXTDATA%")) {
      if (textIterator.hasNext()) {
        InfoTextDetail textDetail = (InfoTextDetail)textIterator.next();
        out.println(Encode.javaStringToHtmlParagraphe(textDetail.getContent()));
      }
      toParse = toParse.substring(11);
    }
    else 
    if (toParse.startsWith("%WAIMGDATA%")) {
      if (imageIterator.hasNext()) {
        InfoImageDetail imageDetail = (InfoImageDetail)imageIterator.next();
        String logicalName = imageDetail.getLogicalName();
        String type = logicalName.substring(logicalName.lastIndexOf(".")+1, logicalName.length());
		type = type.toLowerCase();
        if (type.equals("gif") || type.equals("jpg") || type.equals("jpe") || type.equals("jpeg") || type.equals("png")) {
            String url = FileServer.getUrl(imageDetail.getPK().getSpace(), imageDetail.getPK().getComponentName(), imageDetail.getLogicalName(), imageDetail.getPhysicalName(), imageDetail.getType(), publicationSettings.getString("imagesSubDirectory"));
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
  Iterator textIterator = infos.getInfoTextList().iterator();
  Iterator imageIterator = infos.getInfoImageList().iterator();
  
  int posit  = toParse.indexOf("%WA");
  while (posit != -1) {
    if (posit > 0) {
      out.println(toParse.substring(0, posit));
      toParse = toParse.substring(posit);
    }
    if (toParse.startsWith("%WATXTDATA%")) {
      if (textIterator.hasNext()) {
        InfoTextDetail textDetail = (InfoTextDetail)textIterator.next();
        out.println(Encode.encodeSpecialChar(Encode.javaStringToHtmlParagraphe(textDetail.getContent())));
      }
      toParse = toParse.substring(11);
    }
    else 
    if (toParse.startsWith("%WAIMGDATA%")) {
      if (imageIterator.hasNext()) {
        InfoImageDetail imageDetail = (InfoImageDetail)imageIterator.next();
        String logicalName = imageDetail.getLogicalName();
        String physicalName = imageDetail.getPhysicalName();
        String mimeType = imageDetail.getType();
        String type = logicalName.substring(logicalName.lastIndexOf(".")+1, logicalName.length());
        if (type.equalsIgnoreCase("gif") || type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase("jpe") || type.equalsIgnoreCase("jpeg") || type.equalsIgnoreCase("png")) {
            String url = FileServer.getUrl(imageDetail.getPK().getSpace(), imageDetail.getPK().getComponentName(), logicalName, physicalName, mimeType, publicationSettings.getString("imagesSubDirectory"));
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