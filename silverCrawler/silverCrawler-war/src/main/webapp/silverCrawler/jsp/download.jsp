<%--

    Copyright (C) 2000 - 2020 Silverpeas

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
<%@ page import="org.silverpeas.core.util.file.FileRepositoryManager" %>
<%@ page import="org.silverpeas.components.silvercrawler.control.FolderZIPInfo" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<html>
<head>
<view:looknfeel/>
<%
  FolderZIPInfo zipInfo = (FolderZIPInfo) request.getAttribute("ZipInfo");
	String zipUrl = zipInfo.getUrl();
	String name = zipInfo.getFileZip();
	long sizeZip = zipInfo.getSize();
	long sizeMax = zipInfo.getMaxiSize();
%>
</head>
<body>
<view:window popup="true">
<view:frame>
  <% if (zipInfo.isMaxSizeReached()) { %>
    <div class="inlineMessage-nok"><%=resource.getString("silverCrawler.sizeMax")%> (<%=FileRepositoryManager.formatFileSize(sizeMax)%>)</div>
  <% } else if ("".equals(name)){ %>
    <div class="inlineMessage-nok"><%=resource.getString("silverCrawler.noFileZip")%></div>
  <% } else { %>
  <view:board>
    <table>
      <tr>
        <td class="txtlibform">
          <%=resource.getString("silverCrawler.fileZip")%> :
        </td>
        <td>
          <img alt="SilverCrawler Zip File" src="<%=resource.getIcon("silverCrawler.zip")%>"/>
        </td>
        <td>
          <a href="<%=zipUrl%>"><%=name%>
          </a>&nbsp;(<%=FileRepositoryManager.formatFileSize(sizeZip)%>)
        </td>
      </tr>
    </table>
  </view:board>
<% } %>
<%
ButtonPane buttonPane = gef.getButtonPane();
Button button = gef.getFormButton(resource.getString("GML.close"), "javaScript:window.close();", false);
buttonPane.addButton(button);
out.println(buttonPane.print());
%>
</view:frame>
</view:window>
</body>
</html>