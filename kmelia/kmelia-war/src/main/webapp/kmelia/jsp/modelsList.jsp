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
<%@ include file="tabManager.jsp.inc" %>

<%
Collection 			dbForms		= (Collection) request.getAttribute("DBForms");
List 				xmlForms	= (List) request.getAttribute("XMLForms");
PublicationDetail 	pubDetail 	= (PublicationDetail) request.getAttribute("CurrentPublicationDetail");
Boolean				wysiwyg		= (Boolean) request.getAttribute("WysiwygValid");
String				wizardLast	= (String) request.getAttribute("WizardLast");			
String 				wizard		= (String) request.getAttribute("Wizard");
String	 			wizardRow	= (String) request.getAttribute("WizardRow");
String				currentLang = (String) request.getAttribute("Language");

String pubId 		 = pubDetail.getPK().getId();
String pubName 		 = pubDetail.getName(currentLang);
boolean wysiwygValid = wysiwyg.booleanValue();
	
String linkedPathString = kmeliaScc.getSessionPath();

boolean isOwner = false;
if (kmeliaScc.getSessionOwner())
	isOwner = true;

if (wizardRow == null)
	wizardRow = "2";

boolean isEnd = false;
if ("2".equals(wizardLast))
	isEnd = true;

//Icons
String hLineSrc = m_context + "/util/icons/colorPix/1px.gif";

Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "DeletePublication?PubId="+pubId, false);
Button nextButton;
if (isEnd)
	nextButton = (Button) gef.getFormButton(resources.getString("kmelia.End"), "WizardNext?Position=Content&WizardRow="+wizardRow, false);
else
	nextButton = (Button) gef.getFormButton(resources.getString("GML.next"), "WizardNext?Position=Content&WizardRow="+wizardRow, false);

%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script language="javaScript">
function sendToWysiwyg() {
    document.toWysiwyg.submit();
}

function changeModel(id) {
    document.modelForm.ModelId.value = id;
    document.modelForm.Action.value = "NewModel";
    document.modelForm.action = "ToDBModel";
    document.modelForm.submit();
}

function goToForm(xmlFormName) {
    document.xmlForm.Name.value = xmlFormName;
    document.xmlForm.submit();
}

function topicGoTo(id) {
	closeWindows();
	location.href="GoToTopic?Id="+id;
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</HEAD>
<BODY onUnload="closeWindows()" id="<%=componentId%>">
<%
    Window window = gef.getWindow();
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();
    Board boardHelp = gef.getBoard();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    browseBar.setPath(linkedPathString);
	browseBar.setExtraInformation(pubName);

    out.println(window.printBefore());

    if ("progress".equals(wizard)) {
    	displayWizardOperations(wizardRow, pubId, kmeliaScc, gef, "ModelChoice", resources, out, kmaxMode);
    }
    else {
	    if (isOwner) {
	        displayAllOperations(pubId, kmeliaScc, gef, "ModelChoice", resources, out, kmaxMode);
	    } else {
	        displayUserOperations(pubId, kmeliaScc, gef, "ModelChoice", resources, out, kmaxMode);
	    }
    }
    out.println(frame.printBefore());
    
	//  cadre d'aide
	if ("progress".equals(wizard) || "finish".equals(wizard))
	{
		// cadre d'aide
	    out.println(boardHelp.printBefore());
		out.println("<table border=\"0\"><tr>");
		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
		out.println("<td>"+kmeliaScc.getString("kmelia.HelpContent")+"</td>");
		out.println("</tr></table>");
	    out.println(boardHelp.printAfter());
	    out.println("<BR>");
	}
	out.println(board.printBefore());
%>

<table cellpadding="5" width="100%" id="templates">
  <tr><td colspan="3" class="txtnav"><%=resources.getString("ModelChoiceTitle")%></td></tr>
<%
    ModelDetail modelDetail;
    int nb = 0;
    out.println("<tr>");
    Iterator iterator = dbForms.iterator();
    while (iterator.hasNext()) {
        modelDetail = (ModelDetail) iterator.next();
        
        if (nb != 0 && nb%3==0)
	        out.println("</tr><tr>");
	        
        nb++;
        out.println("<td class=\"template\"><a href=\"javaScript:changeModel('"+modelDetail.getId()+"')\"><img src=\"../../util/icons/model/"+modelDetail.getImageName()+"\" border=\"0\" alt=\""+modelDetail.getDescription()+"\"/><br/>"+modelDetail.getName()+"</a></td>");
    }
    
    if (xmlForms != null)
    {
	    PublicationTemplate xmlForm;
	    iterator = xmlForms.iterator();
	    String thumbnail = "";
	    while (iterator.hasNext()) {
	        xmlForm = (PublicationTemplate) iterator.next();
	        
	        if (nb != 0 && nb%3==0)
	        {
		        out.println("</tr><tr>");
	        }   
	        nb++;
	        
	        thumbnail = xmlForm.getThumbnail();
	        if (!StringUtil.isDefined(thumbnail)) {
	          thumbnail = PublicationTemplate.DEFAULT_THUMBNAIL;
	        }
	        out.println("<td class=\"template\"><a href=\"javaScript:goToForm('"+xmlForm.getFileName()+"')\"><img src=\""+thumbnail+"\" border=\"0\" alt=\""+xmlForm.getDescription()+"\"/><br/>"+xmlForm.getName()+"</a></td>");
	    }
	}
    
	if (wysiwygValid)
	{
	    if (nb != 0 && nb%3 == 0)
			out.println("</tr><tr>");
			
	    out.println("<td class=\"template\"><a href=\"javaScript:sendToWysiwyg();\"><img src=\"../../util/icons/model/wysiwyg.gif\" border=\"0\" alt=\"Wysiwyg\"/><br/>WYSIWYG</a></td>");
	    out.println("</tr>");
	}
%>
</TABLE>

<%
	out.println(board.printAfter());
	if (wizard.equals("progress"))
	{
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(nextButton);
		buttonPane.addButton(cancelButton);
		buttonPane.setHorizontalPosition();
		out.println("<br/><center>"+buttonPane.print()+"</center><br/>");
	}
    out.println(frame.printAfter());
%>

<form name="modelForm" action="modelManager.jsp" method="post">
	<input type="hidden" name="ModelId"/>
	<input type="hidden" name="PubId" value="<%=pubId%>"/>
	<input type="hidden" name="Action"/>
</form>
<form name="toWysiwyg" action="ToWysiwyg" method="post"/>
</form>
<form name="xmlForm" action="GoToXMLForm" method="post">
	<input type="hidden" name="Name"/>
	<input type="hidden" name="PubId" value="<%=pubId%>"/>
</form>
<% out.println(window.printAfter()); %>
</BODY>
</HTML>