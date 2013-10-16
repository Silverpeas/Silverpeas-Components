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
<%
	response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
	response.setHeader( "Pragma", "no-cache" );
	response.setHeader( "Cache-control", "no-cache" );
	response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
	response.setStatus( HttpServletResponse.SC_CREATED );
%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>

<%
String componentId 	= request.getParameter("ComponentId");
String language 	= request.getParameter("Language");
String fieldName    = request.getParameter("FieldName");

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<html>
<head>
<title></title>
<script language="javascript">
function selectImage(url)
{
<%if(fieldName != null){%>
    window.opener.choixImageInGallery<%=fieldName%>(url);
<%}else{%>
	window.opener.choixImageInGallery(url);
<%}%>
	window.close();
}
</script>
</head>
<frameset cols="200,600" rows="*" framespacing="0" frameborder="NO">
  <frame src="<%=m_context%>/GalleryInWysiwyg/dummy?ComponentId=<%=componentId%>&Language=<%=language%>" name="treeview" scrolling="AUTO" frameborder="no">
  <frame src="wysiwygImages.jsp" name="images" scrolling="AUTO" frameborder="NO">
</frameset><noframes></noframes>
</html>