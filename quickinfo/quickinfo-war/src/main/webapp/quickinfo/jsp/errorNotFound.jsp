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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
  Exception exception = (Exception) request.getAttribute("error");
%>

<%@ include file="checkQuickInfo.jsp" %>

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
<view:looknfeel/>
  </head>
  <body>
<%
	Window window = gef.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(quickinfo.getSpaceLabel());
	browseBar.setComponentName(quickinfo.getComponentLabel() );

	browseBar.setPath(resources.getString("edition"));

	Frame maFrame = gef.getFrame();

	out.println(window.printBefore());
	out.println(maFrame.printBefore());
%>
	<table border="0" cellspacing="0" cellpadding="2" width="100%" class="ArrayColumn">
        <tr>
           <td align="center"> <!--TABLE SAISIE-->
              <table width="100%" border="0" cellspacing="0" cellpadding="5" class="intfdcolor4">
               <tr>
		<td align="center"><span class="txtnote"><%=resources.getString(exception.getMessage())%></span>

	        </td>
               </tr>
	      </TABLE>
           </td>
	</tr>
	</table>
        <table width="100%" border="0" cellspacing="0" cellpadding="5">
         <tr>
	 <td align="center"><%
			Button button = gef.getFormButton(resources.getString("GML.back"), "Main.jsp", false);
			out.print(button.print());
			%>
					</td>
         </tr>
       </TABLE>
			<%
					out.println(maFrame.printAfter());
					out.println(window.printAfter());
			%>
				<br/><br/>
	</body>
</html>