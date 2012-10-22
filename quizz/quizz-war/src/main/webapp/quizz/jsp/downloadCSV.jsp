<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ include file="checkQuizz.jsp" %>
<%
  String  csvFileName = (String) request.getAttribute("CSVFilename");
  Long  csvFileSize = (Long) request.getAttribute("CSVFileSize");
  String  csvFileURL  = (String) request.getAttribute("CSVFileURL");
%>


<html>
  <head>
    <%
      out.println(gef.getLookStyleSheet());
    %>
  </head>
  <body>
    <%
    Window window = gef.getWindow();  
    BrowseBar browseBar = window.getBrowseBar();
    Frame frame = gef.getFrame();
    Board board = gef.getBoard();
    
    browseBar.setDomainName(quizzScc.getSpaceLabel());
      browseBar.setComponentName(quizzScc.getComponentLabel());
    browseBar.setExtraInformation(resources.getString("GML.export"));
    
    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
    %>
    <table>
      <tr>
        <td class="txtlibform"><%=resources.getString("GML.size")%> :</td>
        <td><%=FileRepositoryManager.formatFileSize(csvFileSize.longValue())%></td>
      </tr>
      <tr>
        <td class="txtlibform"><%=resources.getString("GML.csvFile")%> :</td>
        <td><a href="<%=csvFileURL%>"><%=csvFileName%></a></td>
      </tr>
    </table>
    <%
      out.println(board.printAfter());
      ButtonPane buttonPane = gef.getButtonPane();
      Button button = (Button) gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
      buttonPane.addButton(button);
      out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
      out.println(frame.printAfter());
      out.println(window.printAfter());
    %>
  </body>
</html>