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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%
//R�cup�ration des param�tres

	ArrayList publicationsDetails = (ArrayList) request.getAttribute("PublicationsDetails");
	int nbPublicationsCreated = 0;
	if (publicationsDetails != null)
		nbPublicationsCreated = publicationsDetails.size();
	int nbFiles = ((Integer) request.getAttribute("NbFiles")).intValue();
	String processDuration = (String) request.getAttribute("ProcessDuration");
	String importMode = (String) request.getAttribute("ImportMode");
	boolean draftMode = ((Boolean) request.getAttribute("DraftMode")).booleanValue();
	String title = (String) request.getAttribute("Title");
	
//Icons
	Button closeButton = (Button) gef.getFormButton(resources.getString("GML.close"), "javascript:onClick=refreshWindow();", false);
%>
<html>
<head><title><%=resources.getString("GML.popupTitle")%></title>
<script language="javascript">
	function refreshWindow()
	{
		window.opener.location.href="GoToCurrentTopic";
		window.close();
	}
</script>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>

<body>
<%
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(kmeliaScc.getSpaceLabel());
  browseBar.setComponentName(kmeliaScc.getComponentLabel());
  browseBar.setPath(kmeliaScc.getString("kmelia.ImportFiles"));
	
	//Le cadre
  Frame frame = gef.getFrame();
  Board board = gef.getBoard();

  //D�but code
  out.println(window.printBefore());
  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
  <TABLE CELLPADDING=5 WIDTH="100%">
	    <tr align="center">
		    <td class="txtlibform" colspan="2"><%=title%>&nbsp;</td>
		  </tr>
	    <tr>
			<td colspan="2"></td>
		</tr>
	    <tr align="left">
		    <td width="60%"><%=kmeliaScc.getString("kmelia.NbFiles")%></td><td align="left"><%=nbFiles%></td>
		</tr>
	    <tr align="left">
		    <td><%=kmeliaScc.getString("kmelia.NbPublications")%></td><td><%=nbPublicationsCreated%></td>
		</tr>
	    <tr align="left">
		    <td><%=kmeliaScc.getString("kmelia.ProcessDuration")%></td><td><%=processDuration%></td>
		</tr>
		<% if (draftMode) { %>
	    	<tr align="left">
		    	<td colspan="2" align="center"><%=kmeliaScc.getString("kmelia.DraftModeActivated")%></td>
		    </tr>
		<% } %>
	</table>
	
<%
	out.println(board.printAfter());
	out.println("<div align=\"center\"><br/>");
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(closeButton);
    out.println(buttonPane.print());
    out.println("<br/></div>");

    //fin du code
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>