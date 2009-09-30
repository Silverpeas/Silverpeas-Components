<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="modelUtils.jsp" %>
<%@ include file="attachmentUtils.jsp" %>
<%@ include file="topicReport.jsp.inc" %>


<%!
 //Icons
String folderSrc;

void displayViewWysiwyg(String id, String spaceId, String componentId, HttpServletRequest request, HttpServletResponse response) throws KmeliaException {
    try {
        getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+id+"&SpaceId="+spaceId+"&ComponentId="+componentId).include(request, response);
    } catch (Exception e) {
	  throw new KmeliaException("JSPpublicationManager.displayViewWysiwyg()",SilverpeasException.ERROR,"root.EX_DISPLAY_WYSIWYG_FAILED", e);
    }
}

void displayUserModelAndAttachmentsView(CompletePublication pubComplete, UserDetail owner, KmeliaSessionController kmeliaScc, ResourceLocator settings, ResourceLocator uploadSettings, ResourceLocator publicationSettings, String m_context, JspWriter out, HttpServletRequest request, HttpServletResponse response, String user_id, String profile, ResourcesWrapper resources) throws IOException, KmeliaException {
    InfoDetail infos = pubComplete.getInfoDetail();
    ModelDetail model = pubComplete.getModelDetail();
    PublicationDetail detail = pubComplete.getPublicationDetail();
	String status = detail.getStatus();
    int type = 0;
    if ( kmeliaScc.isVersionControlled() )
    {
        type = 1; // Versioning
    }
    out.println("<TABLE border=\"0\" width=\"98%\" align=center>");
    out.println("<TR><TD align=\"left\">");

	out.println("<span class=txtnav><b>"+Encode.javaStringToHtmlString(detail.getName(kmeliaScc.getCurrentLanguage()))+"</b></span><BR>");

    out.println("<b>"+Encode.javaStringToHtmlString(detail.getDescription(kmeliaScc.getCurrentLanguage()))+"<b><BR><BR>");

	String userId = detail.getUpdaterId();
	if (userId == null || userId.length() == 0)
		userId = detail.getCreatorId();

	if (userId != null && userId.length() > 0)
	{
		UserDetail user = kmeliaScc.getUserDetail(userId);
		if (user != null) {
			out.println("</td><td valign=top align=right>");
			out.println(user.getFirstName()+" "+user.getLastName()+"<br><i>"+resources.getOutputDate(detail.getUpdateDate()));
			out.println("</i><BR>");
		}
	}

    out.println("</TD></TR></table>");
	out.println("<TABLE border=\"0\" width=\"98%\" align=center>");
    if (WysiwygController.haveGotWysiwyg(detail.getPK().getSpace(), detail.getPK().getComponentName(), detail.getPK().getId())) {
        out.println("<TR><TD>");
        out.flush();
        displayViewWysiwyg(detail.getPK().getId(), detail.getPK().getSpace(), detail.getPK().getComponentName(), request, response);
        out.println("</TD>");
        if (! ("bottom".equals(settings.getString("attachmentPosition") ) ) ) {
	        if (infos != null) {
				out.println("<TD width=\"25%\" valign=\"top\" align=\"center\">");
	            out.println("<A NAME=attachments></a>");
	            displayUserAttachmentsView(detail, m_context, out, type, user_id, true, resources);
				out.println("</TD>");
	        }
	    } else {
	        if (infos != null) {
		        out.println("</TR><TR>");
				out.println("<TD valign=\"top\">");
	            out.println("<A NAME=attachments></a>");
	            displayUserAttachmentsView(detail, m_context, out, type, user_id, false, resources);
				out.println("</TD>");
	        }
	    }
        out.println("</TR>");
    } else {
        if (infos != null) {
            out.println("<TR><TD align=\"center\">");
            if (model != null) {
            	displayViewInfoModel(out, model, infos, resources, publicationSettings, m_context);
                //displayViewInfoModel(out, model, infos, settings, publicationSettings, m_context, kmeliaScc);
            }
            out.println("</TD>");

	        if (! ("bottom".equals(settings.getString("attachmentPosition") ) ) ) {
				out.println("<TD width=\"25%\" valign=\"top\" align=\"center\">");
				out.println("<A NAME=attachments></a>");
				displayUserAttachmentsView(detail, m_context, out, type, user_id, true, resources);
				//displayUserAttachmentsView(detail, m_context, out, type, user_id);
				out.println("</TD>");
		    } else {
		        out.println("</TR><TR>");
				out.println("<TD valign=\"top\">");
	            out.println("<A NAME=attachments></a>");
	            displayUserAttachmentsView(detail, m_context, out, type, user_id, false, resources);
				//displayUserAttachmentsView(kmeliaScc, detail, m_context, out, type, user_id, false);
				out.println("</TD>");
		    }
            out.println("</TR>");
        }
    }
    out.println("</TABLE>");
}

// Fin des déclarations
%>

<%
  //Icons
  folderSrc 			= m_context + "/util/icons/component/kmeliaSmall.gif";

  String topicId		= "";
  String pubName		= "";

  ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", kmeliaScc.getLanguage());
  ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", kmeliaScc.getLanguage());
  ResourceLocator publicationSettings = new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings", kmeliaScc.getLanguage());

  UserCompletePublication userPubComplete = null;
  UserDetail ownerDetail = null;

  CompletePublication pubComplete = null;
  PublicationDetail pubDetail = null;
  InfoDetail infos = null;
  ModelDetail model = null;

String action = "";
String path = "";
String profile = "user";
String checkPath = "";

String id 			= (String) request.getParameter("Id");
String currentLang 	= (String) request.getAttribute("Language");

//Vrai si le user connecte est le createur de cette publication ou si il est admin
boolean isOwner = false;
TopicDetail currentTopic = null;

String pathString = "";

String user_id = kmeliaScc.getUserId();

      //Calcul du chemin de la publication
      currentTopic = kmeliaScc.getPublicationFather(id);
      Collection pathColl = currentTopic.getPath();
	  pathString = displayPath(pathColl, false, 3);

      //Recuperation des parametres de la publication
	  userPubComplete = kmeliaScc.getUserCompletePublication(id);
 	  kmeliaScc.setSessionPublication(userPubComplete);
	  pubName = kmeliaScc.getSessionPublication().getPublication().getPublicationDetail().getName(currentLang);
      pubComplete = userPubComplete.getPublication();
      ownerDetail = userPubComplete.getOwner();
%>
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<TITLE></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
</HEAD>
<BODY onUnload="closeWindows()">
<%
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(kmeliaScc.getSpaceLabel());
        browseBar.setComponentName(kmeliaScc.getComponentLabel());
        browseBar.setPath(pathString);
		browseBar.setExtraInformation(pubName);

        out.println(window.printBefore());
		out.println(frame.printBefore());

        displayUserModelAndAttachmentsView(pubComplete, ownerDetail, kmeliaScc, settings, uploadSettings, publicationSettings, m_context, out, request, response, user_id, profile, resources);
        
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</BODY>
</HTML>