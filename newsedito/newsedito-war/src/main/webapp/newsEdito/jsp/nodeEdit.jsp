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
<%@ include file="imports.jsp" %>
<%@ include file="declarations.jsp.inc" %>

<%@ include file="newsUtils.jsp.inc" %>


<%!

void showAddNode(JspWriter out, String fatherId, NewsEditoSessionController news, ResourceLocator settings, ResourceLocator generalMessage, GraphicElementFactory graphicFactory)
  throws NewsEditoException
{
		showUpdateNode(out, null, fatherId, "", "", news, settings, generalMessage, graphicFactory);
}

void showUpdateNode(JspWriter out, String nodeId, String fatherId, 
  String name, String description, NewsEditoSessionController news, ResourceLocator settings, ResourceLocator generalMessage, GraphicElementFactory graphicFactory)
  throws NewsEditoException
{
	try{
	  out.println("      <TD  class=\"txtlibform\">"+news.getString("nodeName")+"</TD>");
	  out.println("      <TD><INPUT TYPE=TEXT  SIZE=\"50\" NAME=Name maxlength=" + DBUtil.TextFieldLength + " VALUE=\""+Encode.javaStringToHtmlString(name)+"\">&nbsp;<img border=\"0\" src=\""+settings.getString("mandatoryField")+"\" width=\"5\" height=\"5\"></TD>");
	  out.println("    </TR>");
	  out.println("    <TR>");
	  out.println("      <TD  class=\"txtlibform\">"+news.getString("nodeDescription")+"</TD>");
	  out.println("      <TD><INPUT TYPE=TEXT  SIZE=\"50\" NAME=Description maxlength=" + DBUtil.TextFieldLength + " VALUE=\""+Encode.javaStringToHtmlString(description)+"\"></TD>");
	  out.println("    </TR>");
	  out.println("	   <TR><TD colspan=\"2\">( <img border=\"0\" src=\""+mandatoryField+"\" width=\"5\" height=\"5\"> = "+generalMessage.getString("GML.requiredField")+" )</TD></TR>");
	  out.println("    <TR>");
	}
	catch(Exception e){
		throw new NewsEditoException("newsEdit_JSP.showUpdateNode",NewsEditoException.WARNING,"NewsEdito.EX_CANNOT_SHOW_NODE_UPDATED",e);			
	}
}


void showReallyAddNode(JspWriter out, NewsEditoSessionController news, String fatherId, 
  String name, String description)
  throws NewsEditoException, IOException
{
  try {
    news.addTitle(fatherId, name, description);
    out.println("<BODY onLoad=reallyClose()>");
    out.println("</BODY>");
  } catch (CreateNewsEditoException e) {
    out.println("<BODY>");
    displayGoBackBanner(out, e.getMessage(), 
      "<A HREF=\"nodeEdit.jsp\">"
      + news.getString("retour")+"</A>");
    out.println("</BODY>");

  }
} 


void showReallyUpdateNode(JspWriter out, NewsEditoSessionController news, String nodeId, 
  String name, String description)
  throws NewsEditoException, IOException
{

  try {
    news.updateTitle(nodeId, name, description);
    out.println("<BODY onLoad=reallyClose()>");
    out.println("</BODY>");
  } catch (CreateNewsEditoException e) {
    out.println("<BODY>");
    displayGoBackBanner(out, e.getMessage(), 
      "<A HREF=\"nodeEdit.jsp\">"+ 
      news.getString("retour")+"</A>");
    out.println("</BODY>");
  }
} 

%>


<HTML>
<HEAD>
<%out.println(gef.getLookStyleSheet()); %>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="../../util/javaScript/checkForm.js"></script>
<Script language="JavaScript">

function reallyClose()
{
    if (window.opener != null) {
      //window.opener.location.replace(window.opener.location.href);
      <%
      	String redirection="";
      	// quand on ajoute un titre on selectionne l'archive dans laquelle le titre va être ajouté
      	// quand on ajoute une archive on ne selectionne rien du tout -> la liste de toutes les archives sera affichée
      	if (news.getArchiveId()!=null)
      	{
      		redirection="?Action=SelectArchive&ArchiveId="+ news.getArchiveId();
      	}
      
      %>
      window.opener.location.replace("manageNews.jsp<%=redirection %>");

      window.close();
    }
    else {
      window.location.replace("ManageNews.jsp")
    }
}

function cancel() {
		document.nodeEditForm.Name.value = "";
		document.nodeEditForm.Description.value = "";
}

