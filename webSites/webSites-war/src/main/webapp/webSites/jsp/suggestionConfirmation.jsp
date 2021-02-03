<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>
<%
  String nomSite = request.getParameter("nomSite");
  String nomPage = request.getParameter("nomPage");
%>
<view:sp-page>
<view:sp-head-part/>
<view:sp-body-part>
<%
  Window window = gef.getWindow();
  window.setPopup(true);
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(spaceLabel);
  browseBar.setComponentName(componentLabel);
  browseBar.setExtraInformation(resources.getString("SuggestionLink"));
  //Le cadre
  Frame frame = gef.getFrame();
  //Le board
  Board board = gef.getBoard();
  //Début code
  out.println(window.printBefore());
  out.println(frame.printBefore());
  out.print(board.printBefore());
%>
<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
  <tr>
    <td class="intfdcolor4"><%=resources.getString("ConfirmationSuggestionMessage1") + "'<strong>" +
        nomSite + "</strong>' (" + nomPage + ") " +
        resources.getString("ConfirmationSuggestionMessage2") + "<BR><BR>"%>
    </td>
  </tr>
</TABLE>
<%
  //fin du code
  out.print(board.printAfter());
  out.println(frame.printMiddle());
  ButtonPane buttonPane = gef.getButtonPane();
  Button closeButton = gef
      .getFormButton(resources.getString("GML.close"), "javascript:onClick=window.close();", false);
  buttonPane.addButton(closeButton);
  buttonPane.setHorizontalPosition();
  out.println("<br><center>" + buttonPane.print() + "</center><br>");
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</view:sp-body-part>
</view:sp-page>