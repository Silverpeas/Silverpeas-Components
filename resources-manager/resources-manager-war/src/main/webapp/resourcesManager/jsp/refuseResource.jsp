<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.silverpeas.util.EncodeHelper" %>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="check.jsp"%>
<%
    String resourceId = request.getParameter("ResourceId");
    String resourceName = request.getParameter("ResourceName");
    String reservationId = request.getParameter("reservationId");
    String objectView = request.getParameter("objectView");
    //Icons
    String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
%>
<html>
<head>
  <%
    out.println(gef.getLookStyleSheet());
  %>
  <script type='text/javascript'>
    function validateResource() {
      document.refusalForm.action = "RefuseResource";
    	document.refusalForm.submit();  
    }

    function cancelResource() {
      document.refusalForm.action = "ViewReservation";
    	document.refusalForm.submit();  
    }
    
    </script>
</head>

<body>
<%
    Board board = gef.getBoard();
    
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel,"Main");
    browseBar.setPath(resource.getString("resourcesManager.RefusalMotive"));
    
    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
    
    %>

    <FORM NAME="refusalForm" action="" Method="POST">
	    <TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
	      <TR>
	         <TD></TD>
	         <TD valign="top"><%=EncodeHelper.javaStringToHtmlString(resourceName)%></TD>
	      <TR>
	         <TD class="txtlibform" valign=top><%=resource.getString("resourcesManager.RefusalMotive")%> :</TD>
	         <TD>
	            <textarea name="Motive" rows="5" cols="60"></textarea>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"> 
	            <input type="hidden" name="ResourceId" value="<%=resourceId%>"> 
	            <input type="hidden" name="reservationId" value="<%=reservationId%>"> 
	            <input type="hidden" name="objectView" value="<%=objectView%>"> 
	         </TD>
	      </TR>
	      <TR>
	         <TD colspan="2">
	           ( <img border="0" src="<%=mandatoryField%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%> )
	         </TD>
	      </TR>
	   </TABLE>  
    </FORM>
    <%

 	  out.println(board.printAfter());
	  out.println("<br/>");
	  
	  ButtonPane msgButtonPane = gef.getButtonPane();
    msgButtonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:validateResource();", false));
    msgButtonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancelResource();", false));
    msgButtonPane.setHorizontalPosition();
    out.println("<center>" + msgButtonPane.print()+ "</center>");
 

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>