<%--

    Copyright (C) 2000 - 2009 Silverpeas

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

<%@ include file="checkKmelia.jsp" %>
<%@ include file="attachmentUtils.jsp" %>
<%@ include file="tabManager.jsp.inc"%>

<%
PublicationDetail 	pubDetail 	= (PublicationDetail) request.getAttribute("CurrentPublicationDetail");

String				wizardLast	= (String) request.getAttribute("WizardLast");
String 				wizard		= (String) request.getAttribute("Wizard");
String	 			wizardRow	= (String) request.getAttribute("WizardRow");
String				currentLang = (String) request.getAttribute("Language");
List				languages	= (List) request.getAttribute("Languages");
String 				xmlForm		= (String) request.getAttribute("XmlFormForFiles");
if (!StringUtil.isDefined(xmlForm))
{
	xmlForm = "";
}

String pubName 	= pubDetail.getName(currentLang);
String pubId 	= pubDetail.getPK().getId();

boolean	indexIt 	= kmeliaScc.isIndexable(pubDetail);
String	pIndexIt	= "0";
if (indexIt)
	pIndexIt = "1";

boolean isOwner = false;
if (kmeliaScc.getSessionOwner())
      isOwner = true;

if (wizardRow == null)
	wizardRow = "3";

boolean isEnd = false;
if ("3".equals(wizardLast))
	isEnd = true;

String linkedPathString = kmeliaScc.getSessionPath();

String url = kmeliaScc.getComponentUrl()+"ViewAttachments";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "DeletePublication?PubId="+pubId, false);
Button nextButton;
if (isEnd)
	nextButton = (Button) gef.getFormButton(resources.getString("kmelia.End"), "WizardNext?Position=Attachment", false);
else
	nextButton = (Button) gef.getFormButton(resources.getString("GML.next"), "WizardNext?Position=Attachment", false);

boolean openUrl = false;
if (request.getParameter("OpenUrl") != null)
	openUrl = new Boolean(request.getParameter("OpenUrl")).booleanValue();
								
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
function showTranslation(lang)
{
	location.href="ViewAttachments?SwitchLanguage="+lang;
}

function topicGoTo(id) 
{
	location.href="GoToTopic?Id="+id;
}
</script>
</HEAD>
<BODY>
<%
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();
	Board boardHelp = gef.getBoard();
	
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(linkedPathString);
	browseBar.setExtraInformation(pubName);
	browseBar.setI18N(languages, currentLang);

	out.println(window.printBefore());
	
	if ("progress".equals(wizard))
		displayWizardOperations(wizardRow, pubId, kmeliaScc, gef, "ViewAttachments", resources, out, kmaxMode);
	else
	{
		if (isOwner)
			displayAllOperations(pubId, kmeliaScc, gef, "ViewAttachments", resources, out, kmaxMode);
		else
			displayUserOperations(pubId, kmeliaScc, gef, "ViewAttachments", resources, out, kmaxMode);
	}
	
	out.println(frame.printBefore());
	if ("progress".equals(wizard) || "finish".equals(wizard))
	{
		//  cadre d'aide
	    out.println(boardHelp.printBefore());
		out.println("<table border=\"0\"><tr>");
		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
		out.println("<td>"+kmeliaScc.getString("kmelia.HelpAttachment")+"</td>");
		out.println("</tr></table>");
	    out.println(boardHelp.printAfter());
	    out.println("<BR>");
	}
	out.flush();

	if (kmeliaScc.isVersionControlled()) 
	{
		//Versioning links
		getServletConfig().getServletContext().getRequestDispatcher("/versioningPeas/jsp/documents.jsp?Id="+URLEncoder.encode(pubId)+"&SpaceId="+URLEncoder.encode(spaceId)+"&ComponentId="+URLEncoder.encode(componentId)+"&Context=Images&IndexIt="+pIndexIt+"&Url="+URLEncoder.encode(url)+"&SL="+URLEncoder.encode(kmeliaScc.getSpaceLabel())+"&NodeId="+kmeliaScc.getSessionTopic().getNodePK().getId()+"&TopicRightsEnabled="+kmeliaScc.isRightsOnTopicsEnabled()+"&VersionningFileRightsMode="+kmeliaScc.getVersionningFileRightsMode()+"&CL="+URLEncoder.encode(kmeliaScc.getComponentLabel())+"&XMLFormName="+URLEncoder.encode(xmlForm)).include(request, response);
	} 
	else
	{
		//Attachments links
		getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/editAttFiles.jsp?Id="+pubId+"&ComponentId="+componentId+"&Context=Images&IndexIt="+pIndexIt+"&Url="+url+"&UserId="+kmeliaScc.getUserId()+"&OpenUrl="+openUrl+"&Profile="+kmeliaScc.getProfile()+"&Language="+currentLang+"&XMLFormName="+URLEncoder.encode(xmlForm)).include(request, response);
	}
	
	if ("progress".equals(wizard))
	{
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(nextButton);
		buttonPane.addButton(cancelButton);
		buttonPane.setHorizontalPosition();
		out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
	}
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>