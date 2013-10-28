<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ include file="check.jsp" %>

<% 
	// r�cup�ration des param�tres :
	String url = (String) request.getAttribute("Url");
%>

<%@page import="java.net.URL"%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<table CELLPADDING=5 WIDTH="100%">

  <% if (StringUtil.isDefined(url)) { %>
  <!-- AFFICHAGE de la photo -->
    <td> 
      <IMG SRC="<%=url%>">
    </td>
  <%} else { %>
    <td>
      <%= resource.getString("gallery.alreadyDownloaded") %>
    </td>
  <% } %>

</table>
<% 
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>	
</body>
</html>