function reallyUpdate()
{
    

    if (checkString(document.nodeEditForm.Name,"<%=news.getString("champsObligatoireNonRenseigne")+" "+news.getString("nodeName") %>") )
    {
	    if (!isValidTextField(document.nodeEditForm.Description)) {
			  window.alert("<%=news.getString("champsDescriptionTropLong")%>");
		}
		else 
		{
	    	document.nodeEditForm.Action.value = "ReallyUpdate";
	    	document.nodeEditForm.submit();
		}
	}
}

function reallyAdd()
{
    if (checkString(document.nodeEditForm.Name,"<%=news.getString("champsObligatoireNonRenseigne")+" "+news.getString("nodeName") %>") )
    {
	    if (!isValidTextField(document.nodeEditForm.Description)) {
			  window.alert("<%=news.getString("champsDescriptionTropLong")%>");
		}else
		{
	    	document.nodeEditForm.Action.value = "ReallyAdd";
	    	document.nodeEditForm.submit();
		}
	}

}

function viewArchive()
{
    document.newsForm.Action.value = "View";
    document.newsForm.submit();
}

</script>
</HEAD>
<%
  response.setHeader("Cache-Control","no-store"); //HTTP 1.1
  response.setHeader("Pragma","no-cache"); //HTTP 1.0
  response.setDateHeader ("Expires",-1); //prevents caching at the proxy server

  if (action == null)
    action = "View";

  String nodeId = null;
  String fatherId = null;

  if (action.equals("ReallyAdd")) {
    fatherId = (String) request.getParameter("FatherId");
    String name = (String) request.getParameter("Name");
    String description = (String) request.getParameter("Description");
    showReallyAddNode(out, news, fatherId, name, description);
  }
  else
  if (action.equals("ReallyUpdate")) {
    nodeId = (String) request.getParameter("NodeId");
    String name = (String) request.getParameter("Name");
    String description = (String) request.getParameter("Description");
    showReallyUpdateNode(out, news, nodeId, name, description);
  }
  else {
%>

<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>

<%
	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(news.getComponentLabel());
	browseBar.setDomainName(news.getSpaceLabel());

	browseBar.setPath(news.getString((String) request.getParameter("ActionTitle")));

	out.println(window.printBefore());
	
	Frame frame = gef.getFrame();
	
	out.println(frame.printBefore());
%>
<center>

<FORM NAME="nodeEditForm" ACTION="nodeEdit.jsp" METHOD=POST >

<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">

<%
  if (action.equals("AddNode")) {
    fatherId = (String) request.getParameter("FatherId");
    showAddNode(out, fatherId, news, settings, generalMessage, gef);
  }
  else
  if (action.equals("UpdateNode")) {
    fatherId = (String) request.getParameter("FatherId");
    nodeId = (String) request.getParameter("NodeId");
    NodeDetail detail = news.getNodeDetail(nodeId);
    String name = detail.getName();
    String description = detail.getDescription();
    showUpdateNode(out, nodeId, fatherId, name, description, news, settings, generalMessage, gef);
  }
  else
    out.println("Error : unknown action = " + action);
 
%>
			</table>
  	</td>
	</tr>
</table>
<br>
<%  
  Button button;
  Button buttonClose;
  if (nodeId == null) {
    button = gef.getFormButton(generalMessage.getString("GML.validate"), 
      "javascript:onClick=reallyAdd()", false, settings.getString("formButtonIconUrl"));
  }
  else {
    button = gef.getFormButton(generalMessage.getString("GML.validate"), 
      "javascript:onClick=reallyUpdate()", false, settings.getString("formButtonIconUrl"));
  }
  buttonClose = gef.getFormButton(generalMessage.getString("GML.cancel"), 
      "javascript:onClick=window.close()", false, settings.getString("formButtonIconUrl"));
			
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(button);
	buttonPane.addButton(buttonClose);
	out.println(buttonPane.print());
 %>

  <input type="hidden" name="Action">
  <input type="hidden" name="NodeId" <%
    if (nodeId != null) 
      if (nodeId.length() > 0) 
        out.println("value=\""+nodeId+"\"");%>>
  <input type="hidden" name="FatherId" <%
    if (fatherId != null) 
      if (fatherId.length() > 0)
        out.println("value=\""+fatherId+"\"");%>>
</FORM>
</center>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

<FORM NAME="newsForm" ACTION="ManageNews.jsp" METHOD=POST >
  <input type="hidden" name="Action">
</FORM>
</BODY>
  <%
  } // end action needs to display node
  %>
</HTML>